package edu.ucla.cens.audiosens.classifier;

import edu.ucla.cens.audiosens.helper.Logger;

public class ClassifierFactory 
{
	public static final String LOGTAG = "ClassifierFactory";
	
	public static BaseClassifier build(String classifierName)
	{
		try 
		{
			return (BaseClassifier) Class.forName("edu.ucla.cens.audiosens.classifier."+classifierName+"Classifier").newInstance();
		} 
		catch (InstantiationException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + classifierName + " : " + e);
			return null;
		} 
		catch (IllegalAccessException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + classifierName + " : " + e);
			return null;
		} 
		catch (ClassNotFoundException e) 
		{
			Logger.w(LOGTAG, "FactoryBuild error for " + classifierName + " : " + e);
			return null;
		}
	}
}
