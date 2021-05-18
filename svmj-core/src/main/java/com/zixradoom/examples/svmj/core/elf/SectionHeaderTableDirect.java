
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class SectionHeaderTableDirect implements SectionHeaderTable {
  
  private ELFDirect elf;
  
  SectionHeaderTableDirect ( ELFDirect elf ) {
    this.elf = elf;
  }
  
  @Override
  public boolean isDirect () {
    return true;
  }
  
  @Override
  public SectionHeader get ( int index ) {
    if ( index < 0 || index >= getSize () ) {
      throw new IndexOutOfBoundsException ( String.format ( "0 <= x < %d | x = %d", getSize (), index )  );
    }
    
    return new SectionHeaderDirect ( this, index );
  }
  
  @Override
  public int getSize () {
    return elf.getSectionHeaderEntryCount ();
  }
  
  ELFDirect getELF () {
    return elf;
  }
}