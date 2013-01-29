package edu.ucla.cens.audiosens.classifier;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.AudioSensRecorder;
import edu.ucla.cens.audiosens.helper.JSONHelper;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.processors.BaseProcessor;

public abstract class BaseClassifier 
{
	private static final String LOGTAG = "BaseClassifier";

	AudioSensRecorder obj;
	ArrayList<Double> results;
	String name;
	
	public void initialize(AudioSensRecorder obj)
	{
		this.obj = obj;
		results = new ArrayList<Double>();
	}
	
	public String getName()
	{
		return name;
	}
	
	public void addResult(double inp)
	{
		results.add(inp);
	}
	
	public void clearResults()
	{
		results.clear();
	}
	
	@SuppressWarnings("rawtypes")
	public ArrayList getResults()
	{
		return results;
	}
	
	public JSONObject getJSONResultsObject(long frameNo)
	{
		try 
		{
			return JSONHelper.buildClassifierJson(this, frameNo);
		} 
		catch (JSONException je) 
		{
			Logger.e(LOGTAG,"Exception in creating JSON object:"+je);
			return null;
		}	
	}

	public abstract void classify(HashMap<String, BaseProcessor> resultMap);
	public abstract void complete();

}
