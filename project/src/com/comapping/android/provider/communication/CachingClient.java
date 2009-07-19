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

	public void logout() throws ConnectionException {
		cache.clear();
		MemoryCache.clear();

		client.logout(true);
	}

	public void applicationClose() throws ConnectionException {
		if (remember) {
			MemoryCache.clear();
		} else {
			logout();
		}
	}

	public String getComap(String mapId)
			throws ConnectionException, LoginInterruptedException,
			InvalidCredentialsException {
		return getComap(mapId, false, false);
	}

	public String getComap(String mapId, boolean ignoreCache,
			boolean ignoreInternet) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		String result = null;

		if (!ignoreCache) {
			result = cache.get(mapId);
		}

		if ((result == null) && (!ignoreInternet)) {
			result = client.getComap(mapId);
			
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