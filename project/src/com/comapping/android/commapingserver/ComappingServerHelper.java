/*
 * Comapping Server Helper
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Helper for Comapping Server class
 */

package com.comapping.android.commapingserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;

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

	static String GetText(InputStream in) {
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
			Log.e("Comapping Server", "Reader error");
		} finally {
			try {
				in.close();
			} catch (Exception ex) {
				Log.e("Comapping Server", "InputStream close error");
			}
		}
		return text;
	}

	static String GetText(HttpResponse response) {
		String text = "";
		try {
			text = GetText(response.getEntity().getContent());
		} catch (Exception ex) {
		}
		return text;
	}
}