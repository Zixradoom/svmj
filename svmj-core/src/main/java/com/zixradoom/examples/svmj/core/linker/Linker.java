

package com.zixradoom.examples.svmj.core.linker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import java.util.Objects;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import com.zixradoom.examples.svmj.core.OperationCode;
import com.zixradoom.examples.svmj.core.elf.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Linker {
  
  private static final Logger LOGGER = LoggerFactory.getLogger ( Linker.class );
  
  public static final String DEFAULT_ENTRY_SYMBOL = "_start";
  
  private List < Path > input;
  private Path out;
  private List < ELF > objects;
  private int physicalBaseAddress;
  private boolean entryCheck;
  private boolean undefinedSymbolCheck;
  private int buildType;
  
  public Linker ( ELF object ) {
    this ( List.of ( object ) );
  }
  
  public Linker ( List < ELF > objects ) {
    this.objects = new ArrayList <> ( Objects.requireNonNull ( objects, "objects is null" ) );
    this.objects = Collections.unmodifiableList ( this.objects );
    
    if ( objects.isEmpty () ) {
      throw new IllegalArgumentException ( "empty object list" );
    }
    
    this.input = Collections.emptyList ();
  }
  
  public Linker ( List < Path > input, Path out ) throws IOException {
    this ( processInput ( input ) );
    this.input = new ArrayList <> ( input );
    this.out = out;
  }
  
  private static List < ELF > processInput ( List < Path > input ) throws IOException {
    List < ELF > objects = new ArrayList <> ();
    for ( Path p : input ) {
      objects.add ( ELFUtil.read ( p ) );
    }
    return objects;
  }
  
  public int getPhysicalBaseAddress () {
    return physicalBaseAddress;
  }
  
  public void setPhysicalBaseAddress ( int address ) {
    this.physicalBaseAddress = address;
  }
  
  public void link () throws IOException {
    
    if ( Files.exists ( out ) ) {
      FileTime outTime = (FileTime)Files.getAttribute ( out, "lastModifiedTime" );
      
      boolean build = false;
      for ( Path objFile : input ) {
        FileTime objFileTime = (FileTime)Files.getAttribute ( objFile, "lastModifiedTime" );
        
        if ( outTime.compareTo ( objFileTime ) < 0 ) {
          build = true;
          LOGGER.info ( "object file is newer '{}'", objFile );
        }
      }
      if ( !build ) {
        return;
      }
    }
    
    ELFRam elf = (ELFRam)linkV2 ();
    
    ByteBuffer bb = elf.toBuffer ();
    byte[] dat = new byte[ bb.position () ];
    bb.flip ().get ( dat );
    
    Files.createDirectories ( out.getParent () );
    
    Files.write ( out, dat );
  }
  
  public ELF linkV2 () {
    // verify
    for ( ELF elf : objects ) {
      verify ( elf, ELF.ELFClass.CLASS_32.getValue (), ELF.ELFData.DATA_MSB.getValue (), 1, 1 );
    }
    
    if ( entryCheck ) {
      ELF entry = getEntry ();
      if ( entry == null ) {
        throw new IllegalArgumentException ( "no entry point specified" );
      }
    }
    
    // ** NOT **
    // we only know how to create static images for now
    
    ELFRam elfProgram = new ELFRam ();
    elfProgram.setObjectType ( ELF.OBJECT_EXEC );
    
    SectionRAM txt = elfProgram.createSection ( ".text" );
    SymbolTableSectionRAM stsr = elfProgram.createSection ( ".symtab", SymbolTableSectionRAM.class );
    RelocationSectionRAM rsr = elfProgram.createSection ( ".rel.text", RelocationSectionRAM.class );
    StringTableSectionRAM strtab = elfProgram.createSection ( ".strtab", StringTableSectionRAM.class );
    StringTableSectionRAM shstrtab = elfProgram.createSection ( ".shstrtab", StringTableSectionRAM.class );
    
    // ** image processing **
    // .text
    //
    
    int textImageSize = 0;
    
    for ( ELF object : objects ) {
      Section text = getTextSection ( object );
      if ( text != null ) {
        textImageSize += text.getSize ();
      }
    }
    
    ByteBuffer bb = ByteBuffer.allocate ( textImageSize );
    
    int[] imageStartOffsets = new int[ objects.size () ];
    
    for ( int index = 0; index < objects.size (); index++ ) {
      ELF object = objects.get ( index );
      imageStartOffsets[ index ] = bb.position ();
      
      // text section
      SectionDirect text = (SectionDirect)getTextSection ( object );
      ByteBuffer textData = text.getData ();
      textData.position ( 0 );
      textData.limit ( textData.capacity () );
      
      bb.put ( textData );
      
      // in the future we will need to handle other sections like .bss
    }
    
    // ** symbol processing **
    //
    // symbol processing currently has 1 scope that all
    // symbols share in the future we need to treat the
    // scopes differently
    
    // process all locals first
    for ( int index = 0; index < objects.size (); index++ ) {
      ELF object = objects.get ( index );
      SymbolTableSection sts = getSymbolTableSection ( object );
      
      boolean hasLocal = false;
      for ( SymbolTableSection.Symbol oldSymbol : sts.getSymbols () ) {
        if ( oldSymbol.getType () != SymbolTableSection.STT_FILE && oldSymbol.getBind () == SymbolTableSection.STB_LOCAL && oldSymbol.getSectionHeaderIndex () != Section.SHN_UNDEF ) {
          LOGGER.debug ( "local non file {}", oldSymbol );
          hasLocal = true;
          break;
        }
      }
      
      if ( hasLocal ) {
        for ( SymbolTableSection.Symbol oldSymbol : sts.getSymbols () ) {
          
          if ( oldSymbol.getBind () != SymbolTableSection.STB_LOCAL || oldSymbol.getSectionHeaderIndex () == Section.SHN_UNDEF ) {
            continue;
          }
          
          LOGGER.debug ( "old {}", oldSymbol );
          
          SymbolTableSectionRAM.Symbol newSymbol = stsr.getSymbol ( oldSymbol.getName () );
          if ( newSymbol == null ) {
            newSymbol = stsr.newSymbol ( oldSymbol.getName () );
          }
          
          if ( oldSymbol.getSectionHeaderIndex () == Section.SHN_ABS ) {
            if ( newSymbol.getSectionHeaderIndex () == Section.SHN_UNDEF ) {
              processSymbolABS ( oldSymbol, newSymbol );
            } else {
              // the symbol is already defined so we have a duplicate
              throw new IllegalArgumentException ( String.format ( "symbol '%s' is already defined", oldSymbol.getName () ) );
            }
          } else if ( oldSymbol.getSectionHeaderIndex () != Section.SHN_UNDEF ) {
            if ( newSymbol.getSectionHeaderIndex () == Section.SHN_UNDEF ) {
              processSymbol ( oldSymbol, newSymbol, imageStartOffsets[ index ], physicalBaseAddress );
            } else {
              // the symbol is already defined so we have a duplicate
              throw new IllegalArgumentException ( String.format ( "symbol '%s' is already defined", oldSymbol.getName () ) );
            }
          }
        }
      }
    }
    
    // process all globals second
    for ( int index = 0; index < objects.size (); index++ ) {
      ELF object = objects.get ( index );
      SymbolTableSection sts = getSymbolTableSection ( object );
      
      for ( SymbolTableSection.Symbol oldSymbol : sts.getSymbols () ) {
        
        if ( oldSymbol.getBind () != SymbolTableSection.STB_GLOBAL ) {
          continue;
        }
        
        LOGGER.debug ( "old {}", oldSymbol );
        
        SymbolTableSectionRAM.Symbol newSymbol = stsr.getSymbol ( oldSymbol.getName () );
        if ( newSymbol == null ) {
          newSymbol = stsr.newSymbol ( oldSymbol.getName () );
        }
        
        if ( oldSymbol.getSectionHeaderIndex () == Section.SHN_ABS ) {
          if ( newSymbol.getSectionHeaderIndex () == Section.SHN_UNDEF ) {
            processSymbolABS ( oldSymbol, newSymbol );
          } else {
            // the symbol is already defined so we have a duplicate
            throw new IllegalArgumentException ( String.format ( "symbol '%s' is already defined", oldSymbol.getName () ) );
          }
        } else if ( oldSymbol.getSectionHeaderIndex () != Section.SHN_UNDEF ) {
          if ( newSymbol.getSectionHeaderIndex () == Section.SHN_UNDEF ) {
            processSymbol ( oldSymbol, newSymbol, imageStartOffsets[ index ], physicalBaseAddress );
          } else {
            // the symbol is already defined so we have a duplicate
            throw new IllegalArgumentException ( String.format ( "symbol '%s' is already defined", oldSymbol.getName () ) );
          }
        }
      }
    }
    
    // ** relocation processing **
    
    for ( int index = 0; index < objects.size (); index++ ) {
      ELF object = objects.get ( index );
      
      RelocationSection rs = getRelocationSection ( object );
      
      for ( RelocationSection.Record oldRecord : rs.getRecords () ) {
        LOGGER.debug ( "{}", oldRecord );
        // we need to create new records and update there location
        RelocationSectionRAM.RelocationRecord newRecord = rsr.newRecord ();
        SymbolTableSectionRAM.Symbol newSymbol = stsr.getSymbol ( oldRecord.getSymbolName () );
        if ( newSymbol == null ) {
          SymbolTableSection.Symbol oldSymbol = ((RelocationSectionDirect.RelocationRecordDirect)oldRecord).getSymbol ();
          newSymbol = stsr.newSymbol ( oldSymbol.getName () );
          
          if ( oldSymbol.getSectionHeaderIndex () != Section.SHN_UNDEF ) {
            processSymbol ( oldSymbol, newSymbol, imageStartOffsets[ index ], physicalBaseAddress );
          }
        }
        
        int newOffset = (int)oldRecord.getOffset () + imageStartOffsets[ index ] + physicalBaseAddress;
        newRecord.setSymbol ( newSymbol );
        newRecord.setOffset ( newOffset );
        newRecord.setType ( oldRecord.getType () );
      }
    }
    
    for ( RelocationSectionRAM.RelocationRecord rr : rsr.getRecords () ) {
      int type = rr.getType ();
      switch ( type ) {
        case RelocationSection.R_SVM_8:{
          int offset = (int)(rr.getOffset () - physicalBaseAddress);
          int instructionOld = bb.getInt ( offset );
          int instructionNew = instructionOld & 0xFFFFFF00;
          instructionNew = instructionNew | ( 0xFF & (int)rr.getSymbol ().getValue () );
          bb.putInt ( offset, instructionNew );
          LOGGER.trace ( "relocation: [{}] 0x{} -> 0x{}", Integer.toHexString ( offset ), Integer.toHexString ( instructionOld ), Integer.toHexString ( instructionNew ) );
          break;
        }
        case RelocationSection.R_SVM_MOVE_I:
        case RelocationSection.R_SVM_LOAD_I:
        case RelocationSection.R_SVM_16:{
          int offset = (int)(rr.getOffset () - physicalBaseAddress);
          int instructionOld = bb.getInt ( offset );
          int instructionNew = instructionOld & 0xFFFF0000;
          instructionNew = instructionNew | ( 0xFFFF & (int)rr.getSymbol ().getValue () );
          bb.putInt ( offset, instructionNew );
          LOGGER.trace ( "relocation: [{}] 0x{} -> 0x{}", Integer.toHexString ( offset ), Integer.toHexString ( instructionOld ), Integer.toHexString ( instructionNew ) );
          break;
        }
        case RelocationSection.R_SVM_CALL_I:
        case RelocationSection.R_SVM_JUMP_I:
        case RelocationSection.R_SVM_JUMP_NZ_I:
        case RelocationSection.R_SVM_24:{
          int offset = (int)(rr.getOffset () - physicalBaseAddress);
          LOGGER.trace ( "offset: 0x{} -> 0x{}", Long.toHexString ( rr.getOffset () ), Integer.toHexString ( offset ) );
          int instructionOld = bb.getInt ( offset );
          int instructionNew = instructionOld & 0xFF000000;
          instructionNew = instructionNew | ( 0xFFFFFF & (int)rr.getSymbol ().getValue () );
          bb.putInt ( offset, instructionNew );
          LOGGER.trace ( "relocation: [{}] 0x{} -> 0x{}", Integer.toHexString ( offset ), Integer.toHexString ( instructionOld ), Integer.toHexString ( instructionNew ) );
          break;
        }
        case RelocationSection.R_SVM_NONE: {
          // no op
          break;
        }
        default: {
          throw new UnsupportedOperationException ( String.format ( "not implemented: '%d'", type ) );
        }
      }
    }
    
    // ** verification checks **
    
    if ( undefinedSymbolCheck ) {
      Set < String > undefined = new TreeSet <> ();
      for ( RelocationSectionRAM.RelocationRecord record : rsr.getRecords () ) {
        if ( record.getSymbol ().getSectionHeaderIndex () == Section.SHN_UNDEF ) {
          undefined.add ( record.getSymbolName () );
        }
      }
      
      if ( !undefined.isEmpty () ) {
        StringBuilder message = new StringBuilder ();
        for ( String symbol : undefined ) {
          message.append ( String.format ( "symbol '%s' is undefined%n", symbol ) );
        }
        throw new IllegalArgumentException ( message.toString () );
      }
    }
    
    if ( stsr.getSymbol ( DEFAULT_ENTRY_SYMBOL ) != null ) {
      elfProgram.setEntryPointAddress ( stsr.getSymbol ( DEFAULT_ENTRY_SYMBOL ).getValue () );
    }
    
    // ** find first global symbol **
    int indexOfFirstGlobal = 0;
    for ( int index = 0; index < stsr.getSymbols ().size (); index++ ) {
      if ( stsr.getSymbols ().get ( index ).getBind () == SymbolTableSection.STB_GLOBAL ) {
        indexOfFirstGlobal = index;
        break;
      }
    }
    
    // ** file creation **
    // ** NOTE **
    // we need to finish implementing the elf api
    // so that we no longer need to do this
    
    // ** index 1
    txt.getHeader ().setType ( Section.SHT_PROGBITS );
    txt.getHeader ().setFlags ( Section.SHF_ALLOC | Section.SHF_EXECINSTR );
    txt.getHeader ().setVritualAddress ( physicalBaseAddress );
    // size of an instruction
    txt.getHeader ().setEntrySize ( 4 );
    txt.getHeader ().setNameOffset ( shstrtab.interString ( txt.getName () ) );
    txt.setData ( bb.flip () );
    
    // ** index 2
    stsr.getHeader ().setLink ( elfProgram.indexOfSection ( strtab.getName () ) );
    stsr.getHeader ().setInfo ( stsr.getIndexOfFirstGlobal () );
    stsr.getHeader ().setNameOffset ( shstrtab.interString ( stsr.getName () ) );
    
    for ( SymbolTableSectionRAM.Symbol symbol : stsr.getSymbols () ) {
      symbol.setNameOffset ( strtab.interString ( symbol.getName () ) );
    }
    
    // ** sh index 3
    rsr.getHeader ().setLink ( elfProgram.indexOfSection ( stsr.getName () ) );
    rsr.getHeader ().setInfo ( elfProgram.indexOfSection ( txt.getName () ) );
    rsr.getHeader ().setNameOffset ( shstrtab.interString ( rsr.getName () ) );
        
    // ** sh index 4
    strtab.getHeader ().setFlags ( (int)strtab.getFlags () | Section.SHF_ALLOC );
    strtab.getHeader ().setNameOffset ( shstrtab.interString ( strtab.getName () ) );
    
    // ** sh index 5
    shstrtab.getHeader ().setNameOffset ( shstrtab.interString ( shstrtab.getName () ) );
    
    elfProgram.setHeaderNameIndex ( elfProgram.indexOfSection ( shstrtab.getName () ) );
    
    return elfProgram;
  }
  
  private static void processSymbol ( SymbolTableSection.Symbol oldSymbol, SymbolTableSectionRAM.Symbol newSymbol, int textImageStart, int physicalBaseAddress ) {
    newSymbol.setValue ( oldSymbol.getValue () + textImageStart + physicalBaseAddress );
    newSymbol.setSectionHeaderIndex ( oldSymbol.getSectionHeaderIndex () );
    newSymbol.setSize ( oldSymbol.getSize () );
    newSymbol.setBind ( oldSymbol.getBind () );
    newSymbol.setType ( oldSymbol.getType () );
    newSymbol.setOther ( oldSymbol.getOther () );
  }
  
  private static void processSymbolABS ( SymbolTableSection.Symbol oldSymbol, SymbolTableSectionRAM.Symbol newSymbol ) {
    newSymbol.setValue ( oldSymbol.getValue () );
    newSymbol.setSectionHeaderIndex ( oldSymbol.getSectionHeaderIndex () );
    newSymbol.setSize ( oldSymbol.getSize () );
    newSymbol.setBind ( oldSymbol.getBind () );
    newSymbol.setType ( oldSymbol.getType () );
    newSymbol.setOther ( oldSymbol.getOther () );
  }
  
  private void verify ( ELF elf, int classType, int dataType, int elfVersion, int objectVersion ) {
    if ( elf.getClassType () != classType ) {
      String typeActual = elf.getClassType () == ELF.CLASS_32 ? "CLASS_32" : elf.getClassType () == ELF.CLASS_64 ? "CLASS_32" : "CLASS_NONE";
      String typeExpected = elf.getClassType () == ELF.CLASS_32 ? "CLASS_32" : elf.getClassType () == ELF.CLASS_64 ? "CLASS_32" : "CLASS_NONE";
      String msg = String.format ( "expected classType %s(%d) but was %s(%d)", typeExpected, classType, typeActual, elf.getClassType () );
      throw new IllegalArgumentException ( msg );
    }
    
    if ( elf.getDataType () != dataType ) {
      String typeActual = elf.getDataType () == ELF.DATA_LITTLE ? "DATA_LITTLE" : elf.getClassType () == ELF.DATA_BIG ? "DATA_BIG" : "DATA_NONE";
      String typeExpected = elf.getDataType () == ELF.DATA_LITTLE ? "DATA_LITTLE" : elf.getClassType () == ELF.DATA_BIG ? "DATA_BIG" : "DATA_NONE";
      String msg = String.format ( "expected dataType %s(%d) but was %s(%d)", typeExpected, classType, typeActual, elf.getDataType () );
      throw new IllegalArgumentException ( msg );
    }
    
    if ( elf.getVersion () != elfVersion ) {
      String msg = String.format ( "expected elf version %d but was %d", elfVersion, elf.getVersion () );
      throw new IllegalArgumentException ( msg );
    }
    
    if ( elf.getObjectVersion () != objectVersion ) {
      String msg = String.format ( "expected object version %d but was %d", objectVersion, elf.getObjectVersion () );
      throw new IllegalArgumentException ( msg );
    }
  }
  
  private ELF getEntry () {
    ELF entry = null;
    for ( ELF elf : objects ) {
      SectionHeaderTable sht = elf.getSectionHeaderTable ();
      SymbolTableSection sts = getSymbolTableSection ( elf );
      if ( sts != null ) {
        SymbolTableSection.Symbol symbol = sts.getSymbol ( DEFAULT_ENTRY_SYMBOL );
        if ( symbol != null && symbol.getBind () == Section.STB_GLOBAL ) {
          if ( entry == null ) {
            entry = elf;
          } else {
            throw new IllegalArgumentException ( String.format ( "more than 1 '%s' symbol", DEFAULT_ENTRY_SYMBOL ) );
          }
        }
      }
    }
    return entry;
  }
  
  private static SymbolTableSection getSymbolTableSection ( ELF object ) {
    SectionHeaderTable sht = object.getSectionHeaderTable ();
    for ( int index = 1; index < sht.getSize (); index++ ) {
      
      SectionHeader sh = sht.get ( index );
      
      LOGGER.trace ( "section header [{}] {} {} {} {} {} {} {} {} {}", index, sh.getNameOffset (), sh.getType (), sh.getFlags (), sh.getVirtualAddress (), sh.getOffset (), sh.getSize (), sh.getLink (), sh.getInfo (), sh.getAddressAlign (), sh.getEntrySize () );
      LOGGER.trace ( "section header name offset {} 0x{} '{}'", sh.getNameOffset (), Integer.toHexString ( sh.getNameOffset () ), sh.getName () );
      
      if ( ".symtab".equals ( sh.getName () ) ) {
        return sh.getSection ( SymbolTableSection.class );
      }
    }
    return null;
  }
  
  private static RelocationSection getRelocationSection ( ELF object ) {
    SectionHeaderTable sht = object.getSectionHeaderTable ();
    for ( int index = 1; index < sht.getSize (); index++ ) {
      
      SectionHeader sh = sht.get ( index );
      
      LOGGER.trace ( "section header [{}] {} {} {} {} {} {} {} {} {}", index, sh.getNameOffset (), sh.getType (), sh.getFlags (), sh.getVirtualAddress (), sh.getOffset (), sh.getSize (), sh.getLink (), sh.getInfo (), sh.getAddressAlign (), sh.getEntrySize () );
      LOGGER.trace ( "section header name offset {} 0x{} '{}'", sh.getNameOffset (), Integer.toHexString ( sh.getNameOffset () ), sh.getName () );
      
      if ( ".rel.text".equals ( sh.getName () ) ) {
        return sh.getSection ( RelocationSection.class );
      }
    }
    return null;
  }
  
  private static Section getTextSection ( ELF object ) {
    SectionHeaderTable sht = object.getSectionHeaderTable ();
    for ( int index = 1; index < sht.getSize (); index++ ) {
      
      SectionHeader sh = sht.get ( index );
      
      LOGGER.trace ( "section header [{}] {} {} {} {} {} {} {} {} {}", index, sh.getNameOffset (), sh.getType (), sh.getFlags (), sh.getVirtualAddress (), sh.getOffset (), sh.getSize (), sh.getLink (), sh.getInfo (), sh.getAddressAlign (), sh.getEntrySize () );
      LOGGER.trace ( "section header name offset {} 0x{} '{}'", sh.getNameOffset (), Integer.toHexString ( sh.getNameOffset () ), sh.getName () );
      
      if ( ".text".equals ( sh.getName () ) ) {
        return sh.getSection ();
      }
    }
    return null;
  }
}
