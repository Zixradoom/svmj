
package com.zixradoom.svmj.launcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.zixradoom.svmj.core.Device;
import com.zixradoom.svmj.core.SVMJ;
import com.zixradoom.svmj.launcher.device.Devices;

public final class Driver {
  public static void main ( String[] args ) throws ClassNotFoundException, IOException {
    Path deviceDir = Paths.get ( args[ 0 ] );
    List < Device > devices = Devices.createDevices ( deviceDir );
    
    SVMJ vm = new SVMJ ( 16, 0x200000, devices );
    
    while ( !(vm.getStatus ().contains ( SVMJ.Status.HALT ) || vm.getStatus ().contains ( SVMJ.Status.ERROR ) ) ) {
    	vm.step ();
    }
  }
}
