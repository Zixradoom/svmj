
package com.zixradoom.examples.svmj.core.elf;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import com.zixradoom.examples.svmj.core.elf.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ELFDirectTests {
  
  @Test
  public void mapELFCheckByteOrderLittle () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x01 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 1, elf.getDataType (), "elf byte order not set to little" );
  }
  
  @Test
  public void mapELFCheckByteOrderBig () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 2, elf.getDataType (), "elf byte order not set to big" );
  }
  
  @Test
  public void mapELFCheckByteOrderOther () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x03 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 3, elf.getDataType (), "elf byte order not set to other" );
  }
  
  @Test
  public void mapELFCheckAddressSize32 () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 1, elf.getClassType (), "elf address size not set to 32" );
  }
  
  @Test
  public void mapELFCheckAddressSize64 () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x02 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 2, elf.getClassType (), "elf address size not set to 64" );
  }
  
  @Test
  public void mapELFCheckAddressSizeOther () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x03 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 3, elf.getClassType (), "elf address size not set to other" );
  }
  
  @Test
  public void mapELFCheckVersionNumber () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x03 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 1, elf.getVersion (), "elf version is not set to 1" );
  }
  
  @Test
  public void mapELFCheckTypeRel () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 1, elf.getObjectType (), "elf object type is not set to 1" );
  }
  
  @Test
  public void mapELFCheckObjectVersion () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 1, elf.getObjectVersion (), "elf object version is not set to 1" );
  }
  
  @Test
  public void mapELFCheckEntryPoint3000 () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 3000, elf.getEntryPointAddress (), "elf entry address is not set to 3000" );
  }
  
  @Test
  public void mapELFCheckProgramHeaderTabelOffset32Bit () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 0x34, elf.getProgramHeaderTableOffset (), "elf Program Header Table Offset is not set to 0x34" );
  }
  
  @Test
  public void mapELFCheckSectionHeaderTabelOffset32Bit () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    dos.writeInt ( 0x400 );
    dos.writeInt ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 0x400, elf.getSectionHeaderTableOffset (), "elf Section Header Table Offset is not set to 0x400" );
  }
  
  @Test
  public void mapELFCheckHeaderSize32Bit () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    dos.writeInt ( 0x400 );
    dos.writeInt ( 0x00 );
    dos.writeShort ( 0x52 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 0x52, elf.getHeaderSize (), "elf Header Size is not set to 0x52" );
  }
  
  @Test
  public void mapELFProgramHeaderTabelEntrySize32Bit () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    dos.writeInt ( 0x400 );
    dos.writeInt ( 0x00 );
    dos.writeShort ( 0x52 );
    dos.writeShort ( 0x20 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 0x20, elf.getProgramHeaderEntrySize (), "elf Program Header Entry Size is not set to 0x20" );
  }
  
  @Test
  public void mapELFProgramHeaderTabelEntrySize64Bit () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    dos.writeInt ( 0x400 );
    dos.writeInt ( 0x00 );
    dos.writeShort ( 0x52 );
    dos.writeShort ( 0x38 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 0x38, elf.getProgramHeaderEntrySize (), "elf Program Header Entry Size is not set to 0x38" );
  }
  
  @Test
  public void mapELFProgramHeaderTabelEntryCount32Bit () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    dos.writeInt ( 0x400 );
    dos.writeInt ( 0x00 );
    dos.writeShort ( 0x52 );
    dos.writeShort ( 0x20 );
    dos.writeShort ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 0x00, elf.getProgramHeaderEntryCount (), "elf Program Header Entry Count is not set to 0x00" );
  }
  
  @Test
  public void mapELFSectionHeaderTabelEntrySize32Bit () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    dos.writeInt ( 0x400 );
    dos.writeInt ( 0x00 );
    dos.writeShort ( 0x52 );
    dos.writeShort ( 0x20 );
    dos.writeShort ( 0x38 );
    dos.writeShort ( 0x28 );
    dos.writeShort ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 0x28, elf.getSectionHeaderEntrySize (), "elf Section Header Entry Size is not set to 0x28" );
  }
  
  @Test
  public void mapELFSectionHeaderTabelEntrySize64Bit () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    dos.writeInt ( 0x400 );
    dos.writeInt ( 0x00 );
    dos.writeShort ( 0x52 );
    dos.writeShort ( 0x38 );
    dos.writeShort ( 0x00 );
    dos.writeShort ( 0x40 );
    dos.writeShort ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 0x40, elf.getSectionHeaderEntrySize (), "elf Section Header Entry Size is not set to 0x40" );
  }
  
  @Test
  public void mapELFSectionHeaderTabelEntryCount32Bit () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    dos.writeInt ( 0x400 );
    dos.writeInt ( 0x00 );
    dos.writeShort ( 0x52 );
    dos.writeShort ( 0x20 );
    dos.writeShort ( 0x00 );
    dos.writeShort ( 0x28 );
    dos.writeShort ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    assertEquals ( 0x00, elf.getSectionHeaderEntryCount (), "elf Section Header Entry Count is not set to 0x00" );
  }
  
  @Test
  public void mapELFProgramHeaderIndex1 () throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream ();
    DataOutputStream dos = new DataOutputStream ( baos );
    dos.write ( 0x7F );
    dos.write ( 0x45 );
    dos.write ( 0x4C );
    dos.write ( 0x46 );
    dos.write ( 0x01 );
    dos.write ( 0x02 );
    dos.write ( 0x01 );
    dos.write ( 0x00 );
    dos.write ( 0x00 );
    dos.write ( new byte[ 7 ] );
    dos.writeShort ( 0x01 );
    dos.writeShort ( 0x00 );
    dos.writeInt ( 0x01 );
    dos.writeInt ( 3000 );
    dos.writeInt ( 0x34 );
    dos.writeInt ( 0x400 );
    dos.writeInt ( 0x00 );
    dos.writeShort ( 0x52 );
    dos.writeShort ( 0x20 );
    dos.writeShort ( 0x00 );
    dos.writeShort ( 0x28 );
    dos.writeShort ( 0x00 );
    
    ByteBuffer bb = ByteBuffer.wrap ( baos.toByteArray () );
    ELFDirect elf = new ELFDirect ( bb );
    
    assertThrows ( IndexOutOfBoundsException.class, () -> {
        elf.getProgramHeaderTable ().get ( 1 );
      } );
  }
}