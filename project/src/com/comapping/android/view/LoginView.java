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
		Button testButton = (Button) MainController.getInstance().findViewById(R.id.test);

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
		
		testButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Map map = new Map(13);
				Topic[] t = new Topic[7];
				for (int i = 0; i < 7; i++)
					t[i] = new Topic(i, i + "");
				t[3].addChild(t[4]);
				t[3].addChild(t[5]);
				t[1].addChild(t[2]);
				t[1].addChild(t[3]);
				t[0].addChild(t[1]);
				t[0].addChild(t[6]);
				map.setRoot(t[0]);
				
				MapController.getInstance().loadMap(map);
			}
		});		
	}

	public void load(final String error) {
		load();
		changeErrorText(error);
	}
}