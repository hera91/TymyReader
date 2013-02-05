package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ph.tymyreader.model.DiscussionPref;
import com.ph.tymyreader.model.TymyPref;

//@ReportsCrashes(formKey = "",
@ReportsCrashes(formUri = "http://www.bugsense.com/api/acra?api_key=34f778f4", formKey="",
//@ReportsCrashes(formKey = "dE5QTVBSNS1KMkY3UVFMTEZaaFRrVnc6MQ",
mode = ReportingInteractionMode.DIALOG,
resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
resDialogText = R.string.crash_dialog_text,
resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
resDialogOkToast = R.string.crash_dialog_ok_toast, // optional. displays a Toast message when the user accepts to send a report.
additionalSharedPreferences={"tymyUrlList", "pd.tymy.cz", "ls.tymy.cz", "dg.tymy.cz", "masters.tymy.cz", "p7.tymy.cz"},
excludeMatchingSharedPreferencesKeys={"pass"})
public class TymyReader extends Application {
	//Application-wide class
	public static final String TAG = "TymyReader";
	private ArrayList<TymyPref> tymyPrefList = new ArrayList<TymyPref>();
	private DiscussionPref dsPref = null; 

	@Override
	public void onCreate() {
		super.onCreate();

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
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

	/**
	 * store object in application class.
	 * @return the data
	 */
	public DiscussionPref getDsPref() {
		return dsPref;
	}

	/**
	 * get object from application class
	 * @param data the data to set
	 */
	public void setDsPref(DiscussionPref dsPref) {
		this.dsPref = dsPref;
	}

	public void clearDsPref () {
		dsPref = null;
	}

	public boolean isOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	// ******************  Private methods  ********************* //
	private void loadTymyPrefList() {
		List<String> tymyURLList = new ArrayList<String>();
		for (String tymyUrl : getTymyUrlList()) {
			tymyURLList.add(tymyUrl);
		}
		// Sort list of tymy aphabetically
		Collections.sort(tymyURLList);
		for (String tymyUrl : tymyURLList) {
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
