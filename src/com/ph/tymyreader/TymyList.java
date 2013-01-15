package com.ph.tymyreader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

// TODO predelat TymyPref na TymyManager, ktery bude poskytovat vsechny funkce kolem Tymu
// TODO doplnit nacteni cookies jakmile vyberu tuhle stranku a pak docilit toho aby cookies DisView uz cookies dostalo

public class TymyList extends ListActivity {
	private static final String TAG = "TymyReader";
	final String ONE = "one";
	final String TWO = "two";
	private String[] from = new String[] {ONE, TWO};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	private List<HashMap<String, String>> tymyList = new ArrayList<HashMap<String,String>>();
//	private TymPref tymPref1 = new TymPref("pd.tymy.cz", "HERA", "bistromat");
	private TymPref tymPref2 = new TymPref("dg.tymy.cz", "admin", "bistromat");
	private ArrayList<TymPref> tymPrefList = new ArrayList<TymPref>();
	private TymConfigManager cfg = new TymConfigManager(this);
	private SimpleAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tymy_list);

		// TODO vyresit zakladani novych tymu a plneni pole diskusi
		tymPrefList.add(tymPref2);

//		tymPrefList.add(cfg.loadCfg("dg.tymy.cz"));

		// Fill list of tymy
		for (TymPref tP : tymPrefList) {
//			Log.v(TAG,"Login to tymy " + tP.getUrl());
			new LoginToTym().execute(tP);
			addMapToList(false, tP.getUrl(), "onCreate", tymyList);			
		}
		if (tymPrefList.isEmpty()) {
			addMapToList(false, getString(R.string.no_tymy), getString(R.string.no_tymy_hint), tymyList);						
		}

		// Set-up adapter for tymyList
		adapter = new SimpleAdapter(this, tymyList, R.layout.two_line_list_discs, from, to);
		setListAdapter(adapter);

		registerForContextMenu(getListView());		
	}
	
	@Override
	public void onPause () {
		super.onPause();
		for (TymPref tP : tymPrefList) {
			cfg.saveCfg(tP);
		}
	}

	// **************  Activity Option menu  ************** //
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_tymy_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_settings:
			showSettings();
			return true;
		case R.id.menu_add_tymy:
			showAddTymy();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if ((tymPrefList.size() == 0) || tymPrefList.get(position).getDsList().isEmpty()) {
			Toast.makeText(this, getString(R.string.no_discussion), Toast.LENGTH_LONG).show();
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putSerializable("tymPref", tymPrefList.get(position));
		Intent intent = new Intent(this, DiscussionList.class);
		intent.putExtras(bundle);
		startActivity(intent);				
	}

	// **************  Context menu  ************** //
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tymy_list_context_menu, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		case R.id.menu_context_edit:
			showTymSettings((int)info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	// *****************  Setting  ******************** //
	private void showSettings() {
		Intent intent = new Intent(this, GlobalSettingsActivity.class);
		startActivity(intent);		
	}
	
	protected void showTymSettings(int position) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("tymPref", tymPrefList.get(position));
		Intent intent = new Intent(this, AppSettingsActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);				
	}
	
	private void showAddTymy() {
		Intent intent = new Intent(this, EditTymyActivity.class);
		startActivity(intent);		
	}

	
	// ******************  Private methods  ********************* //
	private void addMapToList(boolean clear, String one, String two, List<HashMap<String, String>> list) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(ONE, one);
		map.put(TWO, two);
		if (clear) { list.clear(); }
		list.add(map);
	}

	//*************************************************************//
	//*******************  AsysncTask  ****************************//
	private class LoginToTym extends AsyncTask<TymPref, Integer, TymPref> {

		@Override
		protected TymPref doInBackground(TymPref... tymPref) {			
			return updateTymDis(tymPref);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			setProgress(progress[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(TymPref tymPref) {
			Toast.makeText(getApplicationContext(), "discussions list updated", Toast.LENGTH_LONG).show();
//			int index = tymPrefList.indexOf(tymPref);
//			StringBuilder s = new StringBuilder();
			for (HashMap<String, String> dsDesc : tymPref.getDsList()) {
				Log.v(TAG, "onPostExecute 3 : " + tymPref.getUrl() + " " + dsDesc.get(ONE));
			}
//			HashMap<String, String> map = new HashMap<String, String>();
//			map.put(ONE, tymPref.getUrl());
//			map.put(TWO, s.toString());
//			tymyList.set(index, map);

			adapter.notifyDataSetChanged();
		}

		// TODO dodelat zobrazeni novych prispevku
		private TymPref updateTymDis(TymPref... tymPref) {
			String mainPage = null;
			
			StringBuilder cookies = tymPref[0].getCookies(); 
			TymyPageLoader page = new TymyPageLoader();
			mainPage = page.loadMainPage(tymPref[0].getUrl(), tymPref[0].getUser(), tymPref[0].getPass(), tymPref[0].getCookies());
			tymPref[0].setCookies(cookies);
			TymyParser parser = new TymyParser(mainPage);
			boolean isFirst = true; // clear map in first cycle
			for ( String dsDesc : parser.getDisArray(mainPage)) {
				addMapToList(isFirst, dsDesc, getString(R.string.unknown), tymPref[0].getDsList());
				isFirst = false;
			}
			return tymPref[0];
		}
	}
}


class TymPref implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url = null;
	private String user = null;			
	private String pass = null;
	private StringBuilder cookies = new StringBuilder();
	private List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();

	public TymPref(String tym, String user, String pass) {
		this.url = tym;
		this.pass = pass;
		this.user = user;
	}

	public TymPref(String tym, String user, String pass, List<HashMap<String, String>> dsList) {
		this.url = tym;
		this.pass = pass;
		this.user = user;
		this.dsList = dsList;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String tym) {
		this.url = tym;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}	
	public StringBuilder getCookies() {
		return cookies;
	}
	public void setCookies(StringBuilder myCookie) {
		this.cookies = myCookie;
	}
	public List<HashMap<String, String>> getDsList() {
		return dsList;
	}
	public void setDsList(List<HashMap<String, String>> dsList) {
		this.dsList = dsList;
	}
}

