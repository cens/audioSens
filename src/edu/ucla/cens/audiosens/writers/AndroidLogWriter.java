package edu.ucla.cens.audiosens.writers;


import java.util.HashMap;
import org.json.JSONObject;

import edu.ucla.cens.audiosens.AudioSensService;
import edu.ucla.cens.audiosens.classifier.BaseClassifier;
import edu.ucla.cens.audiosens.config.AndroidLogWriterConfig;
import edu.ucla.cens.audiosens.helper.JSONHelper;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.processors.BaseProcessor;
import edu.ucla.cens.audiosens.sensors.BaseSensor;

public class AndroidLogWriter extends BaseWriter {

	private static final String LOGTAG = "AndroidLogWriter";
	private static final String DATATAG = "AudioSensData";

	public AndroidLogWriter()
	{
		super();
		isConnected = true;
		writesFeatures = AndroidLogWriterConfig.WRITESFEATURES;
		writesClassifier = AndroidLogWriterConfig.WRITESCLASSIFIER;
		writesSensors = AndroidLogWriterConfig.WRITESSENSORS;
	}

	@Override
	public void initialize(AudioSensService service) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(BaseProcessor processor, long frameNo) 
	{
		Logger.d(LOGTAG,"Writing in AndroidLogWriter");
		JSONObject tempJson = processor.getJSONResultsObject(frameNo);;
		if (tempJson != null)
			Logger.i(DATATAG, tempJson.toString());
	}

	@Override
	public void writeSensors(HashMap<String, BaseSensor> sensorMap, long frameNo) 
	{
		JSONObject tempJson = JSONHelper.buildSensorJson(sensorMap, frameNo);
		if(tempJson != null)
			Logger.i(DATATAG, tempJson.toString());
	}


	@Override
	public void writeClassifier(BaseClassifier classifier, long frameNo) 
	{
		JSONObject jsonObject = classifier.getJSONResultsObject(frameNo);
		if (jsonObject != null)
			Logger.i(DATATAG, jsonObject.toString());
	}

}
