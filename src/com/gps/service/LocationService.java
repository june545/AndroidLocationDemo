package com.gps.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;

import com.gps.service.MyLocationListener.UpdateLocation;

/**
 * 监听gps坐标，取当前gps坐标，设定时间间隔记录gps
 */
public class LocationService {

	private Handler							handler	= new Handler();

	// 变量定义
	private static LocationManager			locationManager;

	private Location						currentLocation;

	/**
	 * 观察者列表 observers' list
	 */
	private static Vector<LocationObserver>	vector;

	/**
	 * 增加一个位置观察者 add an observer
	 * 
	 * @param observer
	 */
	public boolean addObserver(LocationObserver observer) {
		if (vector == null) {
			vector = new Vector<LocationService.LocationObserver>();
		}
		return vector.add(observer);
	}

	/**
	 * 删除一个指定位置观察者 delete a specific observer
	 * 
	 * @param observer
	 */
	public boolean removeObserver(LocationObserver observer) {
		if (vector != null) {
			return vector.removeElement(observer);
		}
		return false;
	}

	/**
	 * singleton instance
	 */
	private static LocationService	locationService;

	/**
	 * get LocationService instance
	 * 
	 * @param applicationContext
	 * @return
	 */
	public static synchronized LocationService getInstance(Context applicationContext) {
		if (locationService == null) {
			locationService = new LocationService(applicationContext);
		}
		return locationService;
	}

	private LocationService(Context context) {
		// get the LocationManager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		startService();
	}

	/**
	 * 开启位置服务
	 */
	public synchronized void startService() {
		registerLocationListener();
		locationTiming();
	}

	/**
	 * 停止位置服务
	 */
	public synchronized void stop() {
		if (timer != null) {
			timer.cancel();
		}
		if (locationManager != null) {
			removeLocationListener(LISTENER_TYPE_NETWORK);
			removeLocationListener(LISTENER_TYPE_GPS);
		}
		locationService = null;
	}

	private LocationListener	networkListener			= null;
	private static boolean		networkListenerAdded	= false;
	private LocationListener	gpsListener				= null;
	private static boolean		gpsListenerAdded		= false;

	/**
	 * 注册位置监听
	 */
	private synchronized void registerLocationListener() {
		if (hasLocateDevice(LocationManager.NETWORK_PROVIDER) && !networkListenerAdded && enableNetworkProvider) {
			networkListener = new MyLocationListener(updateLocation);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, networkListener);
			networkListenerAdded = true;
			System.out.println("networkListener has been added");
		}
		if (hasLocateDevice(LocationManager.GPS_PROVIDER) && !gpsListenerAdded) {
			gpsListener = new MyLocationListener(updateLocation);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsListener);
			gpsListenerAdded = true;
			System.out.println("gpsListener has been added");
		}
	}

	/**
	 * 移除位置监听
	 * 
	 * @param listenerType
	 *            {@link LocationService#LISTENER_TYPE_GPS} or
	 *            {@link LocationService#LISTENER_TYPE_NETWORK}
	 */
	private synchronized void removeLocationListener(int listenerType) {
		if (listenerType == LISTENER_TYPE_GPS && gpsListenerAdded) {
			locationManager.removeUpdates(gpsListener);
			gpsListenerAdded = false;
			System.out.println("gpsListener has been removed");
		}
		else if (listenerType == LISTENER_TYPE_NETWORK && networkListenerAdded) {
			locationManager.removeUpdates(networkListener);
			networkListenerAdded = false;
			System.out.println("networkListener has been removed");
		}
	}

	public static final int	LISTENER_TYPE_GPS		= 1;
	public static final int	LISTENER_TYPE_NETWORK	= 0;

	private UpdateLocation	updateLocation			= new UpdateLocation() {

														@Override
														public synchronized void update(Location location) {
															currentLocation = location;
															if (vector != null) {
																synchronized (vector) {
																	int size = vector.size();
																	if (size > 0) {
																		for (LocationObserver observer : vector) {
																			observer.execute(location);// 观察者刷新位置信息
																		}
																	}
																}
															}
														}

														@Override
														public void removeLocationListner(int listenerType) {
															removeLocationListener(listenerType);
														}

													};

	/**
	 * 立即通知所有观察者
	 */
	public void notifyObservers() {
		if (vector != null) {
			Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (gpsLocation != null) {
				updateLocation.update(gpsLocation);
			}
			else {
				Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (location != null) {
					updateLocation.update(location);
				}
			}
		}
	}

	/**
	 * 
	 * @param provider
	 * @return true if has the specific provider,otherwise false.
	 */
	public boolean hasLocateDevice(String provider) {
		if (locationManager == null)
			return false;
		final List<String> providers = locationManager.getAllProviders();
		if (providers == null)
			return false;
		return providers.contains(provider);
	}

	/**
	 * 取得当前位置 get the current location if not null
	 * 
	 * @return
	 */
	public Location getCurrentLocation() {
		return currentLocation;
	}

	private boolean	enableNetworkProvider	= true;

	/**
	 * 是否启用network方式，默认true
	 * 
	 * @param enable
	 */
	public synchronized void enableNetworkProvider(boolean enable) {
		this.enableNetworkProvider = enable;
		if (enable) {
			registerLocationListener();
		}
		else {
			removeLocationListener(LISTENER_TYPE_NETWORK);
		}
	}

	private static Timer	timer;
	private static int		DELTATIME	= 30 * 1000;

	private void locationTiming() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			/**
			 * 定时检测位置，长时间未获取新的位置，则开启网络获取方式
			 */
			private Location	timingLocation;

			@Override
			public void run() {
				Location curLocation = getCurrentLocation();
				if (curLocation == null) {
					return;
				}
				if (timingLocation == null) {
					timingLocation = curLocation;
					handler.post(new Runnable() {
						@Override
						public void run() {
							registerLocationListener();
						}
					});
					return;
				}

				long time1 = timingLocation.getTime();
				long time2 = curLocation.getTime();
				int deltaTime = (int) (time2 - time1);
				if (deltaTime <= 10 * 1000) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							registerLocationListener();
						}
					});
				}
				timingLocation = curLocation;
			}
		}, 5000, DELTATIME);
	}

	/**
	 * 监听GPS坐标的变化
	 * 
	 */
	public interface LocationObserver {
		public void execute(Location location);
	}

	/**
	 * 记录GPS坐标
	 * 
	 */
	public interface TrackRecorder {
		public void record(Location location);
	}

}
