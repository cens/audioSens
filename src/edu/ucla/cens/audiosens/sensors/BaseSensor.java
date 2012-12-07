package edu.ucla.cens.audiosens.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.AudioSensService;

public abstract class BaseSensor {
	
	protected boolean initialized;
	public abstract void init(AudioSensService obj);
	public abstract void destroy();
	public abstract JSONObject getJsonResult() throws JSONException;
	
	public BaseSensor()
	{
		initialized = false;
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
}
