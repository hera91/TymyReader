package com.ph.tymyreader;

import java.util.ArrayList;

import android.app.Application;
import android.util.Log;


public class TymyReader extends Application {
	//Application-wide singleton
	private static final String TAG = "TymyReader";
	// TODO rekurzivne se vytvari reference na context!!
//	private TymyConfigManager cfg = new TymyConfigManager(this);
	private ArrayList<TymyPref> tymyPrefList = new ArrayList<TymyPref>();

	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate TymyReader");		
	}

	
	public TymyReader() {
		super();
	}

	public ArrayList<TymyPref> getTymyList () {
		return null;		
	}

	public ArrayList<TymyPref> getTymyPrefList() {
		return tymyPrefList;
	}
	public void setTymyPrefList(ArrayList<TymyPref> tymyPrefList) {
		this.tymyPrefList = tymyPrefList;
	}	
}
