package edu.ucla.cens.audiosens.helper;

public class HammingWindow 
{

	public double[] window;
	public int n;

	public HammingWindow(int windowSize)
	{
		n = windowSize;

		// Make a Hamming window
		window = new double[n];
		for(int i = 0; i < n; i++)
		{
			window[i] = 0.54 - 0.46*Math.cos(2*Math.PI*(double)i/((double)n-1));
		}
	}

	public void applyWindow(double[] buffer)
	{
		for (int i = 0; i < n; i ++)
		{
			buffer[i] *= window[i];
		}
	}

}
