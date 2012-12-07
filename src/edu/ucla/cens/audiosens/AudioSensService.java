package edu.ucla.cens.audiosens;

import java.util.HashMap;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
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
					loadFromSharedPreferences();
					
					//If it is in continuous mode, Check every few minutes to check if the app is currently running
					//If running, ignore the alarm, else start Recording
					if(continuousMode && isActive())
					{
						Logger.d(LOGTAG,"Service already running in continuous mode");
					}
					else
					{
						Thread t = new Thread()
						{
							public void run()
							{
								startRecording();
							}
						};
						t.start();
					}
				}
				else if(action.equals(AudioSensConfig.AUTOSTART_TAG)) 
				{
					//TODO: check when to start
					Logger.d(LOGTAG,"Boot Alarm Received");
					if(mSettings.getBoolean(PreferencesHelper.ENABLED, false))
					{
						loadFromSharedPreferences();
						long now = SystemClock.elapsedRealtime();
						mAlarmManager.cancel(mScanSender);
						if(mSettings.getBoolean(PreferencesHelper.CONTINUOUSMODE, false))
						{
							mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now, AudioSensConfig.CONTINUOUSMODE_ALARM * 1000, mScanSender);
						}
						else
						{
							mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now, period * 1000, mScanSender);
						}
					}
				}
			}
			else
			{
				loadFromSharedPreferences();
				Logger.i(LOGTAG,"Null Intent received, Setting alarm for"+period);
				long now = SystemClock.elapsedRealtime();
				mAlarmManager.cancel(mScanSender);
				if(mSettings.getBoolean(PreferencesHelper.CONTINUOUSMODE, false))
				{
					mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now, AudioSensConfig.CONTINUOUSMODE_ALARM * 1000, mScanSender);
				}
				else
				{
					mAlarmManager.setRepeating (AlarmManager.ELAPSED_REALTIME_WAKEUP, now, period * 1000, mScanSender);
				}
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

	/**
	 * Starts the actual recording
	 */
	private void startRecording()
	{
		Logger.d(LOGTAG,"StartRecording");
		Logger.d(LOGTAG,"StartRecording:"+continuousMode);

		if(continuousMode)
			setNotification(NotificationLevel.GREEN, "AudioSens Running", "Capturing audio continuously");
		else
			setNotification(NotificationLevel.GREEN, "AudioSens Running", "Capturing audio for "+ duration + "seconds");
		acquireWakeLock();
		mEditor.putBoolean(PreferencesHelper.RECORDSTATUS, true);
		mEditor.commit();

		sendStatusBroadcast(true, null);

		recorderInstance = new AudioSensRecorder(this, duration, continuousMode); 
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


	//load local variables from SharedPreferences
	private void loadFromSharedPreferences()
	{
		period = mSettings.getInt(PreferencesHelper.PERIOD, AudioSensConfig.PERIOD);
		duration = mSettings.getInt(PreferencesHelper.DURATION, AudioSensConfig.DURATION);
		continuousMode = mSettings.getBoolean(PreferencesHelper.CONTINUOUSMODE, false);
	}

}
