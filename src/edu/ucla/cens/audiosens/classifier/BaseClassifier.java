package edu.ucla.cens.audiosens.classifier;

import java.util.HashMap;

import edu.ucla.cens.audiosens.AudioSensRecorder;
import edu.ucla.cens.audiosens.processors.BaseProcessor;

public abstract class BaseClassifier 
{
	AudioSensRecorder obj;
	public void initialize(AudioSensRecorder obj)
	{
		this.obj = obj;
	}

	public abstract void classify(HashMap<String, BaseProcessor> resultMap);

}
