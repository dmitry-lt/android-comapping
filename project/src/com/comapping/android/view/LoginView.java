package com.comapping.android.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.comapping.android.controller.LoginController;
import com.comapping.android.controller.MainController;
import com.comapping.android.controller.MapController;
import com.comapping.android.controller.R;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

public class LoginView {
	public void changeErrorText(final String error) {
		final TextView errorText = (TextView) MainController.getInstance()
				.findViewById(R.id.error);

		errorText.setText(error);
	}

	public void load() {
		MainController.getInstance().setContentView(R.layout.login);

		// bind login button
		Button loginButton = (Button) MainController.getInstance().findViewById(R.id.login);		

		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String email = ((TextView) MainController.getInstance()
						.findViewById(R.id.email)).getText().toString();
				final String password = ((TextView) MainController.getInstance()
						.findViewById(R.id.password)).getText().toString();

				LoginController.getInstance().loginClick(
						email, password);
			}
		});	
		
	}

	public void load(final String error) {
		load();
		changeErrorText(error);
	}
}