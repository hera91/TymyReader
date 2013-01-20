package com.ph.tymyreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import android.widget.Toast;

// TODO Zobecnit nacitaki stranek, aby umely pracovat s vice strankami a nebylo jen na tahani diskuse

// TODO Osetrit vypnuty internet


public class DiscussionViewActivity extends ListActivity {

//	private DiscussionPref dsPref = new DiscussionPref("pd.tymy.cz", "HERA", "bistromat", "1", "kecarna");
	private DiscussionPref dsPref;
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
		
		dsPref = (DiscussionPref) getIntent().getSerializableExtra("dsPref");

		final DiscussionPref data = (DiscussionPref) getLastNonConfigurationInstance();
		if (data == null) {
			// activity was started
			addItemsList(true, getString(R.string.loading), dsPref.getName() + " (" + dsPref.getUrl() + ")");

			dsPref.setDsItems(itemsList);
			SimpleAdapter adapter = new SimpleAdapter(this, itemsList, R.layout.two_line_list_item, from, to);
			setListAdapter(adapter);

			new DownloadWebpageText(getApplicationContext()).execute(dsPref);
		} else {
			// after configuration change
			dsPref = data;
			SimpleAdapter adapter = new SimpleAdapter(this, dsPref.getDsItems(),
					R.layout.two_line_list_item, from, to);
			setListAdapter(adapter);
		}
		setTitle( dsPref.getUrl() + " / " + dsPref.getName());

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
		getMenuInflater().inflate(R.menu.activity_discussion_view, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_post:
			Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final DiscussionPref data = dsPref;
		return data;
	}

	protected class DownloadWebpageText extends
	AsyncTask<DiscussionPref, Integer, String> {
		private Context context;

		public DownloadWebpageText(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(DiscussionPref... dsPref) {
			TymyPageLoader page = new TymyPageLoader();
			return page.loadDsPage(dsPref[0].getUrl(), dsPref[0].getUser(), dsPref[0].getPass(), dsPref[0].getCookies(), dsPref[0].getId());
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
				dsPref.setDsItems(itemsList);
				SimpleAdapter adapter = new SimpleAdapter( context,	dsPref.getDsItems(),
						R.layout.two_line_list_item, from, to);
				setListAdapter(adapter);
			}
		}
	}
}
