
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSectionDirect implements Section {
  
  private static final Logger LOGGER = LoggerFactory.getLogger ( AbstractSectionDirect.class );
  
  protected SectionHeaderDirect shd;
  
  protected AbstractSectionDirect ( SectionHeaderDirect shd ) {
    this.shd = Objects.requireNonNull ( shd, "SectionHeaderDirect is null" );
  }
  
  @Override
  public SectionHeaderDirect getHeader () {
    return shd;
  }
  
  public ByteBuffer getData () {
    ByteBuffer data = shd.getSectionHeaderTableDirect ().getELF ().getELFData ();
    //LOGGER.trace ( "elf data {}", data );
    return data.slice ( (int)getOffset (), (int)getSize () );
  }
}