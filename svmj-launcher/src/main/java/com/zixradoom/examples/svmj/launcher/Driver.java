
package com.zixradoom.examples.svmj.launcher;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.Properties;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.zixradoom.examples.svmj.core.SVMJ;
import com.zixradoom.examples.svmj.core.Device;
import com.zixradoom.examples.svmj.core.SimpleROM;
import com.zixradoom.examples.svmj.core.OperationCode;
import com.zixradoom.examples.svmj.core.Assembler2;
import com.zixradoom.examples.svmj.core.ParseException;
import com.zixradoom.examples.svmj.core.elf.ELF;
import com.zixradoom.examples.svmj.core.elf.ELFRam;
import com.zixradoom.examples.svmj.core.elf.ELFUtil;
import com.zixradoom.examples.svmj.core.linker.Linker;
import com.zixradoom.examples.svmj.core.rom.ROMFactory;
import com.zixradoom.examples.svmj.assembler.Assembler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Driver {
  private static final Logger LOGGER = LoggerFactory.getLogger ( Driver.class );
  
  private static final String SEP = "================================";
  private static final int REGISTER_COUNT = 24; //2MiB
  private static final int RAM_SIZE = 0x200000; //2MiB
  private static final int BITS_PER_ELEMENT = 32;
  
  public static void main ( String[] args ) throws IOException, ClassNotFoundException, ParseException {
    List < Device > devices = loadDevices ( Paths.get ( args[ 0 ] ) );
    
    SVMJ vm = new SVMJ ( REGISTER_COUNT, RAM_SIZE, devices );

    printMMIO ( vm.getMMIO () );
    System.out.println ( SEP );

    try {
      vm.run ();
    } catch ( Exception e ) {
      e.printStackTrace ( System.out );
      System.out.println ( SEP );
    }
    System.out.println ( SEP );
    System.out.println ( vm.getStatus () );
    System.out.println ( vm.getRing () );
    printRegisters ( vm );
    
    Device ram = vm.getMMIO ().get ( SVMJ.PHYSICAL_RAM_ADDRESS_START ).getDevice ();
    
    System.out.println ( SEP );
    printPageTable ( System.out, ram, 0x1000, SVMJ.PHYSICAL_RAM_ADDRESS_START );
    System.out.println ( SEP );
    printStack ( System.out, ram, 0x1ffffc, SVMJ.PHYSICAL_RAM_ADDRESS_START );
    System.out.println ( SEP );
    printStack ( System.out, ram, 0x1ffbf0, SVMJ.PHYSICAL_RAM_ADDRESS_START );
    System.out.println ( SEP );
    printFrameBitmap ( System.out, ram, SVMJ.PHYSICAL_RAM_ADDRESS_START, SVMJ.PHYSICAL_RAM_ADDRESS_START, ( RAM_SIZE / SVMJ.PAGE_SIZE ) / BITS_PER_ELEMENT );
    System.out.println ( SEP );
    
    System.out.printf ( "instruction count: %d", vm.getInstructionCount () );
  }
  
  private static List < Device > loadDevices ( Path deviceDirectory ) throws IOException, ClassNotFoundException, ParseException {
    List < Device > devices = new ArrayList <> ();
    try ( DirectoryStream < Path > deviceFiles = Files.newDirectoryStream ( deviceDirectory, "*.device" ) ) {
      for ( Path deviceFile : deviceFiles ) {
        System.out.println ( SEP );
        System.out.printf ( "device file: %s%n", deviceFile );
        devices.add ( loadDevice ( deviceFile ) );
      }
      System.out.println ( SEP );
    }
    return devices;
  }
  
  private static Device loadDevice ( Path file ) throws IOException, ClassNotFoundException, ParseException {
    Properties props = new Properties ();
    try ( InputStream is = Files.newInputStream ( file ) ) {
      props.load ( is );
    }
    String className = props.getProperty ( "driver" );
    Class< ? > clazz = Class.forName ( className );
    if ( SimpleROM.class.isAssignableFrom ( clazz ) ) {
      return createSimpleRom ( props, file );
    } else {
      throw new RuntimeException ( String.format ( "unrecognized class name [%s]", className ) );
    }
  }
  
  private static SimpleROM createSimpleRom ( Properties props, Path deviceFile ) throws IOException, ParseException {
    String source = props.getProperty ( "source.code" );
    String sourceDir = props.getProperty ( "source.dir" );
    String sourceBin = props.getProperty ( "source.bin" );
    String name = props.getProperty ( "name" );
    boolean bios = Boolean.valueOf ( props.getProperty ( "source.bios" ) );
    int baseAddress = Integer.valueOf ( props.getProperty ( "base.address" ) );
    
    LOGGER.debug ( "source.bios={}", bios );
    
    if ( source != null ) {
      Path sourceFile = deviceFile.getParent ().resolve ( source );
      ByteBuffer image = compileSimple ( sourceFile, baseAddress );
      return new SimpleROM ( name, baseAddress, image );
    } else {
      Path sourceDirPath = deviceFile.getParent ().resolve ( sourceDir );
      Path sourceBinPath = deviceFile.getParent ().resolve ( sourceBin );
      Path output = sourceBinPath.resolve ( deviceFile.getFileName ().toString ().replace ( "device", "exe" ) );
      ELF elf = compile ( sourceDirPath, sourceBinPath, output, baseAddress );
      if ( bios ) {
        return ROMFactory.createBIOSRom ( name, baseAddress, elf );
      } else {
        return ROMFactory.createRom ( name, baseAddress, elf );
      }
    }
  }
  
  private static ByteBuffer compileSimple ( Path file, int baseAddress ) throws IOException, ParseException {
    try ( InputStream is = Files.newInputStream ( file ) ) {
      Assembler2 parser = new Assembler2 ( is );
      parser.setBaseAddress ( baseAddress );
      parser.Program ();
      
      ByteBuffer program = parser.getProgramBuilder ().getProgram ();
      System.out.println ( "SimpleROM contents" );
      printProgram ( program.duplicate () );
      System.out.println ( "----" );
      printSymbolTable ( parser.getSymbolTable () );
      
      return program;
    }
  }
  
  private static ELF compile ( Path sourceDir, Path sourceBin, Path output, int baseAddress ) throws IOException, ParseException {
    List < Path > objFiles = new ArrayList <> ();
    if ( Files.notExists ( sourceBin ) ) {
      Files.createDirectories ( sourceBin );
    }
    try ( DirectoryStream < Path > ds = Files.newDirectoryStream ( sourceDir, "*.asm" ) ) {
      for ( Path source : ds ) {
        Path objectFile = sourceBin.resolve ( source.getFileName ().toString ().replace ( ".asm", ".o" ) );
        objFiles.add ( objectFile );
        compileSingle ( source, objectFile );
      }
    }
    Linker linker = new Linker ( objFiles, output );
    linker.setPhysicalBaseAddress ( baseAddress );
    linker.link ();
        
    return ELFUtil.read ( output );
  }
  
  private static void compileSingle ( Path file, Path out ) throws IOException, ParseException {
    Assembler assembler = new Assembler ( file, out );
    assembler.assemble ();
  }
  
  private static void printMMIO ( Map < Integer, SVMJ.MMIOEntry > mmioTable ) {
    System.out.println ( "MMIO" );
    for ( Map.Entry < Integer, SVMJ.MMIOEntry > e : mmioTable.entrySet () ) {
      int start = e.getKey ();
      int end = start + e.getValue ().getDevice ().getMemorySize () - 1;
      Device d = e.getValue ().getDevice ();
      System.out.printf ( "%8xH %8xH %s%n", start, end, d.toString () );
    }
  }
  
  private static void printRegisters ( SVMJ vm ) {
    ByteBuffer registers = vm.getControlRegisters ();
    System.out.println ( "Control" );
    for ( int index = 0; index < SVMJ.CONTROL_REGISTERS; ) {
      int start = index;
      for ( ; index < start + 4; index++ ) {
        System.out.printf ( "C%3d %16xH ", index, registers.getLong ( index * Long.BYTES ) );
      }
      System.out.println ();
    }
    System.out.println ( "General" );
    registers = vm.getGeneralRegisters ();
    for ( int index = 0; index < vm.getRegisterCount (); ) {
      int start = index;
      for ( ; index < start + 4; index++ ) {
        System.out.printf ( "R%3d %16xH ", index, registers.getLong ( index * Long.BYTES ) );
      }
      System.out.println ();
    }
  }
  
  private static void printProgram ( ByteBuffer data ) {
    while ( data.hasRemaining () ) {
      if ( ( data.getInt ( data.position () ) >>> 16 ) == OperationCode.ENTER.getCode () ) System.out.println ();
      System.out.printf ( "%8xH %8xH%n", data.position (), data.getInt () );
    }
  }
  
  private static void printSymbolTable ( Map < String, Integer > table ) {
    for ( String label : table.keySet () ) {
      System.out.printf ( "%8xH %s%n", table.get ( label ), label );
    }
  }
  
  private static void printPageTable ( PrintStream ps, Device d, int pdBaseAddress, int ramBase ) {
    ps.printf ( "Page Directory @ %xh%n", pdBaseAddress + ramBase );
    List < Integer > pts = new ArrayList <> ();
    // there are 1024 entries in a page directory
    // but, the last one (index 1023) always points
    // to the address of **this** page directory
    for ( int index = 0; index < 1023; index++ ) {
      int address = pdBaseAddress + index * 4;
      int entry = d.readMemoryAddress ( address );
      if ( ( entry & 0x1 ) == 1 ) {
        pts.add ( ( entry & 0xFFFFF000 ) - ramBase );
        ps.printf ( "[%3x] %4d: %8xh%n", index, index, entry );
      }
    }
    
    for ( Integer ptBaseAddress : pts ) {
      ps.printf ( "Page Table @ %xh%n", ptBaseAddress + ramBase );
      
      for ( int index = 0; index < 1024; index++ ) {
        int address = ptBaseAddress + index * 4;
        int entry = d.readMemoryAddress ( address );
        if ( ( entry & 0x1 ) == 1 ) {
          ps.printf ( "[%3x] %4d: %8xh%n", index, index, entry );
        }
      }
    }
  }
  
  private static void printStack ( PrintStream ps, Device d, int stack, int ramBase ) {
    boolean search = true;
    int emptyCount = 0;
    int index = 0;
    ps.printf ( "Stack @ %xh%n", stack + ramBase );
    while ( search && emptyCount < 5 ) {
      int address = stack - index;
      int value = d.readMemoryAddress ( address );
      if ( value == 0 ) {
        emptyCount++;
      } else {
        emptyCount = 0;
      }
      ps.printf ( "[%8x]: %8xh%n", address + ramBase, value );
      index += 4;
    }
  }
  
  private static void printFrameBitmap ( PrintStream ps, Device d, int address, int baseAddress, int size ) {
    ps.printf ( "Page Frame Allocator Bitmap @ %xh%n", address );
    for ( int index = 0; index < size; index++ ) {
      int offset = index * Integer.BYTES;
      int entryAddress =  ( address - baseAddress ) + offset;
      int value = d.readMemoryAddress ( entryAddress );
      ps.printf ( "[%x]=%x%n", address + offset, value );
    }
  }
}