package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.util.Log;

public class TymyConfigManager {
//	private static final String TAG = TymyReader.TAG;
	private static final String ONE = TymyListUtil.ONE;
	private static final String TWO = TymyListUtil.TWO;
	private static final String URL = "url";
	private static final String USER = "use";
	private static final String PASS = "pass";
	private static final String DS_SEQUENCE = "dsSequence";
	private List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();
	private SharedPreferences prefs;

	public TymyConfigManager(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public void saveCfg(TymyPref tymyPref) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(URL, tymyPref.getUrl());
		editor.putString(USER, tymyPref.getUser());
		editor.putString(PASS, tymyPref.getPass());
		editor.putString(DS_SEQUENCE, dsListToSequence(tymyPref.getDsList()));
		editor.commit();
	}

	public TymyPref loadCfg(String tymyName) {
		String dsSequence = prefs.getString(DS_SEQUENCE, "");
		String url = prefs.getString(URL, null);
		String user = prefs.getString(USER, null);
		String pass = prefs.getString(PASS, null);
		TymyPref tymPref = null;
		if ((url != null) && (user != null) && (pass != null)) {
			dsList = dsSequenceToList(dsSequence);
			tymPref = new TymyPref(url, user, pass, dsList);
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

	private String dsListToSequence(List<HashMap<String, String>> dsList) {
		boolean isFirst = true;
		StringBuilder seq = new StringBuilder();
		for (HashMap<String, String> dsDesc : dsList) {
			if (isFirst) {
				seq.append(dsDesc.get(ONE));
				isFirst = false;
			}
			seq.append("|" + dsDesc.get(ONE));
		}
		return seq.toString();
	}

	private List<HashMap<String, String>> dsSequenceToList(String dsSequence) {
		dsList.clear();
		if (("").equals(dsSequence)) return dsList;
		for ( String dsDesc : dsSequence.split("\\|")) {
			addMapToList(false, dsDesc, "", dsList);
		}
		//		Log.v(TAG, dsListToSequence(dsList));
		return dsList;
	}

	private void addMapToList(boolean clear, String dsDesc, String two,
			List<HashMap<String, String>> list) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(ONE, dsDesc);
		map.put(TWO, two);
		if (clear) { list.clear(); }
		list.add(map);
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
