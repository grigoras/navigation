package com.example.nav;

import java.util.ArrayList;

import android.location.Location;
import android.util.Pair;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

public class SharedVariables {

	// Location for service
	public static LocationClient mLocationClient;
	// Actual location
	public static Location location;
	// The set destination
	public static LatLng destination;
	// The arrayList of Points needed to be parsed
	public static ArrayList<LatLng> points = new ArrayList<LatLng>();
	// The arrayList of string directions
	public static ArrayList<Pair<String, Integer>> directions = new ArrayList<Pair<String, Integer>>();
	// The arrayList of string distance
	public static ArrayList<Pair<String, Integer>> distances = new ArrayList<Pair<String, Integer>>();

	public static Object sync = new Object();

	public static boolean venit;

	public static boolean take = true;

}
