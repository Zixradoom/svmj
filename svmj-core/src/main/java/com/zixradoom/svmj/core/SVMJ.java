
package com.zixradoom.svmj.core;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public final class SVMJ {
	public static final int PAGE_SIZE = 4096;
	public static final int PAGE_OFFSET_MASK = PAGE_SIZE - 1;
	public static final long MAIN_MEMORY_VIRTUAL_ADDRESS_BASE = 0x3F000000L;
	public static final int R_PC = 0;
	public static final int R_PD = 1;
	public static final int R_SP = 2;
	public static final int R_BP = 3;
	
	private ByteBuffer registers;
	private ByteBuffer mainMemory;
	
	private List < Device > devices;
	private Device ram;
	private NavigableMap < Long, Device > deviceAddressTable;
	private Set < Status > status;
	private Map < Long, Long > tlb;
	
	private boolean paging = false;
	private int instructionCount;
	private int registerCount;
	private int lastInstResult;
	private int lastInstAuxBits;
	
	public SVMJ ( int registerCount, int memorySize, List < Device > devices ) {
		this.registerCount = registerCount;
		this.devices = devices;
		this.registers = ByteBuffer.allocate ( registerCount * Long.BYTES );
		this.status = EnumSet.noneOf ( Status.class );
		this.deviceAddressTable = new TreeMap <> ();
		this.tlb = new HashMap <> ();
		
		ram = new RAM ();
		this.devices.add ( ram );
		
		for ( Device d : this.devices ) {
			//TODO check for address space overlap
			deviceAddressTable.put ( d.getBaseAddress (), d );
		}
	}
	
	public void step () {
		int pc = readRegister ( R_PC );
		int instruction = readMemoryAddress ( pc );
		int opCode = ( instruction & 0xFF000000 ) >>> 24;
		int opCodeW = ( instruction & 0xFFFF0000 ) >>> 16;
	  int r1 = ( instruction & 0xFF0000 ) >>> 16;
	  int r2 = ( instruction & 0xFF00 ) >>> 8;
	  int r3 = instruction & 0xFF;
		int immediate24 = instruction & 0xFFFFFF;
		int immediate16 = instruction & 0xFFFF;
		int immediate8 = instruction & 0xFF;
		
		OpCode oc = OpCode.getByCode ( opCodeW );
		if ( oc == null ) {
			oc = OpCode.getByCode ( opCode );
			if ( oc == null ) {
				status.add ( Status.ERROR );
				return;
			}
		}
		
		switch ( oc ) {
		case ADD:{
			int a = readRegister ( r1 );
			int b = readRegister ( r2 );
			int c = a + b;
			lastInstResult = c;
			lastInstAuxBits = 0;
			writeRegister ( r3, c );
			break;
		}
		case SUBTRACT:{
			int a = readRegister ( r1 );
			int b = readRegister ( r2 );
			int c = a - b;
			lastInstResult = c;
			lastInstAuxBits = 0;
			writeRegister ( r3, c );
			break;
		}
		case MULTIPLY:{
			int a = readRegister ( r1 );
			int b = readRegister ( r2 );
			int c = a * b;
			lastInstResult = c;
			lastInstAuxBits = 0;
			writeRegister ( r3, c );
			break;
		}
		case DIVIDE:{
			int a = readRegister ( r1 );
			int b = readRegister ( r2 );
			int c = a / b;
			lastInstResult = c;
			lastInstAuxBits = 0;
			writeRegister ( r3, c );
			break;
		}
		case COMPARE:{
			int a = readRegister ( r1 );
			int b = readRegister ( r2 );
			int c = a - b;
			lastInstResult = c;
			lastInstAuxBits = 0;
			break;
		}
		case MOVE:{
			writeRegister ( r2, readRegister ( r1 ) );
			break;
		}
		case LOAD:{
			long address = readRegister ( r2 );
			int value = readMemoryAddress ( address );
			writeRegister ( r1, value );
			break;
		}
		case STORE:{
			long address = readRegister ( r2 );
			int value = readRegister ( r1 );
			writeMemoryAddress ( address, value );
			break;
		}
		case PUSH:{
			int sp = readRegister ( R_SP );
			sp = sp - 4;
			writeMemoryAddress ( sp, readRegister ( r3 ) );
			writeRegister ( R_SP, sp );
			break;
		}
		case POP:{
			int sp = readRegister ( R_SP );
			int value = readMemoryAddress ( sp );
			writeRegister ( r3, value );
			sp = sp + 4;
			writeRegister ( R_SP, sp );
			break;
		}
		case ENTER:{
			int sp = readRegister ( R_SP );
			sp = sp - 4;
			writeMemoryAddress ( sp, readRegister ( R_BP ) );
			writeRegister ( R_BP, sp );
			writeRegister ( R_SP, sp );
			break;
		}
		case LEAVE:{
			int bp = readRegister ( R_BP );
			int sp = bp;
			int oldBp = readMemoryAddress ( sp ); 
			sp = sp + 4;
			writeRegister ( R_SP, sp );
			writeRegister ( R_BP, oldBp );
			break;
		}
		case CALL:{
			int sp = readRegister ( R_SP );
			sp = sp - 4;
			writeMemoryAddress ( sp, readRegister ( R_PC ) );
			writeRegister ( R_SP, sp );
			writeRegister ( R_PC, readRegister ( r3 ) );
			break;
		}
		case RETURN:{
			int sp = readRegister ( R_SP );
			int value = readMemoryAddress ( sp );
			writeRegister ( R_PC, value );
			sp = sp + 4;
			writeRegister ( R_SP, sp );
			break;
		}
		case HALT:{
			status.add ( Status.HALT );
			break;
		}
		default:
			status.add ( Status.ERROR );
			break;
		}
		instructionCount++;
	}
	
	public void run () {
		while ( !( getStatus ().contains ( Status.HALT ) || getStatus ().contains ( Status.ERROR ) ) ) {
			step ();
		}
	}
	
	public int getInstructionCount () {
		return instructionCount;
	}
	
	public int getRegisterCount () {
		return registerCount;
	}
	
	public long getMemorySize () {
		return mainMemory.capacity ();
	}
	
	public Map< Long, Device > getDeviceAddressTable () {
		return Collections.unmodifiableMap ( deviceAddressTable );
	}
	
	public Set< Status > getStatus () {
		return Collections.unmodifiableSet ( status );
	}
	
	private int readRegister ( int register ) {
		return registers.getInt ( ( register * Long.BYTES ) + Integer.BYTES );
	}
	
	private void writeRegister ( int register, int value ) {
		registers.putInt ( ( register * Long.BYTES ) + Integer.BYTES, value );
	}
	
	private long readRegisterL ( int register ) {
		return registers.getLong ( register * Long.BYTES );
	}
	
	private void writeRegisterL ( int register, long value ) {
		registers.putLong ( register * Long.BYTES, value );
	}
	
	private int readMemoryAddress ( long address ) {
    if ( paging ) {
			return readVirtualAddress ( address );
		} else {
			return readPhysicalAddress ( address );
		}
	}
	
	private void writeMemoryAddress ( long address, int value ) {
    if ( paging ) {
			writeVirtualAddress ( address, value );
		} else {
			writePhysicalAddress ( address, value );
		}
	}
	
	private int readVirtualAddress ( long address ) {
		long logical = (int)address & ~PAGE_OFFSET_MASK;
		long offset = (int)address & PAGE_OFFSET_MASK;
		long physical;
		if ( tlb.containsKey ( logical ) ) {
			long frame = tlb.get ( logical );
			physical = frame | offset;
		} else {
			long frame = walkPageTable ( address );
			tlb.put ( logical, frame );
			physical = frame | offset;
		}
		return readPhysicalAddress ( physical );
	}
	
  private void writeVirtualAddress ( long address, int value ) {
  	long logical = (int)address & ~PAGE_OFFSET_MASK;
		long offset = (int)address & PAGE_OFFSET_MASK;
		long physical;
		if ( tlb.containsKey ( logical ) ) {
			long frame = tlb.get ( logical );
			physical = frame | offset;
		} else {
			long frame = walkPageTable ( address );
			tlb.put ( logical, frame );
			physical = frame | offset;
		}
		writePhysicalAddress ( physical, value );
	}
  
  private long walkPageTable ( long address ) {
  	int pdBase = readRegister ( R_PD );
  	int pdIndex = (int)address & 0xFFC00000;
  	int pdEntryAddress = pdIndex * Integer.BYTES + pdBase;
  	int pdEntry = readMemoryAddress ( pdEntryAddress );
  	int ptBase = pdEntry & ~PAGE_OFFSET_MASK;
  	int ptIndex = (int)address & 0x3FF000;
  	int ptEntryAddress = ptIndex * Integer.BYTES + ptBase;
  	int ptEntry = readMemoryAddress ( ptEntryAddress );
  	int pageFrame = ptEntry & 0x3FF000;
  	return pageFrame;
  }
  
  private int readPhysicalAddress ( long address ) {
  	Map.Entry < Long, Device > ceil = deviceAddressTable.floorEntry ( address );
		return ceil.getValue ().readMemoryAddress ( address - ceil.getKey () );
	}
	
  private void writePhysicalAddress ( long address, int value ) {
  	Map.Entry < Long, Device > ceil = deviceAddressTable.floorEntry ( address );
		ceil.getValue ().writeMemoryAddress ( address - ceil.getKey (), value );
	}
	
	public enum Status {
		HALT,
		ERROR
	}
	
	private class RAM implements Device {

		@Override
		public long getBaseAddress ()
		{
			return MAIN_MEMORY_VIRTUAL_ADDRESS_BASE;
		}

		@Override
		public int getMemorySize ()
		{
			return SVMJ.this.mainMemory.capacity ();
		}

		@Override
		public int readMemoryAddress ( long address )
		{
			try {
				//TODO handle addresses that are too large??
			  return SVMJ.this.mainMemory.getInt ( (int)address );
			} catch ( IndexOutOfBoundsException e ) {
				throw new AddressOutOfBoundsException ( address, e );
			}
		}

		@Override
		public void writeMemoryAddress ( long address, int value )
		{
			try {
				//TODO handle addresses that are too large??
			  SVMJ.this.mainMemory.putInt ( (int)address, value );
			} catch ( IndexOutOfBoundsException e ) {
				throw new AddressOutOfBoundsException ( address, e );
			}
		}
	}
}