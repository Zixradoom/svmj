
package com.zixradoom.examples.svmj.core;

public interface MemoryAccessResult {
  
  int getAddress ();
  Status getStatus ();
  
  public enum Status {
    SUCCESS,
    PAGE_FAULT,
    UNSUPPORTED
  }
}