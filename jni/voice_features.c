#include <stdlib.h>
#include <math.h>
//#include "filehandler/filehandler.h"
#include "voice_features.h"
#include "kiss_fftr.h"
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>


#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "JNI_DEBUGGING", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,   "JNI_DEBUGGING", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "JNI_DEBUGGING", __VA_ARGS__)


//**********************************************************************************
//
// 	GLOBAL VARIABLES
//
//**********************************************************************************

//increment values
int i,j;
double *unnormed_sum_spec=NULL;//[FFT_LENGTH];
double *dummy=NULL;

//auto correlation variables
float *acorr=NULL;
float *normalizedAcorr=NULL; //[FRAME_LENGTH/2];
int numAcorrPeaks, maxAcorrPeakLag;
float maxAcorrPeakVal;

//spectral entropy variables
double spectral_entropy;
double rel_spectral_entropy;
float divider_spec = 0.0;
int no_of_samples=0;
double **prev_sum_spec=NULL;//[FFT_LENGTH];
double **prev_spec=NULL;//[FFT_LENGTH];
double *unnormed_mean_spec=NULL;//[FFT_LENGTH];
//double *unnormed_sum_spec=NULL;//[FFT_LENGTH];
double *normed_mean_spec=NULL;//[FFT_LENGTH];
double *norm_spec=NULL;//[FFT_LENGTH];

//configurations
kiss_fftr_cfg cfgFwd;
kiss_fftr_cfg cfgInv;


//others
kiss_fft_cpx *fftx=NULL;//[FFT_LENGTH];
double sum_full_data;
double sum_full_data_squared;
jshort buf[256];
jdouble mean_full_data;
float *normalizedData=NULL;//[FRAME_LENGTH];
double *factorsHanning=NULL;
double *factorsHamming=NULL;
float *dataHanning=NULL;
float *dataHamming=NULL;
double *acorrPeakLagValueArray=NULL;
double *acorrPeakValueArray=NULL;

//new noise levels
float noise_levels_squared[] = {313323.3139,3447421.4631,849487.3317,4970466.8673,113357.2991,5555636.7655,1855021.6219,895606.1497,3065567.8316,776335.2660,854274.0327,1090088.0994,3019730.0877,3655844.2966,236999.0142,4304609.4843,6405226.2696,1843092.3695,9173316.6939,21630828.8307,16263388.4042,11404754.1548,19941196.6663,1457255.9371,18168342.4730,42052289.2467,44042325.6875,32247722.5981,2011676.3041,6074299.4380,5882211.8827,9995846.5714,7108377.8073,9648296.5913,7957625.2395,4869497.9966,640651.8908,6916802.0686,2757616.1853,4164544.5107,12342580.5526,13503568.5890,13845908.3417,9493927.5368,3114302.4080,2392750.0994,8492050.7365,10400196.9471,1305503.7668,1086534.2132,4764130.5073,16487630.4039,8986034.1857,13336078.2968,31309463.3870,10731556.8386,1014672.1116,34366201.4782,50240615.6186,21330372.7745,24961380.5634,10249259.9054,30486819.3726,16894792.6252,7039800.0114,12147734.2424,2267833.3929,902650.3518,12137600.2109,14307545.3700,5672974.7581,8532034.5470,2681650.1898,2414907.9629,18043696.6965,6332893.9686,4360285.3629,25501331.0764,11391750.0371,21282070.4616,16918551.0222,5188720.3195,1865565.8037,5893415.0590,33805262.5840,30592159.4570,7335110.9880,14156640.2657,4487435.8089,988106.6597,728928.4012,8288801.8810,28150424.8026,32061833.2068,11747452.8986,575020.8570,6235905.0577,10763087.3341,8611935.2125,2721796.2762,11942092.8518,10560377.4934,5342085.1527,8831059.9619,16781420.4441,12025451.2722,6820551.0588,8460722.3187,3395084.4233,1645975.5070,9650400.4853,21628000.1132,12631230.1225,19762860.7356,2021392.2510,174910.5152,2625306.3453,7631385.1782,542882.9105,131875.3727,9141038.7323,22794483.8868,3463261.7127,2486781.1943,22100238.5963,32082266.0727,3480147.2106,5468995.4561};
int indexx;
int prevIndex;




//**********************************************************************************
//
// 	initialize voiced features values
//
//**********************************************************************************
void initVoicedFeaturesFunction()
{

	cfgFwd = kiss_fftr_alloc(FRAME_LENGTH,0, NULL, NULL);
	cfgInv = kiss_fftr_alloc(FRAME_LENGTH,1, NULL, NULL);

	//dummy = (double *) malloc(FRAME_LENGTH * sizeof(double));
	//SAFE_DELETE(dummy);

	//SAFE_DELETE(unnormed_sum_spec);


	//initialization
	unnormed_sum_spec = (double *) malloc(FFT_LENGTH * sizeof(double));

	//acorr =  (float *) malloc(HALF_FRAME_LENGTH * sizeof(float));
	acorr =  (float *) malloc(FRAME_LENGTH * sizeof(float));
	normalizedAcorr = (float *) malloc((HALF_FRAME_LENGTH) * sizeof(float));

	norm_spec = (double *) malloc(FFT_LENGTH * sizeof(double));

	//make a bigger prev_sum_spec
	prev_sum_spec = (double **) malloc(REL_SPEC_WINDOW * sizeof(double *));
	for(i=0;i<REL_SPEC_WINDOW;i++)
		prev_sum_spec[i] = (double *) malloc(FFT_LENGTH * sizeof(double));
	for(i=0;i<REL_SPEC_WINDOW;i++)//initialize to zero
		for(j=0;j<FFT_LENGTH;j++)
			prev_sum_spec[i][j] = 0.0;

	prev_spec = (double **) malloc(REL_SPEC_WINDOW * sizeof(double *));
	for(i=0;i<REL_SPEC_WINDOW;i++)
		prev_spec[i] = (double *) malloc(FFT_LENGTH * sizeof(double));
	for(i=0;i<REL_SPEC_WINDOW;i++)//initialize to zero
		for(j=0;j<FFT_LENGTH;j++)
			prev_spec[i][j] = 0.0;

	unnormed_mean_spec = (double *) malloc(FFT_LENGTH * sizeof(double));
	//unnormed_sum_spec = (double *) malloc(FFT_LENGTH * sizeof(double));
	normed_mean_spec = (double *) malloc(FFT_LENGTH * sizeof(double));

	fftx = (kiss_fft_cpx *) malloc(FFT_LENGTH * sizeof(kiss_fft_cpx));
	normalizedData = (float *) malloc(FRAME_LENGTH * sizeof(float));
	factorsHanning = (double *) malloc(FRAME_LENGTH * sizeof(double));
	factorsHamming = (double *) malloc(FRAME_LENGTH * sizeof(double));
	dataHanning = (float *) malloc(FRAME_LENGTH * sizeof(float));
	dataHamming = (float *) malloc(FRAME_LENGTH * sizeof(float));
	acorrPeakValueArray = (double *) malloc(HALF_FRAME_LENGTH * sizeof(double));
	acorrPeakLagValueArray = (double *) malloc(HALF_FRAME_LENGTH * sizeof(double));


	for (i = 0; i < FFT_LENGTH; i++) {
		//*(unnormed_sum_spec+i) = 0.0;
		unnormed_sum_spec[i] = 0.0;
	}


	//computing hamming factors
	computeHammingFactors();

	//make the index for reuse
	indexx = 0;

}


//**********************************************************************************
//
// 	destroy voiced features values
//
//**********************************************************************************
void destroyVoicedFeaturesFunction()
{

	//free memory
	//SAFE_DELETE(norm_spec);
	//SAFE_DELETE(prev_spec);


	//free
	//


	SAFE_DELETE(unnormed_sum_spec);

	SAFE_DELETE(acorr);


	SAFE_DELETE(normalizedAcorr);

	SAFE_DELETE(norm_spec);

	//delete spectral entropy
	for(i = 0; i < REL_SPEC_WINDOW; i++)
		free(prev_sum_spec[i]);
	free(prev_sum_spec);


	SAFE_DELETE(unnormed_mean_spec);
	SAFE_DELETE(normed_mean_spec);

	SAFE_DELETE(fftx);
	SAFE_DELETE(normalizedData);
	SAFE_DELETE(factorsHanning);
	SAFE_DELETE(factorsHamming);
	SAFE_DELETE(dataHanning);
	SAFE_DELETE(dataHamming);
	SAFE_DELETE(acorrPeakValueArray);
	SAFE_DELETE(acorrPeakLagValueArray);
	/*
	 */
	free(cfgFwd);
	free(cfgInv);


}



//**********************************************************************************
//
// 	computes the autorcorrelation values
//
//**********************************************************************************
void computeAutoCorrelationPeaks2(const kiss_fft_scalar* powerSpec_l, kiss_fft_cpx* powerSpecCpx_l, int NOISE_01_l, int len)
{
	whitenPowerSpectrumToCpx(powerSpec_l, powerSpecCpx_l, NOISE_01_l, len);


	kiss_fftri(cfgInv, powerSpecCpx_l, acorr);


	normalizeAcorr(acorr, normalizedAcorr, HALF_FRAME_LENGTH);


	//find peaks using autocorrealation values
	findPeaks(normalizedAcorr, HALF_FRAME_LENGTH,
			&numAcorrPeaks,
			&maxAcorrPeakVal,
			&maxAcorrPeakLag);


}



//**********************************************************************************
//
// 	computes spectral entropy and relative spectral entropy values
//
//**********************************************************************************
void computeSpectralEntropy2(kiss_fft_scalar* magnitudeSpec_l,int len)
{

	double sum_spec = 0;



	//sum data for normalizing later
	for(i = 0; i< len; i++){
		sum_spec = sum_spec + magnitudeSpec_l[i];
	}

	//normalized spec
	spectral_entropy = 0;
	rel_spectral_entropy = 0;
	divider_spec = 0.0;

	if(no_of_samples <= REL_SPEC_WINDOW){
		no_of_samples++; // the value will fix at "REL_SPEC_WINDOW+1"
		divider_spec = no_of_samples;
	}
	else{ // the value will fix at "REL_SPEC_WINDOW+1"
		divider_spec = REL_SPEC_WINDOW;
	}


	if(indexx!=0)
		prevIndex = indexx-1;
	else
		prevIndex = REL_SPEC_WINDOW - 1;//means go back to the last index

	//spectral entropy and saving moving average code
	for(i = 0; i< FFT_LENGTH; i++){

		norm_spec[i] = magnitudeSpec_l[i]/(sum_spec + 0.00001); //making a distribution


		//no initialization because initially it will not be used
		//before (no_of_samples > REL_SPEC_WINDOW) is true
		prev_sum_spec[indexx][i] =  prev_sum_spec[prevIndex][i]  + magnitudeSpec_l[i] - prev_spec[indexx][i];//keep the previous
		prev_spec[indexx][i] = magnitudeSpec_l[i];

		//if(no_of_samples > REL_SPEC_WINDOW){
		//will come here for the 501th sample but "no_of_samples=500"
		unnormed_sum_spec[i] = prev_sum_spec[indexx][i];
		//}




		//unnormed_sum_spec[i] = unnormed_sum_spec[i] + magnitudeSpec_l[i]; //magnitude is added to the all the time, used to compute the mean

		//spectral entropy
		if(norm_spec[i] != 0)
		{
			spectral_entropy = spectral_entropy - norm_spec[i]*log(norm_spec[i]);
		}



	}

	//I want to add current results one index ahead so that I can find it in later iteration
	indexx = (indexx+1)%REL_SPEC_WINDOW;

	//normalize mean spectral entropy
	sum_spec = 0;
	for(i=0;i<FFT_LENGTH;i++){
		unnormed_mean_spec[i] = unnormed_sum_spec[i]/divider_spec;
		sum_spec+=unnormed_mean_spec[i];
	}

	//realative spectral entropy
	for(i=0;i<FFT_LENGTH;i++){
		normed_mean_spec[i] = unnormed_mean_spec[i]/(sum_spec + 0.00001);
		if(normed_mean_spec[i] < 0.0000001)
			normed_mean_spec[i] = 0.000001;

		if(norm_spec[i] != 0)
			rel_spectral_entropy = rel_spectral_entropy + norm_spec[i]*(log( norm_spec[i]) - log(normed_mean_spec[i]));
	}

	//Debug code
	//char buffer [500];
	//sprintf (buffer, "FrameNO:%d RSE:%f NrmedMeanSPec %f ", indexx, rel_spectral_entropy, sum_spec);
	//LOGE(buffer);
}


//**********************************************************************************
//
// 	computed energy or loudness
//
//**********************************************************************************
double computeEnergy(const kiss_fft_scalar *powerSpec2,int len)
{
	double r=0;

	for(i=0; i<len; i++){
		r += powerSpec2[i];
	}
	return r;
}


//**********************************************************************************
//
// 	computed margnitude of the fft values
//	needed for computing spectral entropy and relative spectral entropy values
//
//**********************************************************************************
void computeMagnitudeSpec(kiss_fft_scalar* src,kiss_fft_scalar* dest,int len)
{
	for(j=0; j<len; j++){
		dest[j] = sqrt(src[j]);
	}
}

//**********************************************************************************
//
// 	normalize autocorrelation values to stay between 1 or -1
//
//**********************************************************************************
void
normalizeAcorr(const float *in, float *out, int outLen)
{
	int i;

	for(i=0; i<outLen; i++){
		out[i] = (float) ((float)in[i] / in[0]);
	}

}

//**********************************************************************************
//
// 	normalize autocorrelation values to stay between 1 or -1
//
//**********************************************************************************
void computePowerSpec(kiss_fft_cpx* fft_l,kiss_fft_scalar* dest,int len)
{
	for(j=0; j<len; j++){
		dest[j] = fftx[j].r * fftx[j].r + fftx[j].i * fftx[j].i;
	}
}


//**********************************************************************************
//
// 	adds low power white noise to the signal to counter against low power peridic humming noise
//
//**********************************************************************************
void
whitenPowerSpectrumToCpx(const kiss_fft_scalar *powerSpec, kiss_fft_cpx *out, int energy, int len)
{

	for(j=0; j<len; j++){
		out[j].r = powerSpec[j] + 2*noise_levels_squared[j]; //energy;
		out[j].i = 0;
	}
}



//**********************************************************************************
//
// 	zero  mean the audio signal
//
//**********************************************************************************
void normalize_data() //zero mean data
{
	//normalize data
	////////// NORMALIZE DATA //////////////

	sum_full_data = 0;
	sum_full_data_squared = 0;

	for(i = 0; i<FRAME_LENGTH; i++){
		sum_full_data = sum_full_data + buf[i];///(2^15);
	}

	mean_full_data = sum_full_data/FRAME_LENGTH;

	for (i = 0; i < FRAME_LENGTH; i++) {
		normalizedData[i] = buf[i] - mean_full_data;//zero mean the data

	}

}

//**********************************************************************************
//
// 	different windowing for the audio frames
//
//**********************************************************************************
void computeHanningFactors() {

	j = 0;
	for (i = 1; i <= FRAME_LENGTH; i++) { //calculate the hanning window
		factorsHanning[j] = 0.5 * (1 - cos(2.0 * PI * i / (FRAME_LENGTH + 1)));
		j++;
	}
}

void computeHammingFactors() {

	double denom = (double)FRAME_LENGTH-1;
	for (i = 0; i < FRAME_LENGTH; i++) { //calculate the hanning window
		factorsHamming[i] = 0.54 -(0.46 * cos( 2.0 * PI * ((double)i / denom) ) );
	}


}

void computeHanning() //apply hanning window
{

	for (j = 0; j < FRAME_LENGTH; j+=1) { //calculate the hanning window
		dataHanning[j] = factorsHanning[j]*normalizedData[j];
	}
}

void computeHamming() //apply hamming window
{
	for (j = 0; j < FRAME_LENGTH; j+=1) { //calculate the hanning window
		dataHamming[j] = factorsHamming[j]*normalizedData[j];
	}
}

//**********************************************************************************
//
// 	find the peak values and corresponding lags of autocorrelation values
//
//**********************************************************************************
void findPeaks(const float *in, int length, int *numPeaks, float *maxPeakVal, int *maxPeakLag)
{

	int i;
	float maxPeak = 0;
	int maxPeakIdx = 0;

	float nonInitialMax = 0;
	int maxIdx = 0;
	float gMin = 0;

	float lastVal;

	int pastFirstZeroCrossing = 0;

	int tn = 0;

	// start with (and thus skip) 0 lag
	lastVal = in[0];

	float localMaxPeakValue = 0;
	int localMaxPeakIndex = 0;

	for(i=1; i<length; i++){



		if(pastFirstZeroCrossing){
			// are we in a peak?
			if(lastVal >= 0 && in[i] >=0){
				// then check for new max

				if(in[i] > localMaxPeakValue){


					localMaxPeakValue = in[i];
					localMaxPeakIndex = i;


					if(in[i] > maxPeak){
						maxPeak = in[i];
						maxPeakIdx = i;
					}
				}

				// did we just leave a peak?
			}else if(lastVal >=0 && in[i] < 0 && maxPeak > 0){

				// count the last peak
				acorrPeakValueArray[tn] = (double)localMaxPeakValue;
				acorrPeakLagValueArray[tn] = (double)localMaxPeakIndex;
				tn++;

			}else if(lastVal < 0 && in[i] >= 0){
				//set the local acorr max to zero
				localMaxPeakValue = in[i];
				//localMaxPeakIndex = 0;

				// then check for new max
				if(in[i] > maxPeak){
					maxPeak = in[i]; //it does only need non-initial maxpeak, so it not resetting the peak value every time
					maxPeakIdx = i;
				}
			}
		}else{
			if(in[i] <= 0){
				pastFirstZeroCrossing = 1; //zero crossing is for initial peak (value always one)
			}
		}

		lastVal = in[i];

	}


	// set the return values
	*numPeaks = tn;

	*maxPeakVal = maxPeak;
	*maxPeakLag = maxPeakIdx;

}
