/**
 * A class representing a helper for Client class.
 * 
 * @author Abishev Timur
 * @version 1.0
 */
package com.lanit_tercom.comapping.android.provider.communication;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import com.lanit_tercom.comapping.android.provider.communication.exceptions.MD5NotSupportedError;
import com.lanit_tercom.comapping.android.provider.communication.exceptions.UTF8NotSupportedError;

public class ClientHelper {
	/**
	 * Method for encoding string by MD5
	 * 
	 * @param string
	 *            String for encoding
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

		StringBuilder output = new StringBuilder();
		for (byte b : bytes) {
			output.append(String.format("%02x", b));
		}

		return output.toString();
	}

	/**
	 * Method for getting sum string bytes
	 * 
	 * @param string
	 *            String
	 * @return Sum of bytes
	 */
	public static long getBytesSum(String string) {
		byte[] bytes;
		try {
			bytes = string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new UTF8NotSupportedError();
		}
		long sum = 0;
		for (byte b : bytes) {
			sum += b;
		}
		return sum;
	}

	public static String getPostParameters(List<BasicNameValuePair> data) {
		StringBuilder parameters = new StringBuilder();

		for (int i = 0; i < data.size(); i++) {
			if (i != 0) {
				parameters.append("&");
			}

			parameters.append(URLEncoder.encode(data.get(i).getName()) + "="
					+ URLEncoder.encode(data.get(i).getValue()));
		}

		return parameters.toString();
	}
}