/**
 * Proxy class for Client.
 * 
 * @author Abishev Timur
 * @version 1.0
 */

package com.comapping.android.communication;

import android.app.Activity;

import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.storage.MemoryCache;
import com.comapping.android.storage.SqliteMapCache;

public class CachingClient implements MapProvider {
	private Client client = null;
	private SqliteMapCache cache = null;
	
	private boolean remember = false;

	public CachingClient(Client client, SqliteMapCache cache) {
		this.client = client;
		this.cache = cache;
	}

	public void login(String email, String password, boolean remember) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		client.login(email, password, remember);
		
		this.remember = remember;
	}

	public boolean isAutologinPossible() {
		return client.isAutologinPossible();
	}

	public void autologin() throws ConnectionException, InvalidCredentialsException, LoginInterruptedException {
		client.autologin();
		
		remember = true;
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
		
		client.logout(context, false);
		cache.close();
	}

	public String getComap(String mapId, Activity context) throws ConnectionException, LoginInterruptedException,
			InvalidCredentialsException {
		return getComap(mapId, context, false);
	}

	public String getComap(String mapId, Activity context, boolean ignoreCache) throws ConnectionException, LoginInterruptedException,
		InvalidCredentialsException {
		String result = null;
		
		if (!ignoreCache) {
			result = cache.get(mapId);
		}
		
		if (result == null) {
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
}