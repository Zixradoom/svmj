package com.zixradoom.svmj.core;

import java.nio.ByteBuffer;

public final class ProgramBuilder {
	
	private ByteBuffer data;
	
	public ProgramBuilder () {
		this ( 8192 );
	}
	
	public ProgramBuilder ( int bufferSize ) {
		data = ByteBuffer.allocate ( bufferSize );
	}
	
	public int getCapacity () {
		return data.capacity ();
	}
	
	public int getPosition () {
		return data.position ();
	}
	
	public ProgramBuilder setPosition ( int position ) {
		data.position ( position );
		return this;
	}
	
	public ProgramBuilder mark () {
		data.mark ();
		return this;
	}
	
	public ProgramBuilder reset () {
		data.reset ();
		return this;
	}
	
	public ProgramBuilder halt () {
		int instruction = OpCode.HALT.getCode () << 16;
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder add ( int r1, int r2, int r3 ) {
		int instruction = OpCode.ADD.getCode () << 24;
		instruction = instruction | ( ( 0xFF & r1 ) << 16 );
		instruction = instruction | ( ( 0xFF & r2 ) << 8 );
		instruction = instruction | ( 0xFF & r3 );
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder subtract ( int r1, int r2, int r3 ) {
		int instruction = OpCode.SUBTRACT.getCode () << 24;
		instruction = instruction | ( ( 0xFF & r1 ) << 16 );
		instruction = instruction | ( ( 0xFF & r2 ) << 8 );
		instruction = instruction | ( 0xFF & r3 );
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder multiply ( int r1, int r2, int r3 ) {
		int instruction = OpCode.MULTIPLY.getCode () << 24;
		instruction = instruction | ( ( 0xFF & r1 ) << 16 );
		instruction = instruction | ( ( 0xFF & r2 ) << 8 );
		instruction = instruction | ( 0xFF & r3 );
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder divide ( int r1, int r2, int r3 ) {
		int instruction = OpCode.DIVIDE.getCode () << 24;
		instruction = instruction | ( ( 0xFF & r1 ) << 16 );
		instruction = instruction | ( ( 0xFF & r2 ) << 8 );
		instruction = instruction | ( 0xFF & r3 );
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder compare ( int r1, int r2 ) {
		int instruction = OpCode.COMPARE.getCode () << 16;
		instruction = instruction | ( ( 0xFF & r1 ) << 8 );
		instruction = instruction | ( 0xFF & r2 );
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder call ( int r1 ) {
		int instruction = OpCode.CALL.getCode () << 16;
		instruction = instruction | ( 0xFF & r1 );
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder ret () {
		int instruction = OpCode.RETURN.getCode () << 16;
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder enter () {
		int instruction = OpCode.ENTER.getCode () << 16;
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder leave () {
		int instruction = OpCode.LEAVE.getCode () << 16;
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder push ( int r1 ) {
		int instruction = OpCode.PUSH.getCode () << 16;
		instruction = instruction | ( 0xFF & r1 );
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder pop ( int r1 ) {
		int instruction = OpCode.POP.getCode () << 16;
		instruction = instruction | ( 0xFF & r1 );
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder load ( int r1, int r2 ) {
		int instruction = OpCode.LOAD.getCode () << 16;
		instruction = instruction | ( ( 0xFF & r1 ) << 8 );
		instruction = instruction | ( 0xFF & r2 );
		data.putInt ( instruction );
		return this;
	}
	
	public ProgramBuilder store ( int r1, int r2 ) {
		int instruction = OpCode.STORE.getCode () << 16;
		instruction = instruction | ( ( 0xFF & r1 ) << 8 );
		instruction = instruction | ( 0xFF & r2 );
		data.putInt ( instruction );
		return this;
	}
	
	public ByteBuffer getImage () {
		return data.slice ( 0, data.position () );
	}
}
