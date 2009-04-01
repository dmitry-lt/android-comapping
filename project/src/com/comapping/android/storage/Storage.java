/*
 * Storage
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements Storage for save options
 */

package com.comapping.android.storage;

import com.comapping.android.controller.MainController;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class Storage {
	// Singleton
	private Storage() {
	}

	public static Storage instance = new Storage();

	public static Storage getInstance() {
		return instance;
	}

	public void set(String key, String value) {
		Editor edit = MainController.getInstance().getPreferences(Context.MODE_PRIVATE).edit();

		edit.putString(key, value);
		edit.commit();

		Log.i("Storage", "[" + key + "] = " + value);
	}

	public String get(String key) {
		Log.i("Storage", "get [" + key + "]");

		return MainController.getInstance().getPreferences(Context.MODE_PRIVATE).getString(key, "");
	}
}
