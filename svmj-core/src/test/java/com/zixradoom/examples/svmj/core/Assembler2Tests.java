
package com.zixradoom.examples.svmj.core;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.StringReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.net.URL;

import com.zixradoom.examples.svmj.core.elf.ELF;
import com.zixradoom.examples.svmj.core.elf.ELFRam;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class Assembler2Tests {
  
  @Test
  public void asmTest42Pass () throws IOException, ParseException  {
    Assembler2 a = null;
    URL url = this.getClass ().getResource ( "/asm/test4.asm" );
    try ( InputStream is = this.getClass ().getResourceAsStream ( "/asm/test4.asm" ) ) {
      a = new Assembler2 ( is );
      a.setCreateObject ( true );
      a.setFileName ( url.toString () );
      a.Program ();
    }
    try ( InputStream is = this.getClass ().getResourceAsStream ( "/asm/test4.asm" ) ) {
      a.ReInit ( is );
      a.Program ();
    }
    ELFRam elf = (ELFRam)a.getObject ();
    ByteBuffer bb = elf.toBuffer ();
    byte[] dat = new byte[ bb.position () ];
    bb.duplicate ().flip ().get ( dat );
    
    Path random = Files.createTempFile ( "test4-", ".o" );
    Files.write ( random, dat );
    System.out.println ( random );
  }
}
