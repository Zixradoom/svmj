
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface ProgramHeaderTable {
  
  public static final int ENTRY_32_SIZE = 0x20;
  public static final int ENTRY_64_SIZE = 0x38;
  
  boolean isDirect ();
  ProgramHeader get ( int index );
  int getSize ();
}