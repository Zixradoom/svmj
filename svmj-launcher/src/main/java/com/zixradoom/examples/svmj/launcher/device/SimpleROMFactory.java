
package com.zixradoom.examples.svmj.launcher.device;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import com.zixradoom.examples.svmj.core.Device;
import com.zixradoom.examples.svmj.core.ProgramBuilder;
import com.zixradoom.examples.svmj.core.SimpleROM;

public final class SimpleROMFactory implements DeviceFactory {

	@Override
	public Device createDevice ( Path propsFile, Properties properties ) {
		long baseAddress = Long.valueOf ( properties.getProperty ( "base.address" ) );
		String name = properties.getProperty ( "name" );
		String sourceFile = properties.getProperty ( "source" );
		
		ByteBuffer rom = null;
		Path source = propsFile.getParent ().resolve ( sourceFile );
		try ( InputStream is = Files.newInputStream ( source ) ) {
			ProgramBuilder pb = new ProgramBuilder ();
			rom = pb.getProgram ();
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}
		
		return new SimpleROM ( name, (int)baseAddress, rom );
	}

	@Override
	public Class< ? extends Device > getSupportedClass () {
		return SimpleROM.class;
	}
}
