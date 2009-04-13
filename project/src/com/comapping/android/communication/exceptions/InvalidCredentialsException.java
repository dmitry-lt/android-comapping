package com.comapping.android.communication.exceptions;

import com.comapping.android.Log;

public class InvalidCredentialsException extends Exception {
	private static final long serialVersionUID = -5988600987221116638L;

	public InvalidCredentialsException() {
		Log.e(Log.connectionTag, "invalid credentials exception");
	}
}