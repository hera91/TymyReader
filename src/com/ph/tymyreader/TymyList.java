package com.ph.tymyreader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

// TODO doplnit nacteni cookies jakmile vyberu tuhle stranku a pak docilit toho aby cookies DisView uz cookies dostalo

public class TymyList extends ListActivity {
	TextView title;
	private static final String TAG = "TymyReader";
	final String ONE = "one";
	final String TWO = "two";
	private String[] from = new String[] {ONE, TWO};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	List<HashMap<String, String>> tymyList = new ArrayList<HashMap<String,String>>();
	private TymPref tymPref = new TymPref("pd.tymy.cz", "HERA", "bistromat", "1", "kecarna");
	private ArrayList<TymPref> tymPrefList = new ArrayList<TymPref>();
	//	List<HashMap<String, String>> itemsList = new ArrayList<HashMap<String,String>>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discussions_list);

		for (Integer i = 0; i < 4; i++ ){
			tymPrefList.add(tymPref);
		}
		for (TymPref tP : tymPrefList) {
			new LoginToTym().execute(tP);
			addMapToList(false, tP.getUrl(), "", tymyList);			
		}

		SimpleAdapter adapter = new SimpleAdapter(this, tymyList, R.layout.two_line_list_discs, from, to);
		setListAdapter(adapter);
		setTitle("pd.tymy.cz");

		ListView lv = (ListView) findViewById(android.R.id.list);
		registerForContextMenu(lv);
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View lv,
					int position, long id) {
				Toast t = Toast.makeText(getApplicationContext(), "LongClick position " + position, Toast.LENGTH_LONG);
				t.show();
				return true;
			}
		});
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

		bundle.putSerializable("tymPref", tymPrefList.get(position));
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.ph.tymyreader", "com.ph.tymyreader.DiscussionList"));
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

	//	ListView lv = (ListView) findViewById(android.R.id.list);
	//	registerForContextMenu(lv);
	//	lv.setOnItemLongClickListener(new OnItemLongClickListener() {
	//		@Override
	//		public boolean onItemLongClick(AdapterView<?> parent, View lv,
	//				int position, long id) {
	//			Toast t = Toast.makeText(getApplicationContext(), "LongClick position " + position, Toast.LENGTH_LONG);
	//			t.show();
	//			return true;
	//		}
	//	});

	private void addMapToList(boolean clear, String one, String two, List<HashMap<String, String>> list) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(ONE, one);
		map.put(TWO, two);
		if (clear) { list.clear(); }
		list.add(map);
	}

	private class LoginToTym extends AsyncTask<TymPref, Integer, String> {

		@Override
		protected String doInBackground(TymPref... tymPref) {
			String t = null;
			TymyPageLoader page = new TymyPageLoader();
			page.login(tymPref[0].getUrl(), tymPref[0].getUser(), tymPref[0].getPass(), tymPref[0].getCookies());
			t = page.loadMainPage(tymPref[0].getUrl(), tymPref[0].getUser(), tymPref[0].getPass(), tymPref[0].getCookies());
			for ( String id : getDis(t)){
				Log.v(TAG, "id :" + id);
				addMapToList(false, id, "", tymPref[0].getDsList());
			}
			return t;
		}

		private ArrayList<String> getDis(String t) {
			// TODO Auto-generated method stub
			int oauth = 0;
			int end = 0;
			ArrayList<String> dis = new ArrayList<String>();

			final String PAT = "id=\"ds_new_";
			while ((oauth = t.indexOf(PAT, end)) != -1) {				
				if (oauth != -1) {
					int start = oauth + PAT.length(); 
					end = t.indexOf('"', start);
					String dsId = end == -1 ? t.substring(start) : t
							.substring(start, end);
					dis.add(dsId);
				}
			}
			return dis;
		}

		protected void onProgressUpdate(Integer... progress) {
			setProgress(progress[0]);
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String input) {
			showList(input);
		}

		private void showList(String input) {
			if ( input.length() == 0 ) {
				input = "No information found\n";
				return;
			}
			tymyList.clear();
			for (int i = 0; i < 4; i++) {
				addMapToList(false, tymPref.getUrl(), "", tymyList);
			}

			if (tymyList != null){
				SimpleAdapter adapter = new SimpleAdapter( getApplicationContext(),	tymyList,
						R.layout.two_line_list_item, from, to);
				setListAdapter(adapter);
			}
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
	private String name;
	private StringBuilder cookies = new StringBuilder();
	private String id;
	private List<HashMap<String, String>> dsList = new ArrayList<HashMap<String,String>>();

	public TymPref(String tym, String user, String pass, String id) {
		this.url = tym;
		this.pass = pass;
		this.user = user;
		this.id = id;
		this.name = id;
	}

	public TymPref(String tym, String user, String pass, String id, String name) {
		this.url = tym;
		this.user = user;
		this.pass = pass;
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
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<HashMap<String, String>> getDsList() {
		return dsList;
	}
	public void setDsList(List<HashMap<String, String>> dsList) {
		this.dsList = dsList;
	}

}

