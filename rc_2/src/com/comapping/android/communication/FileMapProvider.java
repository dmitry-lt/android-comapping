package com.comapping.android.communication;

import static com.comapping.android.communication.ClientHelper.getBytesSum;
import static com.comapping.android.communication.ClientHelper.getTextFromInputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;

import com.comapping.android.Log;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;

public class FileMapProvider implements MapProvider {

	@Override
	public String getComap(String mapId, Activity context) throws ConnectionException, LoginInterruptedException,
			InvalidCredentialsException {
		String response = "";

		try {
			response = getTextFromInputStream(new FileInputStream(mapId));
		} catch (FileNotFoundException e) {
			Log.e(Log.CONNECTION_TAG, "map file not found");
			throw new ConnectionException();
		} catch (IOException e) {
			Log.e(Log.CONNECTION_TAG, "map file IO exception");
			throw new ConnectionException();
		}
		
		Log.d(Log.CONNECTION_TAG, "file comap provider response: " + response);
		Log.d(Log.CONNECTION_TAG, "file comap provider check sum: " + getBytesSum(response));
		
		return response;
	}
}
