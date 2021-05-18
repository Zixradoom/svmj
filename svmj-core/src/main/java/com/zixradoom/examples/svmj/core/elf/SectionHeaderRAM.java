
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public final class SectionHeaderRAM implements SectionHeader {
  
  private int elfClass;
  private int headerSize;
  
  private int nameOffset = 0;
  private String name;
  private int type;
  private long flags;
  private long virtualAddress;
  private long offset;
  private int link;
  private int info;
  private long align;
  private long entrySize;
  
  private final AbstractSectionRAM section;
  
  SectionHeaderRAM ( AbstractSectionRAM section, String name, int elfClass, int headerSize ) {
    this.elfClass = elfClass;
    this.headerSize = headerSize;
    this.name = name;
    this.section = section;
  }
  
  @Override
  public int getNameOffset () {
    return nameOffset;
  }
  
  public void setNameOffset ( int index ) {
    nameOffset = index;
  }
  
  @Override
  public String getName () {
    return name;
  }
  
  @Override
  public int getType () {
    return type;
  }
  
  public void setType ( int type ) {
    this.type = type;
  }
  
  @Override
  public long getFlags () {
    return flags;
  }
  
  public void setFlags ( int flags ) {
    this.flags = flags;
  }
  
  @Override
  public long getVirtualAddress () {
    return virtualAddress;
  }
  
  public void setVritualAddress ( long addr ) {
    virtualAddress = addr;
  }
  
  @Override
  public long getOffset () {
    return offset;
  }
  
  public void setOffset ( long offset ) {
    this.offset = offset;
  }
  
  @Override
  public long getSize () {
    ByteBuffer bb = section.getData ();
    return bb == null ? 0 : bb.capacity ();
  }
  
  @Override
  public int getLink () {
    return link;
  }
  
  public void setLink ( int link ) {
    this.link = link;
  }
  
  @Override
  public int getInfo () {
    return info;
  }
  
  public void setInfo ( int info ) {
    this.info = info;
  }
  
  @Override
  public long getAddressAlign () {
    return align;
  }
  
  public void setAddressAlign ( long align ) {
    this.align = align;
  }
  
  @Override
  public long getEntrySize () {
    return entrySize;
  }
  
  public void setEntrySize ( long size ) {
    this.entrySize = size;
  }
  
  @Override
  public AbstractSectionRAM getSection () {
    return section;
  }
  
  @Override
  public < T extends Section > T getSection ( Class < T > clazz ) {
    return clazz.cast ( getSection () );
  }
  
  ByteBuffer toBuffer () {
    ByteBuffer bb = ByteBuffer.allocate ( headerSize );
    bb.putInt ( nameOffset );
    bb.putInt ( type );
    if ( elfClass == ELF.ELFClass.CLASS_32.getValue () ) {
      bb.putInt ( (int)flags );
      bb.putInt ( (int)virtualAddress );
      bb.putInt ( (int)offset );
      bb.putInt ( (int)getSize () );
      bb.putInt ( link );
      bb.putInt ( info );
      bb.putInt ( (int)align );
      bb.putInt ( (int)entrySize );
    } else if ( elfClass == ELF.ELFClass.CLASS_64.getValue ()  ) {
      bb.putLong ( flags );
      bb.putLong ( virtualAddress );
      bb.putLong ( offset );
      bb.putLong ( getSize () );
      bb.putInt ( link );
      bb.putInt ( info );
      bb.putLong ( align );
      bb.putLong ( entrySize );
    } else {
      throw new IllegalArgumentException ( String.format ( "elf class %d", elfClass ) );
    }
    return bb;
  }
}