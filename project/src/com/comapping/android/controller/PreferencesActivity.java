package com.comapping.android.controller;

import com.comapping.android.Options;
import com.comapping.android.ViewType;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class PreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	public final static String PREFERENCES_ACTIVITY_INTENT = "com.comapping.android.intent.PREFERENCES";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.layout.preferences);

		ListPreference viewType = (ListPreference) findPreference("viewType");
		if (viewType.getEntry() == null) {
			// set default value
			viewType.setValue(ViewType.COMAPPING_VIEW.toString());
		}

		viewType.setSummary(viewType.getEntry());
		viewType.setOnPreferenceChangeListener(this);
		
		EditTextPreference downloadFolder = (EditTextPreference) findPreference("downloadFolder");
		if (downloadFolder.getText() == null) {
			downloadFolder.setText(Options.DEFAULT_DOWNLOAD_FOLDER);
		}
		
		downloadFolder.setSummary(downloadFolder.getText());
		downloadFolder.setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		preference.setSummary((CharSequence) newValue);

		return true;
	}
}