package edu.ucla.cens.audiosens.classifier;

import java.util.HashMap;

import edu.ucla.cens.audiosens.processors.BaseProcessor;

public abstract class BaseClassifier 
{
	public void initialize()
	{
		
	}

	public abstract void classify(HashMap<String, BaseProcessor> resultMap);

}