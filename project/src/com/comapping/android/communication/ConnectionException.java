package com.comapping.android.communication;

import android.util.Log;

public class ConnectionException extends Exception {
	public ConnectionException() {
		Log.e("Comapping", "Communication: connection exception");
	}
}