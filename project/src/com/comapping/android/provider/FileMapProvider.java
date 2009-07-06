package com.comapping.android.provider;

import static com.comapping.android.communication.ClientHelper.getBytesSum;
import static com.comapping.android.communication.ClientHelper.getTextFromInputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.comapping.android.Log;

import android.app.Activity;

public class FileMapProvider implements IMapProvider {

	public void close(Activity context) {
		// TODO Auto-generated method stub

	}

	public String getComap(String mapId, boolean ignoreCache, Activity context) {
		String response = null;

		try {
			response = getTextFromInputStream(new FileInputStream(mapId));
		} catch (FileNotFoundException e) {
			Log.e(Log.CONNECTION_TAG, "map file not found");
		} catch (IOException e) {
			Log.e(Log.CONNECTION_TAG, "map file IO exception");
		}

		Log.d(Log.CONNECTION_TAG, "file comap provider response: " + response);
		Log.d(Log.CONNECTION_TAG, "file comap provider check sum: "
				+ getBytesSum(response));

		return response;
	}

	public void logout(Activity context) {
		// TODO Auto-generated method stub

	}
	
}
