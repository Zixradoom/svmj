
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public final class SymbolTableSectionDirect extends AbstractSectionDirect implements SymbolTableSection {
  
  private List < SymbolDirect > symbols;
  
  SymbolTableSectionDirect ( SectionHeaderDirect shd ) {
    super ( shd );
  }
  
  @Override
  public Symbol createSymbol ( String name ) {
    throw new UnsupportedOperationException ( "not implemented" );
  }
  
  @Override
  public Symbol getSymbol ( String name ) {
    if ( name == null )
      return null;
    
    for ( Symbol s : getSymbols () ) {
      if ( name.equals ( s.getName () ) )
        return s;
    }
    return null;
  }
  
  @Override
  public Symbol getSymbol ( int index ) {
    return getSymbols ().get ( index );
  }
  
  @Override
  public List < SymbolDirect > getSymbols () {
    if ( symbols == null ) {
      synchronized ( this ) {
        if ( symbols == null ) {
          List < SymbolDirect > temp = new ArrayList <> ();
          for ( int index = 0; index < getEntryCount (); index++ ) {
            temp.add ( new SymbolDirect ( this, index ) );
          }
          symbols = Collections.unmodifiableList ( temp );
        }
      }
    }
    return symbols;
  }
  
  private int getEntryCount () {
    return (int)(getData ().capacity () / getEntrySize ());
  }
  
  public static final class SymbolDirect implements Symbol {
    
    private SymbolTableSectionDirect stsd;
    private int index;
    private ByteBuffer data;
    private int elfClass;
    
    SymbolDirect ( SymbolTableSectionDirect stsd, int index ) {
      this.stsd = stsd;
      this.index = index;
      this.data = stsd.getData ().slice ( (int)( index * stsd.getEntrySize () ), (int)stsd.getEntrySize () );
      this.elfClass = stsd.getHeader ().getSectionHeaderTableDirect ().getELF ().getClassType ();
    }
    
    @Override
    public String getName () {
      if ( getNameOffset () == 0 ) {
        return "";
      }
      return stsd.getHeader ().getSectionHeaderTableDirect ().get ( stsd.getHeader ().getLink () ).getSection ( StringTableSection.class ).getString ( (int)getNameOffset () );
    }
    
    @Override
    public long getNameOffset () {
      return data.getInt ( 0 );
    }
    
    @Override
    public int getSectionHeaderIndex () {
      if ( elfClass == ELF.ELFClass.CLASS_32.getValue () ) {
        return data.getShort ( 14 ) & 0xFFFF;
      } else if ( elfClass == ELF.ELFClass.CLASS_64.getValue () ) {
        return data.getShort ( 6 ) & 0xFFFF;
      } else {
        throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
      }
    }
    
    @Override
    public int getOther () {
      if ( elfClass == ELF.ELFClass.CLASS_32.getValue () ) {
        return data.get ( 13 );
      } else if ( elfClass == ELF.ELFClass.CLASS_64.getValue () ) {
        return data.get ( 5 );
      } else {
        throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
      }
    }
    
    @Override
    public int getInfo () {
      if ( elfClass == ELF.ELFClass.CLASS_32.getValue () ) {
        return data.get ( 12 );
      } else if ( elfClass == ELF.ELFClass.CLASS_64.getValue () ) {
        return data.get ( 4 );
      } else {
        throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
      }
    }
    
    @Override
    public long getSize () {
      if ( elfClass == ELF.ELFClass.CLASS_32.getValue () ) {
        return data.getInt ( 8 );
      } else if ( elfClass == ELF.ELFClass.CLASS_64.getValue () ) {
        return data.getLong ( 16 );
      } else {
        throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
      }
    }
    
    @Override
    public long getValue () {
      if ( elfClass == ELF.ELFClass.CLASS_32.getValue () ) {
        return data.getInt ( 4 );
      } else if ( elfClass == ELF.ELFClass.CLASS_64.getValue () ) {
        return data.getLong ( 8 );
      } else {
        throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
      }
    }
    
    @Override
    public String toString () {
      return "SymbolDirect[value="+getValue ()+", size="+getSize ()+", bind="+getBind ()+", type="+getType ()+", shndx="+getSectionHeaderIndex ()+", name="+getName ()+"]";
    }
  }
}
