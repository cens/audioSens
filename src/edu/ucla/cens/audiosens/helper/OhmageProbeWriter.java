
package edu.ucla.cens.audiosens.helper;

import java.util.TimeZone;

import android.content.Context;
import android.os.RemoteException;
import org.json.JSONObject;
import org.ohmage.probemanager.ProbeBuilder;
import org.ohmage.probemanager.ProbeWriter;

import edu.ucla.cens.audiosens.config.OhmageWriterConfig;

public class OhmageProbeWriter extends ProbeWriter 
{
	static final String LOGTAG = "OhmageProbeWriter";
	
    public OhmageProbeWriter(Context context) 
    {
        super(context);
    }

    public void writeFeatures(JSONObject data, long timestamp) 
    {
        try 
        {
            ProbeBuilder probe = new ProbeBuilder(OhmageWriterConfig.OBSERVER_ID, 
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
            ProbeBuilder probe = new ProbeBuilder(OhmageWriterConfig.OBSERVER_ID, 
            										OhmageWriterConfig.OBSERVER_VERSION);
            probe.setStream(OhmageWriterConfig.STREAM_SENSORS, 
            				OhmageWriterConfig.STREAM_SENSORS_VERSION);

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
            ProbeBuilder probe = new ProbeBuilder(OhmageWriterConfig.OBSERVER_ID, 
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
    
}
