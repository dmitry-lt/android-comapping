package com.comapping.android.communication;

import com.comapping.android.Log;

public class NotLoggedInException extends Exception {
	public NotLoggedInException() {
		Log.e(Log.connectionTag, "not logged in exception");
	}
}