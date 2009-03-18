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

import com.comapping.android.commapingserver.ComappingServer;
import com.comapping.android.commapingserver.ComappingServer.Status;
import com.comapping.android.storage.Storage;

public class LoginViewController {
	// Singleton
	private LoginViewController() {
	}

	public static LoginViewController instance = new LoginViewController();

	public static LoginViewController getInstance() {
		return instance;
	}

	// use server from MainController
	ComappingServer server = MainController.instance.server;

	private void loginClick(String email, String password) {
		// TODO: Why isn't work?
		changeErrorText("Loading ...");

		server.login(email, password);

		if (server.getStatus() == Status.LOGGEDIN) {
			MainController.instance.login();
		} else
			changeErrorText("Login or password is incorrect");
	}

	private void changeErrorText(String error) {
		TextView errorText = (TextView) MainController.instance
				.findViewById(R.id.error);

		final String error_msg = error; 
		final TextView error_rv = errorText;
		MainController.instance.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				error_rv.setText(error_msg);
			}
		});
		errorText.postInvalidate();
	}

	private void loadLoginView() {
		MainController.instance.setContentView(R.layout.login);

		// bind login button
		Button loginButton = (Button) MainController.instance
				.findViewById(R.id.login);

		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String email = ((TextView) MainController.instance
						.findViewById(R.id.email)).getText().toString();
				final String password = ((TextView) MainController.instance
						.findViewById(R.id.password)).getText().toString();
				
				new Thread()
				{
					public void run() {
						
						loginClick(email, password);
					}
					//loginClick();
				}.start();
			}
		});
	}

	private void loadLoginView(final String error) {
		loadLoginView();
		changeErrorText(error);
	}

	public void activate() {
		if (!Storage.instance.get("key").equals("")) {
			// attempt to autoLogin
			server.autoLogin(Storage.instance.get("email"), Storage.instance
					.get("key"));

			if (server.getStatus() == Status.LOGGEDIN)
				MainController.instance.login();
			else {
				loadLoginView("AutoLogin attempt failed");
			}
		} else
			loadLoginView();
	}
}