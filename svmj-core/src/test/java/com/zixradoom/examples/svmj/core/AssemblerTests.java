
package com.zixradoom.examples.svmj.core;

import java.nio.ByteBuffer;
import java.io.StringReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import com.zixradoom.examples.svmj.core.elf.ELF;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class AssemblerTests {
  
  @Test
  public void emptyStringTest () {
    StringReader sr = new StringReader ( "" );
    Assembler a = new Assembler ( sr );
    a.process ();
  }
  
  @Test
  public void nullStreamTest () {
    Assembler a = new Assembler ( InputStream.nullInputStream () );
    a.process ();
  }
  
  @Disabled
  @Test
  public void asmTest1 () throws IOException {
    try ( InputStream is = this.getClass ().getResourceAsStream ( "/asm/test1.asm" ) ) {
      Assembler a = new Assembler ( is );
      a.process ();
      Map < String, Integer > symbols = a.getSymbolTable ();
      Map < String, Integer > expected = Map.of (
        "_start", 0x0,
        "stuff", 0x18,
        "moreStuff", 0x18
      );
      assertEquals ( expected, symbols, "symbol table does not match expected" );
    }
  }
  
  @Test
  public void asmTest2 () throws IOException  {
    try ( InputStream is = this.getClass ().getResourceAsStream ( "/asm/test2.asm" ) ) {
      Assembler a = new Assembler ( is );
      assertThrows ( IllegalStateException.class, () -> {
        a.process ();
      } );
    }
  }
  
  @Test
  public void asmTest3 () throws IOException  {
    try ( InputStream is = this.getClass ().getResourceAsStream ( "/asm/test3.asm" ) ) {
      Assembler a = new Assembler ( is );
      a.process ();
    }
  }
}