package edu.ucla.cens.audiosens.processors;

import java.util.HashMap;

import edu.ucla.cens.audiosens.helper.FeaturesList;

public class ZeroCrossingRateProcessor extends BaseProcessor 
{

	public ZeroCrossingRateProcessor() {
		super();
	}

	@Override
	public void process(Object data, HashMap<String, String> options) 
	{
		short[] data_short = (short[])data;

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

}
