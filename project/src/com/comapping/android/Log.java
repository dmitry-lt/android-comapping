package com.comapping.android;

public class Log {
	private static final String applicationTag = "Comapping";
	
	public static void v(String module, String msg) {
		android.util.Log.v(applicationTag, module + ": " + msg);
	}
	
	public static void d(String module, String msg) {
		android.util.Log.d(applicationTag, module + ": " + msg);
	}
	
	public static void i(String module, String msg) {
		android.util.Log.i(applicationTag, module + ": " + msg);
	}
	
	public static void w(String module, String msg) {
		android.util.Log.w(applicationTag, module + ": " + msg);
	}
	
	public static void e(String module, String msg) {
		android.util.Log.e(applicationTag, module + ": " + msg);
	}
}