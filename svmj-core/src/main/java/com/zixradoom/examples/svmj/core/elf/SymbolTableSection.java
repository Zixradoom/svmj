
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

public interface SymbolTableSection extends Section {
  Symbol createSymbol ( String name );
  Symbol getSymbol ( String name );
  Symbol getSymbol ( int index );
  List < ? extends Symbol > getSymbols ();
  
  public static interface Symbol {
    
    public static final int STB_LOCAL = 0x0;
    public static final int STB_GLOBAL = 0x1;
    public static final int STB_WEAK = 0x2;
    
    public static final int STT_NOTYPE = 0x0;
    public static final int STT_OBJECT = 0x1;
    public static final int STT_FUNC = 0x2;
    public static final int STT_SECTION = 0x3;
    public static final int STT_FILE = 0x4;
    public static final int STT_COMMON = 0x5;
    public static final int STT_TLS = 0x6;
    
    public static final int STV_DEFAULT = 0x0;
    public static final int STV_INTERNAL = 0x1;
    public static final int STV_HIDDEN = 0x2;
    public static final int STV_PROTECTED = 0x3;
    
    String getName ();
    long getNameOffset ();
    int getSectionHeaderIndex ();
    int getInfo ();
    int getOther ();
    long getSize ();
    long getValue ();
    
    default int getBind () {
      return ( getInfo () >>> 4 ) & 0xf;
    }
    
    default int getType () {
      return ( getInfo () & 0xf );
    }
    
  }
}