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

// TODO doplnit nacteni cookies jakmile vyberu tuhle stranku a pak docilit toho aby cookies DisView uz cookies dostalo

public class TymyList extends ListActivity {
	private static final String TAG = "TymyReader";
	final String ONE = "one";
	final String TWO = "two";
	private String[] from = new String[] {ONE, TWO};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	private List<HashMap<String, String>> tymyList = new ArrayList<HashMap<String,String>>();
	private TymPref tymPref1 = new TymPref("pd.tymy.cz", "HERA", "bistromat");
	private TymPref tymPref2 = new TymPref("dg.tymy.cz", "admin", "bistromat");
	private ArrayList<TymPref> tymPrefList = new ArrayList<TymPref>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tymy_list);

		tymPrefList.add(tymPref1);
		tymPrefList.add(tymPref2);

		// Fill list of tymy
		for (TymPref tP : tymPrefList) {
			new LoginToTym().execute(tP);
			addMapToList(false, tP.getUrl(), "onCreate", tymyList);			
		}
		// Set-up adapter for tymyList
		SimpleAdapter adapter = new SimpleAdapter(this, tymyList, R.layout.two_line_list_discs, from, to);
		setListAdapter(adapter);
		
		registerForContextMenu(getListView());
	}

	// **************  Activity menu  ************** //
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
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
			Toast.makeText(this, "You have chosen the " + getResources().getString(R.string.edit) +
					" context menu option for " + tymPrefList.get((int)info.id).getUrl(),
					Toast.LENGTH_SHORT).show();
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
		// TODO Auto-generated method stub
		Bundle bundle = new Bundle();
		bundle.putSerializable("tymPref", tymPrefList.get(position));
		Intent intent = new Intent(this, TymSettingsActivity.class);
		intent.putExtras(bundle);
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
	private class LoginToTym extends AsyncTask<TymPref, Integer, String> {

		@Override
		protected String doInBackground(TymPref... tymPref) {			
			return updateTymDis(tymPref);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			setProgress(progress[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String input) {
		}

		private String updateTymDis(TymPref... tymPref) {
			String mainPage = null;
			StringBuilder cookies = tymPref[0].getCookies(); 
			TymyPageLoader page = new TymyPageLoader();
			mainPage = page.loadMainPage(tymPref[0].getUrl(), tymPref[0].getUser(), tymPref[0].getPass(), tymPref[0].getCookies());
			tymPref[0].setCookies(cookies);
			Log.v(TAG, "updateTymDis login cookies " + tymPref[0].getCookies().toString());
			TymyParser parser = new TymyParser(mainPage);
			for ( String id : parser.getDisArray(mainPage)) {
				//Log.v(TAG, "id :" + id);
				addMapToList(false, id, "", tymPref[0].getDsList());
			}
			return mainPage;
		}
	}
}


class TymPref implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url;
	private String user;			
	private String pass;
	private StringBuilder cookies = new StringBuilder();
	private List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();

	public TymPref(String tym, String user, String pass) {
		this.url = tym;
		this.pass = pass;
		this.user = user;
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

