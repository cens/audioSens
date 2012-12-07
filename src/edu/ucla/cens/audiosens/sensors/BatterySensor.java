package edu.ucla.cens.audiosens.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import edu.ucla.cens.audiosens.AudioSensService;

public class BatterySensor extends BaseSensor {

	AudioSensService obj;
	IntentFilter ifilter;
	Intent batteryStatus;

	boolean isCharging;
	boolean usbCharge;
	boolean acCharge;
	double batteryPercent;


	@Override
	public void init(AudioSensService obj) 
	{
		this.obj = obj;
		ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	}

	@Override
	public void destroy() {
	}

	void updateResults()
	{
		batteryStatus = obj.getBaseContext().registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		batteryPercent= level * 100 / scale;

		// Are we charging / charged?
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
				status == BatteryManager.BATTERY_STATUS_FULL;

		// How are we charging?
		int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
	}

	@Override
	public JSONObject getJsonResult() throws JSONException 
	{
		updateResults();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("percent", batteryPercent);
		jsonObject.put("Charging", isCharging);
		jsonObject.put("USB Charging", usbCharge);
		jsonObject.put("AC Charging", acCharge);
		return jsonObject;
	}

}
