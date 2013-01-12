package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

// TODO doplnit nacteni cookies jakmile vyberu tuhle stranku a pak docilit toho aby cookies DisView uz cookies dostalo

public class DiscussionList extends ListActivity {
	TextView title;
	final String NAME = "name";
	final String NEW = "new";
	private String[] from = new String[] {NAME, NEW};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	List<HashMap<String, String>> discsList = new ArrayList<HashMap<String,String>>();
	//	private DiscussionPref disPref = new DiscussionPref("pd.tymy.cz", "HERA", "bistromat", "1", "kecarna");
	private ArrayList<DiscussionPref> disPrefList = new ArrayList<DiscussionPref>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussions_list);

		for (Integer i = 0; i < 4; i++ ){
			DiscussionPref disPref = new DiscussionPref("pd.tymy.cz", "HERA", "bistromat", Integer.toString(i));
			disPrefList.add(disPref);
		}
		for (DiscussionPref dP : disPrefList) {
			addDiscsList(false, dP.getName(), "");			
		}

		SimpleAdapter adapter = new SimpleAdapter(this, discsList, R.layout.two_line_list_discs, from, to);
		setListAdapter(adapter);
		setTitle("pd.tymy.cz");
		
		//		title = (TextView) findViewById(R.id.title);
		//		title.setText("pd.tymy.cz");
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.discussions_list_context_menu, menu);
	}

	private void addDiscsList(boolean clear, String caption, String text) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(NAME, caption);
		map.put(NEW, text);
		if (clear) { discsList.clear(); }
		discsList.add(map);
	}

}
