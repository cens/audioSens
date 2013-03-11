package edu.ucla.cens.audiosens.config;

import edu.ucla.cens.audiosens.helper.FeaturesList;
import android.media.AudioFormat;

public final class AudioSensConfig 
{
	//Initial Settings
	public final static int PERIOD = 60;
	public final static int DURATION = 10;
	
	//AutoStart
	public final static boolean AUTOSTART = true;
	public static final String AUTOSTART_TAG = "audiosens_boot";
	
	//Alarms
	public static final String MAINSERVICE_TAG = "audiosens_start";
	public static final String SUMMARIZER_TAG = "audiosens_summarizer";
	
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
	
	//special Mode
	public final static boolean SPECIALMODE_DEFAULT = false;
	
	//Speech Trigger mode
	public final static boolean SPEECHTRIGGERMODE_DEFAULT = true;
	public final static float SPEECHRATE_DEFAULT = 3;
	public final static float SILENCERATE_DEFAULT = 1;
	public final static int SPEECHTRIGGER = 2;
	public final static int SILENCETRIGGER = 5;
	
	//Features
	public final static String[] FEATURES = {FeaturesList.ENERGY,
											FeaturesList.ZCR,
											FeaturesList.SPEECHINFERENCEFEATURES,
											FeaturesList.MFCC};
	
	//Writers
	public final static String[] DATAWRITERS = {/*"AndroidLog",*/"Ohmage"};
	
	//Classifiers
	public final static String[] CLASSIFIERS = {"VoiceActivityDetection"};
	
	//Sensors
	public final static String[] SENSORS = {"Location", "Battery"};
	
	//Raw Audio
	public final static boolean RAWMODE_DEFAULT = false;
	//public final static boolean RAWAUDIO = true;
	public final static boolean RAWAUDIO_WRITEONLYSPEECH = true;
	
	//Intents
	public final static String STATUSRECEIVERTAG = "statusReceiverIntent";
	public final static String STATUSRECEIVER_RECORD = "recordStatus";
	public final static String STATUSRECEIVER_MSG = "messageStatus";
	public final static String INFERENCERECEIVERTAG = "speechInferenceReceiverIntent";
	public final static String INFERENCERECEIVER_PERCENT = "speechInferencePercent";
	
	//Location
	public final static int LOCATION_MINDISTANCE = 0;	//Minimum Distance between Location Updates (in meters)
	public final static int LOCATION_MINTIME = 30 * 1000;	//Minimum Time between Location Updates (in ms)

}
