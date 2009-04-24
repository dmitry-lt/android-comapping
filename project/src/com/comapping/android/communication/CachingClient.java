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
import com.comapping.android.storage.SqliteMapCache;

public class CachingClient implements MapProvider {
	private Client client = null;
	private SqliteMapCache cache = null;

	public CachingClient(Client client, SqliteMapCache cache) {
		this.client = client;
		this.cache = cache;
	}

	public void login(String email, String password, boolean remember) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		client.login(email, password, remember);
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

	public void clientSideLogout() {
		client.clientSideLogout();
	}

	public void logout(Activity context) throws ConnectionException {
		client.logout(context);
	}

	public String getComap(String mapId, Activity context) throws ConnectionException, LoginInterruptedException,
			InvalidCredentialsException {
		String result = cache.get(mapId);

		if (result == null) {
			// save result to cache
			result = client.getComap(mapId, context);
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