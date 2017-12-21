package es.ihm.ismadani.calendar.controller;

import es.ihm.ismadani.calendar.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.widget.TextView;

public class AboutMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		  TextView textView = new TextView(this);
		  textView.setTextSize(17);
		  textView.setText(getString(R.string.about));	
		  textView.setGravity(Gravity.CENTER);
		  setContentView(textView);
		
	}

}
