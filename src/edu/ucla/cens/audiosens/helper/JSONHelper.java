package edu.ucla.cens.audiosens.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.classifier.BaseClassifier;
import edu.ucla.cens.audiosens.sensors.BaseSensor;

public class JSONHelper {

	public static final String LOGTAG = "JSONHelper";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object build(Object inp) throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		if(inp instanceof ArrayList)
		{
			JSONArray jsonArray = new JSONArray((ArrayList)inp);
			jsonObject.put("data", jsonArray);
		}
		else if(inp instanceof HashMap)
		{
			for(Entry<String,Object> entry : ((HashMap<String,Object>)inp).entrySet())
			{
				jsonObject.put(entry.getKey(), build(entry.getValue()));
			}
		}
		else if(inp instanceof Object)
		{
			return inp;
		}
		return jsonObject;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static JSONArray buildFeaturesAsJSONArray(Object inp, Object summaries, long frameNo) throws JSONException
	{
		JSONArray op = new JSONArray();
		JSONObject jsonObject;
		if(inp instanceof HashMap)
		{
			for(Entry<String,Object> entry : ((HashMap<String,Object>)inp).entrySet())
			{
				jsonObject = (JSONObject)build(entry.getValue());
				jsonObject.put("name", entry.getKey());
				HashMap<String, HashMap> temp = (HashMap<String, HashMap>)summaries;
				if(temp.containsKey(entry.getKey()))
				{
					jsonObject.put("summary", build(temp.get(entry.getKey())));
				}
				op.put(jsonObject);
			}
		}
		return op;
	}
	
	public static JSONObject buildFeaturesAsJSONObject(Object inp, Object summaries, String featureName, long frameNo) throws JSONException
	{
		JSONObject op = new JSONObject();
		op.put("featureArray", buildFeaturesAsJSONArray(inp, summaries, frameNo));
		op.put("frameNo", frameNo);
		op.put("name", featureName);
		return op;
	}
	
	public static JSONObject buildClassifierJson(BaseClassifier classifier, long frameNo) throws JSONException
	{
		JSONObject jsonObject = (JSONObject)build(classifier.getResults());
		jsonObject.put("classifier", classifier.getName());
		jsonObject.put("frameNo", frameNo);
		return jsonObject;
	}
	
	public static JSONObject buildSensorJson(HashMap<String, BaseSensor> sensorMap, long frameNo)
	{
		try 
		{
			JSONObject  jsonObject = new JSONObject();
			jsonObject.put("frameNo", frameNo);
			for(Entry<String, BaseSensor> entry : sensorMap.entrySet())
			{
				try 
				{
					jsonObject.put(entry.getKey(), entry.getValue().getJsonResult());
				} 
				catch (JSONException e) 
				{
					Logger.e(LOGTAG,"Exception with Writing Sensor " + entry.getKey());
				}
			}
			return jsonObject;
		} 
		catch (JSONException je) 
		{
			Logger.e(LOGTAG,"Exception with buildSensorJson");
			return null;
		}

	}

}
