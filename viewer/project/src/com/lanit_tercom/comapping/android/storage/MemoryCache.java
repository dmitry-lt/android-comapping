package com.lanit_tercom.comapping.android.storage;

import java.util.HashMap;

import com.lanit_tercom.comapping.android.Log;

public class MemoryCache {
	// max objects in cache
	final private static int maxObjectsCount = 3;

	private static HashMap<String, Object> cache = new HashMap<String, Object>();
	private static String[] queue = new String[maxObjectsCount];
	private static int currentPosition = 0;

	synchronized public static Object get(String key) {
		return cache.get(key);
	}

	synchronized public static void set(String key, Object object) {
		if (cache.containsKey(key)) {
			cache.remove(key);
		} else {
			// clear current position
			if (queue[currentPosition] != null) {
				cache.remove(queue[currentPosition]);
			}
		}

		cache.put(key, object);
		queue[currentPosition] = key;

		currentPosition = (currentPosition + 1) % maxObjectsCount;
	}

	synchronized public static boolean has(String key) {
		return cache.containsKey(key);
	}

	synchronized public static void clear() {
		Log.d(Log.MEMORY_CACHE_TAG, "Clearing");
		
		cache = new HashMap<String, Object>();
		queue = new String[maxObjectsCount];
		currentPosition = 0;

		System.gc();
	}
}