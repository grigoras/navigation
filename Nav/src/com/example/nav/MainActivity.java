package com.example.nav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity implements OnClickListener,
		OnMyLocationButtonClickListener {

	GoogleMap mGoogleMap;
	ArrayList<LatLng> mMarkerPoints;
	double mLatitude = 0;
	double mLongitude = 0;
	Button b;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		b = (Button) findViewById(R.id.button1);
		b.setOnClickListener(this);

		// Getting Google Play availability status
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
													// not available

			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
			dialog.show();

		}
		else { // Google Play Services are available

			// Initializing
			mMarkerPoints = new ArrayList<LatLng>();

			// Getting reference to SupportMapFragment of the activity_main
			SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);

			// Getting Map for the SupportMapFragment
			mGoogleMap = fm.getMap();

			// Enable MyLocation Button in the Map
			mGoogleMap.setMyLocationEnabled(true);
			mGoogleMap.setOnMyLocationButtonClickListener(this);
		}
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
			PolylineOptions lineOptions = null;

			while (SharedVariables.take == false) {}
			// Traversing through all the routes
			for (int i = 0; i < result.size(); i++) {
				SharedVariables.points.clear();
				lineOptions = new PolylineOptions();

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
				System.out.println("Size-ul este : " + SharedVariables.points.size());
				for (int k = 0; k < SharedVariables.directions.size(); k++) {
					System.out.println(SharedVariables.directions.get(k).first + " : "
							+ SharedVariables.directions.get(k).second);
					System.out.println(SharedVariables.distances.get(k).first + " : "
							+ SharedVariables.distances.get(k).second);
				}
				SharedVariables.venit = true;
				// Adding all the points in the route to LineOptions
				lineOptions.addAll(SharedVariables.points);
				lineOptions.width(2);
				lineOptions.color(Color.RED);
			}

			// Drawing polyline in the Google Map for the i-th route
			mGoogleMap.addPolyline(lineOptions);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void drawMarker(LatLng point) {
		mMarkerPoints.add(point);

		// Creating MarkerOptions
		MarkerOptions options = new MarkerOptions();

		// Setting the position of the marker
		options.position(point);

		/**
		 * For the start location, the color of marker is GREEN and for the end
		 * location, the color of marker is RED.
		 */
		if (mMarkerPoints.size() == 1) {
			options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		}
		else if (mMarkerPoints.size() == 2) {
			options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		}

		// Add new marker to the Google Map Android API V2
		mGoogleMap.addMarker(options);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, Draw.class);
		startActivity(intent);
		this.onPause();
	}

	@Override
	public boolean onMyLocationButtonClick() {
		System.out.println("Aici");
		Location myLocation = mGoogleMap.getMyLocation();
		if (myLocation != null) {
			mLatitude = myLocation.getLatitude();
			mLongitude = myLocation.getLongitude();
			LatLng startPoint = new LatLng(mLatitude, mLongitude);
			if (!mMarkerPoints.isEmpty())
				mMarkerPoints.remove(0);
			mMarkerPoints.add(0, startPoint);

			// Setting onclick event listener for the map
			mGoogleMap.setOnMapClickListener(new OnMapClickListener() {

				@Override
				public void onMapClick(LatLng point) {

					// Already map contain destination location
					if (mMarkerPoints.size() > 1) {

						mMarkerPoints.clear();
						mGoogleMap.clear();
						LatLng startPoint = new LatLng(mLatitude, mLongitude);

						// draw the marker at the current position
						mMarkerPoints.add(startPoint);
					}

					// draws the marker at the currently touched location
					drawMarker(point);

					LatLng origin = mMarkerPoints.get(0);
					SharedVariables.destination = mMarkerPoints.get(1);

					// Getting URL to the Google Directions API
					String url = getDirectionsUrl(origin, SharedVariables.destination);

					DownloadTask downloadTask = new DownloadTask();

					// Start downloading json data from Google Directions
					// API
					System.out.println("Execut");
					downloadTask.execute(url);
				}
			});

		}
		return false;
	}

}