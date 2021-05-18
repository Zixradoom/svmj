
package com.zixradoom.examples.svmj.launcher.device;

import java.nio.file.Path;
import java.util.Properties;

import com.zixradoom.examples.svmj.core.Device;

public interface DeviceFactory {
	Device createDevice ( Path propsFile, Properties properties );
  Class< ? extends Device > getSupportedClass ();
}
