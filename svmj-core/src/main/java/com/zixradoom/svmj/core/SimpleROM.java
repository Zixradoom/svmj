package com.zixradoom.svmj.core;

import java.nio.ByteBuffer;

public final class SimpleROM implements Device {
	
	private String name;
	private ByteBuffer memory;
	private long baseAddress;
	
	public SimpleROM ( String name, long baseAddress, ByteBuffer memory ) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.memory = memory;
	}

	@Override
	public long getBaseAddress () {
		return baseAddress;
	}

	@Override
	public int getMemorySize () {
		return memory.capacity ();
	}

	@Override
	public int readMemoryAddress ( long address ) {
		try {
		  return memory.getInt ( (int)address );
		} catch ( IndexOutOfBoundsException e ) {
			throw new AddressOutOfBoundsException ( address, e );
		}
	}

	@Override
	public void writeMemoryAddress ( long address, int value ) {
		throw new UnsupportedOperationException ( String.format ( "read only [%x]=%x", address, value ) );
	}
	
	@Override
	public String toString () {
		return "SimpleROM[baseAddress="+baseAddress+", memory="+memory+", name="+name+"]";
	}
}
