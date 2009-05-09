/**
 * Proxy class for Client.
 * 
 * @author Abishev Timur
 * @version 1.0
 */

package com.comapping.android.communication;

import java.sql.Timestamp;

import android.app.Activity;

import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.storage.MemoryCache;
import com.comapping.android.storage.SqliteMapCache;

public class CachingClient implements MapProvider {
	private Client client = null;
	private SqliteMapCache cache = null;

	private boolean remember = true;

	public CachingClient(Client client, SqliteMapCache cache) {
		this.client = client;
		this.cache = cache;
	}

	public void login(String email, String password, boolean remember) throws ConnectionException, InvalidCredentialsException, LoginInterruptedException {
		client.login(email, password, remember);
		
		this.remember = remember;
	}

	public boolean isAutologinPossible() {
		return client.isAutologinPossible();
	}

	public void autologin() throws ConnectionException, InvalidCredentialsException, LoginInterruptedException {
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

	public String getComap(String mapId, Activity context) throws ConnectionException, LoginInterruptedException, InvalidCredentialsException {
		return getComap(mapId, context, false, false);
	}

	public String getComap(String mapId, Activity context, boolean ignoreCache, boolean ignoreInternet) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		String result = null;

		if (!ignoreCache) {
			result = cache.get(mapId);
		}

		if ((result == null) && (!ignoreInternet)) {
			result = client.getComap(mapId, context);
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
}