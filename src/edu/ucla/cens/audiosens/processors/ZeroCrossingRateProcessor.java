package edu.ucla.cens.audiosens.processors;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ucla.cens.audiosens.helper.FeaturesList;

public class ZeroCrossingRateProcessor extends BaseProcessor 
{
	short[] data_short;
	@SuppressWarnings("rawtypes")
	ArrayList tempList;

	public ZeroCrossingRateProcessor() {
		super();
	}

	@Override
	public void process(Object data, HashMap<String, String> options) 
	{
		data_short = (short[])data;

		long sum=0;
		for (int i = 1; i < data_short.length; i++) 
		{
			sum += Math.abs(sign(data_short[i]) - sign(data_short[i-1]));
		}
		sum= (sum*1000) / data_short.length;
		addResult(sum);
	}
	
	private int sign(short inp)
	{
		if(inp>0)
			return 1;
		else if (inp==0)
			return 0;
		else
			return -1;
	}

	@Override
	public void setName() 
	{
		setName(FeaturesList.ZCR);
	}

	@Override
	public void summarize() 
	{
		//Returns the average ZCR in the current result List
		tempList = results.get(featureName);
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
