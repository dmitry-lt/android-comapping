package com.comapping.android.communication;

import com.comapping.android.Log;

public class ConnectionException extends Exception {
	public ConnectionException() {
		Log.e(Log.connectionTag, "connection exception");
	}
}