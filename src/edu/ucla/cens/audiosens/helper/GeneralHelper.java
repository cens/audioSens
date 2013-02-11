package edu.ucla.cens.audiosens.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class GeneralHelper 
{
	public static Date dateFromHourMin(final String hhmm)
	{
	    if (hhmm.matches("^[0-2][0-4]:[0-5][0-9]$"))
	    {
	        final String[] hms = hhmm.split(":");
	        final GregorianCalendar gc = new GregorianCalendar();
	        gc.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hms[0]));
	        gc.set(Calendar.MINUTE, Integer.parseInt(hms[1]));
	        gc.set(Calendar.SECOND, 0);
	        gc.set(Calendar.MILLISECOND, 0);
	        gc.setTimeZone(TimeZone.getDefault());
	        return gc.getTime();
	    }
	    else
	    {
	        throw new IllegalArgumentException(hhmm + " is not a valid time, expecting HH:MM format");
	    }
	}

}
