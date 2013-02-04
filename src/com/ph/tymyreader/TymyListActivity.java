package com.ph.tymyreader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
	private static final int DS_LIST_ACTIVITY = 2;
	private String[] from = new String[] {TymyPref.ONE, TymyPref.TWO};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	private List<HashMap<String, String>> tymyList = new ArrayList<HashMap<String,String>>();
	private ArrayList<TymyPref> tymyPrefList = new ArrayList<TymyPref>();
	private SimpleAdapter adapter;
	private TymyListUtil tlu = new TymyListUtil();
	ListView lv;
	private TymyReader app; 
	private List<LoginAndUpdateTymy> loginAndUpdateTymy = new ArrayList<TymyListActivity.LoginAndUpdateTymy>();
	private List<UpdateNewItemsTymy> updateNewItemsTymy = new ArrayList<TymyListActivity.UpdateNewItemsTymy>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tymy_list);
		app = (TymyReader) getApplication();
		adapter = new SimpleAdapter(this, tymyList, R.layout.two_line_list_discs, from, to);

		@SuppressWarnings("unchecked")
		List<HashMap<String, String>> data = (List<HashMap<String, String>>) getLastNonConfigurationInstance();
		if (data == null) {
			// activity was started => load configuration
			app.loadTymyCfg();
			tymyPrefList = app.getTymyPrefList();
			
			refreshListView();
			//refresh discussions from web
//			reloadTymyDsList(); // reload i seznamu diskusi, muze byt pomaljesi
			reloadTymyNewItems(); // reload pouze poctu novych prispevku
		} else {
			// Configuration was changed, reload data
			tymyList = data;
			adapter = new SimpleAdapter(this, tymyList, R.layout.two_line_list_discs, from, to);
		}
		// Set-up adapter for tymyList
		lv = getListView();
		lv.setAdapter(adapter);

		registerForContextMenu(getListView());
		if (!app.isOnline()) {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		//cancel background threads
		//Log.v(TymyReader.TAG, "onPause: cancel loaders");
		for (LoginAndUpdateTymy loader : loginAndUpdateTymy) {
			//Log.v(TymyReader.TAG, "onPause: cancel loader " + loader.toString());
			if (loader != null) {
				//Log.v(TymyReader.TAG, "onPause: cancel loader " + loader.toString());
				loader.cancel(true);
			}
		}
		for (UpdateNewItemsTymy loader : updateNewItemsTymy) {
			if (loader != null) {
				//Log.v(TymyReader.TAG, "onPause: cancel loader " + loader.toString());
				loader.cancel(true);
			}
		}
	}
	
	@Override
	protected void onDestroy () {
		super.onDestroy();
		//save configuration
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
//			refreshTymyPrefList();
			reloadTymyNewItems();
			return true;
//		case R.id.menu_send_report:
//			ACRA.getErrorReporter().handleException(new Exception("Manual report"));
//			return true;
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
		bundle.putInt("index", index);
		Intent intent = new Intent(this, DiscussionListActivity.class);
		intent.putExtras(bundle);
		startActivityForResult(intent, DS_LIST_ACTIVITY);				
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
		case R.id.menu_context_web:
			goToWeb(index);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void goToWeb(int index) {
		String attr = new String();
		if (tymyPrefList.get(index).getHttpContext() == null) {
			// TODO Doresit spravnou skladbu parametru v URL aby nebylo nutne prihlasovani
			attr = TymyLoader.getURLLoginAttr(tymyPrefList.get(index).getHttpContext());
		}
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + tymyPrefList.get(index).getUrl() + attr));
		startActivity(browserIntent);
	}

	// *****************  Setting  ******************** //
	private void showSettings() {
		Intent intent = new Intent(this, GeneralSettingsActivity.class);
		startActivity(intent);		
	}

	private void showAddTymy() {
		showAddTymy(-1);
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
				int index = data.getIntExtra("index", -1);
				reloadTymyDsList(index);
				refreshListView();
			}
			break;
		case DS_LIST_ACTIVITY:
			if (resultCode == RESULT_OK) {
				int index = data.getIntExtra("index", -1);
				reloadTymyNewItems(index);
			}
			break;	
		}
	}

	private void deleteTymy(int position) {
		app.deleteTymyCfg(tymyPrefList.get(position).getUrl());
		tlu.removeTymyPref(tymyPrefList, position);
		app.setTymyPrefList(tymyPrefList);
		refreshListView();
	}

	// TODO tyhle methody by meli byt v samostatny tride
	private void reloadTymyDsList(int index) {
		if (app.isOnline()) {
			if (index == -1) reloadTymyDsList();
			int i = 0;
			i = loginAndUpdateTymy.size();
			loginAndUpdateTymy.add(i, (LoginAndUpdateTymy) new LoginAndUpdateTymy());
			loginAndUpdateTymy.get(i).execute(tymyPrefList.get(index));
			app.setTymyPrefList(tymyPrefList);
			app.saveTymyCfg(tymyPrefList);
		}
	}


	private void reloadTymyDsList() {
		if (app.isOnline()) {
			// Slozitejsi pouziti copy_tymyPrefList aby se zabranilo soucasne modifikaci tymyPrefList
			ArrayList<TymyPref> copy_tymyPrefList = new ArrayList<TymyPref>();
			for (TymyPref tP : tymyPrefList) {
				copy_tymyPrefList.add(tP);
			}
			int i = 0;
			for(TymyPref tP : copy_tymyPrefList) {
				i = loginAndUpdateTymy.size();
				int index = copy_tymyPrefList.indexOf(tP);
				loginAndUpdateTymy.add(i, (LoginAndUpdateTymy) new LoginAndUpdateTymy());
				loginAndUpdateTymy.get(i).execute(tP);
				tymyPrefList.remove(index);
				tymyPrefList.add(index, tP);
				app.setTymyPrefList(tymyPrefList);
				//This maybe could cause problems when due to lost connectivity the download data will be corrupted,
				//but next update should fix it (or refresh UI functionality)
				app.saveTymyCfg(tymyPrefList);
			}
		}
	}

	private void reloadTymyNewItems(int index) {
		if (app.isOnline()) {
			if (index == -1) reloadTymyNewItems();
			// Slozitejsi pouziti copy_tymyPrefList aby se zabranilo soucasne modifikaci tymyPrefList
			int i = 0;
			i = updateNewItemsTymy.size();
			updateNewItemsTymy.add(i, new UpdateNewItemsTymy());
			updateNewItemsTymy.get(i).execute(tymyPrefList.get(index));
			app.setTymyPrefList(tymyPrefList);
			refreshListView();
		}
	}

	private void reloadTymyNewItems() {
		if (app.isOnline()) {
			// Slozitejsi pouziti copy_tymyPrefList aby se zabranilo soucasne modifikaci tymyPrefList
			ArrayList<TymyPref> copy_tymyPrefList = new ArrayList<TymyPref>();
			for (TymyPref tP : tymyPrefList) {
				copy_tymyPrefList.add(tP);
			}
			int i = 0;
			for(TymyPref tP : copy_tymyPrefList) {
				i = updateNewItemsTymy.size();
				int index = copy_tymyPrefList.indexOf(tP);
				updateNewItemsTymy.add(i, new UpdateNewItemsTymy());
				updateNewItemsTymy.get(i).execute(tP);
				tymyPrefList.remove(index);
				tymyPrefList.add(index, tP);
				app.setTymyPrefList(tymyPrefList);
			}
			refreshListView();
		}
	}

	private void refreshListView() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String noNewItems = pref.getString(getString(R.string.no_new_items_key), getString(R.string.no_new_items_default));
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
		protected TymyPref doInBackground(TymyPref... tymyPref) {			
			return updateTymDs(tymyPref);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			setProgress(progress[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(TymyPref tymyPref) {
			//			Toast.makeText(getApplicationContext(), "discussions list " + tymyPref.getUrl() + " updated" , Toast.LENGTH_SHORT).show();
			refreshListView();
		}

		// TODO premistit tuhle funkci do jine tridy
		private TymyPref updateTymDs(TymyPref... tymyPref) {

			TymyLoader page = new TymyLoader();
			TymyParser parser = new TymyParser();
			HashMap<String, Integer> dsNews = new HashMap<String, Integer>();

			String mainPage = page.loadMainPage(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getHttpContext());
			if (mainPage == null ) return tymyPref[0];
			
			String ajax = page.loadAjaxPage(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getHttpContext());
			if (ajax != null) {
				dsNews = parser.getNewItems(ajax);
			}
			
			boolean isFirst = true; // clear map in first cycle
			for ( String dsDesc : parser.getDsArray(mainPage)) {
				Integer news = dsNews.get(getDsId(dsDesc));
				news = news == null ? 0 : news;
				tlu.addMapToList(isFirst, dsDesc, "" + news, tymyPref[0].getDsList());
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

			TymyLoader page = new TymyLoader();
			TymyParser parser = new TymyParser();
			HashMap<String, Integer> dsNews = new HashMap<String, Integer>();
			String ajax = page.loadAjaxPage(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getHttpContext());

			if (ajax == null) {
				return tymyPref[0];
			}
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
