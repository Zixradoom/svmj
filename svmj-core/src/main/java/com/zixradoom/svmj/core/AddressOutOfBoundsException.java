package com.zixradoom.svmj.core;

public class AddressOutOfBoundsException extends RuntimeException {

	private static final long serialVersionUID = 8846825018719615294L;

	public AddressOutOfBoundsException ( long address ) {
		this ( address, null );
	}
	
	public AddressOutOfBoundsException ( long address, Throwable cause ) {
		super ( String.format ( "%xH", address ), cause );
	}
}
