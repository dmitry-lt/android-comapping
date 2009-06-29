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

	private static boolean isWorking = false;
	private static Thread workThread = null;
	private static String stateMsg = "";

	private void finishLoginAttempt(final String errorMsg) {
		runOnUiThread(new Runnable() {
			
			public void run() {
				if (MetaMapActivity.client.isLoggedIn()) {
					setResult(RESULT_LOGIN_SUCCESSFUL);
					finish();
				} else {
					loginView.setErrorText(errorMsg);
					loginView.setPasswordText("");
				}
			}
		});
	}

	void startWork(final String email, final String password,
			final boolean remember) {
		if (isWorking)
		{
			loginView.splashDeactivate();
			workThread.stop();
		}

		workThread = new Thread() {
			public void run() {
				isWorking = true;
				stateMsg = UNKNOWN_RESULT_MESSAGE;

				try {
					MetaMapActivity.client.login(email, password, remember);
				} catch (ConnectionException e) {
					Log.e(Log.LOGIN_TAG, "connection exception");
					stateMsg = CONNECTION_ERROR_MESSAGE;
				} catch (LoginInterruptedException e) {
					Log.e(Log.LOGIN_TAG, "login interrupted");
				} catch (InvalidCredentialsException e) {
					Log.e(Log.LOGIN_TAG, "invalid credentails");
					stateMsg = EMAIL_OR_PASSWORD_INCORRECT_MESSAGE;
				}

				finishLoginAttempt(stateMsg);

				loginView.splashDeactivate();

				isWorking = false;
				workThread = null;
			}
		};
		workThread.start();
	}

	void startAutologin() {
		if (isWorking)
		{
			loginView.splashDeactivate();
			workThread.stop();
		}
		
		workThread = new Thread() {
			public void run() {
				isWorking = true;
				stateMsg = AUTOLOGIN_ATTEMPT_FAILED_MESSAGE;

				try {
					MetaMapActivity.client.autologin();
				} catch (ConnectionException e) {
					Log.e(Log.LOGIN_TAG, "connection exception");
					stateMsg = CONNECTION_ERROR_MESSAGE;
				} catch (LoginInterruptedException e) {
					Log.e(Log.LOGIN_TAG, "login interrupted");
				} catch (InvalidCredentialsException e) {
					Log.e(Log.LOGIN_TAG, "invalid credentails");
				}

				finishLoginAttempt(stateMsg);

				if (!MetaMapActivity.client.isLoggedIn()) {
					loginView.splashDeactivate();
				}

				isWorking = false;
				workThread = null;
			}
		};
		workThread.start();
	}

	public void loginClick(final String email, final String password) {
		loginView.splashActivate(LOGIN_ATTEMPT_MESSAGE);
		CheckBox remember = (CheckBox) findViewById(R.id.CheckBox01);
		startWork(email, password, remember.isChecked());
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (MetaMapActivity.client.isLoggedIn()) {
			setResult(RESULT_LOGIN_SUCCESSFUL);
			finish();
			return;
		}

		loginView = new LoginView(this);
		loginView.load();
		if (isWorking) {
			loginView.splashActivate(LOGIN_ATTEMPT_MESSAGE);
		} else {
			loginView.setErrorText(stateMsg);
			if (MetaMapActivity.client.isAutologinPossible()) {
				// autologin attempt
				loginView.setPasswordText("******");

				loginView.splashActivate(LOGIN_ATTEMPT_MESSAGE);

				startAutologin();
			} else {
				// manual login
			}
		}
	}
	
//	
//	protected void onPause() {
//		loginView.splashDeactivate();
//
//		super.onDestroy();
//
//		if (!MetaMapActivity.client.isLoggedIn()) {
//			stateMsg = "widow is paused";
//			MetaMapActivity.client.interruptLogin();
//		}
//	}
//
	
	protected void onDestroy() {
		loginView.splashDeactivate();

		super.onDestroy();

//		if (!MetaMapActivity.client.isLoggedIn()) {
//			stateMsg = "widow is destroyed";
//			MetaMapActivity.client.interruptLogin();
//		}
	}
}