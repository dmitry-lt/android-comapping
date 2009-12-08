package com.comapping.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * author Yan Lobkarev;
 * It is the Activity thats
 * drawing a single comapping 
 * notifications raws obtained 
 * from a @Bundle that must be 
 * initialized in @LocalHistoryViewer
 * 
 */
public class SingleNotificationViewer extends Activity {

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_notification_layout);

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.single_message_layout);

		Bundle extras = getIntent().getExtras();
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		/* add image information if exist */
		if (extras.containsKey("image")) {
			int imageResourceKey = extras.getInt("image");
			ImageView imageView = new ImageView(this);
			imageView.setImageResource(imageResourceKey);
			linearLayout.addView(imageView, p);
		}

		/* add Message information if exist */
		if (extras.containsKey("category")) {
			String message = "Category: " + extras.getString("category")
					+ "\n\n";
			TextView textView = new TextView(this);
			textView.setText(message);
			textView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
			linearLayout.addView(textView, p);
		}
		
		/* add Date information if exist */
		if (extras.containsKey("date")) {
			String dateString = "Date: " + extras.getString("date") + "\n\n";
			TextView textView = new TextView(this);
			textView.setText(dateString);
			linearLayout.addView(textView, p);
		}

		/* add user information if exist */
		if (extras.containsKey("description")) {
			String userString = "Description: "
					+ extras.getString("description") + "\n\n";
			TextView textView = new TextView(this);
			textView.setText(userString);
			linearLayout.addView(textView, p);
		}

		/* add link information if exist */ /*
		if (extras.containsKey("link")) {
			String linkString = "Link: " + extras.getString("link") + "\n\n";
			TextView textView = new TextView(this);
			textView.setText(linkString);
			linearLayout.addView(textView, p);
		} //*/

		/*
		 * remove this notification from @NotifyingActivity list if user click
		 * on the 'accept' button
		 */
		if (extras.containsKey("position")) { // position means the id of
												// notification
			// must be know as read
			// to-do
			
			// thats in case of 'accepting' we must
			// remove it from the notifications database
			final int position = extras.getInt("position");
			Button removeButton = (Button) findViewById(R.id.remove_message_button);
			removeButton.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {

					/*
					 * unnecessary the removing it from the comapping
					 * notifications data base
					 */
				}
			});
		}
		
		//Button
		Button okButton = (Button) findViewById(R.id.ok_message_button);
		okButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});

	}

}
