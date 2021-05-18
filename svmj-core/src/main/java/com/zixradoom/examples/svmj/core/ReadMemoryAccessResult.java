
package com.zixradoom.examples.svmj.core;

import java.util.Objects;

public class ReadMemoryAccessResult implements MemoryAccessResult {
  
  private int address;
  private int value;
  private MemoryAccessResult.Status status;
  
  public ReadMemoryAccessResult ( int address, int value, MemoryAccessResult.Status status ) {
    this.address = address;
    this.value = value;
    this.status = Objects.requireNonNull ( status, "status is null" );
  }
  
  @Override
  public int getAddress () {
    return address;
  }
  
  @Override
  public Status getStatus () {
    return status;
  }
  
  public int getValue () {
    return value;
  }
  
  public static ReadMemoryAccessResult success ( int address, int value ) {
    return new ReadMemoryAccessResult ( address, value, MemoryAccessResult.Status.SUCCESS );
  }
  
  public static ReadMemoryAccessResult pageFault ( int address ) {
    return new ReadMemoryAccessResult ( address, -1, MemoryAccessResult.Status.PAGE_FAULT );
  }
  
  @Override
  public String toString () {
    return "ReadMemoryAccessResult[status="+status+", address=0x"+Integer.toHexString (address)+", value="+value+"]";
  }
}