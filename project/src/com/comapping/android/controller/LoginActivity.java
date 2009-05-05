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
import android.widget.CheckBox;

import com.comapping.android.Log;
import com.comapping.android.communication.CachingClient;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.view.LoginView;

public class LoginActivity extends Activity {
	private static final int RESULT_LOGIN_SUCCESSFUL = 200;

	public static final String LOGIN_ACTIVITY_INTENT = "com.comapping.android.intent.LOGIN";

	// messages
	private static final String LOGIN_ATTEMPT_MESSAGE = "Login attempt...";
	private static final String AUTOLOGIN_ATTEMPT_FAILED_MESSAGE = "Autologin attempt failed";
	private static final String CONNECTION_ERROR_MESSAGE = "Connection error";
	private static final String EMAIL_OR_PASSWORD_INCORRECT_MESSAGE = "E-mail or password is incorrect";
	private static final String UNKNOWN_RESULT_MESSAGE = "Unknown result";

	private LoginView loginView;

	CachingClient client = null;

	private void finishLoginAttempt(final String errorMsg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (client.isLoggedIn()) {
					setResult(RESULT_LOGIN_SUCCESSFUL);
					finish();
				} else {
					loginView.setErrorText(errorMsg);
					loginView.setPasswordText("");
				}
			}
		});
	}

	public void loginClick(final String email, final String password) {
		loginView.splashActivate(LOGIN_ATTEMPT_MESSAGE);

		new Thread() {
			public void run() {
				String errorMsg = UNKNOWN_RESULT_MESSAGE;

				CheckBox remember = (CheckBox) findViewById(R.id.CheckBox01);

				try {
					client.login(email, password, remember.isChecked());
				} catch (ConnectionException e) {
					Log.e(Log.loginTag, "connection exception");
					errorMsg = CONNECTION_ERROR_MESSAGE;
				} catch (LoginInterruptedException e) {
					Log.e(Log.loginTag, "login interrupted");
				} catch (InvalidCredentialsException e) {
					Log.e(Log.loginTag, "invalid credentails");
					errorMsg = EMAIL_OR_PASSWORD_INCORRECT_MESSAGE;
				}

				finishLoginAttempt(errorMsg);

				if (!client.isLoggedIn()) {
					loginView.splashDeactivate();
				}
			}
		}.start();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		client = MetaMapActivity.client;

		loginView = new LoginView(this);
		loginView.load();

		if (client.isAutologinPossible()) {
			// autologin attempt
			loginView.setPasswordText("******");

			loginView.splashActivate(LOGIN_ATTEMPT_MESSAGE);

			new Thread() {
				public void run() {
					String errorMsg = AUTOLOGIN_ATTEMPT_FAILED_MESSAGE;

					try {
						client.autologin();
					} catch (ConnectionException e) {
						Log.e(Log.loginTag, "connection exception");
						errorMsg = CONNECTION_ERROR_MESSAGE;
					} catch (LoginInterruptedException e) {
						Log.e(Log.loginTag, "login interrupted");
					} catch (InvalidCredentialsException e) {
						Log.e(Log.loginTag, "invalid credentails");
					}

					finishLoginAttempt(errorMsg);

					if (!client.isLoggedIn()) {
						loginView.splashDeactivate();
					}
				}
			}.start();
		} else {
			// manual login
		}
	}

	@Override
	protected void onDestroy() {
		loginView.splashDeactivate();

		super.onDestroy();

		if (!client.isLoggedIn()) {
			client.interruptLogin();
		}
	}
}