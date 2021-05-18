
package com.zixradoom.examples.svmj.core;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * This class outlines the Instruction Set Architecture of the Simon Virtual Machine
 * <br /><br />
 * <h2>Instruction Suffix</h2>
 */
public enum OperationCode {
  
  // d,s,a1,a2 are all registers
  
  /**
   *  Format<br /><br />
   *  <pre>
   *  +--------+----------------------+----------+
   *  |Op Code | destination register | Immedate |
   *  +--------+----------------------+----------+
   *  |Op Code | destination register | Label    |
   *  +--------+----------------------+----------+
   *  </pre>
   */
  LOAD_I        ( 0x04 ),

  // oc, d, immediate
  MOVE_I        ( 0x08 ),
  // oc, a1 + a2 = d
  ADD           ( 0x09 ),
  // oc, a1 - a2 = d
  SUBTRACT      ( 0x0B ),
  // oc, a1 * a2 = d
  MULTIPLY      ( 0x0D ),
  // oc, a1 / a2 = d
  DIVIDE        ( 0x0F ),
  
  // oc, a1 % a2 = d
  MOD           ( 0x10 ),
  
  // oc, s
  PUSH          ( 0x8211 ),
  // oc, d
  POP           ( 0x8213 ),
  // oc, stack pointer register, base pointer register
  ENTER         ( 0x8215 ),
  // oc, stack pointer register, base pointer register
  LEAVE         ( 0x8217 ),
  // oc, stack pointer, address register
  CALL          ( 0x19 ),
  // oc, stack pointer, immediate16
  CALL_I        ( 0x20 ),
  // oc, stack pointer
  RETURN        ( 0x821B ),
  // oc
  NO_OP         ( 0x1D ),
  // oc, a1, a2, d
  AND           ( 0x1F ),
  // oc, a1, a2, d
  OR            ( 0x21 ),
  // oc, a1, d
  NOT           ( 0x23 ),
  // oc, a1, a2, d
  XOR           ( 0x25 ),
  // oc, a, b
  CMP           ( 0x27 ),
  
  // oc, d, base, signed immediate
  LOAD_Y        ( 0x2D ),
  
  // oc, d, base, index ( mulitpled by 4 )
  LOAD_4X       ( 0x2F ),
  
  // oc, s, base, signed immediate
  STORE_Y       ( 0x31 ),
  
  // oc, s, base, index ( mulitpled by 4 )
  STORE_4X      ( 0x33 ),
  
  // oc, s
  JUMP          ( 0x8041 ),
  // oc, immediate
  JUMP_I        ( 0x42 ),
  // oc, s
  JUMP_E        ( 0x43 ),
  // oc, immediate
  JUMP_E_I      ( 0x44 ),
  // oc, s
  JUMP_NE       ( 0x45 ),
  // oc, immediate
  JUMP_NE_I     ( 0x46 ),
  // oc, s
  JUMP_Z        ( 0x47 ),
  // oc, immediate
  JUMP_Z_I      ( 0x48 ),
  // oc, s
  JUMP_NZ       ( 0x49 ),
  // oc, immediate24
  JUMP_NZ_I     ( 0x4A ),
  
  // oc, pc + immediate24
  JUMP_Y        ( 0x4C ),
  
  // oc, pc + immediate24
  JUMP_NZ_Y     ( 0x4E ),
  
  // instructions that start with a 1 in the MSB are 2 byte opcodes
  // oc
  TLB_FLUSH      ( 0x8001 ),
  // oc
  ENABLE_PAGING  ( 0x8003 ),
  // oc
  DISABLE_PAGING ( 0x8004 ),
  // oc
  SYSTEM_ENTER   ( 0x8005 ),
  // oc
  SYSTEM_EXIT    ( 0x8006 ),
  // oc
  HALT           ( 0x8007 ),
  
  INCREMENT      ( 0x8009 ),
  
  DECREMENT      ( 0x800a ),
  
  // oc, address register, d
  LOAD           ( 0x800c ),
  
  // oc, address register, s
  STORE          ( 0x800e ),
  
  // oc, s, d
  MOVE           ( 0x800f ),
  
  // oc, a
  CMP_Z          ( 0x8011 ),
  
  // oc, a, s
  SHIFT_LEFT     ( 0x8013 ),
  
  // oc, a, s
  SHIFT_RIGHT    ( 0x8015 ),
  
  // oc, a, s
  SHIFT_RIGHT_A  ( 0x8016 ),
  
  // oc, r*, c*
  GR_TO_CR       ( 0x8017 ),
  
  // oc, c*, r*
  CR_TO_GR       ( 0x8019 ),
  
  // oc
  INTERRUPT_RETURN ( 0x81FF ),
  
  ;
  
  
  private static final Map < Integer, OperationCode > codeToOP = createMap ();
  
  private final int opcode;
  
  private OperationCode ( int code ) {
    opcode = code;
  }
  
  public int getCode () {
    return opcode;
  }
  
  public static OperationCode getByCode ( int code ) {
    return codeToOP.get ( code );
  }
  
  private static Map < Integer, OperationCode > createMap () {
    Map < Integer, OperationCode > returnValue = new HashMap <> ();
    for ( OperationCode oc : OperationCode.values () ) {
      returnValue.put ( oc.getCode (), oc );
    }
    return Collections.unmodifiableMap ( returnValue );
  }
}