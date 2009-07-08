package com.comapping.android.provider;

import com.comapping.android.provider.communication.CachingClient;
import com.comapping.android.provider.communication.Client;
import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.provider.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.provider.communication.exceptions.LoginInterruptedException;
import android.app.Activity;

public class InternetMapProvider implements IMapProvider {
	private CachingClient cachingClient;

	public InternetMapProvider(Activity context) {
		cachingClient = Client.getClient(context);
	}

	public String getComap(String mapId, boolean ignoreCache, Activity context) {
		String result = null;

		try {
			result = cachingClient.getComap(mapId, context, ignoreCache, false);
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LoginInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCredentialsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public void logout(Activity context) {
		try {
			cachingClient.logout(context);
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close(Activity context) {
		try {
			cachingClient.applicationClose(context);
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
