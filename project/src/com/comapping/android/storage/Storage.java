/*
 * Storage
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements Storage for save options
 */

package com.comapping.android.storage;

import com.comapping.android.Log;
import com.comapping.android.controller.MetaMapActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Storage {
	public static final String VIEW_TYPE_KEY = "viewType";
	public static final String DOWNLOAD_FOLDER_KEY = "downloadFolder";
	public static final String AUTOLOGIN_KEY = "autologin";
	public static final String EMAIL_KEY = "email";	
	
	public static final String USE_PROXY = "useProxy";	
	public static final String PROXY_HOST = "proxyHost";
	public static final String PROXY_PORT = "proxyPort";
	
	public static final String USE_PROXY_AUTH = "useProxyAuth";	
	public static final String PROXY_NAME = "proxyName";
	public static final String PROXY_PASSWORD = "proxyPassword";
	

	// Singleton
	private Storage() {
	}

	public static Storage instance = new Storage();

	public static Storage getInstance() {
		return instance;
	}

	public void set(String key, String value) {
		SharedPreferences preferances = PreferenceManager.getDefaultSharedPreferences(MetaMapActivity.getInstance());

		Editor edit = preferances.edit();

		edit.putString(key, value);
		edit.commit();

		Log.i(Log.STORAGE_TAG, "[" + key + "] = " + value);
	}

	public String get(String key, String defaultValue) {
		SharedPreferences preferances = PreferenceManager.getDefaultSharedPreferences(MetaMapActivity.getInstance());

		String value = preferances.getString(key, defaultValue);

		Log.i(Log.STORAGE_TAG, "get [" + key + "] = " + value);

		return value;
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		// false if not created
		SharedPreferences preferances = PreferenceManager.getDefaultSharedPreferences(MetaMapActivity.getInstance());

		return preferances.getBoolean(key, defaultValue);
	}
}
