/**
 * Proxy class for Client.
 * 
 * @author Abishev Timur
 * @version 1.0
 */

package com.comapping.android.provider.communication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;

import android.app.Activity;
import android.content.Context;
import com.comapping.android.Log;

import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.provider.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.provider.communication.exceptions.LoginInterruptedException;
import com.comapping.android.storage.MemoryCache;
import com.comapping.android.storage.SqliteMapCache;

public class CachingClient {
	private Client client = null;
	private SqliteMapCache cache = null;

	private boolean remember = true;

	public CachingClient(Client client, SqliteMapCache cache) {
		this.client = client;
		this.cache = cache;
	}

	public void login(String email, String password, boolean remember)
			throws ConnectionException, InvalidCredentialsException,
			LoginInterruptedException {
		client.login(email, password, remember);

		this.remember = remember;
	}

	public boolean isAutologinPossible() {
		return client.isAutologinPossible();
	}

	public void autologin() throws ConnectionException,
			InvalidCredentialsException, LoginInterruptedException {
		client.autologin();
	}

	public boolean isLoggedIn() {
		return client.isLoggedIn();
	}

	public void logout(Activity context) throws ConnectionException {
		cache.clear();
		MemoryCache.clear();

		client.logout(context, true);
	}

	public void applicationClose(Activity context) throws ConnectionException {
		MemoryCache.clear();

		if (!remember) {
			cache.clear();
		}
	}

	public String getComap(String mapId, Context context)
			throws ConnectionException, LoginInterruptedException,
			InvalidCredentialsException {
		return getComap(mapId, context, false, false);
	}

	public String getComap(String mapId, Context context, boolean ignoreCache,
			boolean ignoreInternet) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		String result = null;

		if (!ignoreCache) {
			result = cache.get(mapId);
		}

		if ((result == null) && (!ignoreInternet)) {
			try {
				result = client.getComap(mapId, context);
			} catch (Exception e) {
				Log.e(Log.CONNECTION_TAG, e.toString());
				result = cache.get(mapId); // if map not retrieved return cached
											// map
			}
			// save result to cache
			cache.set(mapId, result);
		}

		return result;
	}

	public void interruptLogin() {
		client.interruptLogin();
	}

	public void clearCache() {
		cache.clear();
	}

	public Timestamp getLastSynchronizationDate(String mapId) {
		return cache.getLastSynchronizationDate(mapId);
	}

	public HttpURLConnection getHttpURLConnection(URL url) throws ConnectionException, IOException {
		return client.getHttpURLConnection(url);
	}
}