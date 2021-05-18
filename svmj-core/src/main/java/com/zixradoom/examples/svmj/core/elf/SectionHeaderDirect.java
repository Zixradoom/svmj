
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class SectionHeaderDirect implements SectionHeader {
  
  private SectionHeaderTableDirect shtd;
  private int index;
  private ByteBuffer data;
  private int elfClass;
  
  SectionHeaderDirect ( SectionHeaderTableDirect shtd, int index ) {
    this.shtd = shtd;
    this.index = index;
    int base = ( (int) shtd.getELF ().getSectionHeaderTableOffset () ) + ( shtd.getELF ().getSectionHeaderEntrySize () * index );
    this.data = shtd.getELF ().getELFData ().slice ( base, shtd.getELF ().getSectionHeaderEntrySize () );
    this.elfClass = shtd.getELF ().getClassType ();
  }
  
  @Override
  public int getNameOffset () {
    return data.getInt ( 0x0 );
  }
  
  @Override
  public String getName () {
    return shtd.get ( shtd.getELF ().getHeaderNameIndex () ).getSection ( StringTableSection.class ).getString ( getNameOffset () );
  }
  
  @Override
  public int getType () {
    return data.getInt ( 0x04 );
  }
  
  @Override
  public long getFlags () {
    if ( elfClass == 1 ) {
      return data.getInt ( 0x08 );
    } else if ( elfClass == 2 ) {
      return data.getLong ( 0x08 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  @Override
  public long getVirtualAddress () {
    if ( elfClass == 1 ) {
      return data.getInt ( 0x0C );
    } else if ( elfClass == 2 ) {
      return data.getLong ( 0x010 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  @Override
  public long getOffset () {
    if ( elfClass == 1 ) {
      return data.getInt ( 0x10 );
    } else if ( elfClass == 2 ) {
      return data.getLong ( 0x18 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  @Override
  public long getSize () {
    if ( elfClass == 1 ) {
      return data.getInt ( 0x14 );
    } else if ( elfClass == 2 ) {
      return data.getLong ( 0x20 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  @Override
  public int getLink () {
    if ( elfClass == 1 ) {
      return data.getInt ( 0x18 );
    } else if ( elfClass == 2 ) {
      return data.getInt ( 0x28 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  @Override
  public int getInfo () {
    if ( elfClass == 1 ) {
      return data.getInt ( 0x1C );
    } else if ( elfClass == 2 ) {
      return data.getInt ( 0x2C );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  @Override
  public long getAddressAlign () {
    if ( elfClass == 1 ) {
      return data.getInt ( 0x20 );
    } else if ( elfClass == 2 ) {
      return data.getLong ( 0x30 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  @Override
  public long getEntrySize () {
    if ( elfClass == 1 ) {
      return data.getInt ( 0x24 );
    } else if ( elfClass == 2 ) {
      return data.getLong ( 0x38 );
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  @Override
  public Section getSection () {
    return new SectionDirect ( this );
  }

  @Override
  public < T extends Section > T getSection ( Class < T > clazz ) {
    if ( clazz.isAssignableFrom ( StringTableSectionDirect.class ) || clazz.isAssignableFrom ( StringTableSection.class ) ) {
      return clazz.cast ( new StringTableSectionDirect ( this ) );
    } else if ( clazz.isAssignableFrom ( SymbolTableSectionDirect.class ) || clazz.isAssignableFrom ( SymbolTableSection.class ) ) {
      return clazz.cast ( new SymbolTableSectionDirect ( this ) );
    } else if ( clazz.isAssignableFrom ( RelocationSectionDirect.class ) || clazz.isAssignableFrom ( RelocationSection.class ) ) {
      return clazz.cast ( new RelocationSectionDirect ( this ) );
    } else {
      throw new UnsupportedOperationException ( String.format ( "class '%s' not supported", clazz ) );
    }
  }
  
  SectionHeaderTableDirect getSectionHeaderTableDirect () {
    return shtd;
  }
}