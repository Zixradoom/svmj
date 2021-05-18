
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public final class SectionRAM extends AbstractSectionRAM {
  
  private ByteBuffer data;
  
  SectionRAM ( ELFRam elf, String name ) {
    super ( elf, name );
  }
  
  @Override
  public ByteBuffer getData () {
    return data;
  }
  
  public void setData ( ByteBuffer data ) {
    this.data = data;
  }
}