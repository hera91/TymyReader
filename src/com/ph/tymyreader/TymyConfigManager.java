package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TymyConfigManager {
	private final static String TAG = "TymyReader";
	private static final String ONE = "one";
	private static final String TWO = "two";
	private List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();
	private SharedPreferences prefs;
	private Context context;

	public TymyConfigManager(Context context) {
		this.context = context;
	}

	public void saveCfg(TymyPref tymyPref) {
		prefs = context.getSharedPreferences(
				tymyPref.getUrl(),
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("url", tymyPref.getUrl());
		editor.putString("user", tymyPref.getUser());
		editor.putString("pass", tymyPref.getPass());
		editor.putString("dsSequence", dsListToSequence(tymyPref.getDsList()));
		editor.commit();
	}

	public TymyPref loadCfg(String tymyName) {
		prefs = context.getSharedPreferences(
				tymyName,
				Context.MODE_PRIVATE);

		String dsSequence = prefs.getString("dsSequence", null);
		dsList = dsSequenceToList(dsSequence);
		TymyPref tymPref = new TymyPref(
				prefs.getString("url", null),
				prefs.getString("user", null), 
				prefs.getString("pass", null),
				dsList);				
		return tymPref;
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
		Log.v(TAG, "dsListToSequence" + seq.toString());
		return seq.toString();
	}

	private List<HashMap<String, String>> dsSequenceToList(String dsSequence) {
		dsList.clear();
		for ( String dsDesc : dsSequence.split("\\|")) {
			Log.v(TAG, "dsSeqToList" + dsDesc);
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
}
