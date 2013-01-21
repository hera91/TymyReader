package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.SharedPreferences;

import com.ph.tymyreader.model.TymyPref;


public class TymyReader extends Application {
	//Application-wide class
	public static final String TAG = "TymyReader";
	private ArrayList<TymyPref> tymyPrefList = new ArrayList<TymyPref>();

	@Override
	public void onCreate() {
		loadTymyPrefList();
	}
	
	public ArrayList<TymyPref> getTymyPrefList() {
		return tymyPrefList;
	}
	
	public void setTymyPrefList(ArrayList<TymyPref> tymyPrefList) {
		this.tymyPrefList = tymyPrefList;
	}

	public void loadTymyCfg() {
		this.tymyPrefList.clear();
		loadTymyPrefList();
	}
	
	public void deleteTymyCfg(String tymyUrl) {
		SharedPreferences prefs = getSharedPreferences(tymyUrl, MODE_PRIVATE);
		prefs.edit().clear().commit();		
	}
	
	public void saveTymyCfg(ArrayList<TymyPref> tymyPrefList) {
		SharedPreferences defaultPrefs = getSharedPreferences("tymyUrlList", MODE_PRIVATE);
		defaultPrefs.edit().clear().commit();
		TymyConfigManager cfg;
		cfg = new TymyConfigManager(defaultPrefs);
		for (TymyPref tP : tymyPrefList) {
			cfg.addTymyToDefaultPrefs(tP.getUrl());
		}
		for (TymyPref tP : tymyPrefList) {
			SharedPreferences prefs = getSharedPreferences(tP.getUrl(), MODE_PRIVATE);
			prefs.edit().clear().commit();
			cfg = new TymyConfigManager(prefs);
			cfg.saveCfg(tP);
		}
	}
	
	// ******************  Private methods  ********************* //
	private void loadTymyPrefList() {
		for (String tymyUrl : getTymyUrlList()) {
			SharedPreferences prefs = getSharedPreferences(tymyUrl, MODE_PRIVATE);
			TymyConfigManager cfg = new TymyConfigManager(prefs);
			TymyPref tp = cfg.loadCfg(tymyUrl); 
			if (tp != null ) tymyPrefList.add(cfg.loadCfg(tymyUrl));
		}
	}
	
	private List<String> getTymyUrlList() {
		List<String> tymyUrlList = new ArrayList<String>();
		SharedPreferences defaultPrefs = getSharedPreferences("tymyUrlList", MODE_PRIVATE);
		Map<String,?> keys = defaultPrefs.getAll();
		for(Map.Entry<String,?> entry : keys.entrySet()){
		    		tymyUrlList.add(entry.getKey());
		 }
		return tymyUrlList;
	}
	

}
