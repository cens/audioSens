package edu.ucla.cens.audiosens.processors;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucla.cens.audiosens.helper.FeaturesList;

public class SpeechInferenceFeaturesProcessor extends BaseProcessor 
{
	//Loads NDK Library
	static {  
		System.loadLibrary("computeFeatures");  
	}
	private native double[] features(short[] array);
	private native void audioFeatureExtractionInit();
	private native void audioFeatureExtractionDestroy();
	
	public SpeechInferenceFeaturesProcessor() 
	{
		super();
		audioFeatureExtractionInit();
	}
	

	@Override
	public void process(Object data, HashMap<String, String> options) 
	{
		short[] data_short = (short[])data;
		double[] tempResults = features(data_short);
		addResult(FeaturesList.NUMCORPEAKS, tempResults[0]);
		addResult(FeaturesList.MAXCORPEAK, tempResults[1]);
		addResult(FeaturesList.MAXCORPEAKLAG, tempResults[2]);
		addResult(FeaturesList.SPECTRALENTROPY, tempResults[3]);
		addResult(FeaturesList.RELATIVESPECTRALENTROPY, tempResults[4]);
		addResult(FeaturesList.SPEECHINFERENCE, tempResults[8]);
		
		/*
		double[] temp_arr;
		temp_arr=new double[(int) tempResults[0]];
		System.arraycopy(tempResults, 6, temp_arr, 0, (int)tempResults[0]);
		addResult(Features.CORPEAKVALUES, temp_arr);
		temp_arr=new double[(int) tempResults[0]];
		System.arraycopy(tempResults, 6 + (int)tempResults[0], temp_arr, 0, (int)tempResults[0]);
		addResult(Features.CORPEAKLAGVALUES, temp_arr);
		*/
	}
	
	@Override
	public void initializeResults()
	{
		results.put(FeaturesList.NUMCORPEAKS, new ArrayList<Double>());
		results.put(FeaturesList.MAXCORPEAK, new ArrayList<Double>());
		results.put(FeaturesList.MAXCORPEAKLAG, new ArrayList<Double>());
		results.put(FeaturesList.SPECTRALENTROPY, new ArrayList<Double>());
		results.put(FeaturesList.RELATIVESPECTRALENTROPY, new ArrayList<Double>());
		results.put(FeaturesList.SPEECHINFERENCE, new ArrayList<Double>());
		//results.put(Features.CORPEAKVALUES, new ArrayList<double[]>());
		//results.put(Features.CORPEAKLAGVALUES, new ArrayList<double[]>());
	}
	@Override
	public void setName() {
		setName("SpeechInferenceFeatures");
	}

}
