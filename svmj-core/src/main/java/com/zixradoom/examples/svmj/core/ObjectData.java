
package com.zixradoom.examples.svmj.core;

import java.util.Map;
import java.util.HashMap;

public final class ObjectData {
  private Object sections;
  private Map < String, Symbol > symbolTable;
  
  public ObjectData () {
    sections = new Object ();
    symbolTable = new HashMap  <> ();
  }
  
  public Symbol createSymbol ( String symbolName ) {
    if ( symbolTable.containsKey ( symbolName ) ) {
      throw new IllegalArgumentException ( String.format ( "symbol %s already exists", symbolName ) );
    }
    Symbol s = new Symbol ( symbolName );
    symbolTable.put ( symbolName, s );
    return s;
  }
  
  public Symbol getSymbol ( String symbolName ) {
    return symbolTable.get ( symbolName );
  }
  
  public static final class Symbol {
    private String name;
    private long value;
    private int size;
    private Bind bind;
    private Type type;
    
    public Symbol ( String name ) {
      this.name = name;
      value = 0;
      size = 0;
      bind = Bind.LOCAL;
      type = Type.NONE;
    }
    
    public String getName () {
      return name;
    }
    
    public long getValue () {
      return value;
    }
    
    public void setValue ( long value ) {
      this.value = value;
    }
    
    public int getSize () {
      return size;
    }
    
    public void setSize ( int size ) {
      this.size = size;
    }
    
    public Bind getBind () {
      return bind;
    }
    
    public void setBind ( Bind bind ) {
      this.bind = bind;
    }
    
    public Type getType () {
      return type;
    }
    
    public void setType ( Type type ) {
      this.type = type;
    }
    
    public enum Bind {
      LOCAL,
      GLOBAL
    }
    
    public enum Type {
      NONE,
      OBJECT,
      FUNTION
    }
  }
}