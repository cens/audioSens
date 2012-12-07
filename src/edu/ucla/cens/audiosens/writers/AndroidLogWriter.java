package edu.ucla.cens.audiosens.writers;


import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.helper.JSONHelper;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.processors.BaseProcessor;
import edu.ucla.cens.audiosens.sensors.BaseSensor;

public class AndroidLogWriter extends BaseWriter {
	
	private static final String LOGTAG = "AndroidLogWriter";
	private static final String DATATAG = "AudioSensData";
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(BaseProcessor processor, long frameNo) 
	{
		Logger.d(LOGTAG,"Writing in AndroidLogWriter");
		writePerFeature(processor, frameNo);
	}
	
	@Override
	public void writeSensors(HashMap<String, BaseSensor> sensorMap, long frameNo) 
	{
		JSONObject tempJson = JSONHelper.buildSensorJson(sensorMap, frameNo);
		if(tempJson != null)
			Logger.i(LOGTAG, tempJson.toString());
	}
	
	private void writePerProcessor(BaseProcessor processor, long frameNo)
	{
		JSONObject tempJson = processor.getJSONResults(frameNo);
		if (tempJson != null)
			Logger.i(LOGTAG, tempJson.toString());
	}
	
	private void writePerFeature(BaseProcessor processor, long frameNo)
	{
		for(JSONObject jsonObject : processor.getJSONResultsArrayList(frameNo))
		{
			if (jsonObject != null)
				Logger.i(LOGTAG, jsonObject.toString());
		}
	}



}
