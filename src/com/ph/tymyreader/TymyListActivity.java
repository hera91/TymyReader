package com.ph.tymyreader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.ph.tymyreader.model.TymyPref;

public class TymyListActivity extends ListActivity {
	//	private static final String TAG = TymyReader.TAG;
	private static final int EDIT_TYMY_ACTIVITY = 1;
	private String[] from = new String[] {TymyPref.ONE, TymyPref.TWO};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	private List<HashMap<String, String>> tymyList = new ArrayList<HashMap<String,String>>();
	private ArrayList<TymyPref> tymyPrefList = new ArrayList<TymyPref>();
	private SimpleAdapter adapter;
	private TymyListUtil tlu = new TymyListUtil();
	ListView lv;
	private LoginAndUpdateTymy loginAndUpdateTymy;
	private UpdateNewItemsTymy updateNewItemsTymy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tymy_list);

		@SuppressWarnings("unchecked")
		List<HashMap<String, String>> data = (List<HashMap<String, String>>) getLastNonConfigurationInstance();
		if (data == null) {
			// activity was started => load configuration
			TymyReader app = (TymyReader) getApplication();
			app.loadTymyCfg();
			tymyPrefList = app.getTymyPrefList();
			//refresh discussions from web
			refreshTymyPrefList(app);
		} else {
			// Configuration was changed, reload data
			tymyList = data;
		}
		// Set-up adapter for tymyList
		adapter = new SimpleAdapter(this, tymyList, R.layout.two_line_list_discs, from, to);
		lv = getListView();
		lv.setAdapter(adapter);

		registerForContextMenu(getListView());
	}

	@Override
	protected void onResume () {
		super.onResume();
		TymyReader app = (TymyReader) getApplication();		
		refreshTymyNewItems(app);
		refreshListView();
	}

	@Override
	protected void onDestroy () {
		super.onDestroy();
		//cancel background threads
		if (loginAndUpdateTymy != null) loginAndUpdateTymy.cancel(true);
		if (updateNewItemsTymy != null) updateNewItemsTymy.cancel(true);
		//save configuration
		TymyReader app = (TymyReader) getApplication();
		app.saveTymyCfg(tymyPrefList);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final List<HashMap<String, String>> data = tymyList;
		return data;
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
		case R.id.menu_refresh:
			refreshTymyPrefList((TymyReader) getApplication());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		int index = tlu.getIndexFromUrl(tymyPrefList, tymyList.get(position).get(TymyPref.ONE));
		if ((index == -1) || (tymyPrefList.size() == 0) || tymyPrefList.get(index).noDs()) {
			Toast.makeText(this, getString(R.string.no_discussion), Toast.LENGTH_LONG).show();
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putInt("position", index);
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
		int index = tlu.getIndexFromUrl(tymyPrefList, tymyList.get(info.position).get(TymyPref.ONE));
		if (index != -1) {
			menu.setHeaderTitle(tymyPrefList.get(index).getUrl());
			inflater.inflate(R.menu.tymy_list_context_menu, menu);
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int index = tlu.getIndexFromUrl(tymyPrefList, tymyList.get((int) info.id).get(TymyPref.ONE));
		switch(item.getItemId()) {
		case R.id.menu_context_edit:
			showAddTymy(index);
			return true;
		case R.id.menu_context_delete:
			deleteTymy(index);
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

	private void showAddTymy(int position) {
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		Intent intent = new Intent(this, EditTymyActivity.class);
		intent.putExtras(bundle);
		startActivityForResult(intent, EDIT_TYMY_ACTIVITY);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case EDIT_TYMY_ACTIVITY:
			if (resultCode == RESULT_OK) {
				TymyReader app = (TymyReader) getApplication();
				refreshTymyPrefList(app);
			}
		}
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
		// Slozitejsi pouziti copy_tymyPrefList aby se zabranilo soucasne modifikaci tymyPrefList
		ArrayList<TymyPref> copy_tymyPrefList = new ArrayList<TymyPref>();
		for (TymyPref tP : tymyPrefList) {
			copy_tymyPrefList.add(tP);
		}
		int i = 0;
		for(TymyPref tP : copy_tymyPrefList) {
			loginAndUpdateTymy = new LoginAndUpdateTymy();
			loginAndUpdateTymy.execute(tP);
			tymyPrefList.remove(i);
			tymyPrefList.add(i, tP);
			app.setTymyPrefList(tymyPrefList);
			//This maybe could cause problems when due to lost connectivity the download data will be corrupted,
			//but next update should fix it (or refresh UI functionality)
			app.saveTymyCfg(tymyPrefList);
			i = i + 1;
		}
	}

	private void refreshTymyNewItems(TymyReader app) {
		// Slozitejsi pouziti copy_tymyPrefList aby se zabranilo soucasne modifikaci tymyPrefList
		ArrayList<TymyPref> copy_tymyPrefList = new ArrayList<TymyPref>();
		for (TymyPref tP : tymyPrefList) {
			copy_tymyPrefList.add(tP);
		}
		int i = 0;
		for(TymyPref tP : copy_tymyPrefList) {
			updateNewItemsTymy = new UpdateNewItemsTymy();
			updateNewItemsTymy.execute(tP);
			tymyPrefList.remove(i);
			tymyPrefList.add(i, tP);
			app.setTymyPrefList(tymyPrefList);
			i = i + 1;
		}
	}

	private void refreshListView() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String noNewItems = pref.getString(getString(R.string.no_new_items_key), getString(R.string.no_new_items_default));
		TymyReader app = (TymyReader) getApplication();
		tymyPrefList = app.getTymyPrefList();
		if (tymyPrefList.isEmpty()) {
			tlu.addMapToList(true, getString(R.string.no_tymy), getString(R.string.no_tymy_hint), tymyList);						
		} else {
			tlu.updateTymyList(tymyPrefList, noNewItems, tymyList);
			adapter.notifyDataSetChanged();
		}
	}

	//**************************************************************//
	//*******************  AsyncTasks  *****************************//
	private class LoginAndUpdateTymy extends AsyncTask<TymyPref, Integer, TymyPref> {

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
		protected void onPostExecute(TymyPref tymyPref) {
			Toast.makeText(getApplicationContext(), "discussions list " + tymyPref.getUrl() + " updated" , Toast.LENGTH_SHORT).show();
			refreshListView();
		}

		// TODO premistit tuhle funkci do jine tridy
		private TymyPref updateTymDs(TymyPref... tymyPref) {

			TymyPageLoader page = new TymyPageLoader();
			TymyParser parser = new TymyParser();
			HashMap<String, Integer> dsNews = new HashMap<String, Integer>();

			String mainPage = page.loadMainPage(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getCookies());
			String ajax = page.loadAjaxPage(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getCookies());

			dsNews = parser.getNewItems(ajax);
			boolean isFirst = true; // clear map in first cycle
			for ( String dsDesc : parser.getDsArray(mainPage)) {
				tlu.addMapToList(isFirst, dsDesc, "" + dsNews.get(getDsId(dsDesc)), tymyPref[0].getDsList());
				isFirst = false;
			}

			return tymyPref[0];
		}

		private String getDsId(String dsDesc) {
			return dsDesc.split(":")[0];
		}
	}

	private class UpdateNewItemsTymy extends AsyncTask<TymyPref, Integer, TymyPref> {

		@Override
		protected TymyPref doInBackground(TymyPref... tymPref) {			
			return updateNewItems(tymPref);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			setProgress(progress[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(TymyPref tymPref) {
			refreshListView();
		}

		// TODO premistit tuhle funkci do jine tridy
		private TymyPref updateNewItems(TymyPref... tymyPref) {

			if (tymyPref[0].getCookies().length() == 0) return tymyPref[0];

			TymyPageLoader page = new TymyPageLoader();
			TymyParser parser = new TymyParser();
			HashMap<String, Integer> dsNews = new HashMap<String, Integer>();
			String ajax = page.loadAjaxPage(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getCookies());

			dsNews = parser.getNewItems(ajax);
			boolean isFirst = true; // clear map in first cycle

			ArrayList<HashMap<String, String>> copy_DsList = new ArrayList<HashMap<String,String>>();
			for (HashMap<String, String> dsDesc : tymyPref[0].getDsList()) {
				copy_DsList.add(dsDesc);
			}
			for ( HashMap<String, String> dsDesc : copy_DsList) {
				tlu.addMapToList(isFirst, dsDesc.get(TymyPref.ONE), "" + dsNews.get(getDsId(dsDesc.get(TymyPref.ONE))), tymyPref[0].getDsList());
				isFirst = false;
			}
			return tymyPref[0];
		}

		private String getDsId(String dsDesc) {
			return dsDesc.split(":")[0];
		}
	}
}
