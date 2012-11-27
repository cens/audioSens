package edu.ucla.cens.audiosens.processors;

import edu.ucla.cens.audiosens.helper.Logger;

public class ProcessorFactory 
{
	public static final String LOGTAG = "ProcessorFactory";
	
	public static BaseProcessor build(String processorName)
	{
		try {
			return (BaseProcessor) Class.forName("edu.ucla.cens.audiosens.processors."+processorName+"Processor").newInstance();
		} catch (InstantiationException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + processorName + " : " + e);
			return null;
		} catch (IllegalAccessException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + processorName + " : " + e);
			return null;
		} catch (ClassNotFoundException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + processorName + " : " + e);
			return null;
		}
	}
}
