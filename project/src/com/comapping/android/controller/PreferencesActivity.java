package com.comapping.android.controller;

import com.comapping.android.Options;
import com.comapping.android.ViewType;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class PreferencesActivity extends PreferenceActivity {
	public final static String PREFERENCES_ACTIVITY_INTENT = "com.comapping.android.intent.PREFERENCES";

	private void initPreference(Preference preference, CharSequence defaultValue) {
		preference.setSummary(defaultValue);
		
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((CharSequence) newValue);

				return true;
			}
			
		});
	}
	
	private void initListPreference(ListPreference preference, String defaultValue) {
		if (preference.getEntry() == null) {
			// set default value
			preference.setValue(defaultValue);
		}
		
		initPreference(preference, preference.getEntry());
	}
	
	private void initTextPreference(EditTextPreference preference, String defaultValue) {
		if (preference.getText() == null) {
			preference.setText(defaultValue);
		}
		
		initPreference(preference, preference.getText());
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.layout.preferences);
		
		// init view type preference
		initListPreference((ListPreference) findPreference("viewType"), ViewType.COMAPPING_VIEW.toString());
		
		// init download preference
		initTextPreference((EditTextPreference) findPreference("downloadFolder"), Options.DEFAULT_DOWNLOAD_FOLDER);
		
		// init proxy preference
		// proxy host
		initTextPreference((EditTextPreference) findPreference("proxyHost"), "");
		
		// proxy port
		initTextPreference((EditTextPreference) findPreference("proxyPort"), "");
	}
}