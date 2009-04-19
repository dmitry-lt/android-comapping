package com.comapping.android.communication;

import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;

import android.app.Activity;

public interface MapProvider {
	public String getComap(String mapId, Activity context) throws ConnectionException, LoginInterruptedException, InvalidCredentialsException;
}