package com.comapping.android;

import java.util.HashMap;

public class Cache {
	private static HashMap<String, Object> cache = new HashMap<String, Object>();

	synchronized public static Object get(String key) {
		return cache.get(key);
	}

	synchronized public static void set(String key, Object object) {
		if (cache.containsKey(key)) {
			cache.remove(key);
		}

		cache.put(key, object);
	}

	synchronized public static void clear() {
		cache = new HashMap<String, Object>();
	}
}
