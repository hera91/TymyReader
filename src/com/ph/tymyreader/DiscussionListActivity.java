package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussions_list);
		
		app = (TymyReader) getApplication();

		int position = (int) getIntent().getIntExtra("position", -1);
		if (position == -1) finish();
		TymyReader app = (TymyReader) getApplication();
		tymyPref = app.getTymyPrefList().get(position);
		if (tymyPref == null) finish();

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
			addDsList(false, getDsName(dP.get(TymyPref.ONE)), items_new);			
		}

		adapter = new SimpleAdapter(this, dsList, R.layout.two_line_list_discs, from, to);
		setListAdapter(adapter);

		setTitle(tymyPref.getUrl());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_discusion_list, menu);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		app.setDsPref(dsPrefList.get(position));
		Intent intent = new Intent(this, DiscussionViewActivity.class);
		startActivity(intent);		
		clearNewItems(position);
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

}

