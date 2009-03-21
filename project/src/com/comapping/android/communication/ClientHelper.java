/*
 * Comapping Server Helper
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Helper for Comapping Server class
 */

package com.comapping.android.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class ClientHelper {
	
	static String MD5Encode(String string) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new MD5NotSupportedError();
		}

		byte[] bytes = null;

		try {
			bytes = string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			throw new UTF8NotSupportedError();
		}

		bytes = md5.digest(bytes);

		StringBuffer output = new StringBuffer();
		for (byte b : bytes) {
			String toAdd = String.format("%x", b);
			int spacesToAdd = 2 - toAdd.length();

			for (int i = 0; i < spacesToAdd; i++)
				output.append("0");

			output.append(toAdd);
		}

		return output.toString();
	}

	static String getTextFromInputStream(InputStream input) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));

		String text = reader.readLine();
		
		String line = reader.readLine();
		while (line != null) {
			text += "\n" + line;
			line = reader.readLine();
		}
		
		reader.close();
		
		if (text == null) text = "";
		
		return text;
	}

	static String getTextFromResponse(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			return getTextFromInputStream(entity.getContent());
		} else {
			return "";
		}
	}
}