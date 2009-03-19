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
import com.comapping.android.communication.NotLoggedInException;
import com.comapping.android.storage.Storage;

public class Login {
	// Singleton
	private Login() {
	}

	private static Login instance = new Login();
	public static Login getInstance() {
		return instance;
	}

	private com.comapping.android.view.Login loginView = new com.comapping.android.view.Login();

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
		Main.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (client.isLoggedIn()) {
					if (remember) {
						saveLoginAndPassword();
					} else {
						Storage.instance.set("key", "");
					}

					Main.getInstance().login();
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
				client.login(email, password);

				CheckBox remember = (CheckBox) Main.getInstance()
						.findViewById(R.id.CheckBox01);

				finishLoginAttempt("Email or password is incorrect", remember
						.isChecked());
			}
		}.start();
	}

	public void activate() {
		client = Main.getInstance().client;

		if (!Storage.instance.get("key").equals("")) {
			// attempt to autoLogin
			new Thread() {
				public void run() {
					client.autoLogin(Storage.instance.get("email"),
							Storage.instance.get("key"));
					finishLoginAttempt("Autologin attempt failed", true);
				}
			}.start();
		} else {
			loginView.load();
		}
	}
}