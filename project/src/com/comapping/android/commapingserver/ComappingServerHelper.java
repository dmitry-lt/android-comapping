/*
 * Comapping Server Helper
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Helper for Comapping Server class
 */

package com.comapping.android.commapingserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.w3c.dom.Entity;

import android.util.Log;

public class ComappingServerHelper {
	static String MD5Encode(String string) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e("Comapping Server", "MD5 not supported");
		}

		byte[] bytes = null;

		try {
			bytes = string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			Log.e("Comapping Server", "Unsupported Encoding");
		}

		bytes = md5.digest(bytes);

		StringBuffer output = new StringBuffer();
		for (byte b : bytes) {
			output.append(String.format("%x", b));
		}

		return output.toString();
	}

	static String getTextFromInputStream(InputStream in) {
		String text = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			text = reader.readLine();

			String line = reader.readLine();

			while (line != null) {
				text += "\n" + line;
				line = reader.readLine();
			}
		} catch (Exception ex) {
			Log.e("Comapping Server", "Reader Exception");
		} finally {
			try {
				in.close();
			} catch (Exception ex) {
				Log.e("Comapping Server", "InputStream Close Exception");
			}
		}
		return text;
	}

	static String getTextFromResponse(HttpResponse response) {
		try {
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				return getTextFromInputStream(entity.getContent());
			}
		} catch (IllegalStateException e) {
			Log.e("Comapping Server", "Http Response State Exception");
		} catch (IOException e) {
			Log.e("Comapping Server", "Http Response IO Exception");
		}
		
		return null;
	}
}