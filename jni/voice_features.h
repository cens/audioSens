#include "kiss_fftr.h"
#include <jni.h>

#define FRAME_LENGTH 256
#define HALF_FRAME_LENGTH 128
#define FFT_LENGTH 129 //FRAME_LENGTH/2+1
#define PI 3.14159265
#define REL_SPEC_WINDOW 200
#define NOISE_LEVEL 420   // == (0.01^2 * 32768^2) / 256
#define NOISE_LEVEL_RIGHT 420

#define SAFE_DELETE(a)  if (a)  {     \
									free(a); \
									a = NULL; \
									}

//configurations
extern kiss_fftr_cfg cfgFwd;
extern kiss_fftr_cfg cfgInv;

//shared variable
extern int numAcorrPeaks, maxAcorrPeakLag;
extern float maxAcorrPeakVal;
extern kiss_fft_cpx *fftx;
extern jshort buf[256];
extern float *normalizedData;
extern double *acorrPeakValueArray;
extern double *acorrPeakLagValueArray;



void normalize_data();
void computeHamming();
void computePowerSpec(kiss_fft_cpx*,kiss_fft_scalar*,int);
void computeMagnitudeSpec(kiss_fft_scalar*,kiss_fft_scalar*,int);
void computeHammingFactors();
double computeEnergy(const kiss_fft_scalar *powerSpec,int len);
void computeSpectralEntropy2(kiss_fft_scalar* magnitudeSpec_l,int len);
void whitenPowerSpectrumToCpx(const kiss_fft_scalar *powerSpec, kiss_fft_cpx *out, int energy, int len);
void computeAutoCorrelationPeaks2(const kiss_fft_scalar* powerSpec_l, kiss_fft_cpx* powerSpecCpx_l, int NOISE_01_l, int len);
void findPeaks(const float *in, int length, int *numPeaks, float *maxPeakVal, int *maxPeakLag);
void normalizeAcorr(const float *in, float *out, int outLen);
void destroyVoicedFeaturesFunction();
void initVoicedFeaturesFunction();





