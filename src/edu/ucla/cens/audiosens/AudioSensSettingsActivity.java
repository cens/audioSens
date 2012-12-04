package edu.ucla.cens.audiosens;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AudioSensSettingsActivity extends Activity {

	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;
	
	ToggleButton enabledButton;
	EditText period_et;
	EditText duration_et;
	CheckBox autoStart_cb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_sens_settings);
		
		//Shared Preferences
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);   
		mEditor = mSettings.edit();

		//Initialize UI Elements
		enabledButton = (ToggleButton)findViewById(R.id.enabled_toggleButton);
		period_et = (EditText)findViewById(R.id.period_et);
		duration_et = (EditText)findViewById(R.id.duration_et);
		autoStart_cb = (CheckBox)findViewById(R.id.autostart_cb);
		
		enabledButton.setChecked(mSettings.getBoolean(PreferencesHelper.ENABLED, false));
		period_et.setText(mSettings.getInt(PreferencesHelper.PERIOD, AudioSensConfig.PERIOD)+"");
		duration_et.setText(mSettings.getInt(PreferencesHelper.DURATION, AudioSensConfig.DURATION)+"");
		autoStart_cb.setChecked(mSettings.getBoolean(PreferencesHelper.AUTOSTART, AudioSensConfig.AUTOSTART));
		
		//Listener for On Off Switch
		enabledButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				Logger.w("ONOFF CHECKED:"+isChecked);
				if(isChecked)
				{
					if(!savePreferences())
					{
						//Error in chosen Settings, hence cancel change
						buttonView.setChecked(!isChecked);
						return;
					}
					
					//Disable Editing
					enableDisableView((View)findViewById(R.id.scrollView1),false);
					mEditor.putBoolean(PreferencesHelper.ENABLED, true);
					mEditor.commit();
					
					Toast.makeText(getApplicationContext(), "Recording Service started", Toast.LENGTH_SHORT).show();
					startService(new Intent(AudioSensSettingsActivity.this, AudioSensService.class));
				}
				else
				{
					//Enable Editing
					enableDisableView((View)findViewById(R.id.scrollView1),true);
					mEditor.putBoolean(PreferencesHelper.ENABLED, false);
					mEditor.commit();

					Toast.makeText(getApplicationContext(), "Recording Service Stopped", Toast.LENGTH_SHORT).show();
					stopService(new Intent(AudioSensSettingsActivity.this, AudioSensService.class));
				}
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_audio_sens_settings, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
		case R.id.menu_save:
			if(!mSettings.getBoolean(PreferencesHelper.ENABLED, false) && savePreferences())
				displayMessage("Info", "User Settings successfully saved");
			else
				displayMessage("Wrror", "User Settings could not be saved!");
			return true;
		}
		return false;
	}
	
	/*
	 * Validates and Save Preferences
	 */
	private boolean savePreferences()
	{
		int period;
		int duration;
		try
		{
			period = Integer.parseInt(period_et.getText().toString());
			duration = Integer.parseInt(duration_et.getText().toString());
		}
		catch(NumberFormatException ne)
		{
			displayMessage("Input Error", "Use integer values as input for Period and Duration");
			return false;
		}
		
		if(period <= 0 || duration <= 0)
		{
			displayMessage("Input Error", "Use non-zero integer values as input for Period and Duration");
			return false;
		}
		
		if(duration > period)
		{
			displayMessage("Input Error", "Period should be greater than or equal to the Recording Duration");
			return false;
		}
		
		mEditor.putInt(PreferencesHelper.PERIOD, period);
		mEditor.putInt(PreferencesHelper.DURATION, duration);
		mEditor.putBoolean(PreferencesHelper.AUTOSTART, autoStart_cb.isChecked());
		mEditor.commit();

		return true;
	}
	
	/*
	 * Disables all elements in the viewGroup
	 */
	private void enableDisableView(View view, boolean enabled) 
	{
	    view.setEnabled(enabled);
	    if ( view instanceof ViewGroup ) 
	    {
	        ViewGroup group = (ViewGroup)view;
	        for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) 
	        {
	            enableDisableView(group.getChildAt(idx), enabled);
	        }
	    }
	}
	
	/*
	 * Pops up a message Box
	 */
	private void displayMessage(String title, String message)
	{
		new AlertDialog.Builder(this).setTitle(title).setMessage(message).setNeutralButton("OK", null).show(); 
	}

}
