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
				} else
					changeErrorText(errorMsg);
			}
		});
	}

	private void loginClick(final String email, final String password) {
		changeErrorText("Loading ...");

		new Thread() {
			public void run() {
				client.login(email, password);

				finishLoginAttempt("Email or password is incorrect");
			}
		}.start();
	}

	private void changeErrorText(final String error) {
		final TextView errorText = (TextView) Main.instance
				.findViewById(R.id.error);

		errorText.setText(error);
	}

	private void loadLoginView() {
		Main.instance.setContentView(R.layout.login);

		// bind login button
		Button loginButton = (Button) Main.instance
				.findViewById(R.id.login);

		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String email = ((TextView) Main.instance
						.findViewById(R.id.email)).getText().toString();
				final String password = ((TextView) Main.instance
						.findViewById(R.id.password)).getText().toString();

				loginClick(email, password);
			}
		});
	}

	private void loadLoginView(final String error) {
		loadLoginView();
		changeErrorText(error);
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
				loadLoginView("AutoLogin attempt failed");
			}
		} else
			loadLoginView();
	}
}