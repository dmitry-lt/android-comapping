/**
 * A class implements a client for comnapping server.
 * 
 * @author Abishev Timur
 * @version 1.0
 */
package com.comapping.android.communication;

import static com.comapping.android.communication.ClientHelper.getTextFromResponse;
import static com.comapping.android.communication.ClientHelper.getTextFromInputStream;
import static com.comapping.android.communication.ClientHelper.MD5Encode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.controller.LoginController;

public class Client {
	private String clientId = null;

	private String email = null;
	private String autoLoginKey = null;

	// public methods
	/**
	 * Method for manual login with email and password
	 * 
	 * @param email
	 * @param password
	 * @throws ConnectionException
	 */
	public void login(String email, String password) throws ConnectionException {
		// if you try to log in, previous user logged out
		setClientId(null);

		String passwordHash = MD5Encode(password);
		String salt = doLogin(email, passwordHash, "simple");

		if (salt.length() > 0) {
			// response from server is valid
			if (salt.charAt(0) == '#') {
				// account with salt
				salt = salt.substring(1);

				autoLoginKey = MD5Encode(password + salt);

				setClientId(doLogin(email, autoLoginKey, "withSalt"));
			} else {
				// account without salt
				autoLoginKey = "#" + passwordHash;

				setClientId(salt);
			}
		} else {
			// login failed
		}
	}

	/**
	 * Method for automatic login with AutoLogin key
	 * 
	 * @param email Email for login
	 * @param key AutoLogin key
	 * @throws ConnectionException
	 */
	public void autoLogin(String email, String key) throws ConnectionException {
		autoLoginKey = key;

		if ((key.length() > 0) && (key.charAt(0) == '#')) {
			//account with salt
			setClientId(doLogin(email, key.substring(1), "simple"));
		} else {
			//account without salt
			setClientId(doLogin(email, key, "flashCookie"));
		}
	}

	/**
	 * Method for AutoLogin key getting
	 * 
	 * @return AutoLogin key
	 * @throws NotLoggedInException
	 */
	public String getAutoLoginKey() throws NotLoggedInException {
		loginRequired();

		return autoLoginKey;
	}

	/**
	 * Method for user email getting
	 * 
	 * @return User email
	 * @throws NotLoggedInException
	 */
	public String getEmail() throws NotLoggedInException {
		loginRequired();

		return email;
	}

	/**
	 * Method for check user login status
	 * 
	 * @return true or false
	 */
	public boolean isLoggedIn() {
		return clientId != null;
	}

	/**
	 * Method for only client side logout
	 */
	public void clientSideLogout() {
		clientId = null;
	}

	/**
	 * Method for both client and server side logout
	 * 
	 * @throws NotLoggedInException
	 * @throws ConnectionException
	 */
	public void logout() throws NotLoggedInException, ConnectionException {
		loginRequired();

		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_logout"));
		data.add(new BasicNameValuePair("clientId", clientId));

		clientSideLogout();

		requestToServer(data);
	}

	/**
	 * Method for getting comap file from server
	 * 
	 * @param mapId Comap Id
	 * @return Comap in String format
	 * @throws NotLoggedInException
	 * @throws ConnectionException
	 */
	public String getComap(String mapId) throws NotLoggedInException,
			ConnectionException {
		Log.d(Log.connectionTag, "getting " + mapId + " comap");

		loginRequired();

		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "download"));
		data.add(new BasicNameValuePair("format", "comap"));
		data.add(new BasicNameValuePair("clientID", clientId));
		data.add(new BasicNameValuePair("mapid", mapId));

		return requestToServer(data);
	}

	// private methods
	private String fakeRequestToServer(ArrayList<BasicNameValuePair> data)
			throws ConnectionException {
		String response = "";

		if (data.get(0).getValue().equals("download")) {
			// "get comap" request
			String mapId = data.get(3).getValue();
			try {
				response = getTextFromInputStream(new FileInputStream(
						Options.COMAP_FILE_SERVER+mapId+".comap"));
			} catch (FileNotFoundException e) {
				Log.e(Log.connectionTag, "XML File Server not found");
				throw new ConnectionException();
			} catch (IOException e) {
				Log.e(Log.connectionTag, "XML File IO exception");
				throw new ConnectionException();
			}
		} else {
			// for successful login
			response = "12345";
		}

		Log.d(Log.connectionTag, "fake response: " + response);
		return response;
	}

	private String requestToServer(ArrayList<BasicNameValuePair> data)
			throws ConnectionException {
		Log.d(Log.connectionTag, "request to server "
				+ Arrays.toString(data.toArray()));

		if (Options.FAKE_SERVER) {
			return fakeRequestToServer(data);
		}

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(Options.SERVER);

		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(data);
		} catch (UnsupportedEncodingException e1) {
			Log
					.e(Log.connectionTag,
							"unsupported encoding for data in request");
		}

		post.setEntity(entity);

		String responseText = "";
		int responseStatus = 0;

		try {
			HttpResponse response = client.execute(post);
			responseText = getTextFromResponse(response);
			responseStatus = response.getStatusLine().getStatusCode();
		} catch (ClientProtocolException e) {
			Log.d(Log.connectionTag, "client protocol exception");
			throw new ConnectionException();
		} catch (IOException e) {
			Log.d(Log.connectionTag, "IO exception");
			throw new ConnectionException();
		}

		Log.i(Log.connectionTag, "response from server: " + responseText);
		Log.d(Log.connectionTag, "response status code: " + responseStatus);

		return responseText;
	}

	private String doLogin(String email, String password, String loginMethod)
			throws ConnectionException {
		this.email = email;

		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_login"));
		data.add(new BasicNameValuePair("login", email));
		data.add(new BasicNameValuePair("password", password));
		data.add(new BasicNameValuePair("loginMethod", loginMethod));

		return requestToServer(data);
	}

	private boolean checkClientId(String clientId) {
		if (clientId.length() == 0)
			return false;

		for (int i = 0; i < clientId.length(); i++)
			if (!Character.isLetterOrDigit(clientId.charAt(i)))
				return false;

		return true;
	}

	private void setClientId(String clientId) {
		if (clientId != null) {
			if (checkClientId(clientId)) {
				this.clientId = clientId;
				Log.i(Log.connectionTag, email + " logged in!");
				LoginController.getInstance().loggedIn();
			}
		} else {
			this.clientId = null;
		}
	}

	private void loginRequired() throws NotLoggedInException {
		if (!isLoggedIn())
			throw new NotLoggedInException();
	}
}