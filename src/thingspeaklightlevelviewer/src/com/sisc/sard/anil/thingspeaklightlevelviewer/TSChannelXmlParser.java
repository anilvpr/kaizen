package com.sisc.sard.anil.thingspeaklightlevelviewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class TSChannelXmlParser {
	private static final String TAG = "TSChannelXmlParser";

	private ArrayList<GeoPointLightVal> mGeoPointLightValList = new ArrayList<GeoPointLightVal>();
	private GeoPointLightVal mGeoPointLightVal = null;
	private String mCurTag = null;

	public ArrayList<GeoPointLightVal> parseXml(String str_xml)
			throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();

		xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		xpp.setInput(new StringReader(str_xml));

		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_DOCUMENT) {
				Log.d(TAG, "Start document");
			} else if (eventType == XmlPullParser.END_DOCUMENT) {
				Log.d(TAG, "End document");
			} else if (eventType == XmlPullParser.START_TAG) {
				String tag = xpp.getName();
				Log.d(TAG, "Start tag " + tag);
				handleStartTag(tag);
			} else if (eventType == XmlPullParser.END_TAG) {
				String tag = xpp.getName();
				Log.d(TAG, "End tag " + tag);
				handleEndTag(tag);
			} else if (eventType == XmlPullParser.TEXT) {
				String text = xpp.getText();
				Log.d(TAG, "Text " + text);
				handleText(text);
			}
			eventType = xpp.next();
		}

		return mGeoPointLightValList;
	}

	private void handleStartTag(String str_tag) {
		mCurTag = str_tag;
		if (str_tag.equals("feed")) {
			mGeoPointLightVal = new GeoPointLightVal();
		}
	}

	private void handleEndTag(String str_tag) {
		mCurTag = null;
		if (str_tag.equals("feed")) {
			if (mGeoPointLightVal != null) {
				GeoPointLightVal tempObj = new GeoPointLightVal(
						mGeoPointLightVal);
				mGeoPointLightValList.add(tempObj);
				mGeoPointLightVal = null;
			}
		}
	}

	private void handleText(String str_text) {
		if(str_text == null || str_text.isEmpty()) {
			return;
		}
		
		if(mCurTag == null || mCurTag.isEmpty()) {
			return;
		}
		
		if (mGeoPointLightVal == null) {
			Log.d(TAG, "mGeoPointLightVal is null");
			return;
		}
		
		Log.d(TAG, "cur-tag: " + mCurTag);
		
		if (mCurTag.equals("field1")) {
			mGeoPointLightVal.light_val = str_text;
		} else if (mCurTag.equals("latitude")) {
			mGeoPointLightVal.loc_lat = str_text;
		} else if (mCurTag.equals("longitude")) {
			mGeoPointLightVal.loc_long = str_text;
		} else if (mCurTag.equals("elevation")) {
			mGeoPointLightVal.loc_alt = str_text;
		} else if (mCurTag.equals("location")) {
			mGeoPointLightVal.address = str_text;
		} else if (mCurTag.equals("status")) {
			mGeoPointLightVal.status = str_text;
		}
	}
}
