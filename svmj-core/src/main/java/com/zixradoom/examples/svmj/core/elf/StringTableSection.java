
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface StringTableSection extends Section {
  String getString ( int offset );
}