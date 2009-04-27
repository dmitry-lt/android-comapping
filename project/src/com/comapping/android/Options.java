package com.comapping.android;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class Options {
	final public static String SERVER = "http://go.comapping.com/cgi-bin/comapping.n";

	final public static int RESULT_CHAIN_CLOSE = 24313;

	// Proxy server options
	final public static boolean USE_PROXY = false;
	final public static Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("217.197.3.157", 8089));

	// Address to downloading attachment files
	final public static String DOWNLOAD_SERVER = "http://upload.comapping.com/";
	final public static String DEFAULT_DOWNLOAD_FOLDER = "\\sdcard\\comapping\\download";
	
	public static final boolean DEBUG_RENDERING = true;
}
