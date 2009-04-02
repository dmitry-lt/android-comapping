/*
 * LoginView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements LoginView controller
 */

package com.comapping.android.controller;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;

import com.comapping.android.communication.Client;
import com.comapping.android.communication.ConnectionException;
import com.comapping.android.communication.LoginInterruptedException;
import com.comapping.android.storage.Storage;
import com.comapping.android.view.LoginView;

public class LoginActivity extends Activity {
	public static final int RESULT_LOGIN_SUCCESSFUL = 200;
	public static final String LOGIN_ACTIVITY_INTENT = "com.comapping.android.intent.LOGIN";

	private LoginView loginView;

	// use server from MetaMapController
	Client client = null;

	private void saveLoginAndPassword() {
		try {
			Storage.instance.set("email", client.getEmail(this));
			Storage.instance.set("key", client.getAutoLoginKey(this));
		} catch (LoginInterruptedException e) {
			Log.e("Login", "User not logged in");
		}
	}

	private void finishLoginAttempt(final String errorMsg, final boolean remember) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (client.isLoggedIn()) {
					if (remember) {
						saveLoginAndPassword();
					} else {
						Storage.instance.set("key", "");
					}

					setResult(RESULT_LOGIN_SUCCESSFUL);
					finish();
				} else {
					loginView.changeErrorText(errorMsg);
				}
			}
		});
	}

	public void loginClick(final String email, final String password) {
		loginView.changeErrorText("Loading ...");

		new Thread() {
			public void run() {
				try {
					client.login(email, password);
				} catch (ConnectionException e) {
					Log.e("Comapping", "Login: connection exception");
				}
				CheckBox remember = (CheckBox) findViewById(R.id.CheckBox01);

				finishLoginAttempt("Email or password is incorrect", remember.isChecked());
			}
		}.start();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		client = MetaMapActivity.client;
		loginView = new LoginView(this);
		loginView.load();

		if (!Storage.instance.get("key").equals("")) {
			// attempt to autoLogin
			new Thread() {
				public void run() {
					try {
						client.autoLogin(Storage.instance.get("email"), Storage.instance.get("key"));
					} catch (ConnectionException e) {
						Log.e("Comapping", "Login: connection exception");
					}
					finishLoginAttempt("Autologin attempt failed", true);
				}
			}.start();
		} else {
			// manual login
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (!client.isLoggedIn()) {
			client.interruptLogin();
		}
	}
}