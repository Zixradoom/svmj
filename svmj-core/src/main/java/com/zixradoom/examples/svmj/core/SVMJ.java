
package com.zixradoom.examples.svmj.core;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Set;
import java.util.EnumSet;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.List;

public final class SVMJ {
  public static final int CONTROL_REGISTERS = 8;
  public static final int MIN_REGISTERS = 24;
  public static final int MAX_REGISTERS = 256;
  public static final int PAGE_SIZE = 4096; // must be power of 2!
  public static final int PAGE_OFFSET_MASK = PAGE_SIZE - 1;
  public static final int PAGE_ENTRY_VALID_FLAG = 0x1;
  public static final int PAGE_TABLE_INDEX_MASK = 0x3FF000;
  public static final int PAGE_TABLE_ENTRY_SIZE = Integer.BYTES;
  public static final int PAGE_TABLE_ENTRY_ADDRESS_MASK = 0xFFFFF000;
  public static final int PAGE_DIRECTORY_INDEX_MASK =  0xFFC00000;
  public static final int PAGE_DIRECTORY_ENTRY_SIZE = Integer.BYTES;
  public static final int PAGE_DIRECTORY_ENTRY_ADDRESS_MASK = 0xFFFFF000;
  //public static final int PAGE_READ_WRITE_MASK = 0x02;
  public static final int PHYSICAL_RAM_ADDRESS_START = 0x1F000000;

  public static final int INSTRUCTION_SIZE_IN_BYTES = 4;

  // System Registers
  public static final int PROGRAM_COUNTER = 0;
  // the physical base address of the
  // page table should be aligned to a page boundery
  public static final int PAGE_TABLE_BASE = 1;
  
  public static final int STACK_POINTER = 2;
  
  public static final int BASE_POINTER = 3;
  // x86 CR2 equivalent
  public static final int PAGE_FAULT_ADDRESS = 5;
  
  public static final int INTERRUPT_DESCRIPTOR_TABLE = 4;
  
  public static final int SYSTEM_ENTER_PROGRAM_COUNTER = 6;
  
  public static final int SYSTEM_ENTER_STACK_POINTER = 7;
  
  private long instructionCount = 0;
  
  private List < Device > devices;
  
  private ProtectionRing ring;
  private Set < Status > status;
  private ByteBuffer registers;
  private ByteBuffer controlRegisters;
  private ByteBuffer memory;
  private int registerCount;
  
  private boolean pagingEnabled = false;

  private NavigableMap < Integer, MMIOEntry > mmio;
  private Map < Integer, Integer > tlb;

  // flag state info
  private int lastInstResult;
  private int lastInstAuxBits;

  public SVMJ () {
    this ( 256, 8192, Collections.emptyList () );
  }
  
  public SVMJ ( int registers, int ram, List < Device > devices ) {
    this.ring = ProtectionRing.RING_0;
    this.registerCount = registers;
    
    if ( registers < MIN_REGISTERS || registers > MAX_REGISTERS )
      throw new IllegalArgumentException ( String.format ( "registers (rc) out of bounds %d<rc<=%d|rc=%d", MIN_REGISTERS, MAX_REGISTERS, registers ) );
    
    this.registers = ByteBuffer.allocate ( Long.BYTES * this.registerCount );
    this.controlRegisters = ByteBuffer.allocate ( Long.BYTES * CONTROL_REGISTERS );
    this.memory = ByteBuffer.allocate ( ram );
    this.status = EnumSet.noneOf ( Status.class );
    // bp
    writeRegister ( controlRegisters, STACK_POINTER, ram );
    // sp
    writeRegister ( controlRegisters, BASE_POINTER, ram );
    this.mmio = new TreeMap < Integer, MMIOEntry > ();
    this.tlb = new HashMap <> ();
    this.devices = devices;
    this.devices.add ( new RAM () );
    
    // setup physical memory address space for mmio
    // to map the devices into the physical address space
    // base addresses must land on a page boundery
    
    for ( Device d : this.devices ) {
      int baseAddress = d.getRequiredBaseAddress ();
      
      if ( baseAddress % PAGE_SIZE != 0 ) {
        throw new IllegalArgumentException ( String.format ( "base address must be mapable to the first address of a page! address %x remainder %d", baseAddress, baseAddress % PAGE_SIZE ) );
      }
      
      Map.Entry < Integer, MMIOEntry > floor = mmio.floorEntry ( baseAddress );
      Map.Entry < Integer, MMIOEntry > ceiling = mmio.ceilingEntry ( baseAddress );
      if ( floor != null ) {
        int start = floor.getKey ();
        int end = start + floor.getValue ().getDevice ().getMemorySize () - 1;
        if ( baseAddress <= end ) {
          throw new IllegalArgumentException ( String.format ( "mmio address range overlap with device [%s]", floor.getValue ().getDevice () ) );
        }
      }
      if ( ceiling != null ) {
        int deviceEnd = baseAddress + d.getMemorySize () - 1;
        if ( deviceEnd >= ceiling.getKey () ) {
          throw new IllegalArgumentException ( String.format ( "mmio address range overlap with device [%s]", ceiling.getValue ().getDevice () ) );
        }
      }
      mmio.put ( baseAddress, new MMIOEntry ( d ) );
    }
  }
  
  public void step () {
    int pc = readRegister ( controlRegisters, PROGRAM_COUNTER );
    
    int instruction = 0;
    ReadMemoryAccessResult instructionRMAR = readMemoryAddress ( pc );
    // PC must always be updated
    writeRegister ( controlRegisters, PROGRAM_COUNTER, pc + INSTRUCTION_SIZE_IN_BYTES );
    
    if ( instructionRMAR.getStatus () == MemoryAccessResult.Status.SUCCESS ) {
      instruction = instructionRMAR.getValue ();
      
      int opCode = ( instruction & 0xFF000000 ) >>> 24;
      int opCodeW = ( instruction & 0xFFFF0000 ) >>> 16;
      int r1 = ( instruction & 0xFF0000 ) >>> 16;
      int r2 = ( instruction & 0xFF00 ) >>> 8;
      int r3 = instruction & 0xFF;
      int immediate24 = instruction & 0xFFFFFF;
      int immediate16 = instruction & 0xFFFF;
      int immediate8 = instruction & 0xFF;
      
      OperationCode oc = OperationCode.getByCode ( opCodeW );
      if ( oc == null ) {
        oc = OperationCode.getByCode ( opCode );
        if ( oc == null ) {
          status.add ( Status.HALT );
          status.add ( Status.ERROR_ILLEGAL_OPCODE );
          System.err.printf ( "%s%n", instructionRMAR );
          System.err.printf ( "[%x]=%x %x%n", pc, opCode, opCodeW );
          return;
        }
      }
      
      switch ( oc ) {
        case HALT:
          status.add ( Status.HALT );
          break;
        case ADD: {
          int a = readRegister ( r1 );
          int b = readRegister ( r2 );
          int c = a + b;
          lastInstResult = c;
          writeRegister ( r3, c );
          break;
        }
        case CMP:{
          int a = readRegister ( r1 );
          int b = readRegister ( r2 );
          int c = a - b;
          lastInstResult = c;
          // need to calc aux bits
          lastInstAuxBits = 0;
          break;
        }
        case CMP_Z:{
          lastInstResult = readRegister ( r2 );
          // need to calc aux bits
          lastInstAuxBits = 0;
          break;
        }
        case SUBTRACT:{
          int a = readRegister ( r1 );
          int b = readRegister ( r2 );
          int c = a - b;
          lastInstResult = c;
          // need to calc aux bits
          lastInstAuxBits = 0;
          writeRegister ( r3, c );
          break;
        }
        case MULTIPLY:{
          int a = readRegister ( r1 );
          int b = readRegister ( r2 );
          int c = a * b;
          lastInstResult = c;
          // need to calc aux bits
          lastInstAuxBits = 0;
          writeRegister ( r3, c );
          break;
        }
        case DIVIDE:{
          int a = readRegister ( r1 );
          int b = readRegister ( r2 );
          int c = a / b;
          lastInstResult = c;
          // need to calc aux bits
          lastInstAuxBits = 0;
          writeRegister ( r3, c );
          break;
        }
        case MOD:{
          int a = readRegister ( r1 );
          int b = readRegister ( r2 );
          int c = a % b;
          lastInstResult = c;
          // need to calc aux bits
          lastInstAuxBits = 0;
          writeRegister ( r3, c );
          break;
        }
        case AND:{
          int a = readRegister ( r1 );
          int b = readRegister ( r2 );
          int c = a & b;
          lastInstResult = c;
          lastInstAuxBits = 0;
          writeRegister ( r3, c );
          break;
        }
        case OR:{
          int a = readRegister ( r1 );
          int b = readRegister ( r2 );
          int c = a | b;
          lastInstResult = c;
          lastInstAuxBits = 0;
          writeRegister ( r3, c );
          break;
        }
        case XOR:{
          int a = readRegister ( r1 );
          int b = readRegister ( r2 );
          int c = a ^ b;
          lastInstResult = c;
          lastInstAuxBits = 0;
          writeRegister ( r3, c );
          break;
        }
        case NOT:{
          int a = readRegister ( r1 );
          int c = ~a;
          writeRegister ( r2, c );
          break;
        }
        case JUMP:{
          writeRegister ( controlRegisters, PROGRAM_COUNTER, readRegister ( r3 ) );
          break;
        }
        case JUMP_I:{
          writeRegister ( controlRegisters, PROGRAM_COUNTER, immediate24 );
          break;
        }
        case JUMP_Y:{
          int offset = immediate24;
          if ( ( offset & 0x800000 ) != 0 ) {
            // the number is negative we need to sign extend it
            offset = offset | 0xFF000000;
          };
          int temp = readRegister ( controlRegisters, PROGRAM_COUNTER );
          int address = temp + offset;
          writeRegister ( controlRegisters, PROGRAM_COUNTER, address );
          break;
        }
        case JUMP_Z:
        case JUMP_E:{
          if ( lastInstResult == 0 ) {
            writeRegister ( controlRegisters, PROGRAM_COUNTER, readRegister ( r1 ) );
          }
          break;
        }
        case JUMP_Z_I:
        case JUMP_E_I:{
          if ( lastInstResult == 0 ) {
            writeRegister ( controlRegisters, PROGRAM_COUNTER, immediate24 );
          }
          break;
        }
        case JUMP_NZ:
        case JUMP_NE:{
          if ( lastInstResult != 0 ) {
            writeRegister ( controlRegisters, PROGRAM_COUNTER, readRegister ( r1 ) );
          }
          break;
        }
        case JUMP_NZ_I:
        case JUMP_NE_I:{
          if ( lastInstResult != 0 ) {
            writeRegister ( controlRegisters, PROGRAM_COUNTER, immediate24 );
          }
          break;
        }
        case JUMP_NZ_Y:{
          if ( lastInstResult != 0 ) {
            int offset = immediate24;
            if ( ( offset & 0x800000 ) != 0 ) {
              // the number is negative we need to sign extend it
              offset = offset | 0xFF000000;
            };
            int temp = readRegister ( controlRegisters, PROGRAM_COUNTER );
            int address = temp + offset;
            writeRegister ( controlRegisters, PROGRAM_COUNTER, address );
          }
          break;
        }
        case SHIFT_LEFT:{
          int a = readRegister ( r2 );
          int s = readRegister ( r3 );
          a = a << s;
          writeRegister ( r2, a );
          break;
        }
        case SHIFT_RIGHT:{
          int a = readRegister ( r2 );
          int s = readRegister ( r3 );
          a = a >>> s;
          writeRegister ( r2, a );
          break;
        }
        case SHIFT_RIGHT_A:{
          int a = readRegister ( r2 );
          int s = readRegister ( r3 );
          a = a >> s;
          writeRegister ( r2, a );
          break;
        }
        case MOVE:{
          writeRegister ( r3, readRegister ( r2 ) );
          break;
        }
        case GR_TO_CR:{
          int general = r2;
          int control = r3;
          if ( ProtectionRing.RING_0.equals ( ring ) ) {
            writeRegister ( controlRegisters, control, readRegister ( registers, general ) );
          } else {
            throw new RuntimeException ( String.format ( "General Protection Fault: write to control register %d", control ) );
          }
          break;
        }
        case CR_TO_GR:{
          int control = r2;
          int general = r3;
          // reads are not protected at the moment
          writeRegister ( registers, general, readRegister ( controlRegisters, control ) );
          break;
        }
        case MOVE_I:{
          writeRegister ( r1, immediate16 );
          break;
        }
        case LOAD:{
          ReadMemoryAccessResult rmar = readMemoryAddress ( readRegister ( r2 ) );
          int value = rmar.getValue ();
          writeRegister ( r3, value );
          break;
        }
        case LOAD_I:{
          ReadMemoryAccessResult rmar = readMemoryAddress ( immediate16 );
          int value = rmar.getValue ();
          writeRegister ( r1, value );
          break;
        }
        case LOAD_Y:{
          int destination = r1;
          int base = r2;
          int offset = immediate8;
          if ( ( offset & 0x080 ) != 0 ) {
            // the number is negative we need to sign extend it
            offset = offset | 0xFFFFFF00;
          };
          int address = readRegister ( base ) + offset;
          ReadMemoryAccessResult rmar = readMemoryAddress ( address );
          int value = rmar.getValue ();
          writeRegister ( destination, value );
          break;
        }
        case LOAD_4X:{
          int destination = r1;
          int base = r2;
          int index = r3;
          int address = readRegister ( base ) + ( readRegister ( index ) * 4 );
          ReadMemoryAccessResult rmar = readMemoryAddress ( address );
          int value = rmar.getValue ();
          writeRegister ( destination, value );
          break;
        }
        case STORE:{
          int value = readRegister ( r3 );
          writeMemoryAddress ( readRegister ( r2 ), value );
          break;
        }
        case STORE_Y:{
          int source = r1;
          int base = r2;
          int offset = immediate8;
          if ( ( offset & 0x080 ) != 0 ) {
            // the number is negative we need to sign extend it
            offset = offset | 0xFFFFFF00;
          };
          int address = readRegister ( base ) + offset;
          int value = readRegister ( r1 );
          writeMemoryAddress ( address, value );
          break;
        }
        case STORE_4X:{
          int source = r1;
          int base = r2;
          int index = r3;
          int address = readRegister ( base ) + ( readRegister ( index ) * 4 );
          int value = readRegister ( r1 );
          writeMemoryAddress ( address, value );
          break;
        }
        case PUSH:{
          int sp = readRegister ( controlRegisters, STACK_POINTER );
          int value = readRegister ( r3 );
          sp -= 4;
          writeMemoryAddress ( sp, value );
          writeRegister ( controlRegisters, STACK_POINTER, sp );
          break;
        }
        case POP:{
          int sp = readRegister ( controlRegisters, STACK_POINTER );
          ReadMemoryAccessResult rmar = readMemoryAddress ( sp );
          int value = rmar.getValue ();
          sp += 4;
          writeRegister ( controlRegisters, STACK_POINTER, sp );
          writeRegister ( r3, value );
          break;
        }
        case ENTER:{
          int sp = readRegister ( controlRegisters, STACK_POINTER );
          int bp = readRegister ( controlRegisters, BASE_POINTER );
          sp -= 4;
          writeMemoryAddress ( sp, bp );
          writeRegister ( controlRegisters, STACK_POINTER, sp );
          writeRegister ( controlRegisters, BASE_POINTER, sp );
          break;
        }
        case LEAVE:{
          int bp = readRegister ( controlRegisters, BASE_POINTER );
          int sp = bp;
          ReadMemoryAccessResult rmar = readMemoryAddress ( sp );
          int old_bp = rmar.getValue ();
          sp += 4;
          writeRegister ( controlRegisters, STACK_POINTER, sp );
          writeRegister ( controlRegisters, BASE_POINTER, old_bp );
          break;
        }
        case CALL:{
          int programCounter = readRegister ( controlRegisters, PROGRAM_COUNTER );
          int sp = readRegister ( controlRegisters, STACK_POINTER );
          int jumpTo = readRegister ( r3 );
          sp -= 4;
          writeMemoryAddress ( sp, programCounter );
          writeRegister ( controlRegisters, STACK_POINTER, sp );
          writeRegister ( controlRegisters, PROGRAM_COUNTER, jumpTo );
          break;
        }
        case CALL_I:{
          int programCounter = readRegister ( controlRegisters, PROGRAM_COUNTER );
          int sp = readRegister ( controlRegisters, STACK_POINTER );
          int jumpTo = immediate24;
          sp -= 4;
          writeMemoryAddress ( sp, programCounter );
          writeRegister ( controlRegisters, STACK_POINTER, sp );
          writeRegister ( controlRegisters, PROGRAM_COUNTER, jumpTo );
          break;
        }
        case RETURN:{
          int sp = readRegister ( controlRegisters, STACK_POINTER );
          ReadMemoryAccessResult rmar = readMemoryAddress ( sp );
          int jumpTo = rmar.getValue ();
          sp += 4;
          writeRegister ( controlRegisters, STACK_POINTER, sp );
          writeRegister ( controlRegisters, PROGRAM_COUNTER, jumpTo );
          break;
        }
        case TLB_FLUSH:{
          tlb.clear ();
          break;
        }
        case ENABLE_PAGING:{
          pagingEnabled = true;
          break;
        }
        case DISABLE_PAGING:{
          pagingEnabled = false;
          break;
        }
        case INCREMENT:{
          int a = readRegister ( r2 );
          a++;
          lastInstResult = a;
          lastInstAuxBits = 0;
          writeRegister ( r2, a );
          break;
        }
        case DECREMENT:{
          int a = readRegister ( r2 );
          a--;
          lastInstResult = a;
          lastInstAuxBits = 0;
          writeRegister ( r2, a );
          break;
        }
        case NO_OP:
          break;
        case SYSTEM_ENTER:
          int enterPC = readRegister ( controlRegisters, SYSTEM_ENTER_PROGRAM_COUNTER );
          int enterStack = readRegister ( controlRegisters, SYSTEM_ENTER_STACK_POINTER );
          writeRegister ( controlRegisters, PROGRAM_COUNTER, enterPC );
          writeRegister ( controlRegisters, STACK_POINTER, enterStack );
          ring = ProtectionRing.RING_0;
          break;
        case SYSTEM_EXIT:
          int returnPC = readRegister ( registers, 0 );
          int returnStackPointer = readRegister ( registers, 1 );
          writeRegister ( controlRegisters, PROGRAM_COUNTER, returnPC );
          writeRegister ( controlRegisters, STACK_POINTER, returnPC );
          ring = ProtectionRing.RING_3;
          break;
        default:
          status.add ( Status.HALT );
          status.add ( Status.ERROR_ILLEGAL_OPCODE );
          break;
      }
      instructionCount++;
    } else {
      // pc page fault
      status.add ( Status.ERROR_PAGE_FAULT );
      writeRegister ( controlRegisters, PAGE_FAULT_ADDRESS, instructionRMAR.getAddress () );
    }
    
    // service inturrupts
    if ( !status.isEmpty () ) {
      Integer idtIndex = null;
      if ( status.contains ( Status.ERROR_PAGE_FAULT ) ) {
        idtIndex = 14;
      }
      
      if ( idtIndex != null ) {
        int idtBase = readRegister ( INTERRUPT_DESCRIPTOR_TABLE );
        // idt entries are 4 bytes
        int address = idtIndex * Integer.BYTES;
        // TODO execute inturrupt handler;
      }
    }
  }
  
  public void run () {
    while ( status.isEmpty () ) {
      step ();
    }
  }
  
  public long getInstructionCount () {
    return instructionCount;
  }
  
  public Set < Status > getStatus () {
    return Collections.unmodifiableSet ( status );
  }
  
  public ProtectionRing getRing () {
    return ring;
  }
  
  public int getRegisterCount () {
    return registerCount;
  }
  
  public ByteBuffer getGeneralRegisters () {
    return registers.asReadOnlyBuffer ();
  }
  
  public ByteBuffer getControlRegisters () {
    return controlRegisters.asReadOnlyBuffer ();
  }
  
  public ByteBuffer getMemory () {
    return memory.asReadOnlyBuffer ();
  }
  
  public Map < Integer, MMIOEntry > getMMIO () {
    return Collections.unmodifiableMap ( mmio );
  }
  
  private int readRegister ( int register ) {
    return readRegister ( this.registers, register );
  }
  
  private int readRegister ( ByteBuffer registers, int register ) {
    int address = ( register * 2 ) + 1;
    IntBuffer intReg = registers.asIntBuffer ();
    // registers are 8 bytes long r0 in int mode is at byte [4,8) r1 is at byte [12,16)
    return intReg.get ( address );
  }
  
  private void writeRegister ( int register, int value ) {
    writeRegister ( this.registers, register, value );
  }
  
  private void writeRegister ( ByteBuffer registers, int register, int value ) {
    int address = ( register * 2 ) + 1;
    IntBuffer intReg = registers.asIntBuffer ();
    intReg.put ( address, value );
  }
  
  private ReadMemoryAccessResult readMemoryAddress ( int address ) {
    if ( pagingEnabled ) {
      return readMemoryVirtual ( address );
    } else {
      return ReadMemoryAccessResult.success ( address, readMemoryPhysical ( address ) );
    }
  }
  
  private WriteMemoryAccessResult writeMemoryAddress ( int address, int value ) {
    if ( pagingEnabled ) {
      return writeMemoryVirtual ( address, value );
    } else {
      writeMemoryPhysical ( address, value );
      return WriteMemoryAccessResult.success ( address );
    }
  }
  
  private ReadMemoryAccessResult readMemoryVirtual ( int address ) {
    int logical = address & (~PAGE_OFFSET_MASK);
    int offset = address & PAGE_OFFSET_MASK;
    Integer phy = tlb.get ( logical );
    if ( phy == null ) {
      phy = walkPageTable ( address );
      if ( phy == null ) {
        return ReadMemoryAccessResult.pageFault ( address );
      } else {
        tlb.put ( logical, phy );
        return ReadMemoryAccessResult.success ( address, readMemoryPhysical ( phy | offset ) );
      }
    } else {
      int result = phy | offset;
      return ReadMemoryAccessResult.success ( address, readMemoryPhysical ( result ) );
    }
  }
  
  private WriteMemoryAccessResult writeMemoryVirtual ( int address, int value ) {
    int logical = address & (~PAGE_OFFSET_MASK);
    int offset = address & PAGE_OFFSET_MASK;
    Integer phy = tlb.get ( logical );
    if ( phy == null ) {
      phy = walkPageTable ( address );
      if ( phy == null ) {
        return WriteMemoryAccessResult.pageFault ( address );
      } else {
        tlb.put ( logical, phy );
        writeMemoryPhysical ( phy | offset, value );
        return WriteMemoryAccessResult.success ( address );
      }
    } else {
      writeMemoryPhysical ( phy | offset, value );
      return WriteMemoryAccessResult.success ( address );
    }
  }
  
  private int readMemoryPhysical ( int address ) {
    Map.Entry < Integer, MMIOEntry > mmioe = mmio.floorEntry ( address );
    if ( mmioe == null ) {
      throw new AddressOutOfBoundsException ( address );
    }
    try {
      int base = mmioe.getKey ();
      int deviceAddress = address - base;
      return mmioe.getValue ().getDevice ().readMemoryAddress ( deviceAddress );
    } catch ( IndexOutOfBoundsException e ) {
      throw new AddressOutOfBoundsException ( address, e );
    }
  }
  
  private void writeMemoryPhysical ( int address, int value ) {
    Map.Entry < Integer, MMIOEntry > mmioe = mmio.floorEntry ( address );
    if ( mmioe == null ) {
      throw new AddressOutOfBoundsException ( address );
    }
    try {
      int base = mmioe.getKey ();
      int deviceAddress = address - base;
      mmioe.getValue ().getDevice ().writeMemoryAddress ( deviceAddress, value );
    } catch ( IndexOutOfBoundsException e ) {
      throw new AddressOutOfBoundsException ( address, e );
    }
  }
  
  private Integer walkPageTable ( int address ) {
    System.err.printf ( "walk: %x%n", address );
    // NOTE: we are reading physical addresses
    //
    // TODO this is more complicated we need to check the entry status bits
    int pageDirectoryBaseAddress = readRegister ( controlRegisters, PAGE_TABLE_BASE );
    System.err.printf ( "pageDirectoryBaseAddress: %x%n", pageDirectoryBaseAddress );
    int pageDirectoryIndex = ( address & PAGE_DIRECTORY_INDEX_MASK ) >>> 22;
    System.err.printf ( "pageDirectoryIndex: %x%n", pageDirectoryIndex );
    int pageDirectoryEntryAddress = pageDirectoryBaseAddress + pageDirectoryIndex * PAGE_DIRECTORY_ENTRY_SIZE;
    System.err.printf ( "pageDirectoryEntryAddress: %x%n", pageDirectoryEntryAddress );
    int pageDirectoryEntry = readMemoryPhysical ( pageDirectoryEntryAddress );
    System.err.printf ( "pageDirectoryEntry: %x%n", pageDirectoryEntry );
    boolean pageDirectoryEntryValid = ( pageDirectoryEntry & PAGE_ENTRY_VALID_FLAG ) != 0;
    
    if ( !pageDirectoryEntryValid )
      return null;
    
    int pageTableBaseAddress = pageDirectoryEntry & PAGE_DIRECTORY_ENTRY_ADDRESS_MASK;
    System.err.printf ( "pageTableBaseAddress: %x%n", pageTableBaseAddress );
    int pageTableIndex = ( address & PAGE_TABLE_INDEX_MASK ) >> 12;
    System.err.printf ( "pageTableIndex: %x%n", pageTableIndex );
    int pageTableEntryAddress = pageTableBaseAddress + pageTableIndex * PAGE_TABLE_ENTRY_SIZE;
    System.err.printf ( "pageTableEntryAddress: %x%n", pageTableEntryAddress );
    int pageTableEntry = readMemoryPhysical ( pageTableEntryAddress );
    System.err.printf ( "pageTableEntry: %x%n", pageTableEntry );
    boolean pageTableEntryValid = ( pageTableEntry & PAGE_ENTRY_VALID_FLAG ) != 0;
    
    if ( !pageTableEntryValid )
      return null;
    
    int physicalAddress = pageTableEntry & PAGE_TABLE_ENTRY_ADDRESS_MASK;
    System.err.printf ( "physicalAddress: %x%n", physicalAddress );
    return physicalAddress;
  }
  
  private class RAM implements Device {
    @Override
    public int getRequiredBaseAddress () {
      return SVMJ.PHYSICAL_RAM_ADDRESS_START;
    }
    
    @Override
    public int readMemoryAddress ( int address ) {
      try {
        return SVMJ.this.memory.getInt ( address );
      } catch ( IndexOutOfBoundsException e ) {
        throw new AddressOutOfBoundsException ( address, e );
      }
    }
    
    @Override
    public void writeMemoryAddress ( int address, int value ) {
      try {
        SVMJ.this.memory.putInt ( address, value );
      } catch ( IndexOutOfBoundsException e ) {
        throw new AddressOutOfBoundsException ( address, e );
      }
    }
    
    @Override
    public int getMemorySize () {
      return SVMJ.this.memory.capacity ();
    }
    
    @Override
    public String toString () {
      return String.format ( "SVMJ$RAM[baseAddress=%xH, memory=%s]", getRequiredBaseAddress (), SVMJ.this.memory );
    }
  }
  
  public enum Status {
    HALT,
    ERROR_ILLEGAL_OPCODE,
    ERROR_UNKNOWN,
    ERROR_PAGE_FAULT
  }
  
  public enum ProtectionRing {
    RING_0,
    RING_3
  }
  
  public static final class MMIOEntry {
    public Device device;
    
    public MMIOEntry () {
      this ( null );
    }
    
    public MMIOEntry ( Device d ) {
      this.device = d;
    }
    
    public Device getDevice () {
      return device;
    }
  }
}