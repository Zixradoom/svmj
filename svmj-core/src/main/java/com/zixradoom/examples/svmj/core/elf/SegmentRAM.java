
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class SegmentRAM implements Segment {
  
  private ELFRam elf;
  private ProgramHeaderRAM header;
  
  SegmentRAM ( ELFRam elf ) {
    this.elf = elf;
    this.header = new ProgramHeaderRAM ( elf.getClassType (), elf.getProgramHeaderEntrySize () );
  }
  
  @Override
  public ProgramHeaderRAM getHeader () {
    return header;
  }
}