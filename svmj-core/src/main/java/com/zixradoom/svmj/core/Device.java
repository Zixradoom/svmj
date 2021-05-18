package com.zixradoom.svmj.core;

public interface Device {
	long getBaseAddress ();
	int getMemorySize ();
	int readMemoryAddress ( long address );
	void writeMemoryAddress ( long address, int value );
}
