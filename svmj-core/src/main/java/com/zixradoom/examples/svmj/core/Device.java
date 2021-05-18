
package com.zixradoom.examples.svmj.core;

public interface Device {
  int getRequiredBaseAddress ();
  int readMemoryAddress ( int address );
  void writeMemoryAddress ( int address, int value );
  int getMemorySize ();
}