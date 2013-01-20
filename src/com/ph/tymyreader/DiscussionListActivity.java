package com.ph.tymyreader;

import java.io.Serializable;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussions_list);

		int position = (int) getIntent().getIntExtra("position", -1);
		if (position == -1) finish();
		TymyReader app = (TymyReader) getApplication();
		tymyPref = app.getTymyPrefList().get(position);
		if (tymyPref == null) finish();

		for ( HashMap<String, String> dP : tymyPref.getDsList()) {
			DiscussionPref dsPref = new DiscussionPref(tymyPref.getUrl(), tymyPref.getUser(), 
					tymyPref.getPass(), tymyPref.getCookies(), getDsId(dP.get("one")), getDsName(dP.get("one")));
			dsPrefList.add(dsPref);
			String items_new = (dP.get("two").equals("0") || dP.get("two").equals("")) ? "" : getString(R.string.items_new) + dP.get("two");
			addDsList(false, getDsName(dP.get("one")), items_new);			
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
		clearNewItems(position);
		Bundle bundle = new Bundle();
		bundle.putSerializable("dsPref", dsPrefList.get(position));
		Intent intent = new Intent(this, DiscussionViewActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);		
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

class DiscussionPref implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url;
	private String user;			
	private String pass;
	private StringBuilder cookies = new StringBuilder();
	private String id;
	private String name;
	private List<HashMap<String, String>> dsItems;

	public DiscussionPref(String tym, String user, String pass, StringBuilder cookies, String id) {
		this.url = tym;
		this.pass = pass;
		this.user = user;
		this.cookies = cookies;
		this.id = id;
		this.name = id;
	}

	public DiscussionPref(String tym, String user, String pass, StringBuilder cookies, String id, String name) {
		this.url = tym;
		this.user = user;
		this.pass = pass;
		this.cookies = cookies;
		this.id = id;
		this.name = name;
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
	public String getName() {
		return name;
	}		
	public void setName(String name) {
		this.name = name;
	}
	public StringBuilder getCookies() {
		return cookies;
	}
	public void setCookies(StringBuilder myCookie) {
		this.cookies = myCookie;
	}
	public List<HashMap<String, String>> getDsItems() {
		return dsItems;
	}
	public void setDsItems(List<HashMap<String, String>> dsItems) {
		this.dsItems = dsItems;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
