package edu.ucla.cens.audiosens.classifier;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucla.cens.audiosens.helper.FeaturesList;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.processors.BaseProcessor;


public class VoiceActivityDetectionClassifier extends BaseClassifier 
{
	public final String LOGTAG = "VoiceActivityDetectionClassifier";
	
	int speechInference;
	double secondInference;
	long totalTime;
	long currentCount;
	long currentSpeech;
	long prevSecond;
	
	public VoiceActivityDetectionClassifier() 
	{
		super();
		totalTime = 0;
		currentSpeech = 0;
		currentCount = 0;
		prevSecond = 0;

	}

	@Override
	public void classify(HashMap<String, BaseProcessor> resultMap) 
	{
		try
		{
			speechInference = (int)loadLatestValue(resultMap, FeaturesList.SPEECHINFERENCE);
			totalTime += 16;
			if(speechInference==1)
				currentSpeech++;
			currentCount++;
			
			if(totalTime/1000 > prevSecond)
			{
				if(currentCount != 0)
					secondInference = currentSpeech * 100 /currentCount;
				else
					secondInference = -1;
				
				currentCount = 0;
				currentSpeech = 0;
				prevSecond = totalTime/1000;
				Logger.w(LOGTAG, "Inference : "+secondInference);
			}
			
			
		}
		catch(NullPointerException e)
		{
			Logger.e(LOGTAG, "Error in VAD : "+e);
			return;
		}
		
	}

	
	@SuppressWarnings("unchecked")
	private double loadLatestValue(HashMap<String, BaseProcessor> resultMap, String featureName) throws NullPointerException
	{
		if(resultMap.containsKey(FeaturesList.SPEECHINFERENCEFEATURES))
		{
			ArrayList<Double> arr = resultMap.get(FeaturesList.SPEECHINFERENCEFEATURES)
					.getResults()
					.get(featureName);
			if(arr.size()>0)
				return arr.get(arr.size() - 1);
		}
		throw new NullPointerException("Cannot oad value for " + featureName);
	}

}
