/**
 * A class representing a connection exception.
 * 
 * @author Abishev Timur
 * @version 1.0
 */
package com.comapping.android.communication;

import com.comapping.android.Log;

public class ConnectionException extends Exception {
	public ConnectionException() {
		Log.e(Log.connectionTag, "connection exception");
	}
}