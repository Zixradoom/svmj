
package com.zixradoom.examples.svmj.readelf;

import java.io.IOException;
import java.io.PrintStream;

import java.nio.file.Paths;
import java.nio.file.Path;

import com.zixradoom.examples.svmj.core.elf.ELF;
import com.zixradoom.examples.svmj.core.elf.Section;
import com.zixradoom.examples.svmj.core.elf.SymbolTableSection;
import com.zixradoom.examples.svmj.core.elf.RelocationSection;
import com.zixradoom.examples.svmj.core.elf.RelocationSectionDirect;
import com.zixradoom.examples.svmj.core.elf.SectionHeader;
import com.zixradoom.examples.svmj.core.elf.SectionHeaderTable;
import com.zixradoom.examples.svmj.core.elf.ELFUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Driver {
  private static final Logger LOGGER = LoggerFactory.getLogger ( Driver.class );
  
  public static void main ( String[] args ) throws IOException {
    Path elfFile = Paths.get ( args[ 0 ] );
    ELF elf = ELFUtil.read ( elfFile );
    
    LOGGER.info ( "reading {}", elfFile );
    printSectionHeaders ( System.out, elf );
    System.out.println ();
    printSymbolSection ( System.out, elf );
    System.out.println ();
    printRelocationSection ( System.out, elf );
  }
  
  public static void printSectionHeaders ( PrintStream out, ELF elf ) {
    SectionHeaderTable sectionHeaderTable = elf.getSectionHeaderTable ();
    if ( elf.getClassType () == 1 ) {
      out.printf ( "There are %d section header, starting at offset 0x%x%n%n",
        sectionHeaderTable.getSize (), elf.getSectionHeaderTableOffset () );
      out.printf ( "Section Headers:%n" );
      out.printf ( "%5s %-10s %6s %8s %8s %8s %8s %8s %8s %8s %s%n",
        "Index",
        "Type",
        "Flags",
        "Address",
        "Offset",
        "Size",
        "Link",
        "Info",
        "Align",
        "Ent Size",
        "Name" );
      for ( int index = 0; index < sectionHeaderTable.getSize (); index++ ) {
        SectionHeader sh = sectionHeaderTable.get ( index );
        out.printf ( "[%3d] %-10s %6s %8x %8x %8x %8x %8x %8x %8x %s%n",
          index,
          sectionHeaderType ( (int)sh.getType () ),
          sectionHeaderFlags ( (int)sh.getFlags () ),
          sh.getVirtualAddress (),
          sh.getOffset (),
          sh.getSize (),
          sh.getLink (),
          sh.getInfo (),
          sh.getAddressAlign (),
          sh.getEntrySize (),
          sh.getName () );
      }
    }
  }
  
  private static String sectionHeaderType ( int value ) {
    switch (value) {
      case Section.SHT_NULL: {
        return "NULL";
      }
      case Section.SHT_PROGBITS: {
        return "PROGBITS";
      }
      case Section.SHT_SYMTAB: {
        return "SYMTAB";
      }
      case Section.SHT_STRTAB: {
        return "STRTAB";
      }
      case Section.SHT_RELA: {
        return "RELA";
      }
      case Section.SHT_REL: {
        return "REL";
      }
      default: {
        return String.format ( "%4x", value );
      }
    }
  }
  
  private static String sectionHeaderFlags ( int value ) {
    StringBuilder returnValue = new StringBuilder ();
    if ( ( value & Section.SHF_INFO_LINK ) != 0 ) returnValue.append ( 'I' ); else returnValue.append ( ' ' );
    if ( ( value & Section.SHF_STRINGS ) != 0 ) returnValue.append ( 'S' ); else returnValue.append ( ' ' );
    if ( ( value & Section.SHF_MERGE ) != 0 ) returnValue.append ( 'M' ); else returnValue.append ( ' ' );
    if ( ( value & Section.SHF_EXECINSTR ) != 0 ) returnValue.append ( 'E' ); else returnValue.append ( ' ' );
    if ( ( value & Section.SHF_ALLOC ) != 0 ) returnValue.append ( 'A' ); else returnValue.append ( ' ' );
    if ( ( value & Section.SHF_WRITE ) != 0 ) returnValue.append ( 'W' ); else returnValue.append ( ' ' );
    return returnValue.toString ();
  }
  
  public static void printSymbolSection ( PrintStream out, ELF elf ) {
    if ( elf.getClassType () == 1 ) {
      SectionHeaderTable sectionHeaderTable = elf.getSectionHeaderTable ();
      SectionHeader sectionHeader = null;
      for ( int index = 0; index < sectionHeaderTable.getSize (); index++ ) {
        if ( sectionHeaderTable.get ( index ).getType () == Section.SHT_SYMTAB ) {
          sectionHeader = sectionHeaderTable.get ( index );
          break;
        }
      }
      SymbolTableSection symbolTableSection = sectionHeader.getSection ( SymbolTableSection.class );
      out.printf ( "Symbol Table '%s' contains %d entries:%n", symbolTableSection.getName (), symbolTableSection.getSymbols ().size () );
      out.printf ( "%5s %8s %8s %2s %2s %5s %s%n",
        "Index",
        "Value",
        "Size",
        "T",
        "O",
        "shndx",
        "Name" );
      for ( int index = 0; index < symbolTableSection.getSymbols ().size (); index++ ) {
        SymbolTableSection.Symbol symbol = symbolTableSection.getSymbol ( index );
        out.printf ( "[%3d] %8x %8x %2s %2x %5s %s%n",
          index,
          symbol.getValue (),
          symbol.getSize (),
          symbolType ( symbol.getBind (), symbol.getType () ),
          symbol.getOther (),
          shndx ( symbol.getSectionHeaderIndex () ),
          symbol.getName () );
      }
    }
  }
  
  private static String symbolType ( int bind, int type ) {
    switch ( type ) {
      case SymbolTableSection.Symbol.STT_NOTYPE: {
        return "";
      }
      case SymbolTableSection.Symbol.STT_OBJECT: {
        if ( bind == SymbolTableSection.Symbol.STB_LOCAL ) {
          return "o";
        } else {
          return "O";
        }
      }
      case SymbolTableSection.Symbol.STT_FUNC: {
        if ( bind == SymbolTableSection.Symbol.STB_LOCAL ) {
          return "t";
        } else {
          return "T";
        }
      }
      case SymbolTableSection.Symbol.STT_FILE: {
        return "f";
      }
      default: {
        return String.format ( "%1x%1x", bind, type );
      }
    }
  }
  
  private static String shndx ( int value ) {
    if ( Section.SHN_ABS == value ) {
      return "ABS";
    } else if ( Section.SHN_UNDEF == value ) {
      return "UNDEF";
    } else {
      return String.format ( "%5d", value );
    }
  }
  
  public static void printRelocationSection ( PrintStream out, ELF elf ) {
    if ( elf.getClassType () == 1 ) {
      SectionHeaderTable sectionHeaderTable = elf.getSectionHeaderTable ();
      SectionHeader sectionHeader = null;
      for ( int index = 0; index < sectionHeaderTable.getSize (); index++ ) {
        if ( sectionHeaderTable.get ( index ).getType () == Section.SHT_REL ) {
          sectionHeader = sectionHeaderTable.get ( index );
          break;
        }
      }
      RelocationSection relocationSection = sectionHeader.getSection ( RelocationSection.class );
      
      out.printf ( "Relocation Table '%s' contains %d entries:%n", relocationSection.getName (), relocationSection.getRecords ().size () );
      out.printf ( "%5s %8s %8s %-16s %8s %s%n",
        "Index",
        "Offset",
        "Sym ndx",
        "Type",
        "Sym Adds",
        "Name" );
      for ( int index = 0; index < relocationSection.getRecords ().size (); index++ ) {
        RelocationSection.Record record = relocationSection.getRecord ( index );
        out.printf ( "[%3d] %8x %8d %-16s %8x %s%n",
          index,
          record.getOffset (),
          record.getSymbolIndex (),
          relocationType ( record.getType () ),
          ((RelocationSectionDirect.RelocationRecordDirect)record).getSymbol ().getValue (),
          record.getSymbolName () );
      }
    }
  }
  
  private static String relocationType ( int value ) {
    switch ( value ) {
      case RelocationSection.R_SVM_NONE: {
        return "R_SVM_NONE";
      }
      case RelocationSection.R_SVM_CALL_I: {
        return "R_SVM_CALL_I";
      }
      case RelocationSection.R_SVM_LOAD_I: {
        return "R_SVM_LOAD_I";
      }
      case RelocationSection.R_SVM_MOVE_I: {
        return "R_SVM_MOVE_I";
      }
      case RelocationSection.R_SVM_JUMP_I: {
        return "R_SVM_JUMP_I";
      }
      case RelocationSection.R_SVM_JUMP_NZ_I: {
        return "R_SVM_JUMP_NZ_I";
      }
      case RelocationSection.R_SVM_8: {
        return "R_SVM_8";
      }
      case RelocationSection.R_SVM_16: {
        return "R_SVM_16";
      }
      case RelocationSection.R_SVM_24: {
        return "R_SVM_24";
      }
      default: {
        return String.format ( "%2x", value );
      }
    }
  }
}