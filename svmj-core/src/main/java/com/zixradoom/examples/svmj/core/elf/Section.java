
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface Section {
  
  public static final int SHT_NULL = 0x0;
  public static final int SHT_PROGBITS = 0x1;
  public static final int SHT_SYMTAB = 0x2;
  public static final int SHT_STRTAB = 0x3;
  public static final int SHT_RELA = 0x4;
  public static final int SHT_REL = 0x9;
  
  public static final int SHF_WRITE = 0x1;
  public static final int SHF_ALLOC = 0x2;
  public static final int SHF_EXECINSTR = 0x4;
  public static final int SHF_MERGE = 0x10;
  public static final int SHF_STRINGS = 0x20;
  public static final int SHF_INFO_LINK = 0x40;
  public static final int SHF_LINK_ORDER = 0x80;
  public static final int SHF_OS_NONCONFORMING = 0x100;
  public static final int SHF_GROUP = 0x200;
  public static final int SHF_TLS = 0x400;
  
  public static final int SHN_UNDEF = 0;
  public static final int SHN_ABS = 0xFFF1;
  public static final int SHN_COMMON = 0xFFF2;
  
  @Deprecated
  public static final int STT_NOTYPE = 0x0;
  @Deprecated
  public static final int STT_OBJECT = 0x1;
  @Deprecated
  public static final int STT_FUNC = 0x2;
  @Deprecated
  public static final int STT_SECTION = 0x3;
  @Deprecated
  public static final int STT_FILE = 0x4;
  @Deprecated
  public static final int STT_COMMON = 0x5;
  @Deprecated
  public static final int STT_TLS = 0x6;
  
  @Deprecated
  public static final int STB_LOCAL = 0x0;
  @Deprecated
  public static final int STB_GLOBAL = 0x1;
  @Deprecated
  public static final int STB_WEAK = 0x2;
  
  @Deprecated
  public static final int STV_DEFAULT = 0x0;
  @Deprecated
  public static final int STV_INTERNAL = 0x1;
  @Deprecated
  public static final int STV_HIDDEN = 0x2;
  @Deprecated
  public static final int STV_PROTECTED = 0x3;
  
  SectionHeader getHeader ();
  
  default long getOffset () {
    return getHeader ().getOffset ();
  }
  
  default long getSize () {
    return getHeader ().getSize ();
  }
  
  default int getNameOffset () {
    return getHeader ().getNameOffset ();
  }
  
  default String getName () {
    return getHeader ().getName ();
  }
  
  default int getType () {
    return getHeader ().getType ();
  }
  
  default long getFlags () {
    return getHeader ().getFlags ();
  }
  
  default long getVirtualAddress () {
    return getHeader ().getVirtualAddress ();
  }
  
  default int getLink () {
    return getHeader ().getLink ();
  }
  
  default int getInfo () {
    return getHeader ().getInfo ();
  }
  
  default long getAddressAlign () {
    return getHeader ().getAddressAlign ();
  }
  
  default long getEntrySize () {
    return getHeader ().getEntrySize ();
  }
}