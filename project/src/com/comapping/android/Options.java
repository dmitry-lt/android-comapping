package com.comapping.android;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class Options {
	public static final String SERVER = "http://go.comapping.com/cgi-bin/comapping.n";

	public static final int RESULT_CHAIN_CLOSE = 24313;

	// Proxy server options
	public static final boolean USE_PROXY = false;
	public static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("217.197.3.157", 8089));

	// Address to downloading attachment files
	public static final String DOWNLOAD_SERVER = "http://upload.comapping.com/";
	public static final String DEFAULT_DOWNLOAD_FOLDER = "\\sdcard\\comapping\\download";
	
	public static final boolean DEBUG_RENDERING = true;
	public static final boolean DEBUG_LOG = true;
	
}
