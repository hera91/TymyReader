package com.ph.tymyreader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.SimpleAdapter;

// TODO Zobecnit nacitaki stranek, aby umely pracovat s vice strankami a nebylo jen na tahani diskuse

// TODO Osetrit vypnuty internet


public class DiscussionView extends ListActivity {

//	private DiscussionPref disPref = new DiscussionPref("pd.tymy.cz", "HERA", "bistromat", "1", "kecarna");
	private DiscussionPref disPref;
	//private final String TAG = "TymyReader";
	final String CAP = "caption";
	final String TEXT = "text";
	private String[] from = new String[] {CAP, TEXT};
	private int[] to = new int[] {R.id.text1, R.id.text2};
	List<HashMap<String, String>> itemsList = new ArrayList<HashMap<String,String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_list_activity_view);
		
		disPref = (DiscussionPref) getIntent().getSerializableExtra("disPref");

		final DiscussionPref data = (DiscussionPref) getLastNonConfigurationInstance();
		if (data == null) {
			// activity was started
			setTitle( disPref.getUrl() + " / " + disPref.getName());

			addItemsList(true, getString(R.string.loading), disPref.getName() + " (" + disPref.getUrl() + ")");

			disPref.setDsItems(itemsList);
			SimpleAdapter adapter = new SimpleAdapter(this, itemsList, R.layout.two_line_list_item, from, to);
			setListAdapter(adapter);

			new DownloadWebpageText(getApplicationContext()).execute(disPref);
		} else {
			// after configuration change
			disPref = data;
			setTitle(disPref.getUrl() + " - " + disPref.getName());
			SimpleAdapter adapter = new SimpleAdapter(this, disPref.getDsItems(),
					R.layout.two_line_list_item, from, to);
			setListAdapter(adapter);
		}
	}

	/**
	 * Add caption-text pair into itemsList, if clear is true then before 
	 * add the list is cleared.
	 * 
	 * @param clear true for clear itemsList before add
	 * @param caption item caption
	 * @param text item text 
	 */
	private void addItemsList(boolean clear, String caption, String text) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(CAP, caption);
		map.put(TEXT, text);
		if (clear) { itemsList.clear(); }
		itemsList.add(map);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final DiscussionPref data = disPref;
		return data;
	}

	protected class DownloadWebpageText extends
	AsyncTask<DiscussionPref, Integer, String> {
		private Context context;

		public DownloadWebpageText(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(DiscussionPref... disPref) {
			TymyPageLoader page = new TymyPageLoader();
			return page.loadPage(disPref[0].getUrl(), disPref[0].getUser(), disPref[0].getPass(), disPref[0].getCookies(), disPref[0].getId());
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
			DsItem dsItem = new DsItem();
			TymyParser tParser = new TymyParser(input);
			itemsList.clear();
			while ((dsItem = tParser.getDsItem()) != null) {
				//Log.v(TAG, name + " " + post);
				addItemsList(false, dsItem.getDsCaption(), dsItem.getDsItemText());
			}

			if (itemsList != null){
				disPref.setDsItems(itemsList);
				SimpleAdapter adapter = new SimpleAdapter( context,	disPref.getDsItems(),
						R.layout.two_line_list_item, from, to);
				setListAdapter(adapter);
			}
		}
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
	private String name;
	private StringBuilder cookies = new StringBuilder();
	private String id;
	private List<HashMap<String, String>> dsItems;

	public DiscussionPref(String tym, String user, String pass, String id) {
		this.url = tym;
		this.pass = pass;
		this.user = user;
		this.id = id;
		this.name = id;
	}
	
	public DiscussionPref(String tym, String user, String pass, String id, String name) {
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
