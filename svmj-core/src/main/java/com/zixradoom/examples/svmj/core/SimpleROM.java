
package com.zixradoom.examples.svmj.core;

import java.nio.ByteBuffer;

public final class SimpleROM implements Device {
  
  private String name;
  private ByteBuffer memory;
  private int baseAddress;
  
  public SimpleROM ( int baseAddress, ByteBuffer memory ) {
    this ( null, baseAddress, memory );
  }
  
  public SimpleROM ( String name, int baseAddress, ByteBuffer memory ) {
    this.name = name;
    this.baseAddress = baseAddress;
    this.memory = memory;
  }
  
  public int getRequiredBaseAddress () {
    return baseAddress;
  }
  
  @Override
  public int readMemoryAddress ( int address ) {
    return memory.getInt ( address );
  }
  
  @Override
  public void writeMemoryAddress ( int address, int value ) {
    throw new UnsupportedOperationException ( String.format ( "read only memory: [%x]=%x", address, value ) );
  }
  
  @Override
  public int getMemorySize () {
    return memory.capacity ();
  }
  
  @Override
  public String toString () {
    return String.format ( "SimpleROM[baseAddress=%xH, memory=%s, name=%s]", baseAddress, memory, name );
  }
}