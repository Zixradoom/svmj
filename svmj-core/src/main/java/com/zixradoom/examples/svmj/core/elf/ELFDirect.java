
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ELFDirect implements ELF {
  
  private ByteBuffer elfData;
  private ProgramHeaderTable pht;
  private SectionHeaderTable sht;
  
  public ELFDirect ( ByteBuffer bb ) {
    this.elfData = bb;
    this.pht = new ProgramHeaderTableDirect ( this );
    this.sht = new SectionHeaderTableDirect ( this );
  }
  
  @Override
  public int getClassType () {
    return elfData.get ( 0x4 );
  }
  
  @Override
  public int getDataType () {
    return elfData.get ( 0x5 );
  }
  
  @Override
  public int getVersion () {
    return elfData.get ( 0x6 );
  }
  
  @Override
  public int getOSABI () {
    return elfData.get ( 0x7 );
  }
  
  @Override
  public int getABIVersion () {
    return elfData.get ( 0x8 );
  }
  
  @Override
  public int getObjectType () {
    return elfData.getShort ( 0x10 );
  }
  
  @Override
  public int getMachine () {
    return elfData.getShort ( 0x12 );
  }
  
  @Override
  public int getObjectVersion () {
    return elfData.getInt ( 0x14 );
  }
  
  @Override
  public long getEntryPointAddress () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getInt ( 0x18 );
    } else if ( clazz == 2 ) {
      return elfData.getLong ( 0x18 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public long getProgramHeaderTableOffset () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getInt ( 0x1C );
    } else if ( clazz == 2 ) {
      return elfData.getLong ( 0x20 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public long getSectionHeaderTableOffset () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getInt ( 0x20 );
    } else if ( clazz == 2 ) {
      return elfData.getLong ( 0x28 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public int getFlags () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getInt ( 0x24 );
    } else if ( clazz == 2 ) {
      return elfData.getInt ( 0x30 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public int getHeaderSize () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getShort ( 0x28 );
    } else if ( clazz == 2 ) {
      return elfData.getShort ( 0x34 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public int getProgramHeaderEntrySize () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getShort ( 0x2A );
    } else if ( clazz == 2 ) {
      return elfData.getShort ( 0x36 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public int getProgramHeaderEntryCount () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getShort ( 0x2C );
    } else if ( clazz == 2 ) {
      return elfData.getShort ( 0x38 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public int getSectionHeaderEntrySize () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getShort ( 0x2E );
    } else if ( clazz == 2 ) {
      return elfData.getShort ( 0x3A );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public int getSectionHeaderEntryCount () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getShort ( 0x30 );
    } else if ( clazz == 2 ) {
      return elfData.getShort ( 0x3C );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public int getHeaderNameIndex () {
    int clazz = getClassType ();
    if ( clazz == 1 ) {
      return elfData.getShort ( 0x32 );
    } else if ( clazz == 2 ) {
      return elfData.getShort ( 0x3E );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public ProgramHeaderTable getProgramHeaderTable () {
    return pht;
  }
  
  @Override
  public SectionHeaderTable getSectionHeaderTable () {
    return sht;
  }
  
  public ByteBuffer getELFData () {
    return elfData.asReadOnlyBuffer ();
  }
}