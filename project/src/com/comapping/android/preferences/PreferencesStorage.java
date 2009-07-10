/*
 * Storage
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements Storage for save options
 */

package com.comapping.android.preferences;

import com.comapping.android.Log;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferencesStorage {
	public static final String VIEW_TYPE_KEY = "viewTypePreferenceKey";
	public static final String DOWNLOAD_FOLDER_KEY = "downloadFolder";
	public static final String AUTOLOGIN_KEY = "autoLogin";
	public static final String EMAIL_KEY = "eMail";	
	
	public static final String USE_PROXY = "useProxy";	
	public static final String PROXY_HOST = "proxyHost";
	public static final String PROXY_PORT = "proxyPort";
	
	public static final String USE_PROXY_AUTH = "useProxyAuth";	
	public static final String PROXY_NAME = "proxyAuthUserName";
	public static final String PROXY_PASSWORD = "proxyAuthUserPassword";
	

//	// Singleton
//	private Storage() {
//	}
//
//	public static Storage instance = new Storage();
//
//	public static Storage getInstance() {
//		return instance;
//	}
	public static void set(String key, String value, Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		Editor edit = preferences.edit();

		edit.putString(key, value);
		edit.commit();

		Log.i(Log.STORAGE_TAG, "[" + key + "] = " + value);
	}

	public static String get(String key, String defaultValue, Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		String value = preferences.getString(key, defaultValue);

		Log.i(Log.STORAGE_TAG, "get [" + key + "] = " + value);

		return value;
	}
	
	public static boolean getBoolean(String key, boolean defaultValue, Context context) {
		// false if not created
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

		return preferences.getBoolean(key, defaultValue);
	}
	
	//******
	// old
//	public static void set(String key, String value) {
//		SharedPreferences preferances = PreferenceManager.getDefaultSharedPreferences(MetaMapActivity.instance);
//
//		Editor edit = preferances.edit();
//
//		edit.putString(key, value);
//		edit.commit();
//
//		Log.i(Log.STORAGE_TAG, "[" + key + "] = " + value);
//	}
//
//	public static String get(String key, String defaultValue) {
//		SharedPreferences preferances = PreferenceManager.getDefaultSharedPreferences(MetaMapActivity.instance);
//
//		String value = preferances.getString(key, defaultValue);
//
//		Log.i(Log.STORAGE_TAG, "get [" + key + "] = " + value);
//
//		return value;
//	}
//	
//	public static boolean getBoolean(String key, boolean defaultValue) {
//		// false if not created
//		SharedPreferences preferances = PreferenceManager.getDefaultSharedPreferences(MetaMapActivity.instance);
//
//		return preferances.getBoolean(key, defaultValue);
//	}
}
