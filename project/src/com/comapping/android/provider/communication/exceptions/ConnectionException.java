/**
 * A class representing a connection exception.
 * 
 * @author Abishev Timur
 * @version 1.0
 */
package com.comapping.android.provider.communication.exceptions;

import com.comapping.android.Log;

public class ConnectionException extends Exception {
	private static final long serialVersionUID = -2905001710791357793L;

	public ConnectionException() {
		Log.w(Log.CONNECTION_TAG, "connection exception");
	}
}