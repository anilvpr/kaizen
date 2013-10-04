package com.sisc.sard.anil.thingspeaklightlevelviewer;

import android.location.Location;

public class GeoPointLightVal {
	public String light_val = null;
	public String address = null;
	public String status = null;
	public String loc_lat = null;
	public String loc_long = null;
	public String loc_alt = null;
	
	public GeoPointLightVal(GeoPointLightVal obj) {
		this.address = obj.address;
		this.light_val = obj.light_val;
		this.loc_alt = obj.loc_alt;
		this.loc_lat = obj.loc_lat;
		this.loc_long = obj.loc_long;
		this.status = obj.status;
	}
	
	public GeoPointLightVal() {
		
	}
}
