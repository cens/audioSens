package edu.ucla.cens.audiosens.processing;

import java.util.HashMap;
import edu.ucla.cens.audiosens.AudioSensRecorder;
import edu.ucla.cens.audiosens.classifier.BaseClassifier;
import edu.ucla.cens.audiosens.classifier.ClassifierFactory;
import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;
import edu.ucla.cens.audiosens.processing.AudioData.Flag;
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
	HashMap<String,BaseClassifier> classifierMap;
	RawAudioFileWriter rawAudioFileWriter;

	private boolean startedRecording;
	private boolean stoppedOnce;
	private boolean continuousMode;
	private boolean forceWrite;

	//state variable
	public boolean processComplete = false;

	public ProcessingQueue(AudioSensRecorder obj, int frameSize, int frameStep, boolean continuousMode)
	{
		this.obj = obj;
		this.frameSize = frameSize;
		this.frameStep = frameStep;
		this.continuousMode = continuousMode;
		queue = new CircularQueue(AudioSensConfig.INITIALQUEUESIZE);
		audioFrame = new short[frameSize];
		resultMap = new HashMap<String,BaseProcessor>();
		writerMap = new HashMap<String, BaseWriter>();
		classifierMap = new HashMap<String, BaseClassifier>();
		forceWrite = false;

		//initialize the first half with zeros
		for(int i=0; i < frameSize; i++)
			audioFrame[i] = 0;

		//Initializes the Processors
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

		//Initialized the Writers
		for(String writerName : AudioSensConfig.DATAWRITERS)
		{
			if(!resultMap.containsKey(writerName))
			{
				BaseWriter writer = WriterFactory.build(writerName);
				if(writer != null)
				{
					writer.initialize(obj.getService());
					writerMap.put(writerName, writer);
				}
				else
				{
					Logger.e(LOGTAG, "Cannot create Writer for " + writerName);
				}
			}
		}
		Logger.d(LOGTAG,"Initialized Writers");

		//Initialized Classifiers
		for(String classifierName : AudioSensConfig.CLASSIFIERS)
		{
			if(!classifierMap.containsKey(classifierName))
			{
				BaseClassifier classifier = ClassifierFactory.build(classifierName);
				if(classifier != null)
				{
					classifier.initialize(obj);
					classifierMap.put(classifierName, classifier);
				}
				else
				{
					Logger.e(LOGTAG, "Cannot create Classifier for " + classifierName);
				}
			}
		}
		Logger.d(LOGTAG,"Initialized Classifiers");

	}

	@Override
	public void run() 
	{
		startedRecording = false;
		stoppedOnce = false;
		while(!queue.emptyq() || obj.isRecording())
		{
			//Logger.d(LOGTAG,"Start of Processing Data Frame:"+obj.isRecording());

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
					//Stops thread if a new thread has been started for the next sampling
					//Else the phone would run out of memory
					if(obj.mSettings.getBoolean(PreferencesHelper.RECORDSTATUS, false) && stoppedOnce)
					{
						Logger.w(LOGTAG, "Terminating Processing thread since new Recording started");
						processComplete = false;
						return;
					}
				}
			}

			audioFromQueueData = (AudioData)(queue.deleteAndHandleData());
			if(audioFromQueueData == null)
				continue;

			System.arraycopy(audioFromQueueData.data, 0, audioFrame, frameStep, frameStep);

			//Sends the current frame to each processor
			for(BaseProcessor processor : resultMap.values())
			{
				processor.process(audioFrame);
				//Logger.d(LOGTAG, "Processing frame : "+processor.framesPending+"/"+queue.getQSize() + "for processor: " + processor.getName());
			}

			//Adds to the raw audio File
			if(obj.mSettings.getBoolean(PreferencesHelper.RAWMODE, AudioSensConfig.RAWMODE_DEFAULT))
			{
				processRawAudio(audioFrame);
			}

			//Calls each classifier
			for(BaseClassifier classifier : classifierMap.values())
			{
				classifier.classify(resultMap);
			}

			//If in Continuous Mode, Force write the data every minute
			//Else the memory used by the Application would increase very fast
			if(continuousMode)
			{
				if(forceWrite)
				{
					writeData();
					forceWrite = false;
				}
			}

		}

	}

	public void writeData()
	{
		Logger.d(LOGTAG,"In writeData");
		summarizeProcessors();
		for(BaseWriter writer : writerMap.values())
		{
			if(writer.isConnected())
			{
				if(writer.writesSensors())
				{
					writer.writeSensors(obj.getSensorMap(), obj.getFrameNo());
				}

				if(writer.writesFeatures())
				{
					for(BaseProcessor processor : resultMap.values())
					{
						writer.write(processor, obj.getFrameNo());
					}
				}
				
				if(writer.writesClassifier())
				{
					for(BaseClassifier classifier : classifierMap.values())
					{
						classifier.complete();
						writer.writeClassifier(classifier, obj.getFrameNo());
					}
				}
			}
		}

		if(obj.mSettings.getBoolean(PreferencesHelper.RAWMODE, AudioSensConfig.RAWMODE_DEFAULT))
		{
			if(rawAudioFileWriter != null && !rawAudioFileWriter.hasPermanentlyFailed())
				rawAudioFileWriter.write();
		}

		obj.setFrameNo();
		cleanUpProcessors();
		cleanUpClassifiers();
	}

	public void forceWrite()
	{
		forceWrite = true;
	}
	
	public void summarizeProcessors()
	{
		for(BaseProcessor processor : resultMap.values())
		{
			processor.summarize();
		}
	}

	public void cleanUpProcessors()
	{
		for(BaseProcessor processor : resultMap.values())
		{
			processor.clearResults();
		}
	}
	
	public void cleanUpClassifiers()
	{
		for(BaseClassifier classifier : classifierMap.values())
		{
			classifier.clearResults();
		}
	}

	public void closeConnections()
	{
		for(BaseWriter writer : writerMap.values())
		{
			writer.destroy();
		}
	}

	private void processRawAudio(short[] data)
	{
		if(rawAudioFileWriter == null)
			rawAudioFileWriter = new RawAudioFileWriter(obj);
		if(!rawAudioFileWriter.hasPermanentlyFailed())
		{
			if(!rawAudioFileWriter.isInitialized())
				rawAudioFileWriter.init(obj.getFrameNo());
			rawAudioFileWriter.process(data);
		}
	}

	/**
	 * Insert Data Frames into the circular queue
	 * @param data
	 * @param timestamp
	 */
	public synchronized void insertData(short[] data,long timestamp,Flag flag)
	{
		queue.insert(data, timestamp, flag);
	}


}
