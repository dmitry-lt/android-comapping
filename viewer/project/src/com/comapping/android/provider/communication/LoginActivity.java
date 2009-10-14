/*
 * LoginView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements LoginView controller
 */

package com.comapping.android.provider.communication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;


import com.comapping.android.Log;
import com.comapping.android.R;
import com.comapping.android.preferences.PreferencesActivity;
import com.comapping.android.preferences.PreferencesStorage;
import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.provider.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.provider.communication.exceptions.LoginInterruptedException;

public class LoginActivity extends Activity {
	private static final int RESULT_LOGIN_SUCCESSFUL = 200;

	public static final String LOGIN_ACTIVITY_INTENT = "com.comapping.android.intent.LOGIN";

	// messages
	private static final String LOGIN_ATTEMPT_MESSAGE = "Login attempt...";
	private static final String AUTOLOGIN_ATTEMPT_FAILED_MESSAGE = "Autologin attempt failed";
	private static final String CONNECTION_ERROR_MESSAGE = "Connection error";
	private static final String EMAIL_OR_PASSWORD_INCORRECT_MESSAGE = "E-mail or password is incorrect";
	private static final String UNKNOWN_RESULT_MESSAGE = "Unknown result";

	private static final int MENU_PREFERENCES = Menu.FIRST;
	// private LoginView loginView;
	private ProgressDialog splash = null;

	private static boolean isWorking = false;
	private static Thread workThread = null;
	private static String stateMsg = "";
	

	private void finishLoginAttempt(final String errorMsg) {

		final Activity context = this;
		runOnUiThread(new Runnable() {

			public void run() {
				CachingClient client = Client.getClient(context);
				if (client.isLoggedIn()) {
					setResult(RESULT_LOGIN_SUCCESSFUL);
					finish();
				} else {
					((TextView) findViewById(R.id.error)).setText(errorMsg);
					((TextView) findViewById(R.id.password)).setText("");
				}
			}
		});
	}

	void startWork(final String email, final String password,
			final boolean remember) {
		if (isWorking) {
			splashDeactivate();
			workThread.stop();
		}

		final Activity context = this;

		workThread = new Thread() {
			@Override
			public void run() {
				isWorking = true;
				stateMsg = UNKNOWN_RESULT_MESSAGE;
				CachingClient client = Client.getClient(context);

				try {
					client.login(email, password, remember);
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

				splashDeactivate();

				isWorking = false;
				workThread = null;
			}
		};
		workThread.start();
	}

	void startAutologin() {
		if (isWorking) {
			splashDeactivate();
			workThread.stop();
		}

		final Activity context = this;
		workThread = new Thread() {
			@Override
			public void run() {
				isWorking = true;
				stateMsg = AUTOLOGIN_ATTEMPT_FAILED_MESSAGE;
				CachingClient client = Client.getClient(context);

				try {

					client.autologin();
				} catch (ConnectionException e) {
					Log.e(Log.LOGIN_TAG, "connection exception");
					stateMsg = CONNECTION_ERROR_MESSAGE;
				} catch (LoginInterruptedException e) {
					Log.e(Log.LOGIN_TAG, "login interrupted");
				} catch (InvalidCredentialsException e) {
					Log.e(Log.LOGIN_TAG, "invalid credentails");
				}

				finishLoginAttempt(stateMsg);

				if (!client.isLoggedIn()) {
					splashDeactivate();
				}

				isWorking = false;
				workThread = null;
			}
		};
		workThread.start();
	}

	public void loginClick() {

//		Log.d("Logic Activity", "splash activate start");
		splashActivate(LOGIN_ATTEMPT_MESSAGE);
//		Log.d("Logic Activity", "splash activate end");
		final String email = ((TextView) findViewById(R.id.eMail)).getText()
				.toString();
		final String password = ((TextView) findViewById(R.id.password))
				.getText().toString();
		final Boolean remember = ((CheckBox) findViewById(R.id.rememberUserCheckBox))
				.isChecked();

		startWork(email, password, remember);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		Log.d(Log.LOGIN_TAG, "LoginActivity.onCreate()");
		super.onCreate(savedInstanceState);

		stateMsg = "";
		
		CachingClient client = Client.getClient(this);
		if (client.isLoggedIn()) {
			setResult(RESULT_LOGIN_SUCCESSFUL);
			finish();
			return;
		}

		// set login Layout
		setContentView(R.layout.login);

		// bind login button
		// loginView = new LoginView(this);

		((Button) findViewById(R.id.login))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						loginClick();
					}
				});

		// set auto login
		if (client.isAutologinPossible()) {
			String email = PreferencesStorage.get(PreferencesStorage.EMAIL_KEY,
					null, this);
			if (email != null) {
				((TextView) findViewById(R.id.eMail)).setText(email);
				((TextView) findViewById(R.id.password)).requestFocus();
			}
		}

		if (isWorking) {
//			Log.d("Logic Activity", "splash activate start");
			splashActivate(LOGIN_ATTEMPT_MESSAGE);
//			Log.d("Logic Activity", "splash activate end");
		} else {
			((TextView) findViewById(R.id.error)).setText(stateMsg);
			if (client.isAutologinPossible()) {
				// autologin attempt
				((TextView) findViewById(R.id.password)).setText("******");
//				Log.d("Logic Activity", "splash activate start");
				splashActivate(LOGIN_ATTEMPT_MESSAGE);
//				Log.d("Logic Activity", "splash activate end");

				startAutologin();
			} else {
				// manual login
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		splashDeactivate();

		CachingClient client = Client.getClient(this);

		if (!client.isLoggedIn()) {
			client.interruptLogin();
		}
	}

	private void splashActivate(final String message) {
		final LoginActivity thisActivity = this;
		runOnUiThread(new Runnable() {
			public void run() {
				if (splash == null) {
					splash = ProgressDialog.show(thisActivity, "Comapping",
							message);
				} else {
					splash.setMessage(message);
				}
			}
		});
	}

	private void splashDeactivate() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (splash != null) {
					splash.dismiss();
					splash = null;
				}
			}
		});
	}

	// ===========================================================
	// Options Menu
	// ===========================================================

	public void showPreferencesDialog() {
		startActivity(new Intent(
				PreferencesActivity.PREFERENCES_ACTIVITY_INTENT));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Build the menu that are shown when login.
		menu.add(0, MENU_PREFERENCES, 0, "Preferences")
		.setIcon(android.R.drawable.ic_menu_preferences);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_PREFERENCES:
				showPreferencesDialog();
				return true;
		}
		return false;
	}
}