/*
 * LoginView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements LoginView controller
 */

package com.comapping.android.controller;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.comapping.android.communication.Client;
import com.comapping.android.storage.Storage;

public class Login {
	// Singleton
	private Login() {

	}

	public static Login instance = new Login();
	private com.comapping.android.view.Login loginView = new com.comapping.android.view.Login();

	public static Login getInstance() {
		return instance;
	}

	// use server from MainController
	Client client = null;

	private void finishLoginAttempt(final String errorMsg) {
		Main.instance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (client.isLoggedIn()) {
					Main.instance.login();
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

				finishLoginAttempt("Email or password is incorrect");
			}
		}.start();
	}

	public void activate() {
		client = Main.instance.client;

		if (!Storage.instance.get("key").equals("")) {
			// attempt to autoLogin
			client.autoLogin(Storage.instance.get("email"), Storage.instance
					.get("key"));
			finishLoginAttempt("Email or password is incorrect");

			if (client.isLoggedIn())
				Main.instance.login();
			else {
				loginView.load("AutoLogin attempt failed");
			}
		} else {
			loginView.load();
		}
	}
}