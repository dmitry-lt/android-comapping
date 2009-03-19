package com.comapping.android.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.comapping.android.controller.Main;
import com.comapping.android.controller.R;

public class Login {
	public void changeErrorText(final String error) {
		final TextView errorText = (TextView) Main.getInstance()
				.findViewById(R.id.error);

		errorText.setText(error);
	}

	public void load() {
		Main.getInstance().setContentView(R.layout.login);

		// bind login button
		Button loginButton = (Button) Main.getInstance().findViewById(R.id.login);

		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String email = ((TextView) Main.getInstance()
						.findViewById(R.id.email)).getText().toString();
				final String password = ((TextView) Main.getInstance()
						.findViewById(R.id.password)).getText().toString();

				com.comapping.android.controller.Login.getInstance().loginClick(
						email, password);
			}
		});
	}

	public void load(final String error) {
		load();
		changeErrorText(error);
	}
}