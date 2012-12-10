package edu.ucla.cens.audiosens.writers;

import java.util.HashMap;

import edu.ucla.cens.audiosens.AudioSensService;
import edu.ucla.cens.audiosens.processors.BaseProcessor;
import edu.ucla.cens.audiosens.sensors.BaseSensor;

public abstract class BaseWriter {
	
	protected boolean isConnected = false;
	protected boolean writesFeatures = false;
	protected boolean writesSensors = true;
	protected boolean writesInference = true;
	
	
	public abstract void initialize(AudioSensService service);
	public abstract void destroy();
	public abstract void write(BaseProcessor processor, long frameNumber);
	public abstract void writeSensors(HashMap<String, BaseSensor> sensorMap, long frameNo); 
	
	public boolean isConnected()
	{
		return isConnected;
	}
	
	public boolean writesFeatures()
	{
		return writesFeatures;
	}
	
	public boolean writesSensors()
	{
		return writesSensors;
	}

	public boolean writesInference()
	{
		return writesInference;
	}
}
