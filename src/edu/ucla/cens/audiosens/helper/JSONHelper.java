package edu.ucla.cens.audiosens.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {
	
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
	
	public static ArrayList<JSONObject> buildAsArrayList(Object inp) throws JSONException
	{
		ArrayList<JSONObject> op = new ArrayList<JSONObject>();
		JSONObject jsonObject;
		if(inp instanceof HashMap)
		{
			for(Entry<String,Object> entry : ((HashMap<String,Object>)inp).entrySet())
			{
				jsonObject = new JSONObject();
				jsonObject.put(entry.getKey(), build(entry.getValue()));
				op.add(jsonObject);
			}
		}
		return op;
	}

}
