package edu.ucla.cens.audiosens.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.helper.JSONHelper;
import edu.ucla.cens.audiosens.helper.Logger;

public abstract class BaseProcessor 
{
	private static final String LOGTAG = "BaseProcessor";
	
	@SuppressWarnings("rawtypes")
	protected HashMap<String,ArrayList> results;
	private String[] dependencies;
	public int framesPending;
	public HashMap<String,Integer> framesPendingMap;
	protected String featureName;
	
	@SuppressWarnings("rawtypes")
	public BaseProcessor()
	{
		setName();
		results = new HashMap<String,ArrayList>();
		framesPendingMap = new HashMap<String, Integer>();
		framesPending = 0;
		initializeResults();
	}
	
	public abstract void setName();
	
	public void setName(String featureName)
	{
		this.featureName = featureName; 
	}
	
	public String getName()
	{
		return featureName; 
	}
	
	public abstract void process(Object data, HashMap<String,String> options);
	
	public void initializeResults()
	{
		ArrayList<Double> arr = new ArrayList<Double>();
		results.put(featureName, arr);
	}
	
	public void process(Object data)
	{
		process(data, null);
	}
	
	@SuppressWarnings("rawtypes")
	public HashMap<String,ArrayList> getResults()
	{
		return results;
	}
	
	public JSONObject getJSONResults(long frameNo)
	{
		try 
		{
			return JSONHelper.build(results);
		} 
		catch (JSONException je) 
		{
			Logger.e(LOGTAG,"Exception in creating JSON object:"+je);
			return null;
		}	
	}
	
	public ArrayList<JSONObject> getJSONResultsArrayList(long frameNo)
	{
		try 
		{
			return JSONHelper.buildAsArrayList(results, frameNo);
		} 
		catch (JSONException je) 
		{
			Logger.e(LOGTAG,"Exception in creating JSON object:"+je);
			return null;
		}	
	}
	
	@SuppressWarnings("unchecked")
	public void addResult(String featureName, Object result)
	{
		if(results.containsKey(featureName))
		{
			results.get(featureName).add(result);
			incrementFramesPending(featureName);
		}
	}
	
	public void addResult(Object result)
	{
		addResult(featureName, result);
	}
	
	private void incrementFramesPending(String featureName)
	{
		int temp = 1;
		if(framesPendingMap.containsKey(featureName))
		{
			temp = framesPendingMap.get(featureName);
			temp++;
		}
		framesPendingMap.put(featureName, temp);
		framesPending = temp;
	}
	
	@SuppressWarnings("rawtypes")
	public void clearResults()
	{
		for(ArrayList element : results.values())
		{
			element.clear();
		}
		resetFramesPending();
	}
	
	private void resetFramesPending()
	{
		for(Entry<String,Integer> entry : framesPendingMap.entrySet())
		{
			framesPendingMap.put(entry.getKey(), 0);
		}
	}
	
	public String[] getDependencies()
	{
		return dependencies;
	}
}
