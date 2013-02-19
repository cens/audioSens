
package edu.ucla.cens.audiosens.helper;

import java.util.TimeZone;

import android.content.Context;
import android.os.RemoteException;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.probemanager.ProbeBuilder;
import org.ohmage.probemanager.ProbeWriter;

import edu.ucla.cens.audiosens.config.OhmageWriterConfig;

public class OhmageProbeWriter extends ProbeWriter 
{
	static final String LOGTAG = "OhmageProbeWriter";
	ProbeBuilder probe;

	public OhmageProbeWriter(Context context) 
	{
		super(context);
	}

	public void writeFeatures(JSONObject data, long timestamp) 
	{
		try 
		{
			probe = new ProbeBuilder(OhmageWriterConfig.OBSERVER_ID, 
					OhmageWriterConfig.OBSERVER_VERSION);
			probe.setStream(OhmageWriterConfig.STREAM_FEATURES, 
					OhmageWriterConfig.STREAM_FEATURES_VERSION);

			probe.setData(data.toString());
			probe.withTime(timestamp, TimeZone.getDefault().getID());
			probe.withId();
			probe.write(this);
		} 
		catch (RemoteException re) 
		{
			Logger.e(LOGTAG,"Exception: " + re);
		}
	}

	public void writeSensors(JSONObject data, long timestamp) 
	{
		try 
		{
			probe = new ProbeBuilder(OhmageWriterConfig.OBSERVER_ID, 
					OhmageWriterConfig.OBSERVER_VERSION);
			probe.setStream(OhmageWriterConfig.STREAM_SENSORS, 
					OhmageWriterConfig.STREAM_SENSORS_VERSION);

			if(data.has("Location"))
			{
				JSONObject locationJSON;
				try 
				{
					locationJSON = data.getJSONObject("Location");
					probe.withLocation(timestamp, 
							TimeZone.getDefault().getID(),
							locationJSON.getDouble("latitude"),
							locationJSON.getDouble("longitude"),
							(float)locationJSON.getDouble("accuracy"), 
							locationJSON.getString("provider"));
					data.remove("Location");
				}
				catch (JSONException je) 
				{
					Logger.e(LOGTAG,"Exception: " + je);			
				}
			}

			probe.setData(data.toString());
			probe.withTime(timestamp, TimeZone.getDefault().getID());
			probe.withId();
			probe.write(this);
		} 
		catch (RemoteException re) 
		{
			Logger.e(LOGTAG,"Exception: " + re);
		}
	}

	public void writeClassifiers(JSONObject data, long timestamp) 
	{
		try 
		{
			probe = new ProbeBuilder(OhmageWriterConfig.OBSERVER_ID, 
					OhmageWriterConfig.OBSERVER_VERSION);
			probe.setStream(OhmageWriterConfig.STREAM_CLASSIFIERS, 
					OhmageWriterConfig.STREAM_CLASSIFIERS_VERSION);

			probe.setData(data.toString());
			probe.withTime(timestamp, TimeZone.getDefault().getID());
			probe.withId();
			probe.write(this);
		} 
		catch (RemoteException re) 
		{
			Logger.e(LOGTAG,"Exception: " + re);
		}
	}
	
	public void writeEvent(JSONObject data, long timestamp) 
	{
		try 
		{
			probe = new ProbeBuilder(OhmageWriterConfig.OBSERVER_ID, 
					OhmageWriterConfig.OBSERVER_VERSION);
			probe.setStream(OhmageWriterConfig.STREAM_EVENTS, 
					OhmageWriterConfig.STREAM_EVENTS_VERSION);

			probe.setData(data.toString());
			probe.withTime(timestamp, TimeZone.getDefault().getID());
			probe.withId();
			probe.write(this);
		} 
		catch (RemoteException re) 
		{
			Logger.e(LOGTAG,"Exception: " + re);
		}
	}
	
	public void writeSummarizer(JSONObject data, long timestamp) 
	{
		try 
		{
			Logger.e("Writing summarizer");
			probe = new ProbeBuilder(OhmageWriterConfig.OBSERVER_ID, 
					OhmageWriterConfig.OBSERVER_VERSION);
			probe.setStream(OhmageWriterConfig.STREAM_SUMMARIZERS, 
					OhmageWriterConfig.STREAM_SUMMARIZERS_VERSION);

			probe.setData(data.toString());
			probe.withTime(timestamp, TimeZone.getDefault().getID());
			probe.withId();
			probe.write(this);
		} 
		catch (RemoteException re) 
		{
			Logger.e(LOGTAG,"Exception: " + re);
		}
	}

}
