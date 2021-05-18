
package com.zixradoom.examples.svmj.core;

public final class PageFaultException extends RuntimeException {
  public PageFaultException () {}
  
  public PageFaultException ( long value ) {
    this ( value, null );
  }
  
  public PageFaultException ( long value, Throwable cause ) {
    super ( String.format ( "%8x", value ), cause );
  }
}