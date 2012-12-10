package edu.ucla.cens.audiosens;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

public class AudioSensActivity extends Activity {

	public static final String LOGTAG = "AudioSensActivity";

	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;

	private boolean recordStatus;
	private boolean appStatus;
	private TextView recordStatus_tv;
	private TextView appStatus_tv;
	private TableRow speechInference_tr;
	private TextView speechInference_tv;
	
	private boolean broadcastRegistered;
	private LocalBroadcastManager localBoradcastManager;
	private IntentFilter iFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_sens);

		//Shared Preferences
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);   
		mEditor = mSettings.edit();

		//Getting Version Number, If it is a new version number, clear all Settings
		try
		{
			String versionNo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			String versionNo_stored = mSettings.getString(PreferencesHelper.VERSION, "0.0");
			if(!versionNo.equals(versionNo_stored))
			{
				mEditor.clear();
				mEditor.putString(PreferencesHelper.VERSION, versionNo);
				mEditor.commit();
			}
		}
		catch (NameNotFoundException e)
		{
			Logger.e("Cannot get the version number");
		}

		iFilter = new IntentFilter();
		iFilter.addAction(AudioSensConfig.STATUSRECEIVERTAG);
		iFilter.addAction(AudioSensConfig.INFERENCERECEIVERTAG);
		localBoradcastManager = LocalBroadcastManager.getInstance(this);
		registerBroadcast(true);
		
		appStatus_tv = (TextView)findViewById(R.id.appStatus_tv);
		recordStatus_tv = (TextView)findViewById(R.id.recordingStatus_tv);
		speechInference_tr = (TableRow)findViewById(R.id.speechPercent_tr);
		speechInference_tv = (TextView)findViewById(R.id.speechPercent_tv);
		
		updateAppStatus();
		updateRecordStatus();
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		registerBroadcast(false);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		registerBroadcast(true);
		updateAppStatus();
		updateRecordStatus();
	}


	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_audio_sens, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
		case R.id.menu_settings:
			Intent settings_Intent = new Intent(AudioSensActivity.this, AudioSensSettingsActivity.class);
			startActivity(settings_Intent);
			return true;
		case R.id.menu_exit:
			onBackPressed();
			return true;
		}
		return false;
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if(action.equals(AudioSensConfig.STATUSRECEIVERTAG))
			{
				recordStatus = intent.getBooleanExtra(AudioSensConfig.STATUSRECEIVER_RECORD, false);
				//String message = intent.getStringExtra(AudioSensConfig.STATUSRECEIVER_MSG);
				if(recordStatus)
					recordStatus_tv.setText("On");
				else
				{
					recordStatus_tv.setText("Off");
					setSpeechVisibility(false);
				}
				updateAppStatus();
			}
			else if(action.equals(AudioSensConfig.INFERENCERECEIVERTAG))
			{
				setSpeechVisibility(true);
				double percent = intent.getDoubleExtra(AudioSensConfig.INFERENCERECEIVER_PERCENT, -1);
				updateSpeechPercent(percent);
			}
		}
	};

	private void registerBroadcast(boolean register)
	{
		if(!broadcastRegistered && register)
		{
			localBoradcastManager.registerReceiver(broadcastReceiver, iFilter);
			broadcastRegistered = true;
		}
		else if(broadcastRegistered && !register)
		{
			localBoradcastManager.unregisterReceiver(broadcastReceiver);
			broadcastRegistered = false;
		}
	}

	private void updateAppStatus()
	{
		appStatus = mSettings.getBoolean(PreferencesHelper.ENABLED, false);
		if(appStatus)
			appStatus_tv.setText("Enabled");
		else
			appStatus_tv.setText("Disabled");
	}

	private void updateRecordStatus()
	{
		recordStatus = mSettings.getBoolean(PreferencesHelper.RECORDSTATUS, false);
		if(recordStatus)
			recordStatus_tv.setText("On");
		else
		{
			recordStatus_tv.setText("Off");
			setSpeechVisibility(false);
		}
	}

	private void setSpeechVisibility(boolean visible)
	{
		if(visible)
			speechInference_tr.setVisibility(View.VISIBLE);
		else
			speechInference_tr.setVisibility(View.GONE);
	}

	private void updateSpeechPercent(double percent)
	{
		speechInference_tv.setText((int)percent + "");
	}
}
