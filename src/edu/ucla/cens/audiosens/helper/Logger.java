package edu.ucla.cens.audiosens.helper;

import android.util.Log;

public class Logger 
{
	private static String TAG = "audioSens";
	
	public static void i(String msg)
	{
		i(TAG,msg);
	}
	
	public static void i(String tag, String msg)
	{
		Log.i(tag,msg);
	}
	
	public static void v(String msg)
	{
		v(TAG,msg);
	}
	
	public static void v(String tag, String msg)
	{
		Log.v(tag,msg);
	}
	
	public static void w(String msg)
	{
		w(TAG,msg);
	}
	
	public static void w(String tag, String msg)
	{
		Log.w(tag,msg);
	}
	
	public static void e(String msg)
	{
		e(TAG,msg);
	}
	
	public static void e(String tag, String msg)
	{
		Log.e(tag,msg);
	}

	
	public static void d(String msg)
	{
		d(TAG,msg);
	}
	
	public static void d(String tag, String msg)
	{
		Log.d(tag,msg);
	}
}
