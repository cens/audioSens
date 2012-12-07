#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <math.h>
#include "kiss_fftr.h"
#include "voice_features.h"
//#include "voicing_parameters.h"
//#include "mvnpdf.h"
#include "viterbi.h"

//**********************************************************************************
//
// 	GLOBAL VARIABLES
//
//**********************************************************************************

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "JNI_DEBUGGING", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,   "JNI_DEBUGGING", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,    "JNI_DEBUGGING", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "JNI_DEBUGGING", __VA_ARGS__)



jint sum = 0;
char buffer [2500];
char temp_buffer [50];
int n;
double spec[FFT_LENGTH];
kiss_fft_cpx freq[FFT_LENGTH];
kiss_fft_cpx y[FRAME_LENGTH];
kiss_fft_cpx z[FRAME_LENGTH];
kiss_fft_cpx powerSpecCpx[FFT_LENGTH];
kiss_fft_scalar powerSpec[FFT_LENGTH];
kiss_fft_scalar magnitudeSpec[FFT_LENGTH];
double spectral_entropy;
double rel_spectral_entropy;
int divider;
double peak_vals[FRAME_LENGTH/2];
int peak_loc[FRAME_LENGTH/2];
//number of autocorrelations
int nacorr = (int)(FRAME_LENGTH/2);

//float normalizedAcorr[FRAME_LENGTH/2];
double comp[FRAME_LENGTH/2];
//float divider_spec = 0.0;

//features
double energy;
double relSpecEntr;
//-- In header: extern int numAcorrPeaks, maxAcorrPeakLag;
//-- In header: float maxAcorrPeakVal;
//double featuresValuesTemp[264 + LOOK_BACK_LENGTH];//(6 + 128 + 128 +  = 262) + 2 + LOOK_BACK_LENGTH
double featuresValuesTemp[9];//(6 + 128 + 128 +  = 262) + 2 + LOOK_BACK_LENGTH
double featureAndInference[2+LOOK_BACK_LENGTH];

double x[3];
int inferenceResult;


//**********************************************************************************
//
// 	initialization function to allocate memory for reuse later.
//
//**********************************************************************************
void Java_edu_ucla_cens_audiosens_processors_SpeechInferenceFeaturesProcessor_audioFeatureExtractionInit(JNIEnv* env, jobject javaThis) {


	initVoicedFeaturesFunction();

	//initialize viterbi
	viterbiInitialize();
}

//**********************************************************************************
//
// 	destroy function for the c code. Currently not called
//
//**********************************************************************************
void Java_edu_ucla_cens_audiosens_processors_SpeechInferenceFeaturesProcessor_audioFeatureExtractionDestroy(JNIEnv* env, jobject javaThis) {

	destroyVoicedFeaturesFunction();

	//kill viterbi
	viterbiDestroy();
}


//**********************************************************************************
//
// 	compute three features for voicing detection. Also a variable length autocorrelation values and
//	lags are stored in the returned double array
//
//**********************************************************************************
jdoubleArray Java_edu_ucla_cens_audiosens_processors_SpeechInferenceFeaturesProcessor_features(JNIEnv* env, jobject javaThis, jshortArray array) {
//void Java_edu_cornell_audioProbe_AudioManager_features(JNIEnv* env, jobject javaThis, jshortArray array) {

	(*env)->GetShortArrayRegion(env, array, 0, FRAME_LENGTH, buf);

	normalize_data();

	//apply window
	computeHamming();

	//computeFwdFFT
	kiss_fftr(cfgFwd, normalizedData, fftx);

	//compute power spectrum
	computePowerSpec(fftx, powerSpec, FFT_LENGTH);

	//compute magnitude spectrum
	computeMagnitudeSpec(powerSpec, magnitudeSpec, FFT_LENGTH);

	// compute total energy
	energy = computeEnergy(powerSpec,FFT_LENGTH) / FFT_LENGTH;

	//compute Spectral Entropy
	computeSpectralEntropy2(magnitudeSpec, FFT_LENGTH);

	//compute auto-correlation peaks
	computeAutoCorrelationPeaks2(powerSpec, powerSpecCpx, NOISE_LEVEL, FFT_LENGTH);

	//data output
	////return data as variable size array caused by variable autocorrelation information.
	//jdoubleArray featureVector = (*env)->NewDoubleArray(env,6 + 2*numAcorrPeaks + 2 + LOOK_BACK_LENGTH);
	jdoubleArray featureVector = (*env)->NewDoubleArray(env, 9);
	featuresValuesTemp[0] = numAcorrPeaks; //autocorrelation values
	featuresValuesTemp[1] = maxAcorrPeakVal;
	featuresValuesTemp[2] = maxAcorrPeakLag;
	featuresValuesTemp[3] = spectral_entropy;
	featuresValuesTemp[4] = rel_spectral_entropy;
	featuresValuesTemp[5] = energy;


	//gaussian distribution
	//test the gaussian distribution with some dummy values first
	x[0] = maxAcorrPeakVal;
	x[1] = numAcorrPeaks;
	x[2] = rel_spectral_entropy;
	inferenceResult = getViterbiInference(x,featureAndInference);

	featuresValuesTemp[6] = featureAndInference[0];
	featuresValuesTemp[7] = featureAndInference[1];
	featuresValuesTemp[8] = featureAndInference[2];

	//memcpy( featuresValuesTemp+6, featureAndInference, (2+LOOK_BACK_LENGTH)*sizeof(double) ); //observation probabilities, inferences

	//put auto correlation values in the string
	//memcpy( featuresValuesTemp+6+2+LOOK_BACK_LENGTH, acorrPeakValueArray, numAcorrPeaks*sizeof(double) );
	//memcpy( featuresValuesTemp+6+numAcorrPeaks+2+LOOK_BACK_LENGTH, acorrPeakLagValueArray, numAcorrPeaks*sizeof(double) );
	//(*env)->SetDoubleArrayRegion( env, featureVector, 0, 6 + numAcorrPeaks*2 + 2 + LOOK_BACK_LENGTH, (const jdouble*)featuresValuesTemp );
	(*env)->SetDoubleArrayRegion( env, featureVector, 0, 9, (const jdouble*)featuresValuesTemp );

	return featureVector;


}


