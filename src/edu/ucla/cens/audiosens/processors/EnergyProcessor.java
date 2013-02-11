package edu.ucla.cens.audiosens.processors;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucla.cens.audiosens.helper.FeaturesList;
import edu.ucla.cens.audiosens.helper.Logger;

public class EnergyProcessor extends BaseProcessor 
{
	public static final String LOGTAG = "EnergyProcessor";
	public EnergyProcessor() 
	{
		super();
	}

	@Override
	public void process(Object data, HashMap<String, String> options) 
	{

		short[] data_short = (short[])data;
		long sum=0;
		for (int i = 0; i < data_short.length; i++) 
		{
			sum += data_short[i] * data_short[i];
		}
		sum = (long)Math.sqrt(sum / data_short.length);
		addResult(sum);
	}

	@Override
	public void setName() {
		setName(FeaturesList.ENERGY);
	}

	@Override
	public void summarize() 
	{
		@SuppressWarnings("rawtypes")
		ArrayList tempList = results.get(featureName);
		long sum = 0;
		int count = 0;
		for( int i = 0; i < tempList.size(); i++)
		{
			sum += (Long)tempList.get(i);
			count++;
		}
		if(count>0)
			sum /= count;
		
		addToSummary("average", sum+"");
	}

}
