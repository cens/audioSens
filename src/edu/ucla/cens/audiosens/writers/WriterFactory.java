package edu.ucla.cens.audiosens.writers;

import edu.ucla.cens.audiosens.helper.Logger;

public class WriterFactory 
{
	public static final String LOGTAG = "WriterFactory";
	
	public static BaseWriter build(String writerName)
	{
		try 
		{
			return (BaseWriter) Class.forName("edu.ucla.cens.audiosens.writers."+writerName+"Writer").newInstance();
		} 
		catch (InstantiationException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + writerName + " : " + e);
			return null;
		} 
		catch (IllegalAccessException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + writerName + " : " + e);
			return null;
		} 
		catch (ClassNotFoundException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + writerName + " : " + e);
			return null;
		}
	}
}
