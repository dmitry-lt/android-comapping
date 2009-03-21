package com.comapping.android.communication;

import com.comapping.android.Log;

public class UTF8NotSupportedError extends Error {
	public UTF8NotSupportedError() {
		Log.e(Log.connectionTag, "Communication: UTF-8 encoding not supported");
	}
}