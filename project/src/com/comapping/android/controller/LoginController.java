/*
 * LoginView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements LoginView controller
 */

package com.comapping.android.controller;

import android.util.Log;
import android.widget.CheckBox;

import com.comapping.android.communication.Client;
import com.comapping.android.communication.ConnectionException;
import com.comapping.android.communication.NotLoggedInException;
import com.comapping.android.storage.Storage;
import com.comapping.android.view.LoginView;

public class LoginController {
	// Singleton
	private LoginController() {
	}

	private static LoginController instance = new LoginController();
	public static LoginController getInstance() {
		return instance;
	}

	private LoginView loginView = new LoginView();

	// use server from MainController
	Client client = null;

	private void saveLoginAndPassword() {
		try {
			Storage.instance.set("email", client.getEmail());
			Storage.instance.set("key", client.getAutoLoginKey());
		} catch (NotLoggedInException e) {
			Log.e("Login", "User not logged in");
		}
	}

	private void finishLoginAttempt(final String errorMsg,
			final boolean remember) {
		MainController.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (client.isLoggedIn()) {
					if (remember) {
						saveLoginAndPassword();
					} else {
						Storage.instance.set("key", "");
					}

					MainController.getInstance().login();
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
				CheckBox remember = (CheckBox) MainController.getInstance()
						.findViewById(R.id.CheckBox01);

				finishLoginAttempt("Email or password is incorrect", remember
						.isChecked());
			}
		}.start();
	}

	public void loggedIn() {
		
	}
	
	public void activate() {
		client = MainController.getInstance().client;

		if (!Storage.instance.get("key").equals("")) {
			// attempt to autoLogin
			new Thread() {
				public void run() {
					try {
						client.autoLogin(Storage.instance.get("email"),
								Storage.instance.get("key"));
					} catch (ConnectionException e) {
						Log.e("Comapping", "Login: connection exception");
					}
					finishLoginAttempt("Autologin attempt failed", true);
				}
			}.start();
		} else {
			loginView.load();
		}
	}
}