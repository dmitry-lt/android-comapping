package com.comapping.android.communication;

import com.comapping.android.Log;

public class LoginInterruptedException extends Exception {
	public LoginInterruptedException() {
		Log.e(Log.connectionTag, "login interrupted in exception");
	}
}