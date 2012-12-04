package edu.ucla.cens.audiosens.writers;


import org.json.JSONObject;

import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.processors.BaseProcessor;

public class AndroidLogWriter extends BaseWriter {
	
	private static final String LOGTAG = "AndroidLogWriter";
	private static final String DATATAG = "AudioSensData";
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(BaseProcessor processor) 
	{
		Logger.d(LOGTAG,"Writing in AndroidLogWriter");

		processor.framesPending = 0;
		//writePerProcessor(processor);
		writePerFeature(processor);
		
	}
	
	private void writePerProcessor(BaseProcessor processor)
	{
		Logger.i(LOGTAG, processor.getJSONResults().toString());
	}
	
	private void writePerFeature(BaseProcessor processor)
	{
		for(JSONObject jsonObject : processor.getJSONResultsArrayList())
		{
			Logger.i(LOGTAG, jsonObject.toString());
		}
	}

}
