package edu.ucla.cens.audiosens;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.processing.ProcessingQueue;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AudioSensRecorder implements Runnable
{	
	//Preferences
	public SharedPreferences mSettings;
	public SharedPreferences.Editor mEditor;
	
	private final String LOGTAG = "AudioSensRecorder";
	
	private AudioSensService obj;	//reference to the calling service
	private AudioRecord recordInstance;
	ProcessingQueue processingQueue;
	
	int duration;	//length of recording
	int frameSize;	//size of frame
	int frameStep;	//0.5 * frameSize
	int bSamples;
	int noChannels;
	
	int bufferRead;
	int bufferSize;
	int framePeriod;
	short[] tempBuffer;
	
	long startTime;
	
	private volatile boolean isRecording;
	private final Object mutex = new Object();

	public AudioSensRecorder(AudioSensService obj, int duration)
	{
		this.obj = obj;
		
		//Preferences
		mSettings = PreferenceManager.getDefaultSharedPreferences(obj);
		mEditor = mSettings.edit();
		
		this.duration = duration; 
		frameSize = AudioSensConfig.FRAMELENGTH * 8;
		frameStep = (int)frameSize/2;
		framePeriod = frameStep;
		
		processingQueue = new ProcessingQueue(this, frameSize, frameStep);
		
		initializeAudioParameters();

	}

	@Override
	public void run() 
	{
		//to ensure that the recording starts only after the command is given
		synchronized (mutex) 
		{
			while (!this.isRecording) 
			{
				try 
				{
					mutex.wait();
				} 
				catch (InterruptedException e) 
				{
					throw new IllegalStateException("Wait Interrupted!",e);
				}
			}
		}
		
		processingQueue.start();
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		
		// Allocate Recorder and Start Recording…
		bufferRead = 0;
		startTime = SystemClock.elapsedRealtime();

		recordInstance = new AudioRecord(
				MediaRecorder.AudioSource.MIC,
				AudioSensConfig.FREQUENCY,
				AudioSensConfig.CHANNELCONFIG,
				AudioSensConfig.ENCODINGTYPE,
				bufferSize);

		//Check if AudioRecord has initiliazed
		try
		{
			int checkCount=0;
			while(recordInstance.getState()!=AudioRecord.STATE_INITIALIZED)
			{
				if(checkCount++ >= 20)
				{
					Logger.w(LOGTAG, "Audio Record failed to initialize even after 100 ms");
					return;
				}
				Thread.sleep(5);
			}
		}
		catch(InterruptedException ie)
		{
			Logger.e(LOGTAG, "Audio Record failed to initialize even after 100 ms:"+ie);
		}

		recordInstance.startRecording();
		while(continueRecording())
		{
			bufferRead = recordInstance.read(tempBuffer, 0, tempBuffer.length);			

			if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) 
			{
				throw new IllegalStateException("read() returned AudioRecord.ERROR_INVALID_OPERATION");
			} 
			else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) 
			{
				throw new IllegalStateException("read() returned AudioRecord.ERROR_BAD_VALUE");
			} 
			else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) 
			{
				throw new IllegalStateException("read() returned AudioRecord.ERROR_INVALID_OPERATION");
			}
			processingQueue.insertData(tempBuffer, System.currentTimeMillis());
		}
		
		setRecording(false);
		recordInstance.stop();
		recordInstance.release();
		
		Logger.d(LOGTAG, "Finished Recording, now processing");
		
		try
		{
			processingQueue.join();
			Logger.d(LOGTAG, "Finished Processing");
			processingQueue.writeData();
			Logger.d(LOGTAG, "Finished Writing remaining data");
			processingQueue.closeConnections();
			processingQueue = null;
		}
		catch(InterruptedException ie)
		{
			Logger.e(LOGTAG, "Exception in joining ProcessingQueue Thread:"+ie);
		}
		
		//TODO Storing results

	}
	
	private boolean continueRecording()
	{
		if(isRecording && (SystemClock.elapsedRealtime() - startTime) < duration * 1000)
			return true;
		return false;
	}
	
	public void setRecording(boolean isRecording) 
	{
		synchronized (mutex) 
		{
			this.isRecording = isRecording;
			if (this.isRecording) 
			{
				mutex.notify();
			}
		}
	}

	public boolean isRecording() 
	{
		synchronized (mutex) 
		{
			return isRecording;
		}
	}
	
	private void initializeAudioParameters()
	{
		//Initialiazing Audio Parameters
		if (AudioSensConfig.ENCODINGTYPE == AudioFormat.ENCODING_PCM_16BIT)
			bSamples = 16;
		else
			bSamples = 8;

		if (AudioSensConfig.CHANNELCONFIG == AudioFormat.CHANNEL_CONFIGURATION_MONO)
			noChannels = 1;
		else
			noChannels = 2;
		
		bufferSize = framePeriod * 100 * bSamples * noChannels / 8;
		
		if (bufferSize < AudioRecord.getMinBufferSize(AudioSensConfig.FREQUENCY, AudioSensConfig.CHANNELCONFIG, AudioSensConfig.ENCODINGTYPE))
		{ 
			// Check to make sure buffer size is not smaller than the smallest allowed one 
			bufferSize = AudioRecord.getMinBufferSize(AudioSensConfig.FREQUENCY, AudioSensConfig.CHANNELCONFIG, AudioSensConfig.ENCODINGTYPE);
			framePeriod = bufferSize / ( 2 * bSamples * noChannels / 8 );
		}
		tempBuffer = new short[framePeriod*bSamples/16*noChannels];
	}
}
