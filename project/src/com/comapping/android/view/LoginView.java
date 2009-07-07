package com.comapping.android.view;

import android.app.ProgressDialog;
import com.comapping.android.controller.LoginActivity;

public class LoginView {
	private LoginActivity loginActivity;
	private ProgressDialog splash = null;

	public LoginView(LoginActivity loginActivity) {
		this.loginActivity = loginActivity;
	}

	public void splashActivate(final String message) {
		loginActivity.runOnUiThread(new Runnable() {
			public void run() {
				if (splash == null) {
					splash = ProgressDialog.show(loginActivity, "Comapping", message);
					
				} else {
					splash.setMessage(message);
				}
			}
		});
	}

	public void splashDeactivate() {
		loginActivity.runOnUiThread(new Runnable() {
			public void run() {
				if (splash != null) {
					splash.dismiss();
					splash = null;
				}
			}
		});
	}
}
