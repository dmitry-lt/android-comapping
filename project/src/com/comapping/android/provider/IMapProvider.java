package com.comapping.android.provider;

import android.app.Activity;

public interface IMapProvider {
	public String getComap(String mapId, boolean ignoreCache, Activity context);

	public void logout(Activity context);

	public void close(Activity context);
}
