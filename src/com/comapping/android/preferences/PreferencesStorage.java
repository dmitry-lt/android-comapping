/*
 * Storage
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements Storage for save options
 */

package com.comapping.android.preferences;

import com.comapping.android.Constants;
import com.comapping.android.Log;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferencesStorage {
	public static final String VIEW_TYPE_KEY = "viewType";
	public static final String VIEW_TYPE_DEFAULT_VALUE = Constants.VIEW_TYPE_COMAPPING;
	public static final String DOWNLOAD_FOLDER_KEY = "downloadFolder";
	public static final String DOWNLOAD_FOLDER_DEFAULT_VALUE = "/sdcard/comapping/download";
	public static final String AUTOLOGIN_KEY = "autoLogin";
	public static final String EMAIL_KEY = "eMail";

	public static final String USE_PROXY_KEY = "useProxy";
	public static final boolean USE_PROXY_DEFAULT_VALUE = false;
	public static final String PROXY_HOST_KEY = "proxyHost";
	public static final String PROXY_PORT_KEY = "proxyPort";

	public static final String USE_PROXY_AUTH_KEY = "useProxyAuth";
	public static final boolean USE_PROXY_AUTH_DEFAULT_VALUE = false;
	public static final String PROXY_NAME_KEY = "proxyAuthUserName";
	public static final String PROXY_PASSWORD_KEY = "proxyAuthUserPassword";

	public static void set(String key, String value, Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());

		Editor edit = preferences.edit();

		edit.putString(key, value);
		edit.commit();

		Log.i(Log.STORAGE_TAG, "[" + key + "] = " + value);
	}

	public static String get(String key, String defaultValue, Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());

		String value = preferences.getString(key, defaultValue);

		Log.i(Log.STORAGE_TAG, "get [" + key + "] = " + value);

		return value;
	}

	public static boolean getBoolean(String key, boolean defaultValue,
			Context context) {
		// false if not created
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context.getApplicationContext());

		return preferences.getBoolean(key, defaultValue);
	}

}