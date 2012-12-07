package edu.ucla.cens.audiosens.sensors;

import edu.ucla.cens.audiosens.helper.Logger;

public class SensorFactory 
{
	public static final String LOGTAG = "SensorFactory";
	
	public static BaseSensor build(String sensorName)
	{
		try 
		{
			return (BaseSensor) Class.forName("edu.ucla.cens.audiosens.sensors."+sensorName+"Sensor").newInstance();
		} 
		catch (InstantiationException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + sensorName + " : " + e);
			return null;
		} 
		catch (IllegalAccessException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + sensorName + " : " + e);
			return null;
		} 
		catch (ClassNotFoundException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + sensorName + " : " + e);
			return null;
		}
	}
}
