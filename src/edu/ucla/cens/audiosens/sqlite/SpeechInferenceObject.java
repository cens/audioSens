package edu.ucla.cens.audiosens.sqlite;

public class SpeechInferenceObject 
{
	long id;
	String version;
	String data;
	int inference;
	int period;
	int duration;
	
	public SpeechInferenceObject()
	{
	}
	
	public SpeechInferenceObject(long id, String version, String data,
			int inference, int period, int duration) 
	{
		this.id = id;
		this.version = version;
		this.data = data;
		this.inference = inference;
		this.period = period;
		this.duration = duration;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getInference() {
		return inference;
	}

	public void setInference(int inference) {
		this.inference = inference;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
