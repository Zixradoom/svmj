
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface ProgramHeader {
  int getType ();
  int getFlags ();
  long getOffset ();
  long getVirtualAddress ();
  long getPhysicalAddress ();
  long getFileSize ();
  long getMemorySize ();
  long getAlign ();
}