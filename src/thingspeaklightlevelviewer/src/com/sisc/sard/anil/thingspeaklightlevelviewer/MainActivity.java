package com.sisc.sard.anil.thingspeaklightlevelviewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.xmlpull.v1.XmlPullParserException;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity {
	private static final String TAG = "TSLLVIEW";
	//private static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	//private static final LatLng KIEL = new LatLng(53.551, 9.993);
	
	private Marker mAmbLightPointMarker = null;
	private Circle mPpmCircle = null;
	private GoogleMap map;

	private Timer mTimer = null;
	private Handler mHandler = new Handler();

	//private static final String TS_CHANNEL_VIEW_URL = "http://api.thingspeak.com/channels/7166/feed.xml?location=true&status=true&results=1";
	private static final String TS_CHANNEL_VIEW_URL = "http://api.thingspeak.com/channels/7166/feed.xml?location=true&status=true";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		map.setIndoorEnabled(true);
		map.setMyLocationEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		map.setTrafficEnabled(true);

		/*Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
				.title("Hamburg"));
		Marker kiel = map.addMarker(new MarkerOptions()
				.position(KIEL)
				.title("Kiel")
				.snippet("Kiel is cool")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_launcher)));*/

		// Move the camera instantly to hamburg with a zoom of 15.
		// map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));

		// Zoom in, animating the camera.
		// map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

		// Construct a CameraPosition focusing on Mountain View and animate the
		// camera to that position.
		/*CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(KIEL) // Sets the center of the map to Mountain View
				.zoom(20) // Sets the zoom
				.bearing(180) // Sets the orientation of the camera to east
				.tilt(90) // Sets the tilt of the camera to 90 degrees
				.build(); // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/

		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		mTimer = new Timer();

		// Set the schedule function and rate (15 sec)
		mTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				getTsChannelUpdate();
			}

		}, 0, (15 * 1000));

	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if(mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		super.onPause();
	}
	
	private int randInt(int min, int max) {

		// Usually this can be a field rather than a method variable
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max + 1) - min) + min;

		return randomNum;
	}

	
	private void updateMarker(GeoPointLightVal geo_light_point) {
		double d_lat = Location.convert(geo_light_point.loc_lat);
		double d_long = Location.convert(geo_light_point.loc_long);
		LatLng amb_light_point = new LatLng(d_lat, d_long);
		if(mAmbLightPointMarker != null) {
			mAmbLightPointMarker.remove();
		}
		
		mAmbLightPointMarker = map.addMarker(new MarkerOptions()
		.position(amb_light_point)
		.title("Intensity: " + geo_light_point.light_val)
		.snippet(geo_light_point.address)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ch4)));
		
		/*generate random PPM val for now... until actual gas sensor readings are available*/
		int ppm_val = randInt(20, 2000);
		
		if(mPpmCircle == null) {
			/* add a PPM concentration circle */
			// Instantiates a new CircleOptions object and defines the center and radius
			CircleOptions circleOptions = new CircleOptions()
			    .center(amb_light_point)
			    .radius(ppm_val); // scale the intensity by 100 (in meters)

			// Get back the mutable Circle
			mPpmCircle = map.addCircle(circleOptions);
			mPpmCircle.setStrokeColor(Color.argb(50, 100, 0, 0));
			mPpmCircle.setFillColor(Color.argb(50, 100, 0, 0));
		} else {
			mPpmCircle.setCenter(amb_light_point);
			mPpmCircle.setRadius(ppm_val);
			mPpmCircle.setStrokeColor(Color.argb(70, 200, 0, 0));
			mPpmCircle.setFillColor(Color.argb(70, 200, 0, 0));
		}
		
		
		
				
		
		CameraPosition cameraPosition = new CameraPosition.Builder()
		.target(amb_light_point) // Sets the center of the map to Mountain View
		.zoom(16) // Sets the zoom
		.bearing(100) 
		.tilt(60) // Sets the tilt of the camera to 60 degrees
		.build(); // Creates a CameraPosition from the builder
		
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
	
	private void getTsChannelUpdate() {
		String xml_feed = TSHttpClient.getXmlFromUrl(TS_CHANNEL_VIEW_URL);
		try {
			TSChannelXmlParser xml_parser = new TSChannelXmlParser();
			ArrayList<GeoPointLightVal> mGeoPointLightValList = xml_parser.parseXml(xml_feed);
			
			Iterator<GeoPointLightVal> iterator = mGeoPointLightValList.iterator();
			while (iterator.hasNext()) {
				final GeoPointLightVal geo_light = iterator.next(); 
				//Log.d(TAG, "light-val: " + (geo_light.light_val));
				//Log.d(TAG, "latitude: " + (geo_light.loc_lat));
				//Log.d(TAG, "longitude: " + (geo_light.loc_long));
				//Log.d(TAG, "altitude: " + (geo_light.loc_alt));
				//Log.d(TAG, "address: " + (geo_light.address));
				//Log.d(TAG, "status: " + (geo_light.status));
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "adding marker...");
						updateMarker(geo_light);
					}});
			}
						
			
			Log.d(TAG, "Done parsing....!!!\n\n");
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
