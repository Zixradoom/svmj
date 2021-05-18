
package com.zixradoom.examples.svmj.core;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import static com.zixradoom.examples.svmj.core.OperationCode.*;

public final class ProgramBuilder {

  private ByteBuffer program;

  public ProgramBuilder () {
    this ( 8192 );
  }

  public ProgramBuilder ( int size ) {
    program = ByteBuffer.allocate ( size );
  }

  public ProgramBuilder halt () {
    int instruction = HALT.getCode () << 16;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder load ( int r1, int r2 ) {
    int instruction = LOAD.getCode () << 16;
    instruction = instruction | ( ( 0xFF & r1 ) << 8 );
    instruction = instruction | ( 0xFF & r2 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder loadImmediate ( int r1, int value ) {
    int instruction = LOAD_I.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    instruction = instruction | ( 0xFFFF & value );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder loady ( int r1, int r2, int value ) {
    int instruction = LOAD_Y.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    instruction = instruction | ( ( 0xFF & r2 ) << 8 );
    instruction = instruction | ( 0xFF & value );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder load4x ( int r1, int r2, int r3 ) {
    int instruction = LOAD_4X.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    instruction = instruction | ( ( 0xFF & r2 ) << 8 );
    instruction = instruction | ( 0xFF & r3 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder store ( int r1, int r2 ) {
    int instruction = STORE.getCode () << 16;
    instruction = instruction | ( ( 0xFF & r1 ) << 8 );
    instruction = instruction | ( 0xFF & r2 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder store4x ( int r1, int r2, int r3 ) {
    int instruction = STORE_4X.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    instruction = instruction | ( ( 0xFF & r2 ) << 8 );
    instruction = instruction | ( 0xFF & r3 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder storey ( int r1, int r2, int value ) {
    int instruction = STORE_Y.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    instruction = instruction | ( ( 0xFF & r2 ) << 8 );
    instruction = instruction | ( 0xFF & value );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder move ( int r1, int r2 ) {
    int instruction = MOVE.getCode () << 16;
    instruction = instruction | ( ( 0xFF & r1 ) << 8 );
    instruction = instruction | ( 0xFF & r2 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder moveImmediate ( int r1, int value ) {
    int instruction = MOVE_I.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    instruction = instruction | ( 0xFFFF & value );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder add ( int a, int b, int c ) {
    int instruction = ADD.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    instruction = instruction | ( 0xFF & c);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder compare ( int a, int b ) {
    int instruction = CMP.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder compareZero ( int a ) {
    int instruction = CMP_Z.getCode () << 16;
    instruction = instruction | ( ( 0xFF & a ) << 8 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder subtract ( int a, int b, int c ) {
    int instruction = SUBTRACT.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    instruction = instruction | ( 0xFF & c);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder multiply ( int a, int b, int c ) {
    int instruction = MULTIPLY.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    instruction = instruction | ( 0xFF & c);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder divide ( int a, int b, int c ) {
    int instruction = DIVIDE.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    instruction = instruction | ( 0xFF & c);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder modulo ( int a, int b, int c ) {
    int instruction = MOD.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    instruction = instruction | ( 0xFF & c);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder increment ( int a ) {
    int instruction = INCREMENT.getCode () << 16;
    instruction = instruction | ( ( 0xFF & a ) << 8 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder decrement ( int a ) {
    int instruction = DECREMENT.getCode () << 16;
    instruction = instruction | ( ( 0xFF & a ) << 8 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder and ( int a, int b, int c ) {
    int instruction = AND.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    instruction = instruction | ( 0xFF & c);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder or ( int a, int b, int c ) {
    int instruction = OR.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    instruction = instruction | ( 0xFF & c);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder xor ( int a, int b, int c ) {
    int instruction = XOR.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    instruction = instruction | ( 0xFF & c);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder not ( int a, int b ) {
    int instruction = NOT.getCode () << 24;
    instruction = instruction | ( ( 0xFF & a ) << 16 );
    instruction = instruction | ( ( 0xFF & b ) << 8 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder push ( int r ) {
    int instruction = PUSH.getCode () << 16;
    instruction = instruction | ( 0xFF & r);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder pop ( int d ) {
    int instruction = POP.getCode () << 16;
    instruction = instruction | ( 0xFF & d);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jump ( int r1 ) {
    int instruction = JUMP.getCode () << 16;
    instruction = instruction | ( 0xFF & r1);
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpImmediate ( int immediate ) {
    int instruction = JUMP_I.getCode () << 24;
    instruction = instruction | ( 0xFFFFFF & immediate );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpY ( int immediate ) {
    int instruction = JUMP_Y.getCode () << 24;
    instruction = instruction | ( 0xFFFFFF & immediate );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpEqual ( int r1 ) {
    int instruction = JUMP_E.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpEqualImmediate ( int immediate ) {
    int instruction = JUMP_E_I.getCode () << 24;
    instruction = instruction | ( 0xFFFFFF & immediate );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpNotEqual ( int r1 ) {
    int instruction = JUMP_NE.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpNotEqualImmediate ( int immediate ) {
    int instruction = JUMP_NE_I.getCode () << 24;
    instruction = instruction | ( 0xFFFFFF & immediate );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpZero ( int r1 ) {
    int instruction = JUMP_Z.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpZeroImmediate ( int immediate ) {
    int instruction = JUMP_Z_I.getCode () << 24;
    instruction = instruction | ( 0xFFFFFF & immediate );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpNotZero ( int r1 ) {
    int instruction = JUMP_NZ.getCode () << 24;
    instruction = instruction | ( ( 0xFF & r1 ) << 16 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpNotZeroImmediate ( int immediate ) {
    int instruction = JUMP_NZ_I.getCode () << 24;
    instruction = instruction | ( 0xFFFFFF & immediate );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder jumpNotZeroY ( int immediate ) {
    int instruction = JUMP_NZ_Y.getCode () << 24;
    instruction = instruction | ( 0xFFFFFF & immediate );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder enter () {
    int instruction = ENTER.getCode () << 16;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder leave () {
    int instruction = LEAVE.getCode () << 16;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder call ( int r1 ) {
    int instruction = CALL.getCode () << 16;
    instruction = instruction | ( 0xFF & r1 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder callImmediate ( int value ) {
    int instruction = CALL_I.getCode () << 24;
    instruction = instruction | ( 0xFFFFFF & value );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder ret () {
    int instruction = RETURN.getCode () << 16;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder noop () {
    int instruction = NO_OP.getCode () << 24;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder shiftLeft ( int r1, int r2 ) {
    int instruction = SHIFT_LEFT.getCode () << 16;
    instruction = instruction | ( ( 0xFF & r1 ) << 8 );
    instruction = instruction | ( 0xFF & r2 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder shiftRight ( int r1, int r2 ) {
    int instruction = SHIFT_RIGHT.getCode () << 16;
    instruction = instruction | ( ( 0xFF & r1 ) << 8 );
    instruction = instruction | ( 0xFF & r2 );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder tlbFlush () {
    int instruction = TLB_FLUSH.getCode () << 16;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder enablePaging () {
    int instruction = ENABLE_PAGING.getCode () << 16;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder disablePaging () {
    int instruction = DISABLE_PAGING.getCode () << 16;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder grtocr ( int r, int c ) {
    int instruction = GR_TO_CR.getCode () << 16;
    instruction = instruction | ( ( 0xFF & r ) << 8 );
    instruction = instruction | ( 0xFF & c );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder crtogr ( int c, int r ) {
    int instruction = CR_TO_GR.getCode () << 16;
    instruction = instruction | ( ( 0xFF & c ) << 8 );
    instruction = instruction | ( 0xFF & r );
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder systemEnter () {
    int instruction = SYSTEM_ENTER.getCode () << 16;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder systemExit () {
    int instruction = SYSTEM_EXIT.getCode () << 16;
    program.putInt ( instruction );
    return this;
  }
  
  public ProgramBuilder memory ( int value ) {
    program.putInt ( value );
    return this;
  }
  
  public ProgramBuilder zero () {
    program.putInt ( 0 );
    return this;
  }
  
  public int getCurrentAddress () {
    return program.position ();
  }
  
  public ByteBuffer getProgram () {
    return program.slice ( 0, program.position () );
  }
  
  public ProgramBuilder mark () {
    program.mark ();
    return this;
  }
  
  public ProgramBuilder reset () {
    program.reset ();
    return this;
  }
  
  public ProgramBuilder rewind () {
    program.rewind ();
    return this;
  }
  
  public ProgramBuilder position ( int address ) {
    program.position ( address );
    return this;
  }
}