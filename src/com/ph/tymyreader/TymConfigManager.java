package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TymConfigManager {
	private final static String TAG = "TymyReader";
	private static final String ONE = "one";
	private static final String TWO = "two";
	private List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();
	private SharedPreferences prefs;
	private Context context;

	public TymConfigManager(Context context) {
		this.context = context;
	}

	public void saveCfg(TymPref tymPref) {
		prefs = context.getSharedPreferences(
				tymPref.getUrl(),
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("url", tymPref.getUrl());
		editor.putString("user", tymPref.getUser());
		editor.putString("pass", tymPref.getPass());
		editor.putString("dsSequence", dsListToSequence(tymPref.getDsList()));
		editor.commit();
	}

	public TymPref loadCfg(String tymName) {
		prefs = context.getSharedPreferences(
				tymName,
				Context.MODE_PRIVATE);

		String dsSequence = prefs.getString("dsSequence", null);
		dsList = dsSequenceToList(dsSequence);
		TymPref tymPref = new TymPref(
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
