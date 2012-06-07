package org.nilis.utils.device_data;

import java.util.Timer;
import java.util.TimerTask;

import org.nilis.utils.data.CancellableDataProvider;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class LocationProvider implements CancellableDataProvider<String, Location> {

	protected static final long TIME_TO_DETECT_LOCATION_ATTEMPT_IN_MS = 30000;
	protected static final Criteria LOCATION_MANAGER_CRITERIA;
	static {
		LOCATION_MANAGER_CRITERIA = new Criteria();
		LOCATION_MANAGER_CRITERIA.setAccuracy(Criteria.ACCURACY_FINE);
		LOCATION_MANAGER_CRITERIA.setAltitudeRequired(false);
		LOCATION_MANAGER_CRITERIA.setBearingRequired(false);
		LOCATION_MANAGER_CRITERIA.setCostAllowed(true);
		LOCATION_MANAGER_CRITERIA.setPowerRequirement(Criteria.POWER_LOW);
	}

	protected Context context = null;
	protected LocationManager locationManager = null;
	protected Timer timer = new Timer();
	WakeLock wakeLock = null;
	protected LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(final String provider, final int status, final Bundle extras) {
			// do nothing
		}

		@Override
		public void onProviderEnabled(final String provider) {
			// do nothing
		}

		@Override
		public void onProviderDisabled(final String provider) {
			// do nothing
		}

		@Override
		public void onLocationChanged(final Location location) {
			// do nothing
		}
	};

	public LocationProvider(final Context contextToUse) {
		context = contextToUse;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DISPLAY LOCATION");
	}

	@Override
	public void get(final String tag, final OnDataListener<String, Location> listener) {
		if (listener == null || context == null) {
			return;
		}

		final String providerType = locationManager.getBestProvider(LOCATION_MANAGER_CRITERIA, true);
		if (providerType != null) {
			// D.r("GET LOCATION");
			getLocationFromProviderOfSpecifiedType(providerType, listener);
		}
	}

	@Override
	public void cancelGet(final String tag) {
		timer.cancel();
		deinitLocationManager();
	}

	protected void initLocationManager(final String providerType) {
		locationManager.requestLocationUpdates(providerType, 0, 0, locationListener, context.getMainLooper());
	}

	protected void deinitLocationManager() {
		if (locationManager != null) {
			locationManager.removeUpdates(locationListener);
		}
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	protected void getLocationFromProviderOfSpecifiedType(final String providerType,
			final OnDataListener<String, Location> listener) {
		final Location location = locationManager.getLastKnownLocation(providerType);
		if (location == null) {
			initLocationManager(providerType);
			wakeLock.acquire();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					final Location loc = locationManager.getLastKnownLocation(providerType);
					if (loc != null) {
						listener.onDataReceived(null, loc);
					} else {
						if (providerType == LocationManager.NETWORK_PROVIDER) {
							listener.onDataFailed(null, new Exception("No location retrieved"));
						} else {
							// D.r("trying to use network provider to get location");
							getLocationFromProviderOfSpecifiedType(LocationManager.NETWORK_PROVIDER, listener);
						}
					}
					deinitLocationManager();
				}
			}, TIME_TO_DETECT_LOCATION_ATTEMPT_IN_MS);
		} else {
			listener.onDataReceived(null, location);
			deinitLocationManager();
		}
	}
}
