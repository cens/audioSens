package edu.ucla.cens.audiosens.summarizers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.OhmageProbeWriter;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;
import edu.ucla.cens.audiosens.sqlite.DatabaseHelper;
import edu.ucla.cens.audiosens.sqlite.SpeechInferenceObject;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;

public class SummarizerService extends Service {

	private PowerManager.WakeLock wakeLock;
	
	private static String LOGTAG = "SummarizerService";
	private String TAG = "audiosens_summarizer";
	private DatabaseHelper db;
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;
	private Calendar calendar;
	private Calendar calendar_temp;
	OhmageProbeWriter probeWriter;

	@Override
	public void onCreate() 
	{
		super.onCreate();
		probeWriter = new OhmageProbeWriter(getApplicationContext());
		db = new DatabaseHelper(this);
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mEditor = mSettings.edit();
		calendar = Calendar.getInstance();
		
		//WakeLocks
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wakeLock.setReferenceCounted(false);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)	
	{
		if (intent != null) 
		{ 
			String action = intent.getAction();
			if (action != null) 
			{ 
				if(action.equals(AudioSensConfig.SUMMARIZER_TAG)) 
				{
					Logger.w("onStart");
					hourlySummarize();
				}
			}
		}

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		Logger.e("Destroy");
		if(probeWriter!=null)
			probeWriter.close();
	}


	@Override
	public IBinder onBind(Intent intent) 
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void hourlySummarize()
	{
		Logger.e("inhourlySummarize");
		acquireWakeLock();
		long seed = mSettings.getLong(PreferencesHelper.PREVSEED, 0);
		
		if(seed == 0)
			seed = System.currentTimeMillis();
		else
			seed = getNextSeed(seed);

		while(isSeedValid(seed))
		{
			hourlySummarize(seed);
			seed = getNextSeed(seed);
		}
		//TODO: Remove
		mEditor.putLong("temp1", System.currentTimeMillis());
		mEditor.commit();
		releaseWakeLock();
	}

	private void hourlySummarize(long seed)
	{
		//TODO: remove
		mEditor.putLong("temp2", System.currentTimeMillis());
		mEditor.commit();
		
		calendar.setTimeInMillis(seed);
		calendar.add(Calendar.HOUR_OF_DAY, -1);

		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long start = calendar.getTime().getTime();
		Logger.w("start:"+calendar.getTime());


		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		long end = calendar.getTime().getTime();
		Logger.w("end:"+calendar.getTime());

		List<SpeechInferenceObject> arr = db.getIntervalInferences(start, end);

		//List<SpeechInferenceObject> arr = db.getAllInferences();
		ArrayList<Integer> inferenceArr_op = new ArrayList<Integer>();
		ArrayList<Integer> countArr_op = new ArrayList<Integer>();
		
		int inferenceArr[] = new int[60];
		int countArr[] = new int[60];
		for(int i = 0; i<60; i++)
		{
			inferenceArr[i] = -1;
			countArr[i] = 0;
		}
		Logger.w("arr:"+arr);


		int minute;
		for(int i=0; i<arr.size(); i++)
		{
			calendar.setTimeInMillis(arr.get(i).getId());
			minute = calendar.get(Calendar.MINUTE);
			countArr[minute]++;
			inferenceArr[minute] = Math.max(arr.get(i).getInference(), inferenceArr[minute]);
		}

		int count_silent=0, count_speech=0, count_missing=0, count_total =0;
		for(int i=0; i<60; i++)
		{
			if(inferenceArr[i]==1)
				count_speech++;
			else if(inferenceArr[i]==0)
				count_silent++;
			else
				count_missing++;

			count_total += countArr[i];
			inferenceArr_op.add(i, inferenceArr[i]);
			countArr_op.add(i, countArr[i]);
		}
		
		JSONObject json = new JSONObject();
		try 
		{
			json.put("version", getVersionNo());
			json.put("frameNo", start);
			json.put("end", end);
			json.put("summarizer", "hourlySummarizer");
			
			JSONObject summary = new JSONObject();
			summary.put("count_silent", count_silent);
			summary.put("count_speech", count_speech);
			summary.put("count_missing", count_missing);
			summary.put("count_total", count_total);
			json.put("summary", summary);

			JSONObject data = new JSONObject();
			JSONArray jsonArr = new JSONArray(inferenceArr_op);
			data.put("inferenceArr", jsonArr);
			jsonArr = new JSONArray(countArr_op);
			data.put("countArr", jsonArr);
			json.put("data", data);
		} 
		catch (JSONException e) 
		{
			Logger.e(LOGTAG,"Exception: "+e);
		}

		Logger.e("json:"+json.toString());
		Logger.e(""+System.currentTimeMillis());
		writeObject(json, start);
		mEditor.putLong(PreferencesHelper.PREVSEED, seed);
		mEditor.commit();
	}
	
	public void writeObject(JSONObject json, long timestamp)
	{
		probeWriter = new OhmageProbeWriter(this);
		if(probeWriter.connect())
		{
			probeWriter.writeSummarizer(json, timestamp);
			try
			{
				probeWriter.close();
			}
			catch(Exception e)
			{
				Logger.e(LOGTAG,"Exception closing Ohmage Writer: "+e);
			}
		}
	}

	private boolean isSeedValid(long seed)
	{
		calendar.setTimeInMillis(seed);
		calendar_temp = Calendar.getInstance();
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar_temp.set(Calendar.MINUTE, 0);
		calendar_temp.set(Calendar.SECOND, 0);
		calendar_temp.set(Calendar.MILLISECOND, 0);

		Logger.e("Current:"+calendar_temp.getTime());
		Logger.e("Current checker:"+calendar.getTime());
		
		if(calendar_temp.after(calendar) || calendar_temp.equals(calendar))
			return true;
		return false;

	}
	
	private void acquireWakeLock()
	{
		if(!wakeLock.isHeld())
			wakeLock.acquire();
	}

	private void releaseWakeLock()
	{
		if(wakeLock.isHeld())
			wakeLock.release();
	}

	private long getNextSeed(long seed)
	{
		calendar.setTimeInMillis(seed);
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		return calendar.getTime().getTime();
	}

	public String getVersionNo() 
	{
		return mSettings.getString(PreferencesHelper.VERSION, "0.0");
	}
}
