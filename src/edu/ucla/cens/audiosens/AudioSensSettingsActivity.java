package edu.ucla.cens.audiosens;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;
import edu.ucla.cens.audiosens.helper.UIHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AudioSensSettingsActivity extends Activity {

	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;
	private boolean adminMode = false;

	ToggleButton enabledButton;
	ToggleButton adminButton;
	EditText period_et;
	EditText duration_et;
	EditText specialstatus_et;
	EditText silencerate_et;
	EditText speechrate_et;
	Button edit_b;
	CheckBox specialMode_cb;
	CheckBox speechtriggerMode_cb;
	CheckBox continuousMode_cb;	
	CheckBox rawMode_cb;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_sens_settings);
		setTitle("AudioSens- Settings");

		//Shared Preferences
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);   
		mEditor = mSettings.edit();

		//Initialize UI Elements
		enabledButton = (ToggleButton)findViewById(R.id.enabled_toggleButton);
		adminButton = (ToggleButton)findViewById(R.id.admin_toggleButton);

		period_et = (EditText)findViewById(R.id.period_et);
		duration_et = (EditText)findViewById(R.id.duration_et);

		specialMode_cb = (CheckBox)findViewById(R.id.specialmode_cb);
		specialstatus_et = (EditText)findViewById(R.id.special_et);
		edit_b = (Button)findViewById(R.id.edit_b);

		speechtriggerMode_cb= (CheckBox)findViewById(R.id.speechtrigger_cb);
		speechrate_et = (EditText)findViewById(R.id.speechrate_et);
		silencerate_et = (EditText)findViewById(R.id.silencerate_et);


		continuousMode_cb = (CheckBox)findViewById(R.id.continuousmode_cb);
		rawMode_cb = (CheckBox)findViewById(R.id.raw_cb);

		refreshUI();

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
					enableDisableSettings(false);
					mEditor.putBoolean(PreferencesHelper.ENABLED, true);
					mEditor.commit();

					Toast.makeText(getApplicationContext(), "Recording Service started", Toast.LENGTH_SHORT).show();
					startService(new Intent(AudioSensSettingsActivity.this, AudioSensService.class));
				}
				else
				{
					//Enable Editing
					enableDisableSettings(true);
					mEditor.putBoolean(PreferencesHelper.ENABLED, false);
					mEditor.commit();

					Toast.makeText(getApplicationContext(), "Recording Service Stopped", Toast.LENGTH_SHORT).show();
					stopService(new Intent(AudioSensSettingsActivity.this, AudioSensService.class));
				}
			}
		});

		//Listener for Admin Switch
		adminButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				Logger.w("Admin CHECKED:"+isChecked);
				adminMode = isChecked;
				if(isChecked)
				{
					showAdminSettings(true);
				}
				else
				{
					showAdminSettings(false);
				}
			}
		});

		//Listener for Continuous Mode
		continuousMode_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				Logger.w("CONTINUOUSRECORDING CHECKED:"+isChecked);
				if(isChecked)
				{
					enableDisableSampleSettings(false);
				}
				else
				{
					enableDisableSampleSettings(true);
				}
			}
		});

		//Listener for time Setting button
		edit_b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) 
			{
				Intent timeSettings_Intent = new Intent(AudioSensSettingsActivity.this, AudioSensTimeSettingsActivity.class);
				startActivity(timeSettings_Intent);
			}
		});

	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		refreshUI();
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

	private void refreshUI()
	{
		enabledButton.setChecked(mSettings.getBoolean(PreferencesHelper.ENABLED, false));
		adminButton.setChecked(adminMode);

		period_et.setText(mSettings.getInt(PreferencesHelper.PERIOD, AudioSensConfig.PERIOD)+"");
		duration_et.setText(mSettings.getInt(PreferencesHelper.DURATION, AudioSensConfig.DURATION)+"");

		specialMode_cb.setChecked(mSettings.getBoolean(PreferencesHelper.SPECIALMODE, AudioSensConfig.SPECIALMODE_DEFAULT));
		specialstatus_et.setText(getSpecialStatus());

		speechtriggerMode_cb.setChecked(mSettings.getBoolean(PreferencesHelper.SPEECHTRIGGERMODE, AudioSensConfig.SPEECHTRIGGERMODE_DEFAULT));
		speechrate_et.setText(mSettings.getFloat(PreferencesHelper.SPEECHRATE, 1)+"");
		silencerate_et.setText(mSettings.getFloat(PreferencesHelper.SILENCERATE, 1)+"");

		continuousMode_cb.setChecked(mSettings.getBoolean(PreferencesHelper.CONTINUOUSMODE, AudioSensConfig.CONTINUOUSMODE_DEFAULT));
		rawMode_cb.setChecked(mSettings.getBoolean(PreferencesHelper.RAWMODE, AudioSensConfig.RAWMODE_DEFAULT));

		//on Starting the activity
		if(!enabledButton.isChecked())
		{
			enableDisableSettings(true);
		}
		else
		{
			enableDisableSettings(false);
		}

		//on Starting the activity
		if(!adminButton.isChecked())
		{
			showAdminSettings(false);
		}
		else
		{
			showAdminSettings(true);
		}
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
		
		float speechRate, silenceRate;
		try
		{
			speechRate = Float.parseFloat(speechrate_et.getText().toString());
			silenceRate = Float.parseFloat(silencerate_et.getText().toString());
		}
		catch(NumberFormatException ne)
		{
			displayMessage("Input Error", "Use numberic values as input for Speech and Silence Rates");
			return false;
		}

		if(speechRate ==0 || silenceRate == 0)
		{
			displayMessage("Input Error", "Use non-zero integer values as inputs for Speech and Silence Rates");
			return false;
		}

		mEditor.putInt(PreferencesHelper.PERIOD, period);
		mEditor.putInt(PreferencesHelper.DURATION, duration);
		mEditor.putBoolean(PreferencesHelper.CONTINUOUSMODE, continuousMode_cb.isChecked());
		mEditor.putBoolean(PreferencesHelper.SPECIALMODE, specialMode_cb.isChecked());
		mEditor.putBoolean(PreferencesHelper.SPEECHTRIGGERMODE, speechtriggerMode_cb.isChecked());
		mEditor.putBoolean(PreferencesHelper.RAWMODE, rawMode_cb.isChecked());
		mEditor.putFloat(PreferencesHelper.SPEECHRATE, speechRate);
		mEditor.putFloat(PreferencesHelper.SILENCERATE, silenceRate);
		mEditor.commit();

		return true;
	}


	private void enableDisableSettings(boolean enabled)
	{
		if(enabled)
		{
			UIHelper.enableDisableView((View)findViewById(R.id.scrollView1),true);
			if(continuousMode_cb.isChecked())
			{
				enableDisableSampleSettings(false);
			}
		}
		else
			UIHelper.enableDisableView((View)findViewById(R.id.scrollView1),false);
	}

	private void enableDisableSampleSettings(boolean enabled)
	{
		UIHelper.enableDisableView((View)findViewById(R.id.samplingSettings_tl),enabled);
	}

	private void showAdminSettings(boolean enabled)
	{
		UIHelper.hideShowView((View)findViewById(R.id.scrollView1),enabled);
	}



	private String getSpecialStatus()
	{
		String op = "";
		op = "Period:" + mSettings.getInt(PreferencesHelper.SPECIALPERIOD, AudioSensConfig.PERIOD)+"\n";
		op += "Duration:" + mSettings.getInt(PreferencesHelper.SPECIALDURATION, AudioSensConfig.DURATION)+"\n";
		op += "Active Time Ranges:";

		String saved = mSettings.getString(PreferencesHelper.TIMERANGE, "[]");
		try 
		{
			JSONArray arr = new JSONArray(saved);
			JSONObject obj;
			for(int i= 0; i<arr.length(); i++)
			{
				obj = arr.getJSONObject(i);
				String start = obj.getString("start");
				String end = obj.getString("end");
				op += "\n" + (i+1) + ". " + start + " to " + end;
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		return op;
	}



	/*
	 * Pops up a message Box
	 */
	private void displayMessage(String title, String message)
	{
		new AlertDialog.Builder(this).setTitle(title).setMessage(message).setNeutralButton("OK", null).show(); 
	}

}
