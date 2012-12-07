#include "viterbi.h"
#include "voicing_parameters.h"
#include "mvnpdf.h"
#include <math.h>
#include <stdlib.h>
#include <android/log.h>
#include <stdio.h>



#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,   "JNI_DEBUGGING", __VA_ARGS__)


int i,j,k;
double d[2][2];
double emissionVoiced;
double emissionUnvoiced;
double emissionProbabilities[2];
double *prevDmax=NULL;
double *tempDmax=NULL;
double *Dmax=NULL;
int **maximalPath=NULL;
int indexMaximalPath = 0;
int indexCurrentVteribiPath = 0;
double *currentVteribiPath = NULL;
//double *featureAndInference=NULL;
//char s[32];

void viterbiInitialize()
//************************************************************
//
//  a random initialization of viterbi will happen here. We
//	will assume that we have a randorm value of x to start with
//  let us make the x that is from a non-voice segment.
//	We will take the mean of unvoiced as the value of x
//  This means we have 1 as nominator [exp(0)].
//
//  Why setting non-voice at the start is not a problem?
//	Since later observation probability will dictate
//	how transition is going to look like
// 	initial value will not be a huge problem
//
//************************************************************
{
	prevDmax = (double*)malloc(2 * sizeof(double));
	double x[3] = { 0.3226, 14.1607, 0.2024 };

	emissionProbabilities[1] = computeMvnPdf(x,mean_voiced, inv_cov_voiced, denom_gauss_voiced);
	emissionProbabilities[0] = computeMvnPdf(x,mean_unvoiced, inv_cov_unvoiced, denom_gauss_unvoiced);

	for(j=0;j<2;j++) //only two classes, y_0
		prevDmax[j] = log(prior[j]) + emissionProbabilities[j];



	//initialization tempDmax
	tempDmax = (double*)malloc(2 * sizeof(double));
	Dmax = (double*)malloc(2 * sizeof(double));

	//initialize Maximal path
	indexMaximalPath = 0;

	//initialize the double array
	maximalPath = (int**)malloc(LOOK_BACK_LENGTH * sizeof(int*));
	for(i=0;i<LOOK_BACK_LENGTH;i++)
		maximalPath[i] = (int*)malloc(2 * sizeof(int));


	//initialize the double array
	currentVteribiPath = (double*)malloc(LOOK_BACK_LENGTH * sizeof(double));
	//featureAndInference = (double*)malloc((2 + LOOK_BACK_LENGTH) * sizeof(double));//first two are for probabilities

	//initalize maximalPath to -1

	for(i=0;i<LOOK_BACK_LENGTH;i++){
		currentVteribiPath[i] = 0.0;
		for(j=0;j<2;j++)
			maximalPath[i][j] = 0;
	}

}

void viterbiDestroy()
{
	free(prevDmax);
	prevDmax = NULL;

	free(tempDmax);
	tempDmax = NULL;

	free(Dmax);
	Dmax = NULL;


	for(i=0;i<LOOK_BACK_LENGTH;i++)
		free(maximalPath[i]);
	free(maximalPath);

	free(currentVteribiPath);
}


int getViterbiInference(double *x,double *featureAndInference){


	//emissionVoiced = computeMvnPdf(x,mean_voiced, inv_cov_voiced, denom_gauss_voiced);
	//emissionUnvoiced = computeMvnPdf(x,mean_unvoiced, inv_cov_unvoiced, denom_gauss_unvoiced);
	emissionProbabilities[1] = computeMvnPdf(x,mean_voiced, inv_cov_voiced, denom_gauss_voiced);
	emissionProbabilities[0] = computeMvnPdf(x,mean_unvoiced, inv_cov_unvoiced, denom_gauss_unvoiced);

	//compute d[j,k]
	for(j=0;j<2;j++){ //only two classes, y_t
		for(k=0;k<2;k++) // j,k = 0 means unvoiced, 1 means voiced, y_{t-1}
		{
			d[j][k] = transitionMatrix[j][k] + emissionProbabilities[j];
			tempDmax[k] = d[j][k] + prevDmax[k];
		}

		//find the max of tempDmax and store them in the path
		if(tempDmax[0] > tempDmax[1])
		{
			Dmax[j] = tempDmax[0];
			maximalPath[indexMaximalPath][j] = 0;
		}
		else
		{
			Dmax[j] = tempDmax[1];
			maximalPath[indexMaximalPath][j] = 1;
		}
	}



	//store Dmax to prevDmax for use later
	prevDmax[0] = Dmax[0];
	prevDmax[1] = Dmax[1];


	if(Dmax[0] > Dmax[1])
		currentVteribiPath[LOOK_BACK_LENGTH-1] = 0;
	else
		currentVteribiPath[LOOK_BACK_LENGTH-1] = 1;


	//
	for(i= LOOK_BACK_LENGTH-2,j=1; i>=0; i--,j++) //indexMaximalPath
	{
		//currentVteribiPath[i] = maximalPath[(indexMaximalPath-j)%LOOK_BACK_LENGTH][(int)currentVteribiPath[i+1]];
		//indexCurrentVteribiPath = (int)currentVteribiPath[i+1];//(LOOK_BACK_LENGTH+indexMaximalPath-j)%LOOK_BACK_LENGTH;
		//sprintf(s,"%d %d %d %d", (LOOK_BACK_LENGTH+indexMaximalPath-j)%LOOK_BACK_LENGTH,indexMaximalPath,(int)currentVteribiPath[i+1],indexCurrentVteribiPath);
		//LOGE(s);

		currentVteribiPath[i] = (double) maximalPath[(LOOK_BACK_LENGTH+indexMaximalPath-j)%LOOK_BACK_LENGTH][(int)currentVteribiPath[i+1]];

	}



	// a circular indexer for updating maximal path
	indexMaximalPath = (indexMaximalPath+1)%LOOK_BACK_LENGTH;

	//initial debug
	//no smoothing
	//if(emissionUnvoiced > emissionVoiced )
		//return 0;
	//else
		//return 1;

	featureAndInference[0] = emissionProbabilities[0];
	featureAndInference[1] = emissionProbabilities[1];
	memcpy( featureAndInference + 2, currentVteribiPath, LOOK_BACK_LENGTH*sizeof(double) ); //copy inference


	return 1;


}
