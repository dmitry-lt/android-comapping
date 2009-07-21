/**
 * Client with cache.
 * 
 * @author Abishev Timur
 * @version 1.0
 */

package com.comapping.android.provider.communication;

import java.sql.Timestamp;

import android.content.Context;

import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.provider.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.provider.communication.exceptions.LoginInterruptedException;
import com.comapping.android.storage.MemoryCache;
import com.comapping.android.storage.SqliteMapCache;

public class CachingClient extends Client {
	private SqliteMapCache cache = null;

	private boolean remember = true;
	private IDownloadingListener downloadingListener;

	public CachingClient(Context context, SqliteMapCache cache) {
		super(context);
		this.cache = cache;
	}

	public void login(String email, String password, boolean remember) throws ConnectionException,
			InvalidCredentialsException, LoginInterruptedException {
		super.login(email, password, remember);

		this.remember = remember;
	}

	public void logout() throws ConnectionException {
		cache.clear();
		MemoryCache.clear();

		super.logout(true);
	}

	public void applicationClose() throws ConnectionException {
		if (remember) {
			MemoryCache.clear();
		} else {
			logout();
		}
	}

	public String getComap(String mapId) throws ConnectionException, LoginInterruptedException,
			InvalidCredentialsException {
		return getComap(mapId, false, false);
	}

	public String getComap(String mapId, boolean ignoreCache, boolean ignoreInternet) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		String result = null;

		if (!ignoreCache) {
			result = cache.get(mapId);
		}

		if ((result == null) && (!ignoreInternet)) {
			result = super.getComap(mapId);

			// save result to cache
			cache.set(mapId, result);
		}

		return result;
	}

	public void clearCache() {
		cache.clear();
	}

	public Timestamp getLastSynchronizationDate(String mapId) {
		return cache.getLastSynchronizationDate(mapId);
	}
	
	public int getMapSizeInBytes(String mapId) throws ConnectionException  {
		
		return super.getSize(mapId);
		
	}
	
	public void startMapDownloading(String mapId) {
		// TODO		
	}
	
	public void setDownloadingListener(IDownloadingListener downloadingListener) {
		this.downloadingListener = downloadingListener;
	}
	
	public static interface IDownloadingListener {
		void statusChanged(String mapId, int downloadedInBytes); 
	}
}