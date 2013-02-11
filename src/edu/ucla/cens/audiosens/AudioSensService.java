package edu.ucla.cens.audiosens;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.GeneralHelper;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;
import edu.ucla.cens.audiosens.sensors.BaseSensor;
import edu.ucla.cens.audiosens.sensors.SensorFactory;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

public class AudioSensService extends Service {
	private static final String LOGTAG = "AudioSensService";

	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;

	private PowerManager.WakeLock wakeLock;
	private AlarmManager mAlarmManager;
	private PendingIntent mScanSender;
	private NotificationManager notificationManager;
	private Notification notification;
	private PendingIntent notificationIntent;
	private enum NotificationLevel {GREEN,YELLOW,RED};

	AudioSensRecorder recorderInstance;
	Thread recordThread;

	private String TAG = "audiosens";
	private static final String ALARM_TAG = "audiosens_alarm";
	private int duration;
	private int period;
	private boolean continuousMode;

	//HashMap for Sensors
	HashMap<String, BaseSensor> sensorMap;


	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() 
	{
		super.onCreate();
		Logger.d(LOGTAG,"Service OnCreate:");

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mEditor = mSettings.edit();

		//WakeLocks
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wakeLock.setReferenceCounted(false);

		//AlarmManager
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent scanAlarmIntent = new Intent(AudioSensService.this,AudioSensService.class);
		scanAlarmIntent.setAction(ALARM_TAG);
		mScanSender = PendingIntent.getService(AudioSensService.this,0, scanAlarmIntent, 0);

		//Notifications
		notificationIntent = PendingIntent.getActivity(this, 0, new Intent(this, AudioSensActivity.class), 0);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		//Sensors
		sensorMap = new HashMap<String, BaseSensor>();
		createSensors();
		initSensors();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)	
	{
		Logger.i(LOGTAG,"In Start Command");
		if (intent != null) 
		{ 
			String action = intent.getAction();
			if (action != null) 
			{ 
				if(action.equals(ALARM_TAG)) 
				{
					Logger.d(LOGTAG,"Alarm Received");
					schedule(false);
					//If it is in continuous mode, Check every few minutes to check if the app is currently running
					//If running, ignore the alarm, else start Recording
					if(continuousMode && isActive())
					{
						Logger.w(LOGTAG,"Service already running in continuous mode");
					}
					else
					{
						Thread t = new Thread()
						{
							public void run()
							{
								long startTime = System.currentTimeMillis();
								startRecording(startTime);
							}
						};
						t.start();
					}
				}
				else if(action.equals(AudioSensConfig.AUTOSTART_TAG)) 
				{
					Logger.d(LOGTAG,"Boot Alarm Received");
					if(mSettings.getBoolean(PreferencesHelper.ENABLED, false))
					{
						schedule(true);
					}
				}
			}
			else
			{
				schedule(true);
			}
		}

		//TODO : Verify
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() 
	{
		Logger.d(LOGTAG,"Service Destroyed");

		if(!mSettings.getBoolean(PreferencesHelper.ENABLED, false))
		{
			forceStopRecording();
		}
		else
		{
			mEditor.putBoolean(PreferencesHelper.ENABLED, false);
			mEditor.commit();
		}

		mAlarmManager.cancel(mScanSender);
		cancelAllNotifications();
		destroySensors();
	}


	/*
	 * Schedules the alarms
	 */
	private void schedule(boolean firstTime)
	{
		long now = SystemClock.elapsedRealtime();
		if(mSettings.getBoolean(PreferencesHelper.CONTINUOUSMODE, false))
		{
			if(firstTime)
			{
				mAlarmManager.cancel(mScanSender);
				loadFromSharedPreferences();
				mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now, AudioSensConfig.CONTINUOUSMODE_ALARM * 1000, mScanSender);
			}
		}
		else
		{
			if(firstTime || !isSameMode())
			{
				mAlarmManager.cancel(mScanSender);
				loadFromSharedPreferences();
				Logger.i(LOGTAG, "Setting Schedule to " + duration + "/" + period);
				
				long startTimeMillis;
				if(firstTime)
					startTimeMillis = now;
				else
					startTimeMillis = now + period * 1000;
				
				mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, startTimeMillis, period * 1000, mScanSender);
			}
		}
	}


	/**
	 * Starts the actual recording
	 */
	private synchronized void startRecording(long startTime)
	{
		Logger.w(LOGTAG,"StartRecording");

		if(continuousMode)
			setNotification(NotificationLevel.GREEN, "AudioSens Running", "Capturing audio continuously");
		else
			setNotification(NotificationLevel.GREEN, "AudioSens Running", "Capturing audio for "+ duration + "seconds");
		acquireWakeLock();
		mEditor.putBoolean(PreferencesHelper.RECORDSTATUS, true);
		mEditor.commit();

		sendStatusBroadcast(true, null);

		recorderInstance = new AudioSensRecorder(this, duration, continuousMode, startTime); 
		recordThread =new Thread(recorderInstance); 
		recordThread.start();
		recorderInstance.setRecording(true);

		cleanup();
	}

	private void cleanup()
	{
		Logger.d(LOGTAG,"Recording Cleanup");

		if(recordThread != null)
		{
			try 
			{
				recordThread.join();
			} 
			catch (InterruptedException e) 
			{
				Logger.e(LOGTAG,"Interrupted in recordThread join");
			}
		}

		Logger.d(LOGTAG,"In CleanUp: Record Thread Joined");

		setNotification(NotificationLevel.RED, "AudioSens Idle", "Currently not recording");

		mEditor.putBoolean(PreferencesHelper.RECORDSTATUS, false);
		mEditor.commit();

		sendStatusBroadcast(false,null);
		releaseWakeLock();
	}

	private boolean isActive()
	{
		return mSettings.getBoolean(PreferencesHelper.RECORDSTATUS, false);
	}

	/*
	 * Called from the recorder to communicate that Recording has stopped, but data processing is pending
	 */
	public void communicateOnlyProcessingState()
	{
		setNotification(NotificationLevel.YELLOW, "AudioSens Running", "Data Processing only Mode");
	}

	private void forceStopRecording()
	{
		Logger.d(LOGTAG,"Service Force Stopped");

		if(recorderInstance != null)
			recorderInstance.setRecording(false);
		cleanup();
	}

	private void createSensors()
	{
		for(String sensorName : AudioSensConfig.SENSORS)
		{
			if(!sensorMap.containsKey(sensorName))
			{
				BaseSensor sensor = SensorFactory.build(sensorName);

				if(sensor != null)
				{
					sensorMap.put(sensorName, sensor);
				}
				else
				{
					Logger.e(LOGTAG, "Cannot create sensor for " + sensorName);
				}
			}
		}
	}

	private void initSensors()
	{
		for(BaseSensor sensor : sensorMap.values())
		{
			sensor.init(this);
		}
	}

	private void destroySensors()
	{
		for(BaseSensor sensor : sensorMap.values())
		{
			sensor.destroy();
		}
	}

	private void setNotification(NotificationLevel notificationLevel, String title, String text)
	{
		NotificationCompat.Builder notificationBuilder= new NotificationCompat.Builder(this)
		.setContentIntent(notificationIntent);

		switch(notificationLevel)
		{
		case RED:
			notificationBuilder.setSmallIcon(R.drawable.notif_icon_red);
			break;
		case GREEN:
			notificationBuilder.setSmallIcon(R.drawable.notif_icon_green);
			break;
		case YELLOW:
			notificationBuilder.setSmallIcon(R.drawable.notif_icon_yellow);
			break;
		}

		if(title!=null)
		{
			notificationBuilder.setContentTitle(title);
		}

		if(text!=null)
		{
			notificationBuilder.setContentText(text);
		}

		notification = notificationBuilder.build();
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notificationManager.notify(TAG, 1, notification);
	}

	private void cancelAllNotifications()
	{
		notificationManager.cancelAll();
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

	private void sendStatusBroadcast(boolean recordStatus, String message)
	{
		Intent intent = new Intent(AudioSensConfig.STATUSRECEIVERTAG);
		intent.putExtra(AudioSensConfig.STATUSRECEIVER_RECORD, recordStatus);
		intent.putExtra(AudioSensConfig.STATUSRECEIVER_MSG, message);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	public void sendSpeechInferenceBroadcast(double percent)
	{
		Intent intent = new Intent(AudioSensConfig.INFERENCERECEIVERTAG);
		intent.putExtra(AudioSensConfig.INFERENCERECEIVER_PERCENT, percent);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}


	//load local variables from SharedPreferences
	private void loadFromSharedPreferences()
	{
		if(isSpecialMode())
		{
			period = mSettings.getInt(PreferencesHelper.SPECIALPERIOD, AudioSensConfig.PERIOD);
			duration = mSettings.getInt(PreferencesHelper.SPECIALDURATION, AudioSensConfig.DURATION);
			mEditor.putBoolean(PreferencesHelper.CURRENTSPECIALMODE, true);
		}
		else
		{
			period = mSettings.getInt(PreferencesHelper.PERIOD, AudioSensConfig.PERIOD);
			duration = mSettings.getInt(PreferencesHelper.DURATION, AudioSensConfig.DURATION);
			mEditor.putBoolean(PreferencesHelper.CURRENTSPECIALMODE, false);
		}

		mEditor.commit();
		continuousMode = mSettings.getBoolean(PreferencesHelper.CONTINUOUSMODE, false);

	}


	private boolean isSpecialMode()
	{
		if(mSettings.getBoolean(PreferencesHelper.SPECIALMODE, false))
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
					if(isNowBetween(start,end))
						return true;
				}
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	private boolean isSameMode()
	{
		return !(isSpecialMode() ^  mSettings.getBoolean(PreferencesHelper.CURRENTSPECIALMODE, false));
	}

	public boolean isNowBetween(String start, String end)
	{
		final Date now = new Date();
	    return now.after(GeneralHelper.dateFromHourMin(start)) && now.before(GeneralHelper.dateFromHourMin(end));
	}


	public String getVersionNo() 
	{
		return mSettings.getString(PreferencesHelper.VERSION, "0.0");
	}

}
