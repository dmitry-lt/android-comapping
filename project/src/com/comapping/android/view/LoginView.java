package com.comapping.android.view;

import android.app.ProgressDialog;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.comapping.android.controller.LoginActivity;
import com.comapping.android.controller.R;
import com.comapping.android.storage.PreferencesStorage;

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

	public void load() {
		loginActivity.setContentView(R.layout.login);
		
		BitmapDrawable dr =(BitmapDrawable)loginActivity.getResources().getDrawable(R.drawable.login_background);
		dr.setTileModeX(TileMode.REPEAT);
		dr.setTileModeY(TileMode.REPEAT);
		loginActivity.findViewById(R.id.loginLayout).setBackgroundDrawable(dr);
		//@drawable/login_bg
		
		String email = PreferencesStorage.get(PreferencesStorage.EMAIL_KEY, null);
		if (email != null) {
			TextView emailText = (TextView) loginActivity.findViewById(R.id.eMail);
			emailText.setText(email);
			TextView passwordText = (TextView) loginActivity.findViewById(R.id.password);
			passwordText.requestFocus();
		} 
		
		// bind login button
		Button loginButton = (Button) loginActivity.findViewById(R.id.login);

		loginButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				loginActivity.loginClick();
			}
		});

	}
}