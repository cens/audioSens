package edu.ucla.cens.audiosens.writers;

import java.util.HashMap;

import edu.ucla.cens.audiosens.processors.BaseProcessor;
import edu.ucla.cens.audiosens.sensors.BaseSensor;

public abstract class BaseWriter {
	
	protected boolean isConnected = false;
	
	public abstract void initialize();
	public abstract void destroy();
	public abstract void write(BaseProcessor processor, long frameNumber);
	public abstract void writeSensors(HashMap<String, BaseSensor> sensorMap, long frameNo); 
	
	public boolean isConnected()
	{
		return isConnected;
	}

}
