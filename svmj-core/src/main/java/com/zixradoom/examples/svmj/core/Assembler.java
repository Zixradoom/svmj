
package com.zixradoom.examples.svmj.core;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
  <program> ::= 1*(<statement>)
  
  <statement> ::= <directive>
               |  <expression>
               
  <directive> ::= <directive-key-word>
*/

public final class Assembler implements AutoCloseable {
  
  private static final Logger LOGGER = LoggerFactory.getLogger ( Assembler.class );
  
  private static final Pattern LABEL_PATTERN = Pattern.compile ( "([A-Za-z_])([A-Za-z0-9])*\\:" );
  private static final Pattern LABEL_PATTERN_2 = Pattern.compile ( "([A-Za-z_])([A-Za-z0-9])*" );
  private static final Pattern REGISTER_PATTERN = Pattern.compile ( "[rR][0-9]+" );
  private static final Pattern ADDRESS_PATTERN = Pattern.compile ( "(\\-)?[0-9a-fA-F]+[H]" );
  private static final Set < String > INSTRUCTION_MNEMONICS = EnumSet.allOf ( OperationCode.class )
    .stream ()
    .map ( e -> e.toString () )
    .collect ( Collectors.toSet () );
  
  private LineNumberReader reader;
  private Scanner scanner;
  private ProgramBuilder pb;
  
  private String token = null;
  private Map < String, Integer > symbolTable;
  
  public Assembler ( Reader reader ) {
    this.reader = new LineNumberReader ( reader );
    this.scanner = new Scanner ( this.reader );
    pb = new ProgramBuilder ( 16384 );
    symbolTable = new HashMap <> ();
  }
  
  public Assembler ( InputStream is ) {
    this ( new InputStreamReader ( is, StandardCharsets.UTF_8 ) );
  }
  
  public void process () {
    skip ();
    program ();
  }
  
  private void program () {
    boolean parse = true;
    while ( parse ) {
      LOGGER.debug ( "token[{}]", token );
      if ( isLabel () ) {
        String label = label ();
        symbolTable.put ( label, pb.getCurrentAddress () );
      } else if ( isOpCode () ) {
        OperationCode oc = opCode ();
        switch ( oc ) {
          case AND: {
            int r1 = register ();
            int r2 = register ();
            int r3 = register ();
            pb.and ( r1, r2, r3 );
            break;
          }
          case OR: {
            int r1 = register ();
            int r2 = register ();
            int r3 = register ();
            pb.or ( r1, r2, r3 );
            break;
          }
          case XOR: {
            int r1 = register ();
            int r2 = register ();
            int r3 = register ();
            pb.xor ( r1, r2, r3 );
            break;
          }
          case NOT: {
            int r1 = register ();
            int r2 = register ();
            pb.not ( r1, r2 );
            break;
          }
          case CMP: {
            int r1 = register ();
            int r2 = register ();
            pb.compare ( r1, r2 );
            break;
          }
          case ADD: {
            int r1 = register ();
            int r2 = register ();
            int r3 = register ();
            pb.add ( r1, r2, r3 );
            break;
          }
          case SUBTRACT: {
            int r1 = register ();
            int r2 = register ();
            int r3 = register ();
            pb.subtract ( r1, r2, r3 );
            break;
          }
          case MULTIPLY: {
            int r1 = register ();
            int r2 = register ();
            int r3 = register ();
            pb.multiply ( r1, r2, r3 );
            break;
          }
          case DIVIDE: {
            int r1 = register ();
            int r2 = register ();
            int r3 = register ();
            pb.divide ( r1, r2, r3 );
            break;
          }
          case STORE: {
            int r1 = register ();
            int r2 = register ();
            pb.store ( r1, r2 );
            break;
          }
          case LOAD: {
            int r1 = register ();
            int r2 = register ();
            pb.load ( r1, r2 );
            break;
          }
          case LOAD_I: {
            int r1 = register ();
            int address = address ();
            pb.loadImmediate ( r1, address );
            break;
          }
          case MOVE: {
            int r1 = register ();
            int r2 = register ();
            pb.move ( r1, r2 );
            break;
          }
          case MOVE_I: {
            int r1 = register ();
            int address = address ();
            pb.moveImmediate ( r1, address );
            break;
          }
          case PUSH: {
            int r = register ();
            pb.push ( r );
            break;
          }
          case POP: {
            int r = register ();
            pb.pop ( r );
            break;
          }
          case ENTER: {
            pb.enter ();
            break;
          }
          case LEAVE: {
            pb.leave ();
            break;
          }
          case CALL: {
            int r2 = register ();
            pb.call ( r2 );
            break;
          }
          case RETURN: {
            pb.ret ();
            break;
          }
          case JUMP: {
            int r1 = register ();
            pb.jump ( r1 );
            break;
          }
          case JUMP_I: {
            int address = address ();
            pb.jumpImmediate ( address );
            break;
          }
          case JUMP_E: {
            int r1 = register ();
            pb.jumpEqual ( r1 );
            break;
          }
          case JUMP_E_I: {
            int address = address ();
            pb.jumpEqualImmediate ( address );
            break;
          }
          case JUMP_NE: {
            int r1 = register ();
            pb.jumpNotEqual ( r1 );
            break;
          }
          case JUMP_NE_I: {
            int address = address ();
            pb.jumpNotEqualImmediate ( address );
            break;
          }
          case JUMP_Z: {
            int r1 = register ();
            pb.jumpZero ( r1 );
            break;
          }
          case JUMP_Z_I: {
            int address = address ();
            pb.jumpZeroImmediate ( address );
            break;
          }
          case JUMP_NZ: {
            int r1 = register ();
            pb.jumpNotZero ( r1 );
            break;
          }
          case JUMP_NZ_I: {
            int address = address ();
            pb.jumpNotZeroImmediate ( address );
            break;
          }
          case HALT:
            pb.halt ();
            break;
          case NO_OP:
            pb.noop ();
            break;
          case ENABLE_PAGING:
            pb.enablePaging ();
            break;
          case DISABLE_PAGING:
            pb.disablePaging ();
            break;
          case SYSTEM_ENTER:
            pb.systemEnter ();
            break;
          case SYSTEM_EXIT:
            pb.systemExit ();
            break;
          default:
            throw new IllegalArgumentException ( String.format ( "Line%s: illegal opcode [%s]", reader.getLineNumber (), oc ) );
        }
      } else if ( isEOF () ) {
        eof ();
        parse = false;
      } else {
        throw new IllegalStateException ( "unprocessed input" );
      }
    }
  }
  
  private boolean isAddress () {
    return ADDRESS_PATTERN.matcher ( token ).matches ();
  }
  
  private int address () {
    int address = Integer.parseInt ( token.replace ( "H", "" ), 16 );
    match ( ADDRESS_PATTERN );
    return address;
  }
  
  private boolean isEOF () {
    return "<EOF>".equals ( token );
  }
  
  private void eof () {
    match ( "<EOF>" );
  }
  
  private boolean isRegister () {
    return REGISTER_PATTERN.matcher ( token ).matches ();
  }
  
  private int register () {
    int r = Integer.valueOf ( token.replace ( "r", "" ) );
    match ( REGISTER_PATTERN );
    return r;
  }
  
  private boolean isOpCode () {
    return INSTRUCTION_MNEMONICS.contains ( token );
  }
  
  private OperationCode opCode () {
    if ( INSTRUCTION_MNEMONICS.contains ( token ) ) {
      OperationCode oc = OperationCode.valueOf ( token );
      match ( token );
      return oc;
    } else {
      throw new IllegalArgumentException ();
    }
  }
  
  private boolean isLabel () {
    return LABEL_PATTERN.matcher ( token ).matches ();
  }
  
  private String label () {
    String returnValue = token.replace ( ":", "" );
    match ( LABEL_PATTERN );
    return returnValue;
  }
  
  private void match ( Pattern symbolPattern ) {
    LOGGER.debug ( "match[{}]", symbolPattern );
    Matcher m = symbolPattern.matcher ( token );
    if ( m.matches () ) {
      if ( scanner.hasNext () ) {
        token = scanner.next ();
      } else {
        token = "<EOF>";
      }
    } else {
      throw new IllegalStateException ( String.format ( "expected '%s' found '%s'", symbolPattern, token ) );
    }
  }
  
  private void match ( String symbol ) {
    LOGGER.debug ( "match[{}]", symbol );
    if ( symbol.equals ( token ) ) {
      if ( scanner.hasNext () ) {
        token = scanner.next ();
      } else {
        token = "<EOF>";
      }
    } else {
      throw new IllegalStateException ( String.format ( "expected '%s' found '%s'", symbol, token ) );
    }
  }
  
  private void skip () {
    if ( scanner.hasNext () ) {
      token = scanner.next ();
    } else {
      token = "<EOF>";
    }
    LOGGER.debug ( "skip" );
  }
  
  public Map < String, Integer > getSymbolTable () {
    return Collections.unmodifiableMap ( symbolTable );
  }
  
  public ByteBuffer getProgram () {
    return pb.getProgram ();
  }
  
  @Override
  public void close () {
    scanner.close ();
  }
}