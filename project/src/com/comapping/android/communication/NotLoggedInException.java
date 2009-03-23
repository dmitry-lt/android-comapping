/**
 * A class representing a not logged in exception.
 * 
 * @author Abishev Timur
 * @version 1.0
 */
package com.comapping.android.communication;

import com.comapping.android.Log;

public class NotLoggedInException extends Exception {
	public NotLoggedInException() {
		Log.e(Log.connectionTag, "not logged in exception");
	}
}