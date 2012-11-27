package edu.ucla.cens.audiosens.config;

import android.media.AudioFormat;

public final class AudioSensConfig 
{
	//Initial Settings
	public final static int PERIOD = 60;
	public final static int DURATION = 10;
	
	//AutoStart
	public final static boolean AUTOSTART = true;
	public static final String AUTOSTART_TAG = "audiosens_boot";
	
	//Audio Settings
	public final static int FRAMELENGTH =32;
	public final static int INITIALQUEUESIZE = 200;
	public final static int CHANNELCONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	public final static int ENCODINGTYPE = AudioFormat.ENCODING_PCM_16BIT;
	public final static int FREQUENCY = 8000;
	
	//Features
	public final static String[] FEATURES = {"Energy","ZeroCrossingRate"};
	
	//Writers
	public final static String[] DATAWRITERS = {"AndroidLog"};
	
	//Intents
	public final static String STATUSRECEIVERTAG = "statusReceiverIntent";
	public final static String STATUSRECEIVER_RECORD = "recordStatus";
	public final static String STATUSRECEIVER_MSG = "messageStatus";

	//Data Size Limits
	public final static boolean DATAFRAMELIMITON = false;
	public final static int DATAFRAMELIMIT = 100; 
	
}
