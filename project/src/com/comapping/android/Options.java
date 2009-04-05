package com.comapping.android;

import org.apache.http.HttpHost;

public class Options {
	final public static String SERVER = "http://go.comapping.com/cgi-bin/comapping.n";

	final public static int RESULT_CHAIN_CLOSE = 24313;

	// Fake server options
	// Folder with *.comap files. If you open comap with id 1234 server returned
	// content of "id.comap" file in COMAP_FILE_SERVER
	final public static boolean FAKE_SERVER = false;
	final public static String COMAP_FILE_SERVER = "/sdcard/";

	// Proxy server options
	final public static boolean USE_PROXY = false;
	final public static HttpHost PROXY_HOST = new HttpHost("217.197.3.157", 8089, "http");
}
