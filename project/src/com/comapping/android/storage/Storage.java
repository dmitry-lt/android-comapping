/*
 * Storage
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements Storage for save options
 */

package com.comapping.android.storage;

public class Storage {
	// Singleton
	private Storage() {
	}

	public static Storage instance = new Storage();

	public static Storage getInstance() {
		return instance;
	}

	public void set(String key, String value) {
	}

	public String get(String key) {
		return "";
	}

}
