package edu.ucla.cens.audiosens.classifier;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import edu.ucla.cens.audiosens.AudioSensRecorder;
import edu.ucla.cens.audiosens.helper.FeaturesList;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;
import edu.ucla.cens.audiosens.processors.BaseProcessor;
import edu.ucla.cens.audiosens.sqlite.SpeechInferenceObject;


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
		name = "VoiceActivityDetectionClassifier";
	}
	
	@Override
	public void initialize(AudioSensRecorder obj)
	{
		super.initialize(obj);
		clearSpeechMode();
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
				notifyUI(secondInference);
				addResult(secondInference);
				updateSpeechMode(secondInference);
			}


		}
		catch(NullPointerException e)
		{
			Logger.e(LOGTAG, "Error in VAD : "+e);
			return;
		}

	}

	@Override
	public void complete() 
	{
		if(totalTime > prevSecond * 1000 + 500)	//At least 500 ms have been recorded
		{
			prevSecond ++;
			if(currentCount != 0)
			{
				secondInference = currentSpeech * 100 /currentCount;
				addResult(secondInference);
			}
		}
	}

	@Override
	public void clearResults()
	{
		addToDatabase();
		super.clearResults();
		clearSpeechMode();
	}
	
	private boolean getInference()
	{
		int current = 0;
		int total = 0;
		for(int i=0; i<results.size(); i++)
		{
		    total ++;
		    if(results.get(i)>5)
		    	current++;
		}
		if(total!=0 && current/total>0.25)
			return true;
		
		return false;
	}

	private void updateSpeechMode(double secondInference)
	{
		if(secondInference > 0)
		{
			obj.mEditor.putBoolean(PreferencesHelper.SPEECHMODE, true);
			obj.mEditor.commit();
		}
	}

	private void clearSpeechMode()
	{
		obj.mEditor.putBoolean(PreferencesHelper.SPEECHMODE, false);
		obj.mEditor.commit();
	}

	private void notifyUI(double percent)
	{
		obj.getService().sendSpeechInferenceBroadcast(percent);
	}
	
	private void addToDatabase()
	{
		JSONArray jsonArray = new JSONArray((ArrayList<Double>)results);
		
		int inference_int = 0;
		if(getInference())
			inference_int = 1;
		
		SpeechInferenceObject inference 
			= new SpeechInferenceObject(obj.getFrameNo(),
										obj.getService().getVersionNo(),
										jsonArray.toString(),
										inference_int,
										obj.getPeriod(),
										obj.getDuration());
		obj.getService().getDB().addInference(inference);
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
