/**
 * A class representing a UTF8 not support error.
 * 
 * @author Abishev Timur
 * @version 1.0
 */
package com.comapping.android.communication.exceptions;

import com.comapping.android.Log;

public class UTF8NotSupportedError extends Error {
	private static final long serialVersionUID = 3638173584258947120L;

	public UTF8NotSupportedError() {
		Log.e(Log.CONNECTION_TAG, "Communication: UTF-8 encoding not supported");
	}
}