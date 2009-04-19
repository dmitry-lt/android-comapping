/**
 * Class implements client for comapping server.
 * 
 * @author Abishev Timur
 * @version 1.0
 */
package com.comapping.android.communication;

import static com.comapping.android.communication.ClientHelper.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;

import com.comapping.android.Cache;
import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.controller.LoginActivity;
import com.comapping.android.storage.Storage;

public class Client implements MapProvider {
	// constants
	final static private String SIMPLE_LOGIN_METHOD = "simple";
	final static private String COOKIE_LOGIN_METHOD = "flashCookie";
	final static private String WITH_SALT_LOGIN_METHOD = "withSalt";

	final static private int SLEEP_TIME = 100;
	final static public int LOGIN_REQUEST_CODE = 438134;

	final static private char SALT_FLAG = '#';

	final static private int MAX_READ_TIMEOUT = 30 * 1000; // 30 seconds in
															// milliseconds
	final static private int MAX_CONNECT_TIMEOUT = 30 * 1000; // 30 seconds in
																// milliseconds

	// private variables
	private String clientId = null;

	private String email = null;

	private boolean loginInterrupted = false;

	// public methods
	/**
	 * Method for manual login with email and password
	 * 
	 * @param email
	 * @param password
	 * @throws ConnectionException
	 * @throws LoginInterruptedException
	 */
	public void login(String email, String password, boolean remember) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		// if you try to log in, previous user logged out
		clientSideLogout();

		// anyway save email
		Storage.getInstance().set(Storage.EMAIL_KEY, email);

		String passwordHash = md5Encode(password);
		String loginResponse = loginRequest(email, passwordHash, SIMPLE_LOGIN_METHOD);

		String autoLoginKey = null;

		String clientId = null;

		if (loginResponse.charAt(0) == SALT_FLAG) {
			// account with salt
			String salt = loginResponse.substring(1);

			autoLoginKey = md5Encode(password + salt);

			clientId = loginRequest(email, autoLoginKey, WITH_SALT_LOGIN_METHOD);
		} else {
			// account without salt
			autoLoginKey = passwordHash;

			clientId = loginResponse;
		}

		setClientId(clientId);

		if (isLoggedIn() && remember) {
			// save autologin key
			Storage.getInstance().set(Storage.AUTOLOGIN_KEY, autoLoginKey);
		}
	}

	/**
	 * Method for check autologin possibility
	 * 
	 * @return autologin possibility
	 */
	public boolean isAutologinPossible() {
		return !Storage.getInstance().get(Storage.AUTOLOGIN_KEY).equals("");
	}

	/**
	 * Method for automatic login
	 * 
	 * @param email
	 *            Email for login
	 * @param key
	 *            AutoLogin key
	 * @throws ConnectionException
	 * @throws LoginInterruptedException
	 */
	public void autologin() throws ConnectionException, InvalidCredentialsException, LoginInterruptedException {
		String email = Storage.getInstance().get(Storage.EMAIL_KEY);
		String autologinKey = Storage.getInstance().get(Storage.AUTOLOGIN_KEY);

		clientSideLogout();

		setClientId(loginRequest(email, autologinKey, COOKIE_LOGIN_METHOD));

		if (!isLoggedIn()) {
			throw new InvalidCredentialsException(); // TODO: ???
		} else {
			// reSet autologin key
			Storage.getInstance().set(Storage.AUTOLOGIN_KEY, autologinKey);
		}
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
		Cache.clear();

		// clear AUTOLOGIN_KEY anyway
		Storage.getInstance().set(Storage.AUTOLOGIN_KEY, "");
	}

	/**
	 * Method for both client and server side logout
	 * 
	 * @throws ConnectionException
	 */
	public void logout(Activity context) throws ConnectionException {
		if (!isLoggedIn()) {
			Log.i(Log.connectionTag, "logout without logn");
		}

		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_logout"));
		data.add(new BasicNameValuePair("clientId", clientId));

		clientSideLogout();

		try {
			requestToServer(data);
		} catch (LoginInterruptedException e) {
			Log.e(Log.connectionTag, "login interrupted in logout");
		} catch (InvalidCredentialsException e) {
			Log.e(Log.connectionTag, "invalid credentails in logout");
		}
	}

	/**
	 * Method for getting comap file from server
	 * 
	 * @param mapId
	 *            Comap Id
	 * @return Comap in String format
	 * @throws ConnectionException
	 * @throws LoginInterruptedException
	 * @throws InvalidCredentialsException
	 */
	public String getComap(String mapId, Activity context) throws ConnectionException, LoginInterruptedException,
			InvalidCredentialsException {
		Log.d(Log.connectionTag, "getting " + mapId + " comap");

		loginRequired(context);

		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "download"));
		data.add(new BasicNameValuePair("format", "comap"));
		data.add(new BasicNameValuePair("clientID", clientId));
		data.add(new BasicNameValuePair("mapid", mapId));

		return requestToServer(data);
	}

	public void interruptLogin() {
		loginInterrupted = true;
	}

	private String requestToServer(List<BasicNameValuePair> data) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		Log.d(Log.connectionTag, "request to server " + Arrays.toString(data.toArray()));

		URL url = null;
		String responseText = null;
		int code = 200; // all ok

		try {
			url = new URL(Options.SERVER);
		} catch (MalformedURLException e1) {
			Log.e(Log.connectionTag, "Malformed URL Exception!!!");
			throw new ConnectionException();
		}

		try {
			HttpURLConnection connection = null;
			
			if (Options.USE_PROXY) {
				connection = (HttpURLConnection) url.openConnection(Options.proxy);
			} else {
				connection = (HttpURLConnection) url.openConnection();
			}
			
			connection.setReadTimeout(MAX_READ_TIMEOUT);
			connection.setConnectTimeout(MAX_CONNECT_TIMEOUT);

			connection.setDoOutput(true);

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(getPostParameters(data));
			writer.flush();

			code = connection.getResponseCode();

			responseText = getTextFromInputStream(connection.getInputStream());

			writer.close();
		} catch (IOException e) {
			if (code == 403) {
				throw new InvalidCredentialsException();
			} else {
				throw new ConnectionException();
			}
		}

		// Log result
		Log.i(Log.connectionTag, "New server response = " + responseText);
		// Log.d(Log.connectionTag, "New server checksum = " +
		// getBytesSum(responseText));
		Log.d(Log.connectionTag, "New server response code = " + code);

		// DefaultHttpClient client = new DefaultHttpClient();
		//
		// // add proxy if needed
		// if (Options.USE_PROXY) {
		// client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// Options.PROXY_HOST);
		// }
		//
		// // set timeout
		// // client.getParams().setLongParameter(ConnManagerPNames.TIMEOUT,
		// // MAX_TIMEOUT);
		//
		// HttpPost post = new HttpPost(Options.SERVER);
		//
		// UrlEncodedFormEntity entity = null;
		// try {
		// entity = new UrlEncodedFormEntity(data);
		// } catch (UnsupportedEncodingException e1) {
		// Log.e(Log.connectionTag, "unsupported encoding for data in request");
		// }
		//
		// post.setEntity(entity);
		//
		// String responseText = "";
		// int responseStatus = 0;
		//
		// try {
		// HttpResponse response = client.execute(post);
		//
		// responseText = getTextFromResponse(response);
		// responseStatus = response.getStatusLine().getStatusCode();
		// } catch (ClientProtocolException e) {
		// Log.d(Log.connectionTag, "client protocol exception");
		// throw new ConnectionException();
		// } catch (IOException e) {
		// Log.d(Log.connectionTag, "IO exception");
		// throw new ConnectionException();
		// }
		//		
		// Log.i(Log.connectionTag, "response from server: " + responseText);
		// Log.i(Log.connectionTag, "response check sum: " +
		// getBytesSum(responseText));
		// Log.i(Log.connectionTag, "response status code: " + responseStatus);

		if (loginInterrupted) {
			throw new LoginInterruptedException();
		}

		return responseText;
	}

	private String loginRequest(String email, String password, String loginMethod) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		this.email = email;

		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "notifier_login"));
		data.add(new BasicNameValuePair("login", email));
		data.add(new BasicNameValuePair("password", password));
		data.add(new BasicNameValuePair("loginMethod", loginMethod));

		return requestToServer(data);
	}

	private boolean checkClientId(String clientId) {
		// length > 0 ?
		if (clientId.length() == 0) {
			return false;
		}

		// only letters or digits in clientId ?
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
			}
		} else {
			this.clientId = null;
		}
	}

	private void loginRequired(Activity context) throws LoginInterruptedException {
		Log.d(Log.connectionTag, "login required action with login status: " + isLoggedIn());

		if (!isLoggedIn()) {
			loginInterrupted = false;
			context.startActivityForResult(new Intent(LoginActivity.LOGIN_ACTIVITY_INTENT), LOGIN_REQUEST_CODE);

			while (!isLoggedIn() && (!loginInterrupted)) {
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					Log.i(Log.connectionTag, "login required interrupted exception");
				}
			}

			if (loginInterrupted) {
				throw new LoginInterruptedException();
			}
		}
	}
}