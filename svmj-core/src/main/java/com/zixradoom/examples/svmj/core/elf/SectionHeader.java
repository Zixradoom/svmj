
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface SectionHeader {
  int getNameOffset ();
  String getName ();
  int getType ();
  long getFlags ();
  long getVirtualAddress ();
  long getOffset ();
  long getSize ();
  int getLink ();
  int getInfo ();
  long getAddressAlign ();
  long getEntrySize ();
  Section getSection ();
  < T extends Section > T getSection ( Class < T > clazz );
}