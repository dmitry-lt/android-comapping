package com.comapping.android;

public class Log {
	private static final String APPLICATION_TAG = "Comapping";
  
	public static final String CONNECTION_TAG = "Communication";
	public static final String MODEL_TAG = "Model";
	public static final String META_MAP_CONTROLLER_TAG = "MetaMapController";
	public static final String MAP_CONTROLLER_TAG = "MapController";
	public static final String EXPLORER_RENDER_TAG = "ExplorerRender";
	public static final String TOPIC_RENDER_TAG = "TopicRender";
	public static final String LOGIN_TAG = "Login";
	public static final String STORAGE_TAG = "Storage";
	public static final String SQLITE_CACHE_TAG = "SQLite";
	public static final String PROVIDER_COMAPPING_TAG = "ComappingMapContentProvider";

	public static void v(String module, String msg) {
		if (!Options.USE_LOG)
			return;
		android.util.Log.v(APPLICATION_TAG, combine(module, msg));
	}

	public static void d(String module, String msg) {
		if (!Options.USE_LOG)
			return;
		
		if (Options.DEBUG_LOG) {
			android.util.Log.d(APPLICATION_TAG, combine(module, msg));
		}
	}

	public static void i(String module, String msg) {
		if (!Options.USE_LOG)
			return;
		
		android.util.Log.i(APPLICATION_TAG, combine(module, msg));
	}

	public static void w(String module, String msg) {
		if (!Options.USE_LOG)
			return;
		
		android.util.Log.w(APPLICATION_TAG, combine(module, msg));
	}

	public static void e(String module, String msg) {
		if (!Options.USE_LOG)
			return;
		
		android.util.Log.e(APPLICATION_TAG, combine(module, msg));
	}
	
	private static String combine(String module, String msg) {
		StringBuilder res = new StringBuilder();
		
		int lineStart = 0;
		while (lineStart < msg.length()) {
			int lineEnd = msg.indexOf('\n', lineStart);
			if (lineEnd == -1) {
				lineEnd = msg.length() - 1;
			}
			
			res.append(module + ": ");
			res.append(msg, lineStart, lineEnd + 1);
			
			lineStart = lineEnd + 1;
		}
		
		return res.toString();
	}
}
