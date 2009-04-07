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

		Log.i(Log.storageTag, "[" + key + "] = " + value);
	}

	public String get(String key) {
		SharedPreferences preferances = PreferenceManager.getDefaultSharedPreferences(MetaMapActivity.getInstance());

		String value = preferances.getString(key, "");

		Log.i(Log.storageTag, "get [" + key + "] = " + value);

		return value;
	}
}
