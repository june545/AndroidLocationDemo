package com.example.ui;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gps.R;
import com.gps.service.LocationService;
import com.gps.service.LocationService.LocationObserver;

public class MainActivity extends Activity {
	private Button		btn;
	private TextView	tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		btn = (Button) findViewById(R.id.btn);
		tv = (TextView) findViewById(R.id.text);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				loc();
			}
		});
	}

	private void loc() {
		final LocationService locationService = LocationService.getInstance(getApplicationContext());
		locationService.addObserver(new LocationObserver() {

			@Override
			public void execute(Location location) {
				tv.setText("lng=" + location.getLongitude() + "\n");
				tv.append("lat=" + location.getLatitude() + "\n");
				tv.append("provider=" + location.getProvider() + "\n");
				tv.append("accuracy=" + location.getAccuracy() + "\n");
				tv.append("time=" + location.getTime() + "\n");
				locationService.stop();
			}
		});
	}

}
