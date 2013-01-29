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
	public static JSONObject build(Object inp) throws JSONException
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
		return jsonObject;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<JSONObject> buildAsArrayList(Object inp, long frameNo) throws JSONException
	{
		ArrayList<JSONObject> op = new ArrayList<JSONObject>();
		JSONObject jsonObject;
		if(inp instanceof HashMap)
		{
			for(Entry<String,Object> entry : ((HashMap<String,Object>)inp).entrySet())
			{
				jsonObject = build(entry.getValue());
				jsonObject.put("feature", entry.getKey());
				jsonObject.put("frameNo", frameNo);
				op.add(jsonObject);
			}
		}
		return op;
	}
	
	public static JSONObject buildClassifierJson(BaseClassifier classifier, long frameNo) throws JSONException
	{
		JSONObject jsonObject = build(classifier.getResults());
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
