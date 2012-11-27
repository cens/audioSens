package edu.ucla.cens.audiosens.processing;

import java.util.HashMap;

import edu.ucla.cens.audiosens.AudioSensRecorder;
import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;
import edu.ucla.cens.audiosens.processors.BaseProcessor;
import edu.ucla.cens.audiosens.processors.ProcessorFactory;
import edu.ucla.cens.audiosens.writers.BaseWriter;
import edu.ucla.cens.audiosens.writers.WriterFactory;


public class ProcessingQueue extends Thread
{
	private final String LOGTAG = "AudioSensProcessingQueue";

	AudioSensRecorder obj;
	private CircularQueue queue;
	private short[] audioFrame;	
	private AudioData audioFromQueueData;
	int frameSize;
	int frameStep;
	HashMap<String,BaseProcessor> resultMap;
	HashMap<String,BaseWriter> writerMap;

	private boolean startedRecording;
	private boolean stoppedOnce;

	//state variable
	public boolean processComplete = false;

	public ProcessingQueue(AudioSensRecorder obj, int frameSize, int frameStep)
	{
		Logger.d(LOGTAG,"Constructor");
		
		this.obj = obj;
		this.frameSize = frameSize;
		this.frameStep = frameStep;
		queue = new CircularQueue(AudioSensConfig.INITIALQUEUESIZE);
		audioFrame = new short[frameSize];
		resultMap = new HashMap<String,BaseProcessor>();
		writerMap = new HashMap<String, BaseWriter>();

		//initialize the first half with zeros
		for(int i=0; i < frameSize; i++)
			audioFrame[i] = 0;

		for(String processorName : AudioSensConfig.FEATURES)
		{
			if(!resultMap.containsKey(processorName))
			{
				BaseProcessor processor = ProcessorFactory.build(processorName);
				if(processor != null)
				{
					resultMap.put(processorName, processor);
				}
				else
				{
					Logger.e(LOGTAG, "Cannot create Processor for " + processorName);
				}
			}
		}
		Logger.d(LOGTAG,"Initiliazed Processors");


		for(String writerName : AudioSensConfig.DATAWRITERS)
		{
			if(!resultMap.containsKey(writerName))
			{
				BaseWriter writer = WriterFactory.build(writerName);
				if(writer != null)
				{
					writer.initialize();
					writerMap.put(writerName, writer);
				}
				else
				{
					Logger.e(LOGTAG, "Cannot create Writer for " + writerName);
				}
			}
		}
		Logger.d(LOGTAG,"Initialized Writers");

	}

	@Override
	public void run() 
	{
		startedRecording = false;
		stoppedOnce = false;
		while(!queue.emptyq() || obj.isRecording())
		{
			Logger.d(LOGTAG,"Start of Processing Data Frame");

			if(startedRecording && !obj.mSettings.getBoolean(PreferencesHelper.RECORDSTATUS, true))
			{
				stoppedOnce = true;
			}

			if(obj.isRecording())
			{
				startedRecording = true;
			}
			else
			{
				if(startedRecording)
				{
					if(obj.mSettings.getBoolean(PreferencesHelper.RECORDSTATUS, false) && stoppedOnce)
					{
						Logger.w(LOGTAG, "Terminating Processing thread since new Recording started");
						processComplete = false;
						return;
					}
				}
			}

			audioFromQueueData = (AudioData)(queue.deleteAndHandleData());
			System.arraycopy(audioFromQueueData.data, 0, audioFrame, frameStep, frameStep);

			for(BaseProcessor processor : resultMap.values())
			{
				processor.process(audioFrame);
				Logger.d(LOGTAG, "Processing frame : "+processor.framesPending+"/"+queue.getQSize());
				if(AudioSensConfig.DATAFRAMELIMITON )
				{
					if(processor.framesPending > AudioSensConfig.DATAFRAMELIMIT)
					{
						writeData();
					}
				}
			}
			Logger.d(LOGTAG, "End of Processing Loop");

		}
		
		Logger.d(LOGTAG,"FinishedProcessingQueue");

	}

	public void writeData()
	{
		for(BaseWriter writer : writerMap.values())
		{
			for(BaseProcessor processor : resultMap.values())
			{
				writer.write(processor);
			}
		}
	}

	public void closeConnections()
	{
		for(BaseWriter writer : writerMap.values())
		{
			writer.destroy();
		}
	}
	
	
	/**
	 * Insert Data Frames into the circular queue
	 * @param data
	 * @param timestamp
	 */
	public synchronized void insertData(short[] data,long timestamp)
	{
		queue.insert(data, timestamp);
	}


}
