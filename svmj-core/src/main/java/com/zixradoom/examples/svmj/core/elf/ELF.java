
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface ELF {
  
  public static final int MAGIC = 0x7F454C46;
  public static final int CLASS_32 = 1;
  public static final int CLASS_64 = 2;
  public static final int DATA_LITTLE = 1;
  public static final int DATA_BIG = 2;
  public static final int VERSION_1 = 1;
  
  public static final int HEADER_SIZE_32 = 0x34;
  public static final int HEADER_SIZE_64 = 0x40;
  
  public static final int OBJECT_NONE = 0;
  public static final int OBJECT_REL  = 1;
  public static final int OBJECT_EXEC = 2;
  public static final int OBJECT_DYN  = 3;
  public static final int OBJECT_CORE = 4;
  
  int getClassType ();
  int getDataType ();
  int getVersion ();
  int getOSABI ();
  int getABIVersion ();
  int getObjectType ();
  int getMachine ();
  int getObjectVersion ();
  long getEntryPointAddress ();
  long getProgramHeaderTableOffset ();
  long getSectionHeaderTableOffset ();
  int getFlags ();
  int getHeaderSize ();
  int getProgramHeaderEntrySize ();
  int getProgramHeaderEntryCount ();
  int getSectionHeaderEntrySize ();
  int getSectionHeaderEntryCount ();
  int getHeaderNameIndex ();
  
  ProgramHeaderTable getProgramHeaderTable ();
  SectionHeaderTable getSectionHeaderTable ();
  
  public enum ELFClass {
    CLASS_NONE ( 0, "ELFCLASSNONE" ),
    CLASS_32  ( 1, "ELFCLASS32" ),
    CLASS_64  ( 2, "ELFCLASS64" );
    
    private final int value;
    private final String symbol;
    
    private ELFClass ( int value, String symbol ) {
      this.value = value;
      this.symbol = symbol;
    }
    
    public int getValue () {
      return value;
    }
    
    public String getSymbol () {
      return symbol;
    }
    
    public static ELFClass parse ( int value ) {
      if ( value == CLASS_32.value ) {
        return ELFClass.CLASS_32;
      }
      if ( value == CLASS_64.value ) {
        return ELFClass.CLASS_64;
      }
      return ELFClass.CLASS_NONE;
    }
  }
  
  public enum ELFData {
    DATA_NONE ( 0, "ELFDATANONE" ),
    DATA_LSB  ( 1, "ELFDATA2LSB" ),
    DATA_MSB  ( 2, "ELFDATA2MSB" );
    
    private final int value;
    private final String symbol;
    
    private ELFData ( int value, String symbol ) {
      this.value = value;
      this.symbol = symbol;
    }
    
    public int getValue () {
      return value;
    }
    
    public String getSymbol () {
      return symbol;
    }
    
    public static ELFData parse ( int value ) {
      if ( value == DATA_LSB.value ) {
        return ELFData.DATA_LSB;
      }
      if ( value == DATA_MSB.value ) {
        return ELFData.DATA_MSB;
      }
      return ELFData.DATA_NONE;
    }
  }
}