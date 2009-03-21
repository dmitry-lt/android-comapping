package com.comapping.android.communication;

import com.comapping.android.Log;

public class MD5NotSupportedError extends Error {
	public MD5NotSupportedError() {
		Log.e(Log.connectionTag, "MD5 not supported");
	}
}