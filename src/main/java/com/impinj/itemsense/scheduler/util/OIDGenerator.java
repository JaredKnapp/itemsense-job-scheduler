package com.impinj.itemsense.scheduler.util;

public class OIDGenerator {

	private static long oid = 0;

	public static long longNext() {
	    return ++oid;
	}

	public static String next() {
	    return new StringBuffer( "id").append( String.valueOf( longNext())).toString();
	}
}
