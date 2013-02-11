package edu.ucla.cens.audiosens.helper;

import android.view.View;
import android.view.ViewGroup;

public class UIHelper 
{
	/*
	 * Disables all elements in the viewGroup
	 */
	public static void enableDisableView(View view, boolean enabled) 
	{
	    view.setEnabled(enabled);
	    if ( view instanceof ViewGroup ) 
	    {
	        ViewGroup group = (ViewGroup)view;
	        for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) 
	        {
	            enableDisableView(group.getChildAt(idx), enabled);
	        }
	    }
	}
	
	/*
	 * Disables all elements in the viewGroup
	 */
	public static void hideShowView(View view, boolean visibility) 
	{
		if(visibility)
			view.setVisibility(View.VISIBLE);
		else
			view.setVisibility(View.GONE);
		
	    if ( view instanceof ViewGroup ) 
	    {
	        ViewGroup group = (ViewGroup)view;
	        for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) 
	        {
	        	hideShowView(group.getChildAt(idx), visibility);
	        }
	    }
	}

}
