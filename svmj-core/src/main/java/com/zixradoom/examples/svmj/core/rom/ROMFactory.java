

package com.zixradoom.examples.svmj.core.rom;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import java.util.Objects;
import java.util.List;

import com.zixradoom.examples.svmj.core.SimpleROM;
import com.zixradoom.examples.svmj.core.OperationCode;
import com.zixradoom.examples.svmj.core.elf.*;
import com.zixradoom.examples.svmj.core.linker.Linker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ROMFactory {
  
  private static final Logger LOGGER = LoggerFactory.getLogger ( ROMFactory.class );
  
  public static SimpleROM createBIOSRom ( String name, int baseAddress, ELF elf ) {
    
    Linker linker = new Linker ( elf );
    linker.setPhysicalBaseAddress ( baseAddress + 4 );
    ELF romImage = linker.linkV2 ();
    
    Section section = romImage.getSectionHeaderTable ().get ( 1 ).getSection ();
    ByteBuffer dat = ((SectionRAM)section).getData ();
    dat.position ( 0 );
    dat.limit ( dat.capacity () );
    
    ByteBuffer image = ByteBuffer.allocate ( dat.capacity () + 4 );
    
    int instruction = OperationCode.JUMP_I.getCode () << 24;
    instruction = instruction | ( 0xFFFFFF & (int)romImage.getEntryPointAddress () );
    image.putInt ( instruction );
    
    image.put (dat);
    
    return new SimpleROM ( name, baseAddress, image );
  }
  
  public static SimpleROM createRom ( String name, int baseAddress, ELF elf ) {
    
    Section section = elf.getSectionHeaderTable ().get ( 1 ).getSection ();
    ByteBuffer dat = ((SectionDirect)section).getData ();
    dat.position ( 0 );
    dat.limit ( dat.capacity () );
    
    return new SimpleROM ( name, baseAddress, dat );
  }
}