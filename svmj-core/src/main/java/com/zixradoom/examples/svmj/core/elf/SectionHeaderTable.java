
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface SectionHeaderTable {
  
  public static final int ENTRY_32_SIZE = 0x28;
  public static final int ENTRY_64_SIZE = 0x40;
  
  boolean isDirect ();
  SectionHeader get ( int index );
  int getSize ();
}