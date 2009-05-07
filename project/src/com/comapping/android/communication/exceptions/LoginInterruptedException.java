package com.comapping.android.communication.exceptions;

import com.comapping.android.Log;

public class LoginInterruptedException extends Exception {	
	private static final long serialVersionUID = -6637254176746744402L;

	public LoginInterruptedException() {
		Log.e(Log.CONNECTION_TAG, "login interrupted in exception");
	}
}