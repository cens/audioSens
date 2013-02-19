package edu.ucla.cens.audiosens.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import edu.ucla.cens.audiosens.config.AudioSensConfig;
import edu.ucla.cens.audiosens.helper.FFT;
import edu.ucla.cens.audiosens.helper.FeaturesList;
import edu.ucla.cens.audiosens.helper.HammingWindow;
import edu.ucla.cens.audiosens.helper.Matrix;

public class MFCCProcessor extends BaseProcessor 
{
	public static final String LOGTAG = "MFCCProcessor";
	
	short[] data_short;

	private static int FFTSIZE = AudioSensConfig.FRAMELENGTH * 8;
	private static int NUMCOEFFS = 12;
	private static int MELBANDS = 20;
	
	private static double minMelFreq = 0;
	private static double maxMelFreq = 4000;
	private static double lifterExp = 0.6;
	private int numCoeffs;
	private int melBands;
	private int numFreqs;
	private double sampleRate;
	private Matrix melWeights = null;
	private Matrix dctMat = null;
	private double[] lifterWeights;
	
	private HammingWindow window;
	private FFT fft;
	double fftBufferR[];
	double fftBufferI[];
	double[] ceps;
	Matrix powerSpec;
	Matrix aSpec;
	Matrix logMelSpec;
	Matrix melCeps;
	
	//temp intermediate variables
	double[] aSpec_inter;
	double[] melCeps_inter;
	String[] mfccName;
	Integer tempInt;
	
	public MFCCProcessor() 
	{
		super();
		init();
	}

	@Override
	public void process(Object data, HashMap<String, String> options) 
	{
		
		data_short = (short[])data;
		
		// Frequency analysis
		Arrays.fill(fftBufferR, 0);
		Arrays.fill(fftBufferI, 0);

		// Convert audio buffer to doubles
		for (int i = 0; i < data_short.length; i++)
		{
			fftBufferR[i] = data_short[i];
		}

		// In-place windowing
		window.applyWindow(fftBufferR);

		// In-place FFT
		fft.fft(fftBufferR, fftBufferI);
		
		for (int i = 0; i < numFreqs; i ++)
		{
			powerSpec.A[i][0] = fftBufferR[i]*fftBufferR[i] + fftBufferI[i]*fftBufferI[i];
		}

		// melWeights - melBands x numFreqs
		// powerSpec  - numFreqs x 1
		// melWeights*powerSpec - melBands x 1
		// aSpec      - melBands x 1
		// dctMat     - numCoeffs x melBands
		// dctMat*log(aSpec) - numCoeffs x 1

		aSpec = melWeights.timesWithResult(powerSpec, aSpec, aSpec_inter);
		
		for (int i = 0; i < melBands; i ++)
		{
			logMelSpec.A[i][0] = Math.log(aSpec.A[i][0]);
		}

		melCeps = dctMat.timesWithResult(logMelSpec, melCeps, melCeps_inter);
		

		for (int i = 0; i < numCoeffs; i ++)
		{
			ceps[i] = lifterWeights[i]*melCeps.A[i][0];
		}
		
		
		for(int i = 0; i < NUMCOEFFS; i++)
		{
			addResult(mfccName[i], ceps[i]);
		}
	}

	@Override
	public void initializeResults()
	{
		for(int i = 0; i < NUMCOEFFS; i++)
		{
			results.put(FeaturesList.MFCC + i, new ArrayList<Double>());
		}
	}
	
	@Override
	public void setName() 
	{
		setName(FeaturesList.MFCC);
	}

	private void init()
	{
		int fftSize = FFTSIZE;
		this.numCoeffs = NUMCOEFFS;
		this.melBands = MELBANDS;
		this.sampleRate = AudioSensConfig.FREQUENCY;
		
		fft = new FFT(fftSize);
		fftBufferR = new double[fftSize];
    	fftBufferI = new double[fftSize];
    	
    	window = new HammingWindow(fftSize);
		
		// Precompute mel-scale auditory perceptual spectrum
		melWeights = new Matrix(melBands, fftSize, 0);

		// Number of non-redundant frequency bins
		numFreqs = fftSize/2 + 1;

		double fftFreqs[] = new double[fftSize];
		for (int i = 0; i < fftSize; i ++)
		{
			fftFreqs[i] = (double)i/(double)fftSize*this.sampleRate;
		}

		double minMel = fhz2mel(minMelFreq);
		double maxMel = fhz2mel(maxMelFreq);

		double binFreqs[] = new double[melBands + 2];
		for (int i = 0; i < melBands + 2; i ++)
		{
			binFreqs[i] = fmel2hz(minMel + (double)i/((double)melBands + 1.0) * (maxMel - minMel));
		}

		for (int i = 0; i < melBands; i ++)
		{
			for (int j = 0; j < fftSize; j ++)
			{
				double loSlope = (fftFreqs[j] - binFreqs[i])/(binFreqs[i+1] - binFreqs[i]);
				double hiSlope = (binFreqs[i+2] - fftFreqs[j])/(binFreqs[i+2] - binFreqs[i+1]);
				melWeights.A[i][j] = Math.max(0, Math.min(loSlope, hiSlope));
			}
		}

		// Keep only positive frequency parts of Fourier transform
		melWeights = melWeights.getMatrix(0, melBands - 1, 0, numFreqs - 1);

		// Precompute DCT matrix
		dctMat = new Matrix(numCoeffs, melBands, 0);
		double scale = Math.sqrt(2.0/melBands);
		for (int i = 0; i < numCoeffs; i ++)
		{
			for (int j = 0; j < melBands; j ++)
			{
				double phase = j*2 + 1;
				dctMat.A[i][j] = Math.cos((double)i*phase/(2.0*(double)melBands)*Math.PI)*scale;
			}
		}
		double root2 = 1.0/Math.sqrt(2.0);
		for (int j = 0; j < melBands; j ++)
		{
			dctMat.A[0][j] *= root2;
		}

		// Precompute liftering vector
		lifterWeights = new double[numCoeffs];
		lifterWeights[0] = 1.0;
		for (int i = 1; i < numCoeffs; i ++)
		{
			lifterWeights[i] = Math.pow((double)i, lifterExp);
		}
		
		powerSpec = new Matrix(numFreqs, 1);
		logMelSpec = new Matrix(melBands, 1);
		ceps = new double[numCoeffs];
		aSpec_inter = new double[melWeights.getColumnDimension()];
		melCeps_inter = new double[dctMat.getColumnDimension()];
		tempInt = new Integer(0);
		mfccName = new String[NUMCOEFFS];
		for(int i=0; i<NUMCOEFFS; i++)
		{
			mfccName[i] = FeaturesList.MFCC + i;
		}
	}

	private double fmel2hz(double mel)
	{
		return 700.0*(Math.pow(10.0, mel/2595.0) - 1.0);
	}

	private double fhz2mel(double freq)
	{
		return 2595.0*Math.log10(1.0 + freq/700.0);
	}

	@Override
	public void summarize() {
		// TODO Auto-generated method stub
		
	}

}
