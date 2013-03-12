package edu.ucla.cens.audiosens.helper;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

public class EventHelper 
{
	private static String LOGTAG = "EventHelper";
	static OhmageProbeWriter probeWriter;

	public static void destroy()
	{
		if(probeWriter!=null)
		{
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
	
	//Logs the Phone Booting Up event
	public static void logBootUp(Context context, String versioNo, Boolean appEnabled)
	{
		JSONObject json = initObject(versioNo, "PhoneStatus", "BootingUp");
		try 
		{
			JSONObject summary = new JSONObject();
			summary.put("appEnabled", appEnabled);
			json.put("summary", summary);
		} 
		catch (JSONException e) 
		{
			Logger.e(LOGTAG,"Exception: "+e);
		}
		writeObject(context, json);
	}

	//Logs the Application Enabled event when the normal mode is on
	public static void logNormalAppStatus(Context context, String versioNo, int period, int duration)
	{
		JSONObject json = initObject(versioNo, "AppStatus", "Enabled");
		try 
		{
			JSONObject summary = new JSONObject();
			summary.put("mode", "normalMode");
			summary.put("period", period);
			summary.put("duration", duration);
			json.put("summary", summary);
		} 
		catch (JSONException e) 
		{
			Logger.e(LOGTAG,"Exception: "+e);
		}
		writeObject(context, json);
	}

	//Logs the Application Enabled event when the continuous mode is on
	public static void logContinuousAppStatus(Context context, String versioNo)
	{
		JSONObject json = initObject(versioNo, "AppStatus", "Enabled");
		try 
		{
			JSONObject summary = new JSONObject();
			summary.put("mode", "continuousMode");
			json.put("summary", summary);
		} 
		catch (JSONException e) 
		{
			Logger.e(LOGTAG,"Exception: "+e);
		}
		writeObject(context, json);
	}

	//Logs when the application is disabled
	public static void logDisableAppStatus(Context context, String versioNo)
	{
		JSONObject json = initObject(versioNo, "AppStatus", "Disabled");
		writeObject(context, json);
	}

	//Logs the Settings when the app is started. Is also called when the app fails due to low memory and is auto-restarted
	public static void logAppStart(Context context, String versioNo, JSONObject summary)
	{
		JSONObject json = initObject(versioNo, "AppStatus", "Starting");
		try 
		{
			json.put("summary", summary);
		} 
		catch (JSONException e) 
		{
			Logger.e(LOGTAG,"Exception: "+e);
		}
		writeObject(context, json);
	}

	//Writes to Ohmage
	private static void writeObject(Context context, JSONObject json)
	{
		long timestamp;
		try
		{
			timestamp = json.getLong("frameNo");
		}
		catch(JSONException e)
		{
			timestamp = System.currentTimeMillis();
		}

		if(probeWriter==null)
			probeWriter = new OhmageProbeWriter(context);
		if(probeWriter.connect())
		{
			probeWriter.writeEvent(json, timestamp);
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
	

	@SuppressWarnings("unused")
	private static JSONObject initObject(String versionNo, String event)
	{
		return initObject(versionNo, event, null);
	}

	private static JSONObject initObject(String versionNo, String event, String subevent)
	{
		JSONObject json = new JSONObject();
		try 
		{
			json.put("version", versionNo);
			json.put("frameNo", System.currentTimeMillis());
			json.put("event", event);
			if(subevent != null)
			{
				json.put("subevent", subevent);
			}
		} 
		catch (JSONException e) 
		{
			Logger.e(LOGTAG,"Exception: "+e);
		}
		return json;
	}
}
