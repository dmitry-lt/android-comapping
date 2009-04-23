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
	
	public static final boolean DEBUG_RENDERING = true;
}
