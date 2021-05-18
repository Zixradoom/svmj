
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ProgramHeaderTableRAM implements ProgramHeaderTable {

  private ELFRam elf;
  
  ProgramHeaderTableRAM ( ELFRam elf ) {
    this.elf = elf;
  }
  
  @Override
  public boolean isDirect () {
    return false;
  }
  
  
  @Override
  public ProgramHeaderRAM get ( int index ) {
    return elf.getSegments ().get ( index ).getHeader ();
  }
  
  @Override
  public int getSize () {
    return elf.getSegments ().size ();
  }
}