/**
 * Class implements client for comapping server.
 * 
 * @author Abishev Timur
 * @version 1.0
 */
package com.comapping.android.communication;

import static com.comapping.android.communication.ClientHelper.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	final static private char SALT_FLAG = '#';
	final static private String SIMPLE_LOGIN_METHOD = "simple";
	final static private String COOKIE_LOGIN_METHOD = "flashCookie";
	final static private String WITH_SALT_LOGIN_METHOD = "withSalt";

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

		String passwordHash = md5Encode(password);
		String loginResponse = loginRequest(email, passwordHash, SIMPLE_LOGIN_METHOD);

		if (loginResponse.length() > 0) {
			// response from server is valid
			String clientId;

			if (loginResponse.charAt(0) == SALT_FLAG) {
				// account with salt
				String salt = loginResponse.substring(1);

				autoLoginKey = SALT_FLAG + md5Encode(password + salt);

				clientId = loginRequest(email, autoLoginKey.substring(1), WITH_SALT_LOGIN_METHOD);
			} else {
				// account without salt
				autoLoginKey = passwordHash;

				clientId = loginResponse;
			}

			setClientId(clientId);
		} else {
			// login failed
		}
	}

	/**
	 * Method for automatic login with AutoLogin key
	 * 
	 * @param email
	 *            Email for login
	 * @param key
	 *            AutoLogin key
	 * @throws ConnectionException
	 */
	public void autoLogin(String email, String key) throws ConnectionException {
		autoLoginKey = key;

		if ((key.length() > 0) && (key.charAt(0) == SALT_FLAG)) {
			// account with salt
			setClientId(loginRequest(email, key.substring(1), COOKIE_LOGIN_METHOD));
		} else {
			// account without salt
			setClientId(loginRequest(email, key, SIMPLE_LOGIN_METHOD));
		}
	}

	/**
	 * Method for AutoLogin key getting
	 * 
	 * @return AutoLogin key
	 * @throws NotLoggedInException
	 */
	public String getAutoLoginKey() throws NotLoggedInException {
		isLoggedInAssertion();

		return autoLoginKey;
	}

	/**
	 * Method for user email getting
	 * 
	 * @return User email
	 * @throws NotLoggedInException
	 */
	public String getEmail() throws NotLoggedInException {
		isLoggedInAssertion();

		return email;
	}

	/**
	 * Method for check user login status
	 * 
	 * @return True if user is logged in and false otherwise
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
		isLoggedInAssertion();

		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_logout"));
		data.add(new BasicNameValuePair("clientId", clientId));

		clientSideLogout();

		requestToServer(data);
	}

	/**
	 * Method for getting comap file from server
	 * 
	 * @param mapId
	 *            Comap Id
	 * @return Comap in String format
	 * @throws NotLoggedInException
	 * @throws ConnectionException
	 */
	public String getComap(String mapId) throws NotLoggedInException, ConnectionException {
		Log.d(Log.connectionTag, "getting " + mapId + " comap");

		isLoggedInAssertion();

		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "download"));
		data.add(new BasicNameValuePair("format", "comap"));
		data.add(new BasicNameValuePair("clientID", clientId));
		data.add(new BasicNameValuePair("mapid", mapId));

		return requestToServer(data);
	}

	// private methods
	private String fakeRequestToServer(List<BasicNameValuePair> data) throws ConnectionException {
		String response = "";

		if (data.get(0).getValue().equals("download")) {
			// "get comap" request
			String mapId = data.get(3).getValue();
			try {
				response = getTextFromInputStream(new FileInputStream(Options.COMAP_FILE_SERVER + mapId + ".comap"));
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

	private String requestToServer(List<BasicNameValuePair> data) throws ConnectionException {
		Log.d(Log.connectionTag, "request to server " + Arrays.toString(data.toArray()));

		if (Options.FAKE_SERVER) {
			return fakeRequestToServer(data);
		}

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(Options.SERVER);

		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(data);
		} catch (UnsupportedEncodingException e1) {
			Log.e(Log.connectionTag, "unsupported encoding for data in request");
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

	private String loginRequest(String email, String password, String loginMethod) throws ConnectionException {
		this.email = email;

		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_login"));
		data.add(new BasicNameValuePair("login", email));
		data.add(new BasicNameValuePair("password", password));
		data.add(new BasicNameValuePair("loginMethod", loginMethod));

		return requestToServer(data);
	}

	private boolean checkClientId(String clientId) {
		if (clientId.length() == 0) {
			return false;
		}

		for (int i = 0; i < clientId.length(); i++) {
			if (!Character.isLetterOrDigit(clientId.charAt(i))) {
				return false;
			}
		}

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

	private void isLoggedInAssertion() throws NotLoggedInException {
		if (!isLoggedIn()) {
			throw new NotLoggedInException();
		}
	}
}