package com.comapping.android;

public class Options {
	final public static String SERVER = "http://go.comapping.com/cgi-bin/comapping.n";

	// Fake server settings
	final public static boolean FAKE_SERVER = false;
	// Folder with *.comap files. If you open comap with id 1234 server returned
	// content of "id.comap" file in COMAP_FILE_SERVER
	final public static String COMAP_FILE_SERVER = "/sdcard/";

	final public static int RESULT_CHAIN_CLOSE = 24313;
}
