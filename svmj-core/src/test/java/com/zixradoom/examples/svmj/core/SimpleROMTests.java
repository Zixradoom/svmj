
package com.zixradoom.examples.svmj.core;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SimpleROMTests {
  @Test
  public void createSimpleROM () {
    
    int baseAddress = 0xf;
    ByteBuffer bb = ByteBuffer.allocate ( 256 );
    
    new SimpleROM ( baseAddress, bb );
  }
}