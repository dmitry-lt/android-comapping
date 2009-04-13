/**
 * A class representing a MD5 not support error.
 * 
 * @author Abishev Timur
 * @version 1.0
 */
package com.comapping.android.communication.exceptions;

import com.comapping.android.Log;

public class MD5NotSupportedError extends Error {
	private static final long serialVersionUID = 853525587660716298L;

	public MD5NotSupportedError() {
		Log.e(Log.connectionTag, "MD5 not supported");
	}
}