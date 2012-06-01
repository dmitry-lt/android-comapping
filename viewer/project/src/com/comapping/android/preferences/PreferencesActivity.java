package com.comapping.android.preferences;

import android.app.Activity;
import android.preference.*;
import com.comapping.android.Constants;
import com.comapping.android.R;

import android.os.Bundle;
import android.preference.Preference.OnPreferenceChangeListener;

public class PreferencesActivity extends PreferenceActivity {
	public final static String PREFERENCES_ACTIVITY_INTENT = "com.comapping.android.intent.PREFERENCES";

	private void initPreference(Preference preference, CharSequence defaultValue) {
		preference.setSummary(defaultValue);

		preference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						preference.setSummary((CharSequence) newValue);

						return true;
					}

				});
	}

	private void initListPreference(ListPreference preference,
			String defaultValue) {
		if (preference.getEntry() == null) {
			// set default value
			preference.setValue(defaultValue);
		}

		initPreference(preference, preference.getEntry());
	}

	private void initTextPreference(EditTextPreference preference,
			String defaultValue) {
		if (preference.getText() == null) {
			preference.setText(defaultValue);
		}

		initPreference(preference, preference.getText());
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.layout.preferences);

		// init view type preference
		initListPreference((ListPreference) findPreference("viewType"),
				Constants.VIEW_TYPE_COMAPPING);

		final Activity preferencesActivity = this;
		findPreference(PreferencesStorage.FULL_SCREEN_KEY).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				PreferencesStorage.updateFullScreenStatus(preferencesActivity, (Boolean) newValue);
				return true;
			}
		});

		// init download preference
		initTextPreference(
				(EditTextPreference) findPreference("downloadFolder"),
				PreferencesStorage.DOWNLOAD_FOLDER_DEFAULT_VALUE);

		// init proxy preference
		// proxy host
		initTextPreference((EditTextPreference) findPreference("proxyHost"), "");

		// proxy port
		initTextPreference((EditTextPreference) findPreference("proxyPort"), "");

		// proxy name
		initTextPreference(
				(EditTextPreference) findPreference("proxyAuthUserName"), "");

		// proxy password
		initTextPreference(
				(EditTextPreference) findPreference("proxyAuthUserPassword"),
				"");

	}

	@Override
	protected void onResume() {
		super.onResume();
		PreferencesStorage.updateFullScreenStatus(this);
	}
}