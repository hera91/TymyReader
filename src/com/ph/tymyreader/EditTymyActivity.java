package com.ph.tymyreader;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ph.tymyreader.model.TymyPref;

public class EditTymyActivity extends Activity {

	private TymyReader app;
	private TymyListUtil tlu = new TymyListUtil();
	
	//TODO osetrit prazdne polozky
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int position = (int) getIntent().getIntExtra("position", -1);
		
		setContentView(R.layout.activity_add_tymy);
		// -1 => Add new Tymy
		if (position != -1) fillFields(position);
		showPass(); // if checkbox is checked
	}

	private void fillFields(int position) {
		// TODO Auto-generated method stub
		app = (TymyReader) getApplication();
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
		case R.id.button_cancel:
			setResult(RESULT_CANCELED);
			finish();
		case R.id.show_pass:
			showPass();
		}
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
		// TODO Auto-generated method stub
		EditText url = (EditText) findViewById(R.id.add_tymy_url_edit);
		EditText user = (EditText) findViewById(R.id.add_tymy_user_edit);
		EditText pass = (EditText) findViewById(R.id.add_tymy_pass_edit);
		TymyPref tymyPref = new TymyPref(url.getText().toString().trim(), 
										user.getText().toString().trim(), 
										pass.getText().toString().trim());
		app = (TymyReader) getApplication();
		ArrayList<TymyPref> tymyPrefList = app.getTymyPrefList();
		tlu.updateTymyPrefList(tymyPrefList, tymyPref);
		app.setTymyPrefList(tymyPrefList);
		app.saveTymyCfg(tymyPrefList);
		
		Intent data = new Intent();
		data.putExtra("index", tymyPrefList.indexOf(tymyPref));
		setResult(RESULT_OK, data);
		finish();
	}
}
