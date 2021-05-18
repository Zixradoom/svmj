package com.zixradoom.svmj.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum OpCode {
	
	// arithmetic
	ADD          (   0x11 ),
	SUBTRACT     (   0x13 ),
	MULTIPLY     (   0x15 ),
	DIVIDE       (   0x17 ),
	COMPARE      ( 0x8019 ),
	
  // logic
	AND          ( 0x21 ),
	OR           ( 0x23 ),
	XOR          ( 0x25 ),
	NOT          ( 0x27 ),
	
  // stack management
	PUSH         ( 0x8031 ),
	POP          ( 0x8033 ),
	CALL         ( 0x8035 ),
	RETURN       ( 0x8037 ),
	ENTER        ( 0x8039 ),
	LEAVE        ( 0x803B ),
	
	// control flow
	JUMP         ( 0x41 ),
	JUMP_Z       ( 0x43 ),
	JUMP_NZ      ( 0x45 ),
	JUMP_E       ( 0x47 ),
	JUMP_NE      ( 0x49 ),
	
	// data storage
	MOVE         ( 0x8071 ),
	LOAD         ( 0x8073 ),
	STORE        ( 0x8075 ),
	
  // mode
	
	// 2 byte opcodes
	
	HALT                   ( 0x8011 ),
	
  // operating system support
	/*
	SYSTEM_ENTER           ( 0x8001 ),
	SYSTEM_LEAVE           ( 0x8003 ),
	VIRTUAL_MEMORY_ENABLED ( 0x8005 ),
	VIRTUAL_MEMORY_DISABLE ( 0x8007 ),
	TLB_FLUSH              ( 0x8009 ),
	*/
	;
	
	public static final Map < Integer, OpCode > OP_CODES = createCodeMap (); 
	
	private final int code;
	
	private OpCode ( int code ) {
		this.code = code;
	}
	
	public int getCode () {
		return code;
	}
	
	public static OpCode getByCode ( int code ) {
		return OP_CODES.get ( code );
	}
	
	private static Map < Integer, OpCode > createCodeMap () {
		Map < Integer, OpCode > returnValue = new HashMap <> ();
		
		for ( OpCode oc : OpCode.values () ) {
			returnValue.put ( oc.getCode (), oc );
		}
		
		return Collections.unmodifiableMap ( returnValue );
	}
}
