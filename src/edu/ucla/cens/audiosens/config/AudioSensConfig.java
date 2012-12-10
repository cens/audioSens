package edu.ucla.cens.audiosens.config;

import edu.ucla.cens.audiosens.helper.FeaturesList;
import android.media.AudioFormat;

public final class AudioSensConfig 
{
	//Initial Settings
	public final static int PERIOD = 5;
	public final static int DURATION = 1;
	
	//AutoStart
	public final static boolean AUTOSTART = true;
	public static final String AUTOSTART_TAG = "audiosens_boot";
	
	//Audio Settings
	public final static int FRAMELENGTH =32;
	public final static int INITIALQUEUESIZE = 200;
	@SuppressWarnings("deprecation")
	public final static int CHANNELCONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	public final static int ENCODINGTYPE = AudioFormat.ENCODING_PCM_16BIT;
	public final static int FREQUENCY = 8000;
	
	//Frequency Mode
	public final static boolean CONTINUOUSMODE_DEFAULT = false;
	public final static int CONTINUOUSMODE_ALARM = 900;	//15 minutes
	public final static int CONTINUOUSMODE_FLUSHTIME =  60; //1 minute
	
	//Features
	public final static String[] FEATURES = {FeaturesList.ENERGY,
											FeaturesList.SPEECHINFERENCEFEATURES};
	
	//Writers
	public final static String[] DATAWRITERS = {"AndroidLog","Ohmage"};
	
	//Classifiers
	public final static String[] CLASSIFIERS = {"VoiceActivityDetection"};
	
	//Sensors
	public final static String[] SENSORS = {"Location", "Battery"};
	
	//Raw Audio
	public final static boolean RAWAUDIO = false;
	
	//Intents
	public final static String STATUSRECEIVERTAG = "statusReceiverIntent";
	public final static String STATUSRECEIVER_RECORD = "recordStatus";
	public final static String STATUSRECEIVER_MSG = "messageStatus";
	public final static String INFERENCERECEIVERTAG = "speechInferenceReceiverIntent";
	public final static String INFERENCERECEIVER_PERCENT = "speechInferencePercent";
	
	//Location
	public final static int LOCATION_MINDISTANCE = 20;	//Minimum Distance between Location Updates (in meters)
	public final static int LOCATION_MINTIME = 60 * 1000;	//Minimum Time between Location Updates (in ms)

}
