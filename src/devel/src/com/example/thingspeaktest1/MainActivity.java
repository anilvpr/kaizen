package com.example.thingspeaktest1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

public class MainActivity extends Activity implements SensorEventListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private static final String TAG = "ThingSpeakTest1";
	Button btnPostData = null;

	private SensorManager sensorManager = null;
	private Sensor currentSensor = null;
	private long ts_update = System.currentTimeMillis();
	
	private LocationClient mLocClient = null;
	
	private Geocoder mGcoder = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnPostData = (Button) findViewById(R.id.btn_post_data);

		btnPostData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {
					@Override
					public void run() {
						int light_val = randInt(0, 255);
						Log.d(TAG, "rand_light_val: " + light_val);
						// postData_2(light_val);
					}
				}).start();
			}
		});

		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		currentSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if (currentSensor != null) {
			sensorManager.registerListener(this, currentSensor,
					SensorManager.SENSOR_DELAY_FASTEST);
		} else {
			Log.d(TAG, "unable to initialize light-sensor");
		}

		if (ConnectionResult.SUCCESS == GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this)) {
			Log.d(TAG, "Google Play services available");
			mLocClient = new LocationClient(this, this, this);
		} else {
			Log.d(TAG, "Google Play services NOT available");
		}
		
		mGcoder = new Geocoder(this);
		
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(mLocClient != null) {
			mLocClient.connect();
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if(mLocClient != null) {
			mLocClient.disconnect();
		}
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (currentSensor != null) {
			sensorManager.registerListener(this, currentSensor,
					SensorManager.SENSOR_DELAY_UI);

		}
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	private int randInt(int min, int max) {

		// Usually this can be a field rather than a method variable
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max + 1) - min) + min;

		return randomNum;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void postData_2(int light_val, Location loc) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://api.thingspeak.com/update");

		String write_API_key = "MZUKX8W3OZIS6FB5";

		String str_light_val = "" + light_val;

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(7);
			nameValuePairs.add(new BasicNameValuePair("key", write_API_key));
			nameValuePairs.add(new BasicNameValuePair("field1", str_light_val));

			if (loc != null) {
				String str_latitude = "" + Location.convert(loc.getLatitude(), Location.FORMAT_DEGREES);
				String str_longitude = "" + Location.convert(loc.getLongitude(), Location.FORMAT_DEGREES);
				String str_altitude = "" + Location.convert(loc.getAltitude(), Location.FORMAT_DEGREES);
				Log.d(TAG, "<lat> " + str_latitude + " <long> " + str_longitude
						+ " <alt> " + str_altitude);
				nameValuePairs.add(new BasicNameValuePair("lat", str_latitude));
				nameValuePairs
						.add(new BasicNameValuePair("long", str_longitude));
				nameValuePairs.add(new BasicNameValuePair("elevation",
						str_altitude));
				
				if(mGcoder != null) {
					List<Address> addr_list = mGcoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
					if(addr_list != null) {
						nameValuePairs.add(new BasicNameValuePair("location",
								addr_list.get(0).toString()));

					}
				}
			} else {
				Log.d(TAG, "Location info not available");
			}

			nameValuePairs.add(new BasicNameValuePair("status",
					"Manufacturer: " + Build.MANUFACTURER + " Model: "
							+ Build.MODEL));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();

			Log.d(TAG, "response: " + response.toString());
			Log.d(TAG, "status: " + status);

		} catch (ClientProtocolException e) {
			Log.d(TAG, "ClientProtocolException");
		} catch (IOException e) {
			Log.d(TAG, "IOException");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			Log.d(TAG, "LUX: " + event.values[0]);
		}

		long cur_tme = System.currentTimeMillis();
		/* lets post light-value to ThingSpeak every 10sec */
		if ((cur_tme - ts_update) > (10 * 1000)) {
			ts_update = cur_tme;
			Log.d(TAG, "posting to ThingSpeak...");
			postToIoT((int) event.values[0]);
		}

	}

	private Location getLocation() {
		LocationManager lm = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);
		Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (loc == null) {
			loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(loc == null) {
				loc = mLocClient.getLastLocation();
				if(loc == null) {
					Log.d(TAG, "Location not available from GOOGLE_PLAY_SERVICES");
				} else {
					Log.d(TAG, "Location received from GOOGLE_PLAY_SERVICES");
				}
			} else {
				Log.d(TAG, "Location from NET_PROVIDER");
			}
		} else {
			Log.d(TAG, "Location from GPS_PROVIDER");
		}

		return loc;
	}

	private void postToIoT(int light_val) {
		final int lgt_val = light_val;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Location loc = getLocation();
				postData_2(lgt_val, loc);
			}
		}).start();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "[GooglePlayServices] LocationClient connection failed");
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "[GooglePlayServices] LocationClient CONNECTED");
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Log.d(TAG, "[GooglePlayServices] LocationClient DISCONNECTED");
	}

}
