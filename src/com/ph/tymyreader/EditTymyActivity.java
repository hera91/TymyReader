package com.ph.tymyreader;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class EditTymyActivity extends Activity {

	private TymConfigManager cfg = new TymConfigManager(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_tymy);
		showPass(); // if checkbox is checked
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
		TymPref tymPref = new TymPref(url.getText().toString(), user.getText().toString(), pass.getText().toString());
		cfg.saveCfg(tymPref);
		finish();
	}

}
