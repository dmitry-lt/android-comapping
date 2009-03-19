/*
 * Comapping Server
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements bridge to Comapping Web Server
 */

package com.comapping.android.communication;

import static com.comapping.android.communication.ClientHelper.getTextFromResponse;
import static com.comapping.android.communication.ClientHelper.MD5Encode;

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

import android.util.Log;

public class Client {
	private String serverURL = "";
	private String clientId = null;
	private String email = null;
	private String autoLoginKey = null;

	// private methods
	private String requestToServer(ArrayList<BasicNameValuePair> data) throws ConnectionException {
		Log.d("Comapping", "Communication: request to server "
				+ Arrays.toString(data.toArray()));

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(serverURL);

		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(data);
		} catch (UnsupportedEncodingException e1) {
			Log.e("Comapping", "Communication: unsupported encoding for data in request");
		}

		post.setEntity(entity);
		
		String responseText = "";
		
		try {
			HttpResponse response = client.execute(post);
			responseText = getTextFromResponse(response);
		} catch (ClientProtocolException e) {
			Log.d("Comapping", "Communication: client protocol exception");
			throw new ConnectionException();
		} catch (IOException e) {
			Log.d("Comapping", "Communication: IO exception");
			throw new ConnectionException();
		}
		
		Log.i("Comapping", "Communication: response from server: " + responseText);
		
		return responseText;
	}

	private String doLogin(String email, String password, String loginMethod) throws ConnectionException {
		this.email = email;

		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_login"));
		data.add(new BasicNameValuePair("login", email));
		data.add(new BasicNameValuePair("password", password));
		data.add(new BasicNameValuePair("loginMethod", loginMethod));

		return requestToServer(data);
	}

	private boolean checkClientId(String clientId) {
		if (clientId.length() == 0) return false;
		
		for (int i = 0; i < clientId.length(); i++)
			if (!Character.isLetterOrDigit(clientId.charAt(i))) return false;
		
		return true;
	}
	
	private void setClientId(String clientId) {
		/*if (clientId != null) {
			if (checkClientId(clientId)) {
				LoginController
			}
		} else {
			this.clientId = null;
		}*/
		// TODO: write a normal clientId check
	}

	private boolean checkLoginResult() {
		if (checkClientId(clientId)) {
			return true;
		} else {
			clientId = null;
			return false;
		}
	}
	
	private void autoLogin(String email, String key, String loginMethod) throws ConnectionException {
		clientId = doLogin(email, key, loginMethod);

		if (checkLoginResult()) {
			autoLoginKey = key;

			Log.i("Comapping Server", email + " logged in" + ":" + this);
		}
	}

	// public methods
	public Client() {
		this("http://go.comapping.com/cgi-bin/comapping.n");
	}

	public Client(String serverURL) {
		this.serverURL = serverURL;
	}

	public void login(String email, String password) throws ConnectionException {
		clientId = null;

		String passwordHash = MD5Encode(password);

		String salt = doLogin(email, passwordHash, "simple");

		if (salt.length() > 0) {
			if (salt.charAt(0) == '#') {
				salt = salt.substring(1);

				autoLogin(email, MD5Encode(password + salt), "withSalt");
			} else {
				clientId = salt;
				autoLoginKey = "#" + passwordHash;
			}
		} else {
			// login failed
		}
	}

	public String getAutoLoginKey() throws NotLoggedInException {
		loginRequired();

		return autoLoginKey;
	}

	public String getEmail() throws NotLoggedInException {
		loginRequired();

		return email;
	}

	public void autoLogin(String email, String key) throws ConnectionException {
		autoLoginKey = key;

		if ((key.length() > 0) && (key.charAt(0) == '#')) {
			clientId = doLogin(email, key.substring(1), "simple");

			checkLoginResult();
		} else {
			autoLogin(email, key, "flashCookie");
		}
	}

	public boolean isLoggedIn() {
		return clientId != null;
	}

	private void loginRequired() throws NotLoggedInException {
		if (!isLoggedIn())
			throw new NotLoggedInException();
	}

	public void clear() {
		clientId = null;
	}

	public void logout() throws NotLoggedInException, ConnectionException {
		loginRequired();
		clientId = null;

		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_logout"));
		data.add(new BasicNameValuePair("clientId", clientId));

		requestToServer(data);
	}

	public String getComap(String mapId) throws NotLoggedInException, ConnectionException {
		Log.i("Comapping Server", "get comap by " + clientId);

		loginRequired();

		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "download"));
		data.add(new BasicNameValuePair("format", "comap"));
		data.add(new BasicNameValuePair("clientID", clientId));
		data.add(new BasicNameValuePair("mapid", mapId));

		return requestToServer(data);
	}
}