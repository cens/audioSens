package edu.ucla.cens.audiosens.writers;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.AudioSensService;
import edu.ucla.cens.audiosens.classifier.BaseClassifier;
import edu.ucla.cens.audiosens.config.OhmageWriterConfig;
import edu.ucla.cens.audiosens.helper.JSONHelper;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.OhmageProbeWriter;
import edu.ucla.cens.audiosens.processors.BaseProcessor;
import edu.ucla.cens.audiosens.sensors.BaseSensor;

public class OhmageWriter extends BaseWriter {

	private final static String LOGTAG = "OhmageWriter";
	OhmageProbeWriter probeWriter;
	String versionNo;
	JSONObject jsonObject;

	@Override
	public void initialize(AudioSensService service) 
	{
		probeWriter = new OhmageProbeWriter(service);
		isConnected = probeWriter.connect();
		versionNo = service.getVersionNo();

		writesFeatures = OhmageWriterConfig.WRITESFEATURES;
		writesClassifier = OhmageWriterConfig.WRITESCLASSIFIERS;
		writesSensors = OhmageWriterConfig.WRITESSENSORS;
	}

	@Override
	public void destroy() 
	{
		try
		{
			probeWriter.close();
		}
		catch(Exception e)
		{
			Logger.e(LOGTAG,"Exception closing Ohmage Writer: "+e);
		}
		isConnected = false;
	}

	@Override
	public void write(BaseProcessor processor, long frameNo) 
	{
		jsonObject = processor.getJSONResultsObject(frameNo);
		if (jsonObject != null)
		{
			try 
			{
				jsonObject.put("version", versionNo);
			} 
			catch (JSONException e) 
			{
				Logger.e(LOGTAG, "Exception :"+e);
			}
			probeWriter.writeFeatures(jsonObject, frameNo);
		}
	}

	@Override
	public void writeSensors(HashMap<String, BaseSensor> sensorMap, long frameNo) 
	{
		jsonObject = JSONHelper.buildSensorJson(sensorMap, frameNo);
		if(jsonObject != null)
		{
			try 
			{
				jsonObject.put("version", versionNo);
			} 
			catch (JSONException e) 
			{
				Logger.e(LOGTAG, "Exception :"+e);
			}
			probeWriter.writeSensors(jsonObject, frameNo);
		}
	}

	@Override
	public void writeClassifier(BaseClassifier classifier, long frameNo) 
	{
		jsonObject = classifier.getJSONResultsObject(frameNo);
		if (jsonObject != null)
		{
			try 
			{
				jsonObject.put("version", versionNo);
			} 
			catch (JSONException e) 
			{
				Logger.e(LOGTAG, "Exception :"+e);
			}
			probeWriter.writeClassifiers(jsonObject, frameNo);
		}
	}

}
