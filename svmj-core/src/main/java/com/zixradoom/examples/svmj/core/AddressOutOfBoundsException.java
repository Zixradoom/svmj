
package com.zixradoom.examples.svmj.core;

public final class AddressOutOfBoundsException extends RuntimeException {
  public AddressOutOfBoundsException () {}
  
  public AddressOutOfBoundsException ( long value ) {
    this ( value, null );
  }
  
  public AddressOutOfBoundsException ( long value, Throwable cause ) {
    super ( String.format ( "%8x", value ), cause );
  }
}