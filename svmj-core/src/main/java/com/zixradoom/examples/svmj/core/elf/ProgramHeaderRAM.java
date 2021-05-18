
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ProgramHeaderRAM implements ProgramHeader {
  
  private int elfClass;
  private int headerSize;
  
  private int type;
  private int flags;
  private long offset;
  private long virtualAddress;
  private long physicalAddress;
  private long fileSize;
  private long memorySize;
  private long align;

  ProgramHeaderRAM ( int elfClass, int headerSize ) {
    this.elfClass = elfClass;
  }

  @Override
  public int getType () {
    return type;
  }
  
  @Override
  public int getFlags () {
    return flags;
  }
  
  @Override
  public long getOffset () {
    return offset;
  }
  
  @Override
  public long getVirtualAddress () {
    return virtualAddress;
  }
  
  @Override
  public long getPhysicalAddress () {
    return physicalAddress;
  }
  
  @Override
  public long getFileSize () {
    return fileSize;
  }
  
  @Override
  public long getMemorySize () {
    return memorySize;
  }
  
  @Override
  public long getAlign () {
    return align;
  }
  
  ByteBuffer toBuffer () {
    ByteBuffer bb = ByteBuffer.allocate ( headerSize );
    bb.putInt ( type );
    if ( elfClass == ELF.ELFClass.CLASS_32.getValue () ) {
      bb.putInt ( (int)offset );
      bb.putInt ( (int)virtualAddress );
      bb.putInt ( (int)physicalAddress );
      bb.putInt ( (int)fileSize );
      bb.putInt ( (int)memorySize );
      bb.putInt ( flags );
      bb.putInt ( (int)align );
    } else if ( elfClass == ELF.ELFClass.CLASS_64.getValue ()  ) {
      bb.putInt ( flags );
      bb.putLong ( offset );
      bb.putLong ( virtualAddress );
      bb.putLong ( physicalAddress );
      bb.putLong ( fileSize );
      bb.putLong ( memorySize );
      bb.putLong ( align );
    } else {
      throw new IllegalArgumentException ( String.format ( "elf class %d", elfClass ) );
    }
    return bb;
  }
}