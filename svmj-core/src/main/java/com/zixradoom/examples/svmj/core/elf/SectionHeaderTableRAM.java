
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class SectionHeaderTableRAM implements SectionHeaderTable {

  private ELFRam elf;
  
  SectionHeaderTableRAM ( ELFRam elf ) {
    this.elf = elf;
  }
  
  @Override
  public boolean isDirect () {
    return false;
  }
  
  
  @Override
  public SectionHeaderRAM get ( int index ) {
    return elf.getSections ().get ( index ).getHeader ();
  }
  
  @Override
  public int getSize () {
    return elf.getSections ().size ();
  }
  
  public int indexOf ( String name ) {
    if ( name == null | name.isBlank () ) {
      return 0;
    }
    for ( int index = 0; index < elf.getSections ().size (); index++ ) {
      AbstractSectionRAM asr = elf.getSections ().get ( index );
      if ( name.equals ( asr.getName () ) ) {
        return index;
      }
    }
    return -1;
  }
  
  public SectionHeaderRAM getByName ( String name ) {
    return elf.getSections ().get ( indexOf ( name ) ).getHeader ();
  }
}