

package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public final class RelocationSectionDirect extends AbstractSectionDirect implements RelocationSection {
  
  private ByteBuffer data;
  private List < RelocationRecordDirect > records;
  
  RelocationSectionDirect ( SectionHeaderDirect shd ) {
    super ( shd );
    data = getData ();
  }
  
  @Override
  public Record newRecord () {
    throw new UnsupportedOperationException ( "not implemented" );
  }
  
  @Override
  public Record getRecord ( int index ) {
    return getRecords ().get ( index );
  }
  
  @Override
  public int getEntryCount () {
    return (int)(data.capacity () / getEntrySize ());
  }
  
  @Override
  public List < RelocationRecordDirect > getRecords () {
    if ( records == null ) {
      synchronized ( this ) {
        if ( records == null ) {
          List < RelocationRecordDirect > temp = new ArrayList < RelocationRecordDirect > ();
          for ( int index = 0; index < getEntryCount (); index++ ) {
            temp.add ( new RelocationRecordDirect ( this, data.slice ( (int)(index * getEntrySize ()), (int)getEntrySize () ) ) );
          }
          records = Collections.unmodifiableList ( temp );
        }
      }
    }
    return records;
  }
  
  private ByteBuffer getData0 () {
    return data;
  }
  
  public static final class RelocationRecordDirect implements Record {

    private RelocationSectionDirect rsd;
    private ByteBuffer record;
    private int elfClass;
    
    RelocationRecordDirect ( RelocationSectionDirect rsd, ByteBuffer record ) {
      this.rsd = rsd;
      this.record = record;
      this.elfClass = rsd.getHeader ().getSectionHeaderTableDirect ().getELF ().getClassType ();
    }

    @Override
    public long getOffset () {
      if ( elfClass == 1 ) {
        return record.getInt ( 0 );
      } else if ( elfClass == 2) {
        return record.getLong ( 0 );
      } else {
        throw new RuntimeException ();
      }
    }
    
    @Override
    public String getSymbolName () {
      int symtabIndex = rsd.getHeader ().getLink ();
      SymbolTableSection symbolTableSection = rsd.getHeader ().getSectionHeaderTableDirect ().get ( symtabIndex ).getSection ( SymbolTableSection.class );
      return symbolTableSection.getSymbol ( getSymbolIndex () ).getName ();
    }
    
    public SymbolTableSection.Symbol getSymbol () {
      int symtabIndex = rsd.getHeader ().getLink ();
      SymbolTableSection symbolTableSection = rsd.getHeader ().getSectionHeaderTableDirect ().get ( symtabIndex ).getSection ( SymbolTableSection.class );
      return symbolTableSection.getSymbol ( getSymbolIndex () );
    }
    
    @Override
    public int getSymbolIndex () {
      if ( elfClass == 1 ) {
        return (int)(getInfo () >>> 8);
      } else if ( elfClass == 2) {
        return (int)(getInfo () >>> 32);
      } else {
        throw new RuntimeException ();
      }
    }
    
    @Override
    public int getType () {
      if ( elfClass == 1 ) {
        return (int)(getInfo () & 0xFF );
      } else if ( elfClass == 2) {
        return (int)getInfo ();
      } else {
        throw new RuntimeException ();
      }
    }
    
    @Override
    public long getInfo () {
      if ( elfClass == 1 ) {
        return record.getInt ( 4 );
      } else if ( elfClass == 2) {
        return record.getLong ( 8 );
      } else {
        throw new RuntimeException ();
      }
    }
    
    @Override
    public String toString () {
      return "RelocationRecordDirect[offset="+getOffset ()+", symbolIndex="+getSymbolIndex ()+", relocationType="+getType ()+", symbolName="+getSymbolName ()+"]";
    }
  }
}
