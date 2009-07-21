/**
 * Class implements client for comapping server.
 * 
 * @author Non-Abishev Timur
 * @version 1.0
 */
package com.comapping.android.provider.communication;

import static com.comapping.android.provider.communication.ClientHelper.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;

import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.preferences.PreferencesStorage;
import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.provider.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.provider.communication.exceptions.LoginInterruptedException;
import com.comapping.android.storage.SqliteMapCache;

public class Client {

	protected Client(Context context) {
		this.context = context;
	}

	private static CachingClient client;

	public static CachingClient getClient(Context context) {
		if (client == null) {
			if (context == null)
				return null;

			client = new CachingClient(context, new SqliteMapCache(context));
		}

		return client;
	}

	final static String MetaMapId = "meta";
	// constants

	final static private String SIMPLE_LOGIN_METHOD = "simple";
	final static private String COOKIE_LOGIN_METHOD = "flashCookie";
	final static private String WITH_SALT_LOGIN_METHOD = "withSalt";

	final static private int SLEEP_TIME = 100;
	final static public int LOGIN_REQUEST_CODE = 438134;

	final static private char SALT_FLAG = '#';

	final static private int MAX_READ_TIMEOUT = 30 * 1000; // 30 seconds in
	// milliseconds
	final static private int MAX_CONNECT_TIMEOUT = 10 * 1000; // 5 seconds in
	// milliseconds

	// private variables
	private Context context;

	private String clientId = null;

	private String email = null;

	private boolean loginInterrupted = false;
	
	private boolean tryToLoginAgain = true;

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
		clientSideLogout(true);

		// anyway save email
		PreferencesStorage.set(PreferencesStorage.EMAIL_KEY, email, context);

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
			PreferencesStorage.set(PreferencesStorage.AUTOLOGIN_KEY, autoLoginKey, context);
		}
	}

	/**
	 * Method for check autologin possibility
	 * 
	 * @return autologin possibility
	 */
	public boolean isAutologinPossible() {
		return !PreferencesStorage.get(PreferencesStorage.AUTOLOGIN_KEY, "", context).equals("");
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
		String email = PreferencesStorage.get(PreferencesStorage.EMAIL_KEY, "", context);
		String autologinKey = PreferencesStorage.get(PreferencesStorage.AUTOLOGIN_KEY, "", context);

		clientSideLogout(true);

		setClientId(loginRequest(email, autologinKey, COOKIE_LOGIN_METHOD));

		if (!isLoggedIn()) {
			throw new InvalidCredentialsException(); // TODO: ???
		} else {
			// reSet autologin key
			PreferencesStorage.set(PreferencesStorage.AUTOLOGIN_KEY, autologinKey, context);
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
	public void clientSideLogout(boolean isToEmptyAutologin) {
		clientId = null;

		if (isToEmptyAutologin) {
			PreferencesStorage.set(PreferencesStorage.AUTOLOGIN_KEY, "", context);
		}
	}

	/**
	 * Method for both client and server side logout
	 * 
	 * @throws ConnectionException
	 */
	public void logout(boolean isToEmptyAutologin) throws ConnectionException {
		if (!isLoggedIn()) {
			Log.i(Log.CONNECTION_TAG, "logout without login");
			clientSideLogout(isToEmptyAutologin);
		} else {
			List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
			data.add(new BasicNameValuePair("action", "notifier_logout"));
			data.add(new BasicNameValuePair("clientId", clientId));

			clientSideLogout(isToEmptyAutologin);

			try {
				requestToServer(data);
			} catch (LoginInterruptedException e) {
				Log.e(Log.CONNECTION_TAG, "login interrupted in logout");
			} catch (InvalidCredentialsException e) {
				Log.e(Log.CONNECTION_TAG, "invalid credentails in logout");
			}
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
	public String getComap(String mapId) throws ConnectionException, LoginInterruptedException,
			InvalidCredentialsException {
		Log.d(Log.CONNECTION_TAG, "getting " + mapId + " comap");

		loginRequired();

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

	public HttpURLConnection getHttpURLConnection(URL url) throws ConnectionException, IOException {
		if (PreferencesStorage.getBoolean(PreferencesStorage.USE_PROXY_KEY, PreferencesStorage.USE_PROXY_DEFAULT_VALUE,
				context)) {
			String proxyHost = PreferencesStorage.get(PreferencesStorage.PROXY_HOST_KEY, "", context);
			int proxyPort = 0;

			try {
				proxyPort = Integer.parseInt(PreferencesStorage.get(PreferencesStorage.PROXY_PORT_KEY, "", context));
			} catch (Exception e) {
				throw new ConnectionException();
			}

			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);

			// TODO: wrong work after login with wrong proxy name and password
			// :-(
			if (PreferencesStorage.getBoolean(PreferencesStorage.USE_PROXY_AUTH_KEY,
					PreferencesStorage.USE_PROXY_AUTH_DEFAULT_VALUE, context)) {
				String proxyUser = PreferencesStorage.get(PreferencesStorage.PROXY_NAME_KEY, "", context);
				String proxyPassword = PreferencesStorage.get(PreferencesStorage.PROXY_PASSWORD_KEY, "", context);;
				StringBuilder encodedInfo = new StringBuilder();
				for (byte b : Base64Encoder.encodeBase64((proxyUser + ":" + proxyPassword).getBytes())) {
					encodedInfo.append((char) b);
				}
				connection.setRequestProperty("Proxy-Authorization", "Basic " + encodedInfo);
			}

			return connection;
		} else {
			return (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
		}
	}

	private String requestToServer(List<BasicNameValuePair> data) throws ConnectionException,
			LoginInterruptedException, InvalidCredentialsException {
		Log.d(Log.CONNECTION_TAG, "request to server " + Arrays.toString(data.toArray()));

		URL url = null;
		String responseText = null;
		int code = 200; // all ok

		try {
			url = new URL(Options.SERVER);
		} catch (MalformedURLException e1) {
			Log.e(Log.CONNECTION_TAG, "Malformed URL Exception!!!");
			throw new ConnectionException();
		}

		try {
			HttpURLConnection connection = getHttpURLConnection(url);

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
			} else if (code == 401 || e.getMessage().equals("Received authentication challenge is null")) {
				if (tryToLoginAgain) {
					tryToLoginAgain = false;					
					clientSideLogout(false);
					loginRequired();
					String res = requestToServer(data);
					tryToLoginAgain = true;
					return res;
				}
				
				Log.w(Log.CONNECTION_TAG, e.toString());
				Log.w(Log.CONNECTION_TAG, "code=" + code);
				tryToLoginAgain = true;
				throw new ConnectionException();
			} else {
				Log.w(Log.CONNECTION_TAG, e.toString());
				Log.w(Log.CONNECTION_TAG, "code=" + code);
				throw new ConnectionException();
			}
		}

		// Log result
		// Log.d(Log.CONNECTION_TAG, "New server response = " + responseText);
		if (responseText != null) {
			Log.d(Log.CONNECTION_TAG, "New server checksum = " + getBytesSum(responseText));
		}
		Log.d(Log.CONNECTION_TAG, "New server response code = " + code);

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
				Log.i(Log.CONNECTION_TAG, email + " logged in!");
			}
		} else {
			this.clientId = null;
		}
	}

	public String getClientId() {
		return clientId;
	}

	public void loginRequired() throws LoginInterruptedException {
		Log.d(Log.CONNECTION_TAG, "login required action with login status: " + isLoggedIn());

		if (!isLoggedIn()) {
			loginInterrupted = false;
			Intent intent = new Intent(LoginActivity.LOGIN_ACTIVITY_INTENT);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);

			while (!isLoggedIn() && (!loginInterrupted)) {
				try {
					Thread.sleep(SLEEP_TIME);
					// Log.d(Log.CONNECTION_TAG, "waiting login...");
				} catch (InterruptedException e) {
					Log.i(Log.CONNECTION_TAG, "login required interrupted exception");
				}
			}

			if (loginInterrupted) {
				throw new LoginInterruptedException();
			}
		}
	}

	public int getSize(String mapId) throws ConnectionException {

		List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
		data.add(new BasicNameValuePair("action", "download"));
		data.add(new BasicNameValuePair("format", "comap"));
		data.add(new BasicNameValuePair("clientID", clientId));
		data.add(new BasicNameValuePair("mapid", mapId));

		return getSizeFromServer(data);
	}

	private int getSizeFromServer(List<BasicNameValuePair> data)  {
		URL url = null;
		int res = -1;
		try {
			url = new URL(Options.SERVER);
		} catch (MalformedURLException e1) {
			Log.e(Log.CONNECTION_TAG, "Malformed URL Exception!!!");
			//throw new ConnectionException();
		}
		try {
			HttpURLConnection connection = getHttpURLConnection(url);

			connection.setReadTimeout(MAX_READ_TIMEOUT);
			connection.setConnectTimeout(MAX_CONNECT_TIMEOUT);

			connection.setDoOutput(true);
			 
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(getPostParameters(data));
			//writer.flush();
			res = connection.getContentLength();

		} catch (IOException e) {
			Log.e(Log.CONNECTION_TAG, e.toString());
		} catch (Exception e){
			Log.e(Log.CONNECTION_TAG, e.toString());
		}
		

		return res;
	}
}