package edu.ucla.cens.audiosens.processors;

import java.util.ArrayList;
import java.util.HashMap;

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
	protected String featureName;
	
	public BaseProcessor()
	{
		setName();
		results = new HashMap<String,ArrayList>();
		framesPending = 0;
		initializeResults();
	}
	
	public abstract void setName();
	
	public void setName(String featureName)
	{
		this.featureName = featureName; 
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
	
	public JSONObject getJSONResults()
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
	
	public ArrayList<JSONObject> getJSONResultsArrayList()
	{
		try 
		{
			return JSONHelper.buildAsArrayList(results);
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
		}
		framesPending ++;
	}
	
	public void addResult(Object result)
	{
		addResult(featureName, result);
	}
	
	public void clearResults()
	{
		results.clear();
	}
	
	public String[] getDependencies()
	{
		return dependencies;
	}
}
