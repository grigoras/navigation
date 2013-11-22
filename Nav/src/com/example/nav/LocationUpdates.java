package com.example.nav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class LocationUpdates extends Service implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

	LatLng origin;

	private static final LocationRequest REQUEST = LocationRequest.create().setInterval(2000)
			.setFastestInterval(16).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	public int onStartCommand(final Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		setUpLocationClientIfNeeded();
		SharedVariables.mLocationClient.connect();
		return Service.START_STICKY;
	}

	private void setUpLocationClientIfNeeded() {
		if (SharedVariables.mLocationClient == null)
			SharedVariables.mLocationClient = new LocationClient(getApplicationContext(), this,
					this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// nothing
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		SharedVariables.mLocationClient.requestLocationUpdates(REQUEST, this);
	}

	@Override
	public void onDisconnected() {
		// nothing
	}

	@Override
	public void onLocationChanged(Location location) {
		SharedVariables.location = location;
		LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());

		// Getting URL to the Google Directions API
		String url = getDirectionsUrl(origin, SharedVariables.destination);

		DownloadTask downloadTask = new DownloadTask();

		// Start downloading json data from Google Directions
		// API
		downloadTask.execute(url);
	}

	private String getDirectionsUrl(LatLng origin, LatLng dest) {

		// Origin of route
		String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

		return url;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		}
		catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		}
		finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	/** A class to download data from Google Directions URL */
	private class DownloadTask extends AsyncTask<String, Void, String> {

		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			}
			catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);
		}
	}

	/** A class to parse the Google Directions in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		// Parsing the data in non-ui thread
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();

				// Starts parsing data
				routes = parser.parse(jObject);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		// Executes in UI thread, after the parsing process
		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			while (SharedVariables.take == false) {
				System.out.println("Astept");
			}
			SharedVariables.points.clear();
			// Traversing through all the routes
			for (int i = 0; i < result.size(); i++) {

				// Fetching i-th route
				List<HashMap<String, String>> path = result.get(i);

				// Fetching all the points in i-th route
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					SharedVariables.points.add(position);
				}
				SharedVariables.venit = true;
				System.out.println("Size-ul este : " + SharedVariables.points.size());
				for (int k = 0; k < SharedVariables.directions.size(); k++) {
					System.out.println(SharedVariables.directions.get(k).first + " : "
							+ SharedVariables.directions.get(k).second);
				}
				SharedVariables.take = false;

			}

		}
	}

}
