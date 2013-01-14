package com.ph.tymyreader;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {
	public boolean boolPref;
	public String stringPref;

	private SharedPreferences prefs;

	public ConfigManager(Context context, String file) {
		prefs = context.getSharedPreferences(
				file,
				Context.MODE_PRIVATE);
		loadCfg();
	}

	public void saveCfg() {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("boolPreference", boolPref);
		editor.putString("stringPreference", stringPref);
		editor.commit();
	}

	public void loadCfg() {
		this.boolPref = prefs.getBoolean("boolPreference", false);
		this.stringPref = prefs.getString("stringPreference", "");
	}
}
