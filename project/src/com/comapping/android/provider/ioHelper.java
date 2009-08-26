package com.comapping.android.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.comapping.android.Log;

public class ioHelper {
	public static String getTextFromInputStream(InputStream input) {
		StringBuffer content = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input), 8 * 1024);
		try {			
			String line = null;
			String separator = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				content.append(line).append(separator);
			}			
		} catch (IOException e) {
			Log.e(Log.PROVIDER_TAG, e.toString());
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				Log.e(Log.PROVIDER_TAG, e.toString());
			}
		}
		return content.toString();
	}
}
