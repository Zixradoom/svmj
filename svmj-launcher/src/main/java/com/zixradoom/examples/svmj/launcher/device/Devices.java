
package com.zixradoom.examples.svmj.launcher.device;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import com.zixradoom.examples.svmj.core.Device;

public final class Devices {
	
	public static final String DEVICE_FILE_EXT_GLOB = "*.device";
	public static final String CLASS_PROPERTY = "class";
	
  public static List < Device > createDevices ( Path dir ) throws IOException, ClassNotFoundException {
  	List < Device > devices = new ArrayList <> ();
  	ServiceLoader < DeviceFactory > sl = ServiceLoader.load ( DeviceFactory.class );
  	
  	try ( DirectoryStream< Path > ds = Files.newDirectoryStream ( dir, DEVICE_FILE_EXT_GLOB ) ) {
  		for ( Path deviceFile : ds ) {
  			Properties properties = new Properties ();
  			try ( InputStream is = Files.newInputStream ( deviceFile ) ) {
  				properties.load ( is );
  			}
  			String clazzName = properties.getProperty ( CLASS_PROPERTY );
  			Class < ? > clazz = Class.forName ( clazzName );
  			
    		for ( DeviceFactory df : sl ) {
    			if ( df.getSupportedClass ().equals ( clazz ) ) {
    				Device d = df.createDevice ( deviceFile, properties );
    				devices.add ( d );
    			}
    		}
  		}
  	}
  	return devices;
  }
}
