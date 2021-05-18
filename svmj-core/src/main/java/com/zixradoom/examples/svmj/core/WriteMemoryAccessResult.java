
package com.zixradoom.examples.svmj.core;

import java.util.Objects;

public class WriteMemoryAccessResult implements MemoryAccessResult {
  
  private int address;
  private MemoryAccessResult.Status status;
  
  public WriteMemoryAccessResult ( int address, MemoryAccessResult.Status status ) {
    this.address = address;
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
  
  public static WriteMemoryAccessResult success ( int address ) {
    return new WriteMemoryAccessResult ( address, MemoryAccessResult.Status.SUCCESS );
  }
  
  public static WriteMemoryAccessResult pageFault ( int address ) {
    return new WriteMemoryAccessResult ( address, MemoryAccessResult.Status.PAGE_FAULT );
  }
  
  @Override
  public String toString () {
    return "WriteMemoryAccessResult[status="+status+", address=0x"+Integer.toHexString (address)+"]";
  }
}