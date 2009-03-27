package com.comapping.android.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.comapping.android.controller.LoginActivity;
import com.comapping.android.controller.R;

public class LoginView {
	private LoginActivity loginActivity;
	
	public LoginView(LoginActivity loginActivity) {
		this.loginActivity = loginActivity;
	}
	
	public void changeErrorText(final String error) {
		final TextView errorText = (TextView) loginActivity.findViewById(R.id.error);

		errorText.setText(error);
	}

	public void load() {
		loginActivity.setContentView(R.layout.login);

		// bind login button
		Button loginButton = (Button) loginActivity.findViewById(R.id.login);		

		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String email = ((TextView) loginActivity.findViewById(R.id.email)).getText().toString();
				final String password = ((TextView) loginActivity.findViewById(R.id.password)).getText().toString();

				loginActivity.loginClick(email, password);
			}
		});	
		
	}

	public void load(final String error) {
		load();
		changeErrorText(error);
	}
}