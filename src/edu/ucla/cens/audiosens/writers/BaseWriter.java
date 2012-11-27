package edu.ucla.cens.audiosens.writers;

import edu.ucla.cens.audiosens.processors.BaseProcessor;

public abstract class BaseWriter {
	
	protected boolean isConnected = false;
	
	public abstract void initialize();
	public abstract void destroy();
	public abstract void write(BaseProcessor processor);
	
	public boolean isConnected()
	{
		return isConnected;
	}

}
