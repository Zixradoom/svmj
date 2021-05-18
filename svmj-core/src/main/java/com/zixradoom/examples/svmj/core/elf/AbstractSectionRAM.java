
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSectionRAM implements Section {
  
  private static final Logger LOGGER = LoggerFactory.getLogger ( AbstractSectionRAM.class );
  
  protected final ELFRam elf;
  protected final SectionHeaderRAM shr;
  
  protected AbstractSectionRAM ( ELFRam elf, String name ) {
    this.elf = Objects.requireNonNull ( elf, "ELFRam is null" );
    this.shr = new SectionHeaderRAM ( this, name, elf.getClassType (), elf.getSectionHeaderEntrySize () );
  }
  
  @Override
  public SectionHeaderRAM getHeader () {
    return shr;
  }
  
  public abstract ByteBuffer getData ();
}