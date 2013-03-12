package edu.ucla.cens.audiosens.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import edu.ucla.cens.audiosens.AudioSensRecorder;
import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.Logger;
import edu.ucla.cens.audiosens.helper.PreferencesHelper;

import android.media.AudioFormat;
import android.os.Environment;

public class RawAudioFileWriter 
{
	private final String LOGTAG = "RawAudioFileWriter";
	private boolean initialized;
	private boolean permanentFailure;
	private AudioSensRecorder obj;

	//To create Raw Audio Files
	private int payloadSize=0;
	RandomAccessFile fWriter;
	File file;
	private String fDirPath;
	private String fPath_temp;
	private String fPath;

	private int bSamples;
	private int noChannels;

	private byte[] data_byte;

	public RawAudioFileWriter(AudioSensRecorder obj)
	{
		initialized = false;
		permanentFailure = false;
		this.obj = obj;
	}

	public void init(long frameNo)
	{
		if(!initializeFolder())
		{
			permanentFailure = true;
			return;
		}
		
		//Initially, creates a file which starts with "p", On completion, this file is renamed without the "p"
		//This is used to detect incomplete files. Incomplete files can be caused due to the App getting closed,
		//due to reasons such as the Phone Booting or extremely low memory
		fPath_temp=fDirPath+"/p"+frameNo+".wav";
		fPath=fDirPath+"/"+frameNo+".wav";
		payloadSize = 0;
		initializeAudioParameters();

		file = new File(fPath_temp);
		try 
		{
			fWriter = new RandomAccessFile(file, "rw");
			fWriter.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
			fWriter.writeBytes("RIFF");
			fWriter.writeInt(0); // Final file size not known yet, write 0 
			fWriter.writeBytes("WAVE");
			fWriter.writeBytes("fmt ");
			fWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
			fWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
			fWriter.writeShort(Short.reverseBytes((short)noChannels));// Number of channels, 1 for mono, 2 for stereo
			fWriter.writeInt(Integer.reverseBytes(AudioSensConfig.FREQUENCY)); // Sample rate
			fWriter.writeInt(Integer.reverseBytes(AudioSensConfig.FREQUENCY*bSamples*noChannels/8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
			fWriter.writeShort(Short.reverseBytes((short)(noChannels*bSamples/8))); // Block align, NumberOfChannels*BitsPerSample/8
			fWriter.writeShort(Short.reverseBytes((short)bSamples)); // Bits per sample
			fWriter.writeBytes("data");
			fWriter.writeInt(0); // Data chunk size not known yet, write 0
		} 
		catch (FileNotFoundException fe) 
		{
			permanentFailure = true;
			Logger.e(LOGTAG,"Exception:" + fe);
			cleanFile();
			return;
		}
		catch (IOException ioe) 
		{
			permanentFailure = true;
			Logger.e(LOGTAG,"Exception:" + ioe);
			cleanFile();
			return;
		}

		initialized = true;
	}

	public void process(short data[])
	{
		if(!initialized || permanentFailure)
			return;
		
		try 
		{
			convertToByteArray(data);
			fWriter.write(data_byte);
		} 
		catch (IOException ioe) 
		{
			permanentFailure = true;
			Logger.e(LOGTAG,"Exception:" + ioe);
			cleanFile();
		}

		payloadSize += data.length;
	}

	//Converts the Short array to a byte array
	private void convertToByteArray(short[] data)
	{
		if(data_byte == null || (data.length) != data_byte.length )
			data_byte = new byte[data.length];

		int halfBufferLength = data.length/2;
		
		for(int i=0;i<halfBufferLength;i++)
		{
			data_byte[i*2] = (byte)(data[halfBufferLength + i] & 0xff);
			data_byte[i*2+1] = (byte)((data[halfBufferLength + i] >> 8) & 0xff);
		}
	}

	public void write()
	{
		if(!initialized || permanentFailure)
			return;

		try
		{
			if(fWriter!=null)
			{
				fWriter.seek(4); // Write size to RIFF header
				fWriter.writeInt(Integer.reverseBytes(36+payloadSize));

				fWriter.seek(40); // Write size to Subchunk2Size field
				fWriter.writeInt(Integer.reverseBytes(payloadSize));

				fWriter.close(); // Remove prepared file
				fWriter= null;
				
				if(AudioSensConfig.RAWAUDIO_WRITEONLYSPEECH && !obj.mSettings.getBoolean(PreferencesHelper.SPEECHMODE, false))
				{
					Logger.i("Deleting File");
					file.delete();
				}
				else
				{
					file.renameTo(new File(fPath));
				}
			}
		}
		catch(IOException ioe)
		{
			permanentFailure = true;
			Logger.e(LOGTAG,"Exception:" + ioe);
			cleanFile();
		}
		initialized = false;
	}

	void cleanFile()
	{
		if(fWriter!=null)
		{
			try 
			{
				fWriter.close();
			} 
			catch (IOException ioe) 
			{
				Logger.e(LOGTAG,"Exception:" + ioe);
			} // Remove prepared file
			fWriter= null;
			file.delete();
		}
	}

	//Creates the folder if not already present
	public boolean initializeFolder()
	{
		if(isSDCardPresent())
		{
			fDirPath=Environment.getExternalStorageDirectory().getAbsolutePath() +"/AudioSensData";
			File folder = new File(fDirPath);
			if (!folder.exists()) 
			{
				if(!folder.mkdir())
				{
					return false;
				}
			}
		}
		return true;
	}

	public boolean isInitialized()
	{
		return initialized;
	}

	public boolean hasPermanentlyFailed()
	{
		return permanentFailure;
	}

	private boolean isSDCardPresent()
	{
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

	private void initializeAudioParameters()
	{
		//Initializing Audio Parameters
		if (AudioSensConfig.ENCODINGTYPE == AudioFormat.ENCODING_PCM_16BIT)
			bSamples = 16;
		else
			bSamples = 8;

		if (AudioSensConfig.CHANNELCONFIG == AudioFormat.CHANNEL_CONFIGURATION_MONO)
			noChannels = 1;
		else
			noChannels = 2;
	}

}
