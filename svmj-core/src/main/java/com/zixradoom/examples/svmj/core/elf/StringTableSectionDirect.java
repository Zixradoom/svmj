
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringTableSectionDirect extends AbstractSectionDirect implements StringTableSection {
  
  private static final Logger LOGGER = LoggerFactory.getLogger ( StringTableSectionDirect.class );
  
  // might be worth creating a cache in the future
  private ByteBuffer stringTable;
  
  StringTableSectionDirect ( SectionHeaderDirect shd ) {
    super ( shd );
    stringTable = getData ();
  }
  
  @Override
  public String getString ( int offset ) {
    //LOGGER.trace ( "get string at offset {} 0x{}", offset, Integer.toHexString ( offset ) );
    if ( offset < 0 ) {
      throw new IllegalArgumentException ( String.format ( "invalid offset '%d'", offset ) );
    }
    if ( offset == 0 ) {
      LOGGER.trace ( "string at offset {} 0x{} '{}'", offset, Integer.toHexString ( offset ), "" );
      return "";
    }
    //LOGGER.trace ( "string table {}", stringTable );
    int start = offset;
    int end = -1;
    
    // this is a bug, with a malformed file it will run forever
    for ( int index = 0; offset + index < stringTable.capacity () ; index++ ) {
      byte b = stringTable.get ( offset + index );
      if ( b == 0 ) {
        end = offset + index;
        break;
      }
    }
    //LOGGER.trace ( "string start {} 0x{} end {} 0x{}", start, Integer.toHexString ( start ), end, Integer.toHexString ( end ) );
    if ( end != -1 ) {
      byte[] str = new byte [ end - start ];
      stringTable.get ( start, str );
      //LOGGER.trace ( "string bytes '{}'", Arrays.toString ( str ) );
      String returnValue = new String ( str, StandardCharsets.UTF_8 );
      //LOGGER.trace ( "string '{}'", returnValue );
      return returnValue;
    } else {
      throw new IllegalArgumentException ( String.format ( "string table offset '%d'", offset ) );
    }
  }
}
