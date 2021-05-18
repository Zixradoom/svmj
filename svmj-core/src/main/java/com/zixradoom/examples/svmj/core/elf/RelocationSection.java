

package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;

import java.util.List;
import java.util.ArrayList;

public interface RelocationSection extends Section {
  
  public static final int R_SVM_NONE = 0;
  public static final int R_SVM_8 = 1;
  public static final int R_SVM_16 = 2;
  public static final int R_SVM_24 = 3;
  
  @Deprecated
  public static final int R_SVM_CALL_I = 16;
  @Deprecated
  public static final int R_SVM_LOAD_I = 17;
  @Deprecated
  public static final int R_SVM_MOVE_I = 18;
  @Deprecated
  public static final int R_SVM_JUMP_I = 19;
  @Deprecated
  public static final int R_SVM_JUMP_NZ_I = 20;
  
  Record newRecord ();
  Record getRecord ( int index );
  int getEntryCount ();
  List < ? extends Record > getRecords ();
  
  public static interface Record {

    long getOffset ();
    int getSymbolIndex ();
    String getSymbolName ();
    int getType ();
    long getInfo ();
  }
}
