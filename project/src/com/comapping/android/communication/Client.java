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

import com.comapping.android.controller.LoginController;

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
		if (clientId != null) {
			if (checkClientId(clientId)) {
				this.clientId = clientId;
				Log.i("Comapping", "Communication: "+email+" logged in!");
				LoginController.getInstance().loggedIn();
			}
		} else {
			this.clientId = null;
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
		setClientId(null);

		String passwordHash = MD5Encode(password);

		String salt = doLogin(email, passwordHash, "simple");

		if (salt.length() > 0) {
			if (salt.charAt(0) == '#') {
				salt = salt.substring(1);
				
				autoLoginKey = MD5Encode(password + salt);
				
				setClientId(doLogin(email, autoLoginKey, "withSalt"));
			} else {
				autoLoginKey = "#" + passwordHash;
				
				setClientId(salt);
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
			setClientId(doLogin(email, key.substring(1), "simple"));
		} else {
			setClientId(doLogin(email, key, "flashCookie"));
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
		
		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_logout"));
		data.add(new BasicNameValuePair("clientId", clientId));

		clear();
		
		requestToServer(data);
	}

	public String getComap(String mapId) throws NotLoggedInException, ConnectionException {
		Log.d("Comapping", "Getting comap by " + getEmail());

		loginRequired();

		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "download"));
		data.add(new BasicNameValuePair("format", "comap"));
		data.add(new BasicNameValuePair("clientID", clientId));
		data.add(new BasicNameValuePair("mapid", mapId));

		return requestToServer(data);
	}
}