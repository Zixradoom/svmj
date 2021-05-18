

package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;

import java.util.List;
import java.util.ArrayList;

public class SymbolTableSectionRAM extends AbstractSectionRAM implements SymbolTableSection {
  
  private int elfClass;
  private List < Symbol > data;
  
  SymbolTableSectionRAM ( ELFRam elf, String name ) {
    super ( elf, name );
    this.elfClass = elf.getClassType ();

    getHeader ().setType ( Section.SHT_SYMTAB );
    getHeader ().setEntrySize ( (int)getEntrySize () );
    
    this.data = new ArrayList <> ();
    data.add ( new Symbol ( this, elfClass, (int)getEntrySize () ) );
  }
  
  @Override
  public Symbol createSymbol ( String name ) {
    return newSymbol ( name );
  }
  
  public Symbol newSymbol ( String name ) {
    Symbol rr = new Symbol ( this, elfClass, (int)getEntrySize (), name );
    data.add ( rr );
    return rr;
  }
  
  @Override
  public long getEntrySize () {
    if ( elfClass == 1 ) {
      return 0x10;
    } else if ( elfClass == 2 ) {
      return 0x18;
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  @Override
  public Symbol getSymbol ( String name ) {
    if ( name == null || name.isBlank () ) {
      return data.get ( 0 );
    }
    for ( Symbol s : data ) {
      if ( s.getName () != null && s.getName ().equals ( name ) ) {
        return s;
      }
    }
    return null;
  }
  
  public int indexOf ( Symbol symbol ) {
    if ( symbol == null ) {
      return 0;
    }
    for ( int index = 0; index < data.size (); index++ ) {
      Symbol temp = data.get ( index );
      if ( temp.getName () != null && temp.getName ().equals ( symbol.getName () ) ) {
        return index;
      }
    }
    return 0;
  }
  
  @Override
  public Symbol getSymbol ( int index ) {
    return data.get ( index );
  }
  
  @Override
  public List < Symbol > getSymbols () {
    return data;
  }
  
  public long getEntryCount () {
    return data.size ();
  }
  
  public int getIndexOfFirstLocal () {
    for ( int index = 0; index < data.size (); index++ ) {
      Symbol symbol = data.get ( index );
      if ( symbol.getBind () == Section.STB_LOCAL ) {
        return index;
      }
    }
    return -1;
  }
  
  public int getIndexOfFirstGlobal () {
    for ( int index = 0; index < data.size (); index++ ) {
      Symbol symbol = data.get ( index );
      if ( symbol.getBind () == Section.STB_GLOBAL ) {
        return index;
      }
    }
    return -1;
  }
  
  public ByteBuffer toBuffer () {
    int size = data.size () * (int)getEntrySize ();
    ByteBuffer bb = ByteBuffer.allocate ( size );
    
    for ( Symbol symbol : data ) {
      bb.put ( symbol.toBuffer () );
    }
    
    return bb.flip ();
  }
  
  @Override
  public ByteBuffer getData () {
    return toBuffer ();
  }
  
  public static final class Symbol implements SymbolTableSection.Symbol {
    private int elfClass;
    private String name;
    private int structureSize;
    private SymbolTableSectionRAM stsr;
    
    private int nameOffset;
    private long value;
    private long size;
    private int info;
    private int other;
    private int sectionHeaderIndex;
    
    Symbol ( SymbolTableSectionRAM stsr, int elfClass, int size ) {
      this ( stsr, elfClass, size, null );
    }
    
    Symbol ( SymbolTableSectionRAM stsr, int elfClass, int size, String name ) {
      this.stsr = stsr;
      this.elfClass = elfClass;
      this.structureSize = size;
      this.name = name;
    }
    
    SymbolTableSectionRAM getSymbolTableSectionRAM () {
      return stsr;
    }
    
    @Override
    public String getName () {
      return name;
    }
    
    public void setNameOffset ( int offset ) {
      nameOffset = offset;
    }
    
    @Override
    public long getNameOffset () {
      return nameOffset;
    }
    
    public void setSectionHeaderIndex ( int value ) {
      sectionHeaderIndex = value & 0xFFFF;
    }
    
    @Override
    public int getSectionHeaderIndex () {
      return sectionHeaderIndex;
    }
    
    @Override
    public int getInfo () {
      return info;
    }
    
    @Override
    public int getOther () {
      return other;
    }
    
    public void setOther ( int other ) {
      this.other = other;
    }
    
    @Override
    public long getSize () {
      return size;
    }
    
    public void setSize ( long size ) {
      this.size = size;
    }
    
    @Override
    public long getValue () {
      return value;
    }
    
    public void setValue ( long value ) {
      this.value = value;
    }
    
    public void setBind ( int bind ) {
      info = info | ( ( bind & 0xf ) << 4 );
    }
    
    public void setType ( int type ) {
      info = info | ( type & 0xf );
    }
    
    public ByteBuffer toBuffer () {
      ByteBuffer bb = ByteBuffer.allocate ( structureSize );
      bb.putInt ( nameOffset );
      if ( elfClass == 1) {
        bb.putInt ( (int)value );
        bb.putInt ( (int)size );
        bb.put ( (byte)info );
        bb.put ( (byte)other );
        bb.putShort ( (short)sectionHeaderIndex );
      } else if ( elfClass == 2 ) {
        bb.put ( (byte)info );
        bb.put ( (byte)other );
        bb.putShort ( (short)sectionHeaderIndex );
        bb.putLong ( value );
        bb.putLong ( size );
      } else {
        throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
      }
      return bb.flip ();
    }
    
    @Override
    public String toString () {
      return "Symbol[value="+getValue ()+", size="+getSize ()+", bind="+getBind ()+", type="+getType ()+", shndx="+getSectionHeaderIndex ()+", name="+getName ()+"]";
    }
  }
}