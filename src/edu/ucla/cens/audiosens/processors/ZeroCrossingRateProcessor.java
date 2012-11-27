package edu.ucla.cens.audiosens.processors;

import java.util.HashMap;

public class ZeroCrossingRateProcessor extends BaseProcessor 
{

	public ZeroCrossingRateProcessor() {
		super();
	}

	@Override
	public void process(short[] data, HashMap<String, String> options) 
	{
		long sum=0;
		for (int i = 1; i < data.length; i++) 
		{
			sum += Math.abs(sign(data[i]) - sign(data[i-1]));
		}
		sum= (sum*1000) / data.length;
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

}
