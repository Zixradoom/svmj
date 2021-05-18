
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ELFRam implements ELF {
  
  private static final Logger LOGGER = LoggerFactory.getLogger ( ELFRam.class );
  
  private int classType;
  private int dataType;
  private int version;
  private int osabi;
  private int abiVersion;
  private int objectType;
  private int machine;
  private int objectVersion;
  // future size may be larger
  private int entryPoint;
  private int flags;
  private int hSize;
  private int phoff;
  private int phteSize;
  private int shteSize;
  private int sectionHeaderNameIndex;
  
  private ProgramHeaderTableRAM pht;
  private SectionHeaderTableRAM sht;
  
  //private ProgramHeaderTable;
  private List < AbstractSectionRAM > sections;
  private List < SegmentRAM > segments;
  
  public ELFRam () {
    classType = ELF.CLASS_32;
    dataType = ELF.DATA_BIG;
    version = ELF.VERSION_1;
    objectVersion = ELF.VERSION_1;
    osabi = 0;
    abiVersion = 0;
    objectType = ELF.OBJECT_REL;
    machine = 0;
    hSize = ELF.HEADER_SIZE_32;
    phoff = ELF.HEADER_SIZE_32;
    phteSize = ProgramHeaderTable.ENTRY_32_SIZE;
    shteSize = SectionHeaderTable.ENTRY_32_SIZE;
    
    sections = new ArrayList <> ();
    segments = new ArrayList <> ();
    
    pht = new ProgramHeaderTableRAM ( this );
    sht = new SectionHeaderTableRAM ( this );
    sections.add ( new SectionRAM ( this, null ) );
  }
  
  @Override
  public int getClassType () {
    return classType;
  }
  
  @Override
  public int getDataType () {
    return dataType;
  }
  
  @Override
  public int getVersion () {
    return version;
  }
  
  @Override
  public int getOSABI () {
    return osabi;
  }
  
  @Override
  public int getABIVersion () {
    return abiVersion;
  }
  
  @Override
  public int getObjectType () {
    return objectType;
  }
  
  public void setObjectType ( int type ) {
    this.objectType = type;
  }
  
  @Override
  public int getMachine () {
    return machine;
  }
  
  @Override
  public int getObjectVersion () {
    return objectVersion;
  }
  
  @Override
  public long getEntryPointAddress () {
    return entryPoint;
  }
  
  public void setEntryPointAddress ( long entryPoint ) {
    this.entryPoint = (int)entryPoint;
  }
  
  @Override
  public long getProgramHeaderTableOffset () {
    // by convetion the header is tightly packed
    // with the program header table
    // so we can just assume this value for now
    return phoff;
  }
  
  @Override
  public long getSectionHeaderTableOffset () {
    
    int programTableSize = getProgramHeaderEntryCount () * getProgramHeaderEntrySize ();
    
    int imageSize = 0;
    
    for ( AbstractSectionRAM s : sections ) {
      imageSize += s.getHeader ().getSize ();
    }
    
    return getHeaderSize () + programTableSize + imageSize;
  }
  
  private long getImageOffset () {
    long programTableSize = getProgramHeaderEntryCount () * getProgramHeaderEntrySize ();
    return getHeaderSize () + programTableSize;
  }
  
  @Override
  public int getFlags () {
    return flags;
  }
  
  @Override
  public int getHeaderSize () {
    return hSize;
  }
  
  @Override
  public int getProgramHeaderEntrySize () {
    return phteSize;
  }
  
  @Override
  public int getProgramHeaderEntryCount () {
    return segments.size ();
  }
  
  @Override
  public int getSectionHeaderEntrySize () {
    return shteSize;
  }
  
  @Override
  public int getSectionHeaderEntryCount () {
    return sections.size ();
  }
  
  @Override
  public int getHeaderNameIndex () {
    return sectionHeaderNameIndex;
  }
  
  public void setHeaderNameIndex ( int index ) {
    sectionHeaderNameIndex = index;
  }
  
  @Override
  public ProgramHeaderTable getProgramHeaderTable () {
    return pht;
  }
  
  List < SegmentRAM > getSegments () {
    return segments;
  }
  
  @Override
  public SectionHeaderTableRAM getSectionHeaderTable () {
    return sht;
  }
  
  List < ? extends AbstractSectionRAM > getSections () {
    return sections;
  }
  
  public SectionRAM createSection ( String name ) {
    SectionRAM s = new SectionRAM ( this, name );
    
    sections.add ( s );
    return s;
  }
  
  public int indexOfSection ( String name ) {
    return getSectionHeaderTable ().indexOf ( name );
  }
  
  public <T extends Section> T createSection ( String name, Class < T > clazz ) {
    
    AbstractSectionRAM temp = null;
    T returnValue = null;
    
    if ( clazz.isAssignableFrom ( StringTableSectionRAM.class ) || clazz.isAssignableFrom ( StringTableSection.class ) ) {
      temp = new StringTableSectionRAM ( this, name );
      returnValue = clazz.cast ( temp );
    } else if ( clazz.isAssignableFrom ( SymbolTableSectionRAM.class ) || clazz.isAssignableFrom ( SymbolTableSection.class ) ) {
      temp = new SymbolTableSectionRAM ( this, name );
      returnValue = clazz.cast ( temp );
    } else if ( clazz.isAssignableFrom ( RelocationSectionRAM.class ) || clazz.isAssignableFrom ( RelocationSection.class ) ) {
      temp = new RelocationSectionRAM ( this, name );
      returnValue = clazz.cast ( temp );
    } else {
      throw new IllegalArgumentException ( String.format ( "unrecognized type '%s'", clazz ) );
    }
    
    sections.add ( temp );
    
    return returnValue;
  }
  
  public ByteBuffer toBuffer () {
    
    int programTableSize = getProgramHeaderEntryCount () * getProgramHeaderEntrySize ();
    
    LOGGER.debug ( "start processing sections" );
    
    int imageSize = 0;
    
    for ( AbstractSectionRAM s : sections ) {
      imageSize += s.getHeader ().getSize ();
    }
    
    ByteBuffer sectionImage = ByteBuffer.allocate ( imageSize );
    
    long imageOffset = getImageOffset ();
    
    for ( AbstractSectionRAM s : sections ) {
      if ( s.getHeader ().getType () == Section.SHT_NULL ) {
        continue;
      }
      int offset = sectionImage.position ();
      s.getHeader ().setOffset ( offset + imageOffset );
      LOGGER.debug ( "processing section {}", s.getHeader ().getName () );
      ByteBuffer data = s.getData ();
      int size = data == null ? 0 : data.capacity ();
      LOGGER.trace ( "section size {} 0x{}", size, Integer.toHexString ( size ) );
      if ( data != null ) {
        sectionImage.put ( data );
      }
    }
    
    // set for reading
    sectionImage.flip ();
    
    // section header table
    ByteBuffer shtImage = ByteBuffer.allocate ( getSectionHeaderEntrySize () * getSectionHeaderEntryCount () );
    
    for ( AbstractSectionRAM s : sections ) {
      shtImage.put ( s.getHeader ().toBuffer ().flip () );
    }
    
    shtImage.flip ();
    
    LOGGER.debug ( "end processing sections" );
    
    // create header;
    
    ByteBuffer bb = ByteBuffer.allocate ( 0x4000 );
    LOGGER.trace ( "elf magic {} 0x{}", MAGIC, Integer.toHexString ( MAGIC ) );
    bb.putInt ( MAGIC );

    byte ct = (byte)classType;
    LOGGER.trace ( "elf class {} 0x{}", ct, Integer.toHexString ( ct ) );
    bb.put ( ct );

    byte dt = (byte)dataType;
    LOGGER.trace ( "elf data {} 0x{}", dt, Integer.toHexString ( dt ) );
    bb.put ( dt );
    
    byte v = (byte)version;
    LOGGER.trace ( "elf version {} 0x{}", v, Integer.toHexString ( v ) );
    bb.put ( v );
    
    byte osabiB = (byte)osabi;
    LOGGER.trace ( "elf osabi {} 0x{}", osabiB, Integer.toHexString ( osabiB ) );
    bb.put ( osabiB );
    
    byte abiVersionB = (byte)abiVersion;
    LOGGER.trace ( "elf abiVersion {} 0x{}", abiVersionB, Integer.toHexString ( abiVersionB ) );
    bb.put ( abiVersionB );
    
    byte[] pad = new byte[7];
    bb.put ( pad );
    
    short ot = (short)objectType;
    LOGGER.trace ( "object type {} 0x{}", ot, Integer.toHexString ( ot ) );
    bb.putShort ( ot );
    
    short machineS = (short)machine;
    LOGGER.trace ( "machine {} 0x{}", machineS, Integer.toHexString ( machineS ) );
    bb.putShort ( machineS );
    
    LOGGER.trace ( "object version {} 0x{}", version, Integer.toHexString ( version ) );
    bb.putInt ( version );
    
    if ( classType == CLASS_32 ) {
      LOGGER.trace ( "entry address {} 0x{}", entryPoint, Integer.toHexString ( entryPoint ) );
      bb.putInt ( entryPoint );
      // program header table offset
      LOGGER.trace ( "ProgramHeaderTableOffset {} 0x{}", getProgramHeaderTableOffset (), Long.toHexString ( getProgramHeaderTableOffset () ) );
      bb.putInt ( (int)getProgramHeaderTableOffset () );
      // section header table offset
      LOGGER.trace ( "SectionHeaderTableOffset {} 0x{}", getSectionHeaderTableOffset (), Long.toHexString ( getSectionHeaderTableOffset () ) );
      bb.putInt ( (int)getSectionHeaderTableOffset () );
      //
      LOGGER.trace ( "flags {} 0x{}", flags, Long.toHexString ( flags ) );
      bb.putInt ( flags );
      // header size 32 bit
      short ehs = (short) getHeaderSize ();
      LOGGER.trace ( "elf header size {} 0x{}", ehs, Integer.toHexString ( ehs ) );
      bb.putShort ( ehs );
      // program header table entry size
      bb.putShort ( (short) getProgramHeaderEntrySize () );
      // program header table entry count
      short phec = (short) getProgramHeaderEntryCount ();
      LOGGER.trace ( "program header entry count {} 0x{}", phec, Integer.toHexString ( phec ) );
      bb.putShort ( phec );
      // section header table entry size
      bb.putShort ( (short) getSectionHeaderEntrySize () );
      // section header table entry count
      short shec = (short) getSectionHeaderEntryCount ();
      LOGGER.trace ( "section header entry count {} 0x{}", shec, Integer.toHexString ( shec ) );
      bb.putShort ( shec );
      
      // section header table index for section ".shstrtab"
      short shstrndx = (short) getHeaderNameIndex ();
      LOGGER.trace ( "section header name string table section header index {} 0x{}", shstrndx, Integer.toHexString ( shstrndx ) );
      bb.putShort ( shstrndx );
    } else if ( classType == CLASS_64 ) {
      throw new UnsupportedOperationException ( "64 bit format not supported" );
    } else {
      throw new IllegalArgumentException ( String.format ( "invalid address size [%d]", classType ) );
    }
    
    // program table
    
    // image 
    bb.put ( sectionImage );
    
    // put section table
    bb.put ( shtImage );
    
    return bb;
  }
  
  public static ELF read ( Path file ) {
    throw new UnsupportedOperationException ( "not implemented" );
  }
}