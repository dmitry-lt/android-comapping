/*
 * Comapping Server
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements bridge to Comapping Web Server
 */

package com.comapping.android.commapingserver;

import static com.comapping.android.commapingserver.ComappingServerHelper.getTextFromResponse;
import static com.comapping.android.commapingserver.ComappingServerHelper.MD5Encode;

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

public class ComappingServer {
	private String serverURL = "";
	private String clientId = null;
	private String autoLoginKey = null;

	// private methods
	private String requestToServer(ArrayList<BasicNameValuePair> data) {
		Log.i("Comapping Server", "Request to server: "+Arrays.toString(data.toArray()));
		
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(serverURL);

		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(data);
		} catch (UnsupportedEncodingException e1) {
			Log.e("Comapping Server", "Unsupported Encoding for Entity");
		}
		
		post.setEntity(entity);

		HttpResponse response = null;
		try {
			// TODO: what if response null ?
			response = client.execute(post);

			String responseText = getTextFromResponse(response);

			Log.i("Comapping Server", "Response from server:" + responseText);

			// TODO: why GetText return null value ?
			if (responseText == null)
				responseText = "";

			return responseText;
		} catch (IOException e) {
			Log.e("Comapping Server", "Error while reading response");
		}
		
		return "";
	}
	
	private String doLogin(String email, String password, String loginMethod) {
		
		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_login"));
		data.add(new BasicNameValuePair("login", email));
		data.add(new BasicNameValuePair("password", password));
		data.add(new BasicNameValuePair("loginMethod", loginMethod));

		return requestToServer(data);
	}

	private boolean checkLoginResult() {
		return (!clientId.equals(""));
		// TODO: write a normal clientId check
	}

	private void autoLogin(String email, String key, String loginMethod) {
		clientId = doLogin(email, key, loginMethod);

		if (checkLoginResult()) {
			autoLoginKey = key;

			Log.i("Comapping Server", email + " logged in" + ":" + this);
		} else
			clientId = null;
	}

	// public methods
	public ComappingServer() {
		this("http://go.comapping.com/cgi-bin/comapping.n");
	}

	public ComappingServer(String serverURL) {
		this.serverURL = serverURL;
	}

	public void login(String email, String password) {
		clientId = null;

		String passwordHash = MD5Encode(password);

		String salt = doLogin(email, passwordHash, "simple");

		if (salt.length() > 0) {
			if (salt.charAt(0) == '#') {
				salt = salt.substring(1);

				autoLogin(email, MD5Encode(password + salt), "withSalt");
			} else {
				clientId = salt;
			}
		} else {
			//login failed
		}
	}

	public String getAutoLoginKey() {
		return autoLoginKey;
	}

	public void autoLogin(String name, String key) {
		autoLogin(name, key, "flashCookie");
	}

	public boolean isLoggedIn() {
		return clientId != null;
	}

	private void loginRequired() throws NotLoggedInException {
		if (!isLoggedIn())
			throw new NotLoggedInException();
	}

	public void logout() throws NotLoggedInException {
		loginRequired();
		clientId = null;
		
		ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_logout"));
		data.add(new BasicNameValuePair("clientId", clientId));

		requestToServer(data);
	}

	public String getComap(String mapId) throws NotLoggedInException {
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