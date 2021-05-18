
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ProgramHeaderDirect implements ProgramHeader {
  
  private ProgramHeaderTableDirect phtd;
  private int index;
  private int base;
  
  ProgramHeaderDirect ( ProgramHeaderTableDirect phtd, int index ) {
    this.phtd = phtd;
    this.index = index;
    this.base = ( (int) phtd.getELF ().getProgramHeaderTableOffset () ) + ( phtd.getELF ().getProgramHeaderEntrySize () * index );
  }
  
  @Override
  public int getType () {
    return phtd.getELF ().getELFData ().getInt ( base );
  }
  
  @Override
  public int getFlags () {
    int clazz = phtd.getELF ().getClassType ();
    if ( clazz == 1 ) {
      return phtd.getELF ().getELFData ().getInt ( base + 0x18 );
    } else if ( clazz == 2 ) {
      return phtd.getELF ().getELFData ().getInt ( base + 0x04 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public long getOffset () {
    int clazz = phtd.getELF ().getClassType ();
    if ( clazz == 1 ) {
      return phtd.getELF ().getELFData ().getInt ( base + 0x04 );
    } else if ( clazz == 2 ) {
      return phtd.getELF ().getELFData ().getLong ( base + 0x08 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public long getVirtualAddress () {
    int clazz = phtd.getELF ().getClassType ();
    if ( clazz == 1 ) {
      return phtd.getELF ().getELFData ().getInt ( base + 0x08 );
    } else if ( clazz == 2 ) {
      return phtd.getELF ().getELFData ().getLong ( base + 0x10 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public long getPhysicalAddress () {
    int clazz = phtd.getELF ().getClassType ();
    if ( clazz == 1 ) {
      return phtd.getELF ().getELFData ().getInt ( base + 0x0C );
    } else if ( clazz == 2 ) {
      return phtd.getELF ().getELFData ().getLong ( base + 0x18 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public long getFileSize () {
    int clazz = phtd.getELF ().getClassType ();
    if ( clazz == 1 ) {
      return phtd.getELF ().getELFData ().getInt ( base + 0x10 );
    } else if ( clazz == 2 ) {
      return phtd.getELF ().getELFData ().getLong ( base + 0x20 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public long getMemorySize () {
    int clazz = phtd.getELF ().getClassType ();
    if ( clazz == 1 ) {
      return phtd.getELF ().getELFData ().getInt ( base + 0x14 );
    } else if ( clazz == 2 ) {
      return phtd.getELF ().getELFData ().getLong ( base + 0x28 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
  
  @Override
  public long getAlign () {
    int clazz = phtd.getELF ().getClassType ();
    if ( clazz == 1 ) {
      return phtd.getELF ().getELFData ().getInt ( base + 0x1C );
    } else if ( clazz == 2 ) {
      return phtd.getELF ().getELFData ().getLong ( base + 0x30 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", clazz ) );
    }
  }
}