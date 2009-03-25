/**
 * A class representing a helper for Client class.
 * 
 * @author Abishev Timur
 * @version 1.0
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
	/**
	 * Method for encoding string by MD5
	 * @param string String for encoding	
	 * @return Encoded string
	 */
	public static String md5Encode(String string) {
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
			output.append(String.format("%02x", b));
		}

		return output.toString();
	}

	/**
	 * Method for getting text from InputStream
	 * @param input InputStream 
	 * @return Text Text from InputStream
	 * @throws IOException
	 */
	public static String getTextFromInputStream(InputStream input) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));

		String line = reader.readLine();
		StringBuilder text;

		if (line != null) {
			text = new StringBuilder(line);
		} else {
			// not exist first line
			return "";
		}

		line = reader.readLine();
		while (line != null) {
			text.append("\n").append(line);
			line = reader.readLine();
		}

		reader.close();

		return text.toString();
	}

	/**
	 * Method for getting text from HttpResponse
	 * @param response HttpResponse
	 * @return Text Text from InputStream
	 * @throws IOException
	 */
	public static String getTextFromResponse(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			return getTextFromInputStream(entity.getContent());
		} else {
			return "";
		}
	}
}