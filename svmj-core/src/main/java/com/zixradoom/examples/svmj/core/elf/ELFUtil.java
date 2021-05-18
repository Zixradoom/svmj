
package com.zixradoom.examples.svmj.core.elf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.PrintStream;

public final class ELFUtil {
  
  public static ELFDirect read ( Path file ) throws IOException {
    ByteBuffer bb = ByteBuffer.wrap ( Files.readAllBytes ( file ) );
    return new ELFDirect ( bb );
  }
  
  public static void printSymbols ( PrintStream ps, ELF elf ) {
    SectionHeaderTable sht = elf.getSectionHeaderTable ();
    
    SectionHeader shSymbols = null;
    for ( int index = 0; index < sht.getSize (); index++ ) {
      SectionHeader sh = sht.get ( index );
      if ( sh.getType () == Section.SHT_SYMTAB ) {
        shSymbols = sh;
      }
    }
    
  }
}