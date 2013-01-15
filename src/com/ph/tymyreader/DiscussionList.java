package com.ph.tymyreader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DiscussionList extends ListActivity {
	TextView title;
	final String NAME = "name";
	final String NEW = "new";
	private String[] from = new String[] {NAME, NEW};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	List<HashMap<String, String>> discsList = new ArrayList<HashMap<String,String>>();
	private ArrayList<DiscussionPref> disPrefList = new ArrayList<DiscussionPref>();
	private TymPref tymPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussions_list);
		
		tymPref = (TymPref) getIntent().getSerializableExtra("tymPref");

		for ( HashMap<String, String> dP : tymPref.getDsList()) {
			DiscussionPref disPref = new DiscussionPref(tymPref.getUrl(), tymPref.getUser(), 
					tymPref.getPass(), tymPref.getCookies(), getDsId(dP.get("one")), getDsName(dP.get("one")));
			disPrefList.add(disPref);
			addDiscsList(false, getDsName(dP.get("one")), dP.get("two"));			
		}

		SimpleAdapter adapter = new SimpleAdapter(this, discsList, R.layout.two_line_list_discs, from, to);
		setListAdapter(adapter);
		setTitle("pd.tymy.cz");
		
		setTitle(tymPref.getUrl());
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
		Bundle bundle = new Bundle();

		bundle.putSerializable("disPref", disPrefList.get(position));
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.ph.tymyreader", "com.ph.tymyreader.DiscussionView"));
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	private void addDiscsList(boolean clear, String caption, String text) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(NAME, caption);
		map.put(NEW, text);
		if (clear) { discsList.clear(); }
		discsList.add(map);
	}
	
	private String getDsId(String dsDesc) {
		// TODO Auto-generated method stub
		return dsDesc.split(":")[0];
	}
	
	private String getDsName(String dsDesc) {
		// TODO Auto-generated method stub
		return dsDesc.split(":")[1];
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
