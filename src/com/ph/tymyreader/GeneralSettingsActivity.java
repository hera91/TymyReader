/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ph.tymyreader;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

/**
 * This preference activity has in its manifest declaration an intent filter for
 * the ACTION_MANAGE_NETWORK_USAGE action. This activity provides a settings UI
 * for users to specify network settings to control data usage.
 */
public class GeneralSettingsActivity extends PreferenceActivity
        implements
            OnSharedPreferenceChangeListener {

	private EditTextPreference mEditTextPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Loads the XML preferences file.
        addPreferencesFromResource(R.xml.global_preference);
        
        mEditTextPreference = (EditTextPreference) getPreferenceScreen().findPreference(getString(R.string.no_new_items_key));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEditTextPreference.setSummary(String.format("%s %s", getString(R.string.current_value_is), mEditTextPreference.getText())); 
        // Registers a callback to be invoked whenever a user changes a preference.
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregisters the listener set in onResume().
        // It's best practice to unregister listeners when your app isn't using them to cut down on
        // unnecessary system overhead. You do this in onPause().
        getPreferenceScreen()
                .getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    // Fires when the user changes a preference.
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	if (key.equals(getString(R.string.no_new_items_key))) {
	        mEditTextPreference.setSummary(String.format("%s %s", getString(R.string.current_value_is), sharedPreferences.getString(key, "")));			
    	}
    }
}
