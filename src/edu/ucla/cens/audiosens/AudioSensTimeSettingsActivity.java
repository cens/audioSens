package edu.ucla.cens.audiosens;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TimePicker;

public class AudioSensTimeSettingsActivity extends Activity 
{
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;
	static final int TIME_DIALOG_ID = 999;
	static final String DEFAULT_START = "06:00";
	static final String DEFAULT_END = "07:00";
	
	EditText period_et;
	EditText duration_et;
	
	ArrayList<View> timeList;
	TableLayout tl;
	Button add_b;
	
	CheckBox enabled1_cb;
	TimePicker start1_tp;
	TimePicker end1_tp;
	
	CheckBox enabled2_cb;
	TimePicker start2_tp;
	TimePicker end2_tp;
	
	int hour;
	int minute;
	View currentView =  null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_sens_time_settings);
		setTitle("AudioSens- Choose Time Range");
		
		//Shared Preferences
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);   
		mEditor = mSettings.edit();

		timeList = new ArrayList<View>();
		
		//Initialize UI Elements
		period_et = (EditText)findViewById(R.id.specialperiod_et);
		duration_et = (EditText)findViewById(R.id.specialduration_et);
		tl = (TableLayout)findViewById(R.id.time_tl);
		add_b = (Button)findViewById(R.id.add_b);
		
		//Loads existing time ranges
		loadTimeRows();
		
		period_et.setText(mSettings.getInt(PreferencesHelper.SPECIALPERIOD, AudioSensConfig.PERIOD)+"");
		duration_et.setText(mSettings.getInt(PreferencesHelper.SPECIALDURATION, AudioSensConfig.DURATION)+"");

		//Listener for the Add button
		add_b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				addNewRow();
			}
		});
	}

	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
	    savePreferences();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_audio_sens_time_settings, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
		case R.id.menu_save:
			if(savePreferences())
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
		
		mEditor.putInt(PreferencesHelper.SPECIALPERIOD, period);
		mEditor.putInt(PreferencesHelper.SPECIALDURATION, duration);
		mEditor.putString(PreferencesHelper.TIMERANGE,getTimeRange());
		mEditor.commit();

		return true;
	}
	
	void initView(View view, String start, String end)
	{
		CheckBox enabled_cb = (CheckBox)view.findViewById(R.id.enable_cb);
		Button start_b = (Button)view.findViewById(R.id.start_b);
		Button end_b = (Button)view.findViewById(R.id.end_b);
		
		enabled_cb.setChecked(true);
		start_b.setText(start);
		end_b.setText(end);
		start_b.setOnClickListener(new View.OnClickListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				loadTime(((Button)v).getText().toString());
				currentView = v;
				showDialog(TIME_DIALOG_ID);
			}
		});
		
		end_b.setOnClickListener(new View.OnClickListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				loadTime(((Button)v).getText().toString());
				currentView = v;
				showDialog(TIME_DIALOG_ID);
			}
		});	

	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TIME_DIALOG_ID:
			// set time picker as current time
			return new TimePickerDialog(this, timePickerListener, hour, minute, true);
		}
		return null;
	}
	
	private TimePickerDialog.OnTimeSetListener timePickerListener = 
            new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int selectedHour,int selectedMinute) 
				{
					hour = selectedHour;
					minute = selectedMinute;
					((Button)currentView).setText(getTime());
				}
	};
	
	
	
	/*
	 * Pops up a message Box
	 */
	private void displayMessage(String title, String message)
	{
		new AlertDialog.Builder(this).setTitle(title).setMessage(message).setNeutralButton("OK", null).show(); 
	}
	
	//Get existing time ranges
	private void loadTimeRows()
	{
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
				if(isValid(start,end))
				{
					addNewRow(start, end);
				}
				
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void addNewRow(String start, String end)
	{
		View view = getLayoutInflater().inflate(R.layout.add_time_layout, null,false);
		tl.addView(view);
		timeList.add(view);
		initView(view, start, end);
	}
	
	private void addNewRow()
	{
		addNewRow(DEFAULT_START, DEFAULT_END);
	}
	
	private String getTimeRange()
	{
		JSONArray arr = new JSONArray();
		JSONObject obj;
		for(int i=0; i<timeList.size(); i++)
		{
			View view = timeList.get(i);
			CheckBox enabled_cb = (CheckBox)view.findViewById(R.id.enable_cb);
			Button start_b = (Button)view.findViewById(R.id.start_b);
			Button end_b = (Button)view.findViewById(R.id.end_b);
			if(enabled_cb.isChecked())
			{
				String start = start_b.getText().toString();
				String end = end_b.getText().toString();
				if(isValid(start,end))
				{
					obj = new JSONObject();
					try 
					{
						obj.put("start", start);
						obj.put("end", end);
						arr.put(obj);
					} 
					catch (JSONException e) 
					{
						e.printStackTrace();
					}
					
				}
			}
		}
		return arr.toString();
	}
	
	private boolean isValid(String start, String end)
	{
		int start_hour, start_minute, end_hour, end_minute;
		String arr[] = start.split(":");
		if(arr.length == 2)
		{
			start_hour = Integer.parseInt(arr[0]);
			start_minute = Integer.parseInt(arr[1]);
		}
		else
		{
			return false;
		}
		
		arr = end.split(":");
		if(arr.length == 2)
		{
			end_hour = Integer.parseInt(arr[0]);
			end_minute = Integer.parseInt(arr[1]);
		}
		else
		{
			return false;
		}
		
		if(end_hour > start_hour || ((end_hour == start_hour) && (end_minute > start_minute)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	private void loadTime(String inp)
	{
		String arr[] = inp.split(":");
		if(arr.length == 2)
		{
			hour = Integer.parseInt(arr[0]);
			minute = Integer.parseInt(arr[1]);
		}
	}
	
	private String getTime()
	{
		return pad(hour) + ":" + pad(minute);
	}
	
	private static String pad(int c) 
	{
		if (c >= 10)
		   return String.valueOf(c);
		else
		   return "0" + String.valueOf(c);
	}

}
