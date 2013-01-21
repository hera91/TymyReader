package com.ph.tymyreader;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ph.tymyreader.model.TymyPref;

public class TymyConfigManager {
	private static final String URL = "url";
	private static final String USER = "use";
	private static final String PASS = "pass";
	private static final String DS_SEQUENCE = "dsSequence";
	private SharedPreferences prefs;

	public TymyConfigManager(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public void saveCfg(TymyPref tymyPref) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(URL, tymyPref.getUrl());
		editor.putString(USER, tymyPref.getUser());
		editor.putString(PASS, tymyPref.getPass());
		editor.putString(DS_SEQUENCE, tymyPref.getDsSequence());
		editor.commit();
	}

	public TymyPref loadCfg(String tymyName) {
		String dsSequence = prefs.getString(DS_SEQUENCE, "");
		String url = prefs.getString(URL, null);
		String user = prefs.getString(USER, null);
		String pass = prefs.getString(PASS, null);
		TymyPref tymPref = null;
		if ((url != null) && (user != null) && (pass != null)) {
			tymPref = new TymyPref(url, user, pass, dsSequence);
		}				
		return tymPref;
	}

	public void addTymyToDefaultPrefs(String url) {
		if ((prefs.getString(url, "")).equals("")) {
			Editor editor = prefs.edit();
			editor.putString(url, url);
			editor.commit();
		}
	}


	public void deleteCfg(TymyPref tymyPref) {
		Editor editor = prefs.edit();
		editor.clear();
		editor.commit();		
	}

	public void deleteTymyInDefaultPrefs(String url) {
		if ((prefs.getString(url, "")).equals(url)) {
			Editor editor = prefs.edit();
			editor.remove(url);
			editor.commit();
		}		
	}

}
