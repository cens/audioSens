package edu.ucla.cens.audiosens.processors;

import java.util.HashMap;

import edu.ucla.cens.audiosens.helper.Logger;

public class EnergyProcessor extends BaseProcessor 
{
	public static final String LOGTAG = "EnergyProcessor";
	public EnergyProcessor() 
	{
		super();
	}

	@Override
	public void process(short[] data, HashMap<String, String> options) 
	{
		//Logger.d(LOGTAG,"Processing Energy");

		long sum=0;
		for (int i = 0; i < data.length; i++) 
		{
			sum += data[i] * data[i];
		}
		sum = (long)Math.sqrt(sum / data.length);
		addResult(sum);
	}

}
