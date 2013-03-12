package edu.ucla.cens.audiosens;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
	public BootBroadcastReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) 
		{
			//If autostart is configured in the Config File
			if(AudioSensConfig.AUTOSTART)
			{
				Intent serviceIntent = new Intent(context, AudioSensService.class);
				serviceIntent.setAction(AudioSensConfig.AUTOSTART_TAG);
				context.startService(serviceIntent);
			}
		} 
	}
}
