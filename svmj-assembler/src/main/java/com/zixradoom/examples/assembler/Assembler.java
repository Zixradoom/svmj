
package com.zixradoom.examples.svmj.assembler;

import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import java.util.Objects;

import com.zixradoom.examples.svmj.core.elf.*;
import com.zixradoom.examples.svmj.core.ParseException;
import com.zixradoom.examples.svmj.core.Assembler2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Assembler {
  
  private static final Logger LOGGER = LoggerFactory.getLogger ( Assembler.class );
  
  private Path sourceFile;
  private Path sourceBin;
  private int baseAddress;

  public Assembler ( Path sourceFile, Path sourceBin ) {
    this.sourceFile = Objects.requireNonNull ( sourceFile, "sourceFile is null" );
    if ( sourceBin == null ) {
      sourceBin = sourceFile.getParent ().resolve ( sourceFile.getFileName ().toString ().replace ( ".asm" , ".o" ) );
    }
    this.sourceBin = sourceBin;
  }
  
  public void assemble () throws IOException, ParseException {
    if ( Files.exists ( sourceFile ) && Files.exists ( sourceBin ) ) {
      FileTime sourceMTime = (FileTime)Files.getAttribute ( sourceFile, "lastModifiedTime" );
      FileTime objectMTime = (FileTime)Files.getAttribute ( sourceBin, "lastModifiedTime" );
      if ( sourceMTime.compareTo ( objectMTime ) < 0 ) {
        return;
      }
      LOGGER.info ( "source file is newer '{}'", sourceFile );
    }
    
    LOGGER.info ( "processing '{}' -> '{}'", sourceFile, sourceBin );
    Assembler2 a = null;
    try ( InputStream is = Files.newInputStream ( sourceFile ) ) {
      a = new Assembler2 ( is );
      a.setCreateObject ( true );
      a.setFileName ( sourceFile.toString () );
      a.Program ();
    }
    try ( InputStream is = Files.newInputStream ( sourceFile ) ) {
      a.ReInit ( is );
      a.Program ();
    }
    ELFRam elf = (ELFRam)a.getObject ();
    ByteBuffer bb = elf.toBuffer ();
    byte[] dat = new byte[ bb.position () ];
    bb.flip ().get ( dat );
    
    Files.write ( sourceBin, dat );
  }
  
  public int getBaseAddress () {
    return baseAddress;
  }
  
  public void setBaseAddress ( int address ) {
    this.baseAddress = address;
  }
}