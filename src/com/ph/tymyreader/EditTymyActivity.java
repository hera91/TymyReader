package com.ph.tymyreader;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.ph.tymyreader.model.TymyPref;

public class EditTymyActivity extends Activity {

	private TymyReader app;
	private TymyListUtil tlu = new TymyListUtil();

	//TODO osetrit prazdne polozky
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (TymyReader) getApplication();

		int position = (int) getIntent().getIntExtra("position", -1);

		setContentView(R.layout.activity_add_tymy);
		// -1 => Add new Tymy
		if (position != -1) fillFields(position);
		showPass(); // if checkbox is checked
	}

	private void fillFields(int position) {
		ArrayList<TymyPref> tymyPrefList = app.getTymyPrefList();
		EditText url = (EditText) findViewById(R.id.add_tymy_url_edit);
		url.setText(tymyPrefList.get(position).getUrl());
		EditText user = (EditText) findViewById(R.id.add_tymy_user_edit);
		user.setText(tymyPrefList.get(position).getUser());
		EditText pass = (EditText) findViewById(R.id.add_tymy_pass_edit);
		pass.setText(tymyPrefList.get(position).getPass());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_add_tymy, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	public void onClick (View v) {
		switch (v.getId()) {
		case R.id.button_save:
			saveTymy();
			return;
		case R.id.button_cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.show_pass:
			showPass();
			return;
		case R.id.try_login:
			tryLogin();
			return;
		}
	}

	private void tryLogin() {
		if (app.isOnline()) {
			EditText url = (EditText) findViewById(R.id.add_tymy_url_edit);
			if (isValidURL(url)) { 
				EditText user = (EditText) findViewById(R.id.add_tymy_user_edit);
				EditText pass = (EditText) findViewById(R.id.add_tymy_pass_edit);
				testLogin(url.getText().toString(), user.getText().toString(), pass.getText().toString());
			}
		} else {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
		}
	}

	private boolean isValidURL(EditText url) {
		boolean isValid = false;
		if (url.getText().toString().equals("")) return false;
		isValid = URLUtil.isValidUrl("http://" + url.getText().toString());
		if (!isValid) {
			Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
		}
		return (isValid);
	}

	private void showPass() {
		// TODO nevim pro ale zmena portrait/ladnscape tuhle metodu zblbne ..??
		CheckBox showPass = (CheckBox) findViewById(R.id.show_pass);
		EditText pass = (EditText) findViewById(R.id.add_tymy_pass_edit);
		if (showPass.isChecked()){
			pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		} else {
			pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}
		pass.refreshDrawableState();
	}

	private void saveTymy() {
		EditText url = (EditText) findViewById(R.id.add_tymy_url_edit);
		if (!isValidURL(url)) return;
		EditText user = (EditText) findViewById(R.id.add_tymy_user_edit);
		EditText pass = (EditText) findViewById(R.id.add_tymy_pass_edit);
		TymyPref tymyPref = new TymyPref(url.getText().toString().trim(), 
				user.getText().toString().trim(), 
				pass.getText().toString().trim());
		ArrayList<TymyPref> tymyPrefList = app.getTymyPrefList();
		tlu.updateTymyPrefList(tymyPrefList, tymyPref);
		app.setTymyPrefList(tymyPrefList);
		app.saveTymyCfg(tymyPrefList);

		Intent data = new Intent();
		data.putExtra("index", tymyPrefList.indexOf(tymyPref));
		setResult(RESULT_OK, data);
		finish();
	}

	private void testLogin(String url, String user, String pass) {
		TymyPref testTymyPref = new TymyPref(url, user, pass);
		new TestLogin().execute(testTymyPref);
	}
	
	private class TestLogin extends AsyncTask<TymyPref, Void, Boolean> {
		private ProgressDialog dialog = new ProgressDialog(EditTymyActivity.this);

		@Override
		protected void onPreExecute() {
			dialog.setMessage(getString(R.string.conecting));
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(TymyPref... tymyPref) {
			TymyLoader loader = new TymyLoader();
			return loader.testLogin(tymyPref[0].getUrl(), tymyPref[0].getUser(), tymyPref[0].getPass(), tymyPref[0].getHttpContext());
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Boolean loginOk) {
			try
			{
				if(dialog.isShowing())
				{
					dialog.dismiss();
				}
				// do your Display and data setting operation here
			} catch(Exception e) {
				Log.v(TymyReader.TAG, "Error while TestLogin " + e);				
			}
			Toast.makeText(EditTymyActivity.this, loginOk ? R.string.login_ok : R.string.login_failed, Toast.LENGTH_LONG).show();
		}
	}
}
