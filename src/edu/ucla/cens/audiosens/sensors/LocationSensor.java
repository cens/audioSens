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
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;

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
		if(isBetterLocation(location, this.location))
			this.location = location;
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

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}
