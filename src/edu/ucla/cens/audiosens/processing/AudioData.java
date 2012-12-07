package edu.ucla.cens.audiosens.processing;

//class to store each frame of Audio
public class AudioData
{
	public short data[];
	public long timestamp;
	public enum Flag {NORMAL};
	private Flag flag;
	
	public AudioData(int frameStep)
	{
		data = new short[frameStep];
	}
	
	public AudioData(short[] data, long timestamp, Flag flag)
	{
		this.data=data;			
		this.timestamp = timestamp;
		this.flag = flag;
	}
	
	public void insert(short[] data_inp, long timestamp_inp, Flag flag_inp)
	{
		if(data != null && data.length==data_inp.length)
		{
			System.arraycopy(data_inp, 0, data, 0, data_inp.length);
		}
		else
		{
			data = data_inp.clone();
		}
		timestamp =  timestamp_inp;
		flag = flag_inp;
	}
}