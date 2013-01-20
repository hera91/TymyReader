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
	private static final int EDIT_TYMY_ACTIVITY = 1;
	private String[] from = new String[] {TymyListUtil.ONE, TymyListUtil.TWO};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	private List<HashMap<String, String>> tymyList = new ArrayList<HashMap<String,String>>();
	private ArrayList<TymyPref> tymyPrefList = new ArrayList<TymyPref>();
	private SimpleAdapter adapter;
	private TymyListUtil tlu = new TymyListUtil();
	ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tymy_list);

		lv = (ListView) findViewById(android.R.id.list);
		TymyReader app = (TymyReader) getApplication();
		app.loadTymyCfg();
		tymyPrefList = app.getTymyPrefList();

		refreshTymyPrefList(app);
		// Set-up adapter for tymyList
		adapter = new SimpleAdapter(this, tymyList, R.layout.two_line_list_discs, from, to);
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
		int index = tlu.getIndexFromUrl(tymyPrefList, tymyList.get(position).get(TymyListUtil.ONE));
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
		int index = tlu.getIndexFromUrl(tymyPrefList, tymyList.get(info.position).get(TymyListUtil.ONE));
		if (index != -1) {
			menu.setHeaderTitle(tymyPrefList.get(index).getUrl());
			inflater.inflate(R.menu.tymy_list_context_menu, menu);
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int index = tlu.getIndexFromUrl(tymyPrefList, tymyList.get((int) info.id).get(TymyListUtil.ONE));
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
			new LoginAndUpdateTymy().execute(tP);
			tymyPrefList.remove(i);
			tymyPrefList.add(i, tP);
			app.setTymyPrefList(tymyPrefList);
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
			new UpdateNewItemsTymy().execute(tP);
			tymyPrefList.remove(i);
			tymyPrefList.add(i, tP);
			app.setTymyPrefList(tymyPrefList);
			i = i + 1;
		}
	}
	
	private void refreshListView() {
		TymyReader app = (TymyReader) getApplication();
		tymyPrefList = app.getTymyPrefList();
		if (tymyPrefList.isEmpty()) {
			tlu.addMapToList(true, getString(R.string.no_tymy), getString(R.string.no_tymy_hint), tymyList);						
		} else {
			tlu.updateTymyList(tymyPrefList, tymyList);
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
		protected void onPostExecute(TymyPref tymPref) {
			Toast.makeText(getApplicationContext(), "discussions list " + tymPref.getUrl() + " updated" , Toast.LENGTH_SHORT).show();
			refreshListView();
		}

		private TymyPref updateTymDs(TymyPref... tymPref) {

			TymyPageLoader page = new TymyPageLoader();
			TymyParser parser = new TymyParser();
			HashMap<String, Integer> dsNews = new HashMap<String, Integer>();

			String mainPage = page.loadMainPage(tymPref[0].getUrl(), tymPref[0].getUser(), tymPref[0].getPass(), tymPref[0].getCookies());
			String ajax = page.loadAjaxPage(tymPref[0].getUrl(), tymPref[0].getCookies());

			dsNews = parser.getNewItems(ajax);
			boolean isFirst = true; // clear map in first cycle
			for ( String dsDesc : parser.getDsArray(mainPage)) {
				tlu.addMapToList(isFirst, dsDesc, "" + dsNews.get(getDsId(dsDesc)), tymPref[0].getDsList());
				isFirst = false;
			}

			return tymPref[0];
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

		private TymyPref updateNewItems(TymyPref... tymyPref) {

			if (tymyPref[0].getCookies().length() == 0) return tymyPref[0];
			
			TymyPageLoader page = new TymyPageLoader();
			TymyParser parser = new TymyParser();
			HashMap<String, Integer> dsNews = new HashMap<String, Integer>();
			String ajax = page.loadAjaxPage(tymyPref[0].getUrl(), tymyPref[0].getCookies());

			dsNews = parser.getNewItems(ajax);
			boolean isFirst = true; // clear map in first cycle
			
			ArrayList<HashMap<String, String>> copy_DsList = new ArrayList<HashMap<String,String>>();
			for (HashMap<String, String> dsDesc : tymyPref[0].getDsList()) {
				copy_DsList.add(dsDesc);
			}
			for ( HashMap<String, String> dsDesc : copy_DsList) {
				tlu.addMapToList(isFirst, dsDesc.get(TymyListUtil.ONE), "" + dsNews.get(getDsId(dsDesc.get(TymyListUtil.ONE))), tymyPref[0].getDsList());
				isFirst = false;
			}
			return tymyPref[0];
		}

		private String getDsId(String dsDesc) {
			return dsDesc.split(":")[0];
		}
	}
}
