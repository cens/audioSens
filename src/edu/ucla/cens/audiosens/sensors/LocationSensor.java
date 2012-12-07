package edu.ucla.cens.audiosens.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import edu.ucla.cens.audiosens.AudioSensService;
import edu.ucla.cens.audiosens.config.AudioSensConfig;

public class LocationSensor extends BaseSensor
{
	LocationManager locationManager;
	LocationListener locationListener;
	Location location;
	
	@Override
	public void init(AudioSensService obj) 
	{
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) obj.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new MyLocationListener();

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
												AudioSensConfig.LOCATION_MINTIME,
												AudioSensConfig.LOCATION_MINDISTANCE,
												locationListener);
		
		updateLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		initialized = true;
	}

	@Override
	public void destroy() 
	{
		if(locationManager != null)
		{
			locationManager.removeUpdates(locationListener);
		}
		initialized = false;
	}
	

	@Override
	public JSONObject getJsonResult() throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		if(location != null)
		{
			jsonObject.put("latitude", location.getLatitude());
			jsonObject.put("longitude", location.getLongitude());
			jsonObject.put("accuracy", location.getAccuracy());
			jsonObject.put("provider", location.getProvider());
		}
		return jsonObject;
	}
	
	private void updateLocation(Location location)
	{
		if(location != null)
		{
			this.location = location;
		}
	}
	
	class MyLocationListener implements LocationListener
	{
	    public void onLocationChanged(Location location) 
	    {
	    	updateLocation(location);
	    }

		@Override
		public void onProviderDisabled(String arg0) {}

		@Override
		public void onProviderEnabled(String arg0) {}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
	}

}
