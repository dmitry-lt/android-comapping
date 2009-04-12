package com.comapping.android.communication;

import com.comapping.android.Log;

public class InvalidCredentialsException extends Exception {
	public InvalidCredentialsException() {
		Log.e(Log.connectionTag, "invalid credentials exception");
	}
}