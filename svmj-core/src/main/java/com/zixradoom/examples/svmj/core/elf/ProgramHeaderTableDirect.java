
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public final class ProgramHeaderTableDirect implements ProgramHeaderTable {
  
  private ELFDirect elf;
  
  ProgramHeaderTableDirect ( ELFDirect elf ) {
    this.elf = elf;
  }
  
  @Override
  public boolean isDirect () {
    return true;
  }
  
  @Override
  public ProgramHeader get ( int index ) {
    
    if ( index < 0 || index >= getSize () ) {
      throw new IndexOutOfBoundsException ( String.format ( "0 <= x < %d | x = %d", getSize (), index )  );
    }
      
    return new ProgramHeaderDirect ( this, index );
  }
  
  @Override
  public int getSize () {
    return elf.getProgramHeaderEntryCount ();
  }
  
  ELFDirect getELF () {
    return elf;
  }
}