package com.gps.service;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class MyLocationListener implements LocationListener {
	// 当前坐标
	private static Location	currentLocation;
	private UpdateLocation	updateLocation;

	public MyLocationListener(UpdateLocation updateLocation) {
		this.updateLocation = updateLocation;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location == null)
			return;
		// Called when a new location is found by the location provider.
		Log.v("GPSTEST", "Got New Location of provider:" + location.getProvider());
		if (currentLocation != null) {
			if (CheckLocationUtil.isBetterLocation(location, currentLocation)) {
				Log.v("GPSTEST", "It's a better location");
				currentLocation = location;
				showLocation(location);
			}
			else {
				Log.v("GPSTEST", "Not very good!");
			}
		}
		else {
			Log.v("GPSTEST", "It's first location");
			currentLocation = location;
			showLocation(location);
		}
		// 移除基于LocationManager.NETWORK_PROVIDER的监听器
		if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider())) {
			updateLocation.removeLocationListner(LocationService.LISTENER_TYPE_NETWORK);
		}

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	private void showLocation(Location location) {
		// 纬度
		Log.v("GPS_SERVICE", "Latitude:" + location.getLatitude());
		// 经度
		Log.v("GPS_SERVICE", "Longitude:" + location.getLongitude());
		// 精确度
		Log.v("GPS_SERVICE", "Accuracy:" + location.getAccuracy());
		// Location还有其它属性，请自行探索

		if (updateLocation != null) {
			updateLocation.update(location);
		}
		else {
			Log.e("MyLocationListener", ">>>updateLocation is null<<<");
		}
	}

	/**
	 * 供位置监听器调用
	 * 
	 */
	interface UpdateLocation {
		/**
		 * 刷新位置
		 */
		public void update(Location location);

		/**
		 * 移除位置监听
		 * 
		 * @param listenerType
		 *            {@link LocationService#LISTENER_TYPE_GPS} or
		 *            {@link LocationService#LISTENER_TYPE_NETWORK}
		 */
		public void removeLocationListner(int listenerType);
	}

}
