package edu.ucla.cens.audiosens.classifier;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucla.cens.audiosens.helper.Features;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.processors.BaseProcessor;


public class VoiceActivityDetectionClassifier extends BaseClassifier 
{
	public final String LOGTAG = "VoiceActivityDetectionClassifier";
	
	double peakValue;
	double noOfPeaks;
	double relSpectralEntropy;
	
	public VoiceActivityDetectionClassifier() 
	{
		super();
	}

	@Override
	public void classify(HashMap<String, BaseProcessor> resultMap) 
	{
		try
		{
			peakValue = loadLatestValue(resultMap, Features.MAXCORPEAK);
			noOfPeaks = loadLatestValue(resultMap, Features.NUMCORPEAKS);
			relSpectralEntropy = loadLatestValue(resultMap, Features.RELATIVESPECTRALENTROPY);
		}
		catch(NullPointerException e)
		{
			Logger.e(LOGTAG, "Error in VAD : "+e)
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private double loadLatestValue(HashMap<String, BaseProcessor> resultMap, String featureName) throws NullPointerException
	{
		if(resultMap.containsKey(Features.SPEECHINFERENCEFEATURES))
		{
			ArrayList<Double> arr = resultMap.get(Features.SPEECHINFERENCEFEATURES)
					.getResults()
					.get(featureName);
			if(arr.size()>0)
				return arr.get(arr.size() - 1);
		}
		throw new NullPointerException("Cannot oad value for " + featureName);
	}
	
	
	
}
