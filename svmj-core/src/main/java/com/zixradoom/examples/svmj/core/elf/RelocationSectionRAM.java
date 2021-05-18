

package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelocationSectionRAM extends AbstractSectionRAM {
  
  private static final Logger LOGGER = LoggerFactory.getLogger ( RelocationSectionRAM.class );
  
  private int elfClass;
  private List < RelocationRecord > data;
  
  RelocationSectionRAM ( ELFRam elf, String name ) {
    super ( elf, name );
    this.elfClass = elf.getClassType ();
    
    getHeader ().setType ( Section.SHT_REL );
    getHeader ().setEntrySize ( getEntrySize () );
    
    this.data = new ArrayList <> ();
  }
  
  public RelocationRecord newRecord (  ) {
    RelocationRecord rr = new RelocationRecord ( this, elfClass );
    data.add ( rr );
    return rr;
  }
  
  public long getEntrySize () {
    if ( elfClass == 1 ) {
      return 0x8;
    } else if ( elfClass == 2 ) {
      return 0x10;
    } else {
      throw new RuntimeException ( String.format ( "elf class type is illegal '%d'", elfClass ) );
    }
  }
  
  public long getEntryCount () {
    return data.size ();
  }
  
  public List < RelocationRecord > getRecords () {
    return data;
  }
  
  public ByteBuffer toBuffer () {
    int size = (int)(getEntryCount () * getEntrySize ());
    ByteBuffer bb = ByteBuffer.allocate ( size );
    
    for ( RelocationRecord rr : data ) {
      LOGGER.trace ( "{}", rr );
      bb.put ( rr.toBuffer () );
    }
    
    return bb.flip ();
  }
  
  @Override
  public ByteBuffer getData () {
    return toBuffer ();
  }
  
  public static final class RelocationRecord {
    
    private SymbolTableSectionRAM.Symbol symbol;
    private RelocationSectionRAM rsr;
    private int elfClass;
    
    private long offset;
    private int type;
    
    RelocationRecord ( RelocationSectionRAM rsr, int elfClass ) {
      this.rsr = rsr;
      this.elfClass = elfClass;
    }
    
    public long getOffset () {
      return offset;
    }
    
    public void setOffset ( long offset ) {
      this.offset = offset;
    }
    
    public SymbolTableSectionRAM.Symbol getSymbol () {
      return symbol;
    }
    
    public void setSymbol ( SymbolTableSectionRAM.Symbol symbol ) {
      this.symbol = symbol;
    }
    
    public int getSymbolIndex () {
      return symbol == null ? 0 : symbol.getSymbolTableSectionRAM ().indexOf ( symbol );
    }
    
    public String getSymbolName () {
      return symbol == null ? null : symbol.getName ();
    }
    
    public int getType () {
      return type;
    }
    
    public void setType ( int type ) {
      this.type = type;
    }
    
    public long getInfo () {
      // need to calculate this differently for
      // 32 and 64 bit
      
      return ( getSymbolIndex () << 8 ) | ( getType () & 0xFF );
    }
    
    ByteBuffer toBuffer () {
      ByteBuffer bb = ByteBuffer.allocate ( (int)rsr.getEntrySize () );
      
      if ( elfClass == 1 ) {
        bb.putInt ( (int)offset );
        bb.putInt ( (int)getInfo () );
      } else if ( elfClass == 2 ) {
        bb.putLong ( offset );
        bb.putLong ( getInfo () );
      } else {
        throw new RuntimeException ( "illegal elf class" );
      }
      
      return bb.flip ();
    }
    
    @Override
    public String toString () {
      return "RelocationRecord[offset="+getOffset ()+", symbolIndex="+getSymbolIndex ()+", relocationType="+getType ()+", symbolName="+getSymbolName ()+"]";
    }
  }
}
