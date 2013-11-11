package com.yupog2003.tripdiary.data;

import java.io.Serializable;
import java.util.ArrayList;

public class TrackCache implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7483356498452412154L;
	
	public ArrayList<MyLatLng2> lats;
	public String startTime;
	public String endTime;
	public String totalTime;
	public float distance;   // meters
	public float avgSpeed;   // km/hr
	public float maxSpeed;		 // km/hr
	public float climb;      // meters
	public float maxAltitude;// meters
	public float minAltitude;// meters
}
