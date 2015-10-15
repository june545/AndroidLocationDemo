package com.gps.service;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

public class LocationProviderUtil {

	/**
	 * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
	 * 
	 * @param context
	 * @return true 表示开启
	 */
	public static final boolean isOPen(final Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		// 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
		boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
		boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (gps || network) {
			return true;
		}
		return false;
	}

	/**
	 * 检测设备的位置功能是否开启
	 */
	public static boolean deviceLocationOpened(Context context) {
		return isGpsEnabled(context) || isNetworkEnabled(context);
	}

	/**
	 * @return true if GPS location is opened,or false
	 */
	public static boolean isGpsEnabled(Context context) {
		String str1 = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		Log.v("GPS", str1);
		if (str1 != null) {
			return str1.contains(LocationManager.GPS_PROVIDER);
		} else {
			return false;
		}
	}

	/**
	 * @return true if network location is opened,or false
	 */
	public static boolean isNetworkEnabled(Context context) {
		String str1 = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		Log.v("GPS", str1);
		if (str1 != null) {
			return str1.contains(LocationManager.NETWORK_PROVIDER);
		} else {
			return false;
		}
	}

	/**
	 * 若GPS没有打开，则打开GPS ,enable GPS。 不管用
	 */
	public static void enableGPS(Context context) {
		try {
			if (android.os.Build.VERSION.SDK_INT >= 8) {
				if (!android.provider.Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER)) {
					android.provider.Settings.Secure.setLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER, true);
				}
			} else {
				if (!isGpsEnabled(context))
					toggleGPS(context);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开启GPS ,设置GPS开启
	 */
	private static void toggleGPS(Context context) {
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}
}
