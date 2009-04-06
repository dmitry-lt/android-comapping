/*
 * LoginView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements LoginView controller
 */

package com.comapping.android.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.CheckBox;

import com.comapping.android.Log;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.ConnectionException;
import com.comapping.android.communication.LoginInterruptedException;
import com.comapping.android.storage.Storage;
import com.comapping.android.view.LoginView;

public class LoginActivity extends Activity {
	private static final int RESULT_LOGIN_SUCCESSFUL = 200;
	private static final int AUTOLOGIN_ATTEMPT_DIALOG = 3542352;

	public static final String LOGIN_ACTIVITY_INTENT = "com.comapping.android.intent.LOGIN";

	// messages
	private static final String AUTOLOGIN_ATTEMPT_MESSAGE = "Autologin attempt...";
	private static final String AUTOLOGIN_ATTEMPT_FAILED_MESSAGE = "Autologin attempt failed";
	private static final String CONNECTION_ERROR_MESSAGE = "Connection error";
	private static final String LOADING_MESSAGE = "Loading...";
	private static final String EMAIL_OR_PASSWORD_INCORRECT_MESSAGE = "Email or password is incorrect";

	private LoginView loginView;

	// use server from MetaMapController
	Client client = null;

	// dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		// DialogInterface.OnCancelListener autologinCancelled = new
		// DialogInterface.OnCancelListener() {
		// @Override
		// public void onCancel(DialogInterface dialog) {
		// finishLoginAttempt("Autologin canceled", true);
		// }
		// };

		switch (id) {
		case AUTOLOGIN_ATTEMPT_DIALOG:
			return new AlertDialog.Builder(this).setMessage(AUTOLOGIN_ATTEMPT_MESSAGE).create();
		}

		// default
		return null;
	}

	private void saveLoginAndPassword() {
		try {
			Storage.instance.set("email", client.getEmail(this));
			Storage.instance.set("key", client.getAutoLoginKey(this));
		} catch (LoginInterruptedException e) {
			Log.e(Log.loginTag, "login interrupted");
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
					loginView.setErrorText(errorMsg);
					loginView.setPasswordText("");
				}
			}
		});
	}

	public void loginClick(final String email, final String password) {
		loginView.setErrorText(LOADING_MESSAGE);

		new Thread() {
			public void run() {
				String errorMsg = EMAIL_OR_PASSWORD_INCORRECT_MESSAGE;

				try {
					client.login(email, password);
				} catch (ConnectionException e) {
					Log.e(Log.loginTag, "connection exception");
					errorMsg = CONNECTION_ERROR_MESSAGE;
				} catch (LoginInterruptedException e) {
					Log.e(Log.loginTag, "login interrupted");
				}

				CheckBox remember = (CheckBox) findViewById(R.id.CheckBox01);

				finishLoginAttempt(errorMsg, remember.isChecked());
			}
		}.start();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		client = MetaMapActivity.client;
		loginView = new LoginView(this);
		loginView.load();

		if (!Storage.getInstance().get("key").equals("")) {
			// autologin attempt
			loginView.setEmailText(Storage.getInstance().get("email"));
			loginView.setPasswordText("******");

			showDialog(AUTOLOGIN_ATTEMPT_DIALOG);

			new Thread() {
				public void run() {
					String errorMsg = AUTOLOGIN_ATTEMPT_FAILED_MESSAGE;

					try {
						client.autoLogin(Storage.instance.get("email"), Storage.instance.get("key"));
					} catch (ConnectionException e) {
						Log.e(Log.loginTag, "connection exception");
						errorMsg = CONNECTION_ERROR_MESSAGE;
					} catch (LoginInterruptedException e) {
						Log.e(Log.loginTag, "login interrupted");
					}

					finishLoginAttempt(errorMsg, true);

					if (!client.isLoggedIn()) {
						removeDialog(AUTOLOGIN_ATTEMPT_DIALOG);
					}
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