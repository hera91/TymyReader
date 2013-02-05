package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ph.tymyreader.model.DiscussionPref;
import com.ph.tymyreader.model.TymyPref;

public class DiscussionListActivity extends ListActivity {
	//private static final String TAG = "TymyReader";
	TextView title;
	final String NAME = "name";
	final String NEW = "new";
	private String[] from = new String[] {NAME, NEW};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();
	private ArrayList<DiscussionPref> dsPrefList = new ArrayList<DiscussionPref>();
	private TymyPref tymyPref;
	SimpleAdapter adapter;
	private TymyReader app;
	private int index;
	private TymyListUtil tlu;
	private UpdateNewItemsTymy updateNewItemsTymy;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussions_list);
		
		tlu = new TymyListUtil();
		app = (TymyReader) getApplication();

		index = (int) getIntent().getIntExtra("index", -1);
		if (index == -1) {
			setResult(RESULT_CANCELED);
			finish();
		}
		TymyReader app = (TymyReader) getApplication();
		tymyPref = app.getTymyPrefList().get(index);
		if (tymyPref == null) finish();
		setTitle(tymyPref.getUrl());


		adapter = new SimpleAdapter(this, dsList, R.layout.two_line_list_discs, from, to);
		setListAdapter(adapter);
		refreshDsList(tymyPref);		
	}

	/**
	 * 
	 */
	private void refreshDsList(TymyPref tymyPref) {
		boolean isFirst = true;
		for ( HashMap<String, String> dP : tymyPref.getDsList()) {
			DiscussionPref dsPref = new DiscussionPref(tymyPref.getUrl(), tymyPref.getUser(), 
					tymyPref.getPass(), tymyPref.getHttpContext(), getDsId(dP.get(TymyPref.ONE)), getDsName(dP.get(TymyPref.ONE)));
			if (dP.get(TymyPref.TWO).equals("")) {
				dsPref.setNewItems(0);
			} else {
				dsPref.setNewItems(Integer.parseInt(dP.get(TymyPref.TWO)));	
			}
			dsPrefList.add(dsPref);
			String items_new = (dP.get(TymyPref.TWO).equals("0") || dP.get(TymyPref.TWO).equals("")) ? "" : getString(R.string.items_new) + " " + dP.get(TymyPref.TWO);
			addDsList(isFirst, getDsName(dP.get(TymyPref.ONE)), items_new);
			isFirst = false;
		}
		if (adapter != null) adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onBackPressed() {
		Intent data = new Intent();
		data.putExtra("index", index);
		setResult(RESULT_OK, data);
	    super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_discusion_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			reloadTymyNewItems();
			return true;
		case R.id.menu_web:
			goToWeb();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void reloadTymyNewItems() {
		updateNewItemsTymy = new UpdateNewItemsTymy();
		updateNewItemsTymy.execute(tymyPref);		
	}
	
	private void goToWeb() {
		String attr = new String();
		attr = TymyLoader.getURLLoginAttr(tymyPref.getHttpContext());
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + tymyPref.getUrl() + attr));
		startActivity(browserIntent);
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (app.isOnline()) {
			app.setDsPref(dsPrefList.get(position));
			Intent intent = new Intent(this, DiscussionViewActivity.class);
			startActivity(intent);		
			clearNewItems(position);
		} else {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (updateNewItemsTymy != null) updateNewItemsTymy.cancel(true);
	}
	
	private void clearNewItems(int position) {
		HashMap<String, String> map = new HashMap<String, String>();
		String dsDesc = dsList.get(position).get(NAME);
		map.put(NAME, dsDesc);
		map.put(NEW, "");
		dsList.remove(position);
		dsList.add(position, map);
		adapter.notifyDataSetChanged();
	}

	private void addDsList(boolean clear, String caption, String text) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(NAME, caption);
		map.put(NEW, text);
		if (clear) { dsList.clear(); }
		dsList.add(map);
	}

	private String getDsId(String dsDesc) {
		return dsDesc.split(":")[0];
	}

	private String getDsName(String dsDesc) {
		if (dsDesc.split(":").length > 1) return dsDesc.split(":")[1];
		return "";
	}
	
	private class UpdateNewItemsTymy extends AsyncTask<TymyPref, Integer, TymyPref> {

		@Override
		protected TymyPref doInBackground(TymyPref... tymPref) {			
			return tlu.updateNewItems(tymPref);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			setProgress(progress[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(TymyPref tymPref) {
			refreshDsList(tymyPref);
		}
	}
}

