package com.comapping.android.communication;

import android.util.Log;

public class NotLoggedInException extends Exception {
	public NotLoggedInException() {
		Log.e("Comapping", "Communication: not logged in exception");
	}
}