package edu.ucla.cens.audiosens.summarizers;

import java.util.Calendar;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.sqlite.DatabaseHelper;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SummarizerService extends Service {
	
	private static String LOGTAG = "SummarizerService";
	private DatabaseHelper db;
	
	public SummarizerService() 
	{
		db = new DatabaseHelper(this);
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
					Logger.e("onStart");
					Thread t = new Thread()
					{
						public void run()
						{
							hourlySummarize();
						}
					};
					t.start();
					try 
					{
						t.join();
					} 
					catch (InterruptedException e) 
					{
						Logger.e(LOGTAG,"Exception: "+e);
					}
				}
			}
		}

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}
	
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private void hourlySummarize()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, -1);
		
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long start = calendar.getTime().getTime();
		
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		long end = calendar.getTime().getTime();
		
		
	}
}
