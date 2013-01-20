package com.ph.tymyreader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class TymyListActivity extends ListActivity {
//	private static final String TAG = TymyReader.TAG;
	private String[] from = new String[] {TymyListUtil.ONE, TymyListUtil.TWO};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	private List<HashMap<String, String>> tymyList = new ArrayList<HashMap<String,String>>();
	private ArrayList<TymyPref> tymyPrefList = new ArrayList<TymyPref>();
	private SimpleAdapter adapter;
	private TymyListUtil tlu = new TymyListUtil();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		Log.v(TAG, "onCreate TymyList");
		setContentView(R.layout.tymy_list);

		TymyReader app = (TymyReader) getApplication();
		app.loadTymyCfg();
		tymyPrefList = app.getTymyPrefList();

		if (tymyPrefList.isEmpty()) {
			tlu.addMapToList(true, getString(R.string.no_tymy), getString(R.string.no_tymy_hint), tymyList);						
		} else {
			refreshTymyPrefList(app);
		}
		// Set-up adapter for tymyList
		adapter = new SimpleAdapter(this, tymyList, R.layout.two_line_list_discs, from, to);
		setListAdapter(adapter);

		registerForContextMenu(getListView());		
	}

	@Override
	protected void onResume () {
		super.onResume();
		TymyReader app = (TymyReader) getApplication();
		refreshTymyPrefList(app);
		refreshListView();
	}

	@Override
	protected void onDestroy () {
		super.onDestroy();
		TymyReader app = (TymyReader) getApplication();
//		Log.v(TAG, "onDestroy: " + tlu.printTymyPrefList(tymyPrefList));
		app.saveTymyCfg(tymyPrefList);
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
		if ((tymyPrefList.size() == 0) || tymyPrefList.get(position).noDs()) {
			Toast.makeText(this, getString(R.string.no_discussion), Toast.LENGTH_LONG).show();
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putSerializable("tymPref", tymyPrefList.get(position));
		Intent intent = new Intent(this, DiscussionListActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);				
	}

	// **************  Context menu  ************** //
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		MenuInflater inflater = getMenuInflater();
		menu.setHeaderTitle(tymyPrefList.get(info.position).getUrl());
		inflater.inflate(R.menu.tymy_list_context_menu, menu);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		case R.id.menu_context_edit:
			showAddTymy((int)info.id);
			return true;
		case R.id.menu_context_delete:
			deleteTymy((int)info.id);
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

	protected void showTymySettings(int position) {
		Bundle bundle = new Bundle();
		bundle.putSerializable("tymPref", tymyPrefList.get(position));
		Intent intent = new Intent(this, AppSettingsActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);				
	}

	private void showAddTymy(int position) {
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		Intent intent = new Intent(this, EditTymyActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);		
	}
	
	private void showAddTymy() {
		showAddTymy(-1);
	}

	private void deleteTymy(int position) {
		TymyReader app = (TymyReader) getApplication();
		app.deleteTymyCfg(tymyPrefList.get(position).getUrl());
		tlu.removeTymyPref(tymyPrefList, position);
		app.setTymyPrefList(tymyPrefList);
		refreshListView();
	}
	
	private void refreshTymyPrefList(TymyReader app) {
		tymyList = tlu.getTymyList(tymyPrefList);
		// Slozitejsi pouziti copy_tymyPrefList aby se zabranilo soucasne modifikaci tymyPrefList
		ArrayList<TymyPref> copy_tymyPrefList = new ArrayList<TymyPref>();
		for (TymyPref tP : tymyPrefList) {
			copy_tymyPrefList.add(tP);
		}
		int i = 0;
		for(TymyPref tP : copy_tymyPrefList) {
			new LoginToTymy().execute(tP);
			tymyPrefList.remove(i);
			tymyPrefList.add(i, tP);
			app.setTymyPrefList(tymyPrefList);
			tymyList = tlu.getTymyList(tymyPrefList);
			i = i + 1;
		}
	}

	private void refreshListView() {
		TymyReader app = (TymyReader) getApplication();
		tymyPrefList = app.getTymyPrefList();
		if (tymyPrefList.isEmpty()) {
			tlu.addMapToList(true, getString(R.string.no_tymy), getString(R.string.no_tymy_hint), tymyList);						
		} else {
			tymyList = tlu.getTymyList(tymyPrefList);
			adapter = new SimpleAdapter(TymyListActivity.this, tymyList, R.layout.two_line_list_discs, from, to);
			setListAdapter(adapter);
		}
	}

	//*************************************************************//
	//*******************  AsyncTask  *****************************//
	private class LoginToTymy extends AsyncTask<TymyPref, Integer, TymyPref> {

		@Override
		protected TymyPref doInBackground(TymyPref... tymPref) {			
			return updateTymDs(tymPref);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			setProgress(progress[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(TymyPref tymPref) {
			Toast.makeText(getApplicationContext(), "discussions list updated", Toast.LENGTH_SHORT).show();
			refreshListView();
		}

		// TODO dodelat zobrazeni novych prispevku
		private TymyPref updateTymDs(TymyPref... tymPref) {
			String mainPage = null;

			StringBuilder cookies = tymPref[0].getCookies(); 
			TymyPageLoader page = new TymyPageLoader();
			mainPage = page.loadMainPage(tymPref[0].getUrl(), tymPref[0].getUser(), tymPref[0].getPass(), tymPref[0].getCookies());
			tymPref[0].setCookies(cookies);
			TymyParser parser = new TymyParser(mainPage);
			boolean isFirst = true; // clear map in first cycle
			for ( String dsDesc : parser.getDsArray(mainPage)) {
				tlu.addMapToList(isFirst, dsDesc, getString(R.string.unknown), tymPref[0].getDsList());
				isFirst = false;
			}
			return tymPref[0];
		}
	}
}
