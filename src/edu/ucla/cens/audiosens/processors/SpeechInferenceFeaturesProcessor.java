package edu.ucla.cens.audiosens.processors;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucla.cens.audiosens.helper.Features;

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
		addResult(Features.NUMCORPEAKS, tempResults[0]);
		addResult(Features.MAXCORPEAK, tempResults[1]);
		addResult(Features.MAXCORPEAKLAG, tempResults[2]);
		addResult(Features.SPECTRALENTROPY, tempResults[3]);
		addResult(Features.RELATIVESPECTRALENTROPY, tempResults[4]);
		
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
		results.put(Features.NUMCORPEAKS, new ArrayList<Double>());
		results.put(Features.MAXCORPEAK, new ArrayList<Double>());
		results.put(Features.MAXCORPEAKLAG, new ArrayList<Double>());
		results.put(Features.SPECTRALENTROPY, new ArrayList<Double>());
		results.put(Features.RELATIVESPECTRALENTROPY, new ArrayList<Double>());
		//results.put(Features.CORPEAKVALUES, new ArrayList<double[]>());
		//results.put(Features.CORPEAKLAGVALUES, new ArrayList<double[]>());
	}
	@Override
	public void setName() {
		setName("SpeechInferenceFeatures");
	}

}
