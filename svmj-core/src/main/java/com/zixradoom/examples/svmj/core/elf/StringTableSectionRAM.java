

package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.util.HashMap;

public class StringTableSectionRAM extends AbstractSectionRAM {
  
  private Map < String, Integer > names;
  private ByteBuffer data;
  
  StringTableSectionRAM ( ELFRam elf, String name ) {
    super ( elf, name );
    getHeader ().setType ( Section.SHT_STRTAB );
    getHeader ().setEntrySize ( 1 );
    this.data = ByteBuffer.allocate ( 0x1000 );
    data.put ( (byte)0 );
    names = new HashMap <> ();
  }
  
  public int interString ( String value ) {
    int offset = 0;
    
    if ( value == null || value.isEmpty () ) {
      return offset;
    }
    
    if ( names.containsKey ( value ) ) {
      offset = names.get ( value );
    } else {
      offset = data.position ();
      byte[] str = value.getBytes ( StandardCharsets.UTF_8 );
      data.put ( str );
      data.put ( (byte)0 );
      names.put ( value, offset );
    }
    
    return offset;
  }
  
  @Override
  public ByteBuffer getData () {
    return toBuffer ();
  }
  
  public ByteBuffer toBuffer () {
    ByteBuffer returnValue =  data.slice ( 0, data.position () );
    returnValue.position ( returnValue.capacity () );
    return returnValue.asReadOnlyBuffer ().flip ();
  }
}