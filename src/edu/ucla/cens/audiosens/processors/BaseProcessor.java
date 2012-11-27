package edu.ucla.cens.audiosens.processors;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.helper.Logger;

public abstract class BaseProcessor 
{
	private static final String LOGTAG = "BaseProcessor";
	
	protected ArrayList<Double> results;
	private String[] dependencies;
	public int framesPending;

	public BaseProcessor()
	{
		results = new ArrayList<Double>();
		framesPending = 0;
	}
	
	public abstract void process(short[] data, HashMap<String,String> options);
	
	public void process(short[] data)
	{
		process(data, null);
	}
	
	public ArrayList<Double> getResults()
	{
		return results;
	}
	
	public JSONObject getJSONResults()
	{
		JSONArray jsonArray = new JSONArray(results);
		JSONObject jsonObject = new JSONObject();
		try 
		{
			jsonObject.put("data", jsonArray);
			return jsonObject;
		} 
		catch (JSONException je) 
		{
			Logger.e(LOGTAG,"Exception in creating JSON object:"+je);
			return null;
		}
		
	}
	
	public void addResult(double result)
	{
		results.add(result);
		framesPending ++;
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
