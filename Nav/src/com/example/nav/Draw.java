package com.example.nav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.maps.model.LatLng;

public class Draw extends Activity {

	OurView v;
	Intent service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		service = new Intent(this, LocationUpdates.class);
		startService(service);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		v = new OurView(this);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(v);
	}

	@Override
	protected void onPause() {
		super.onPause();
		v.onPause();
		stopService(service);
		SharedVariables.mLocationClient.disconnect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		v.onResume();
	}

	@Override
	protected void onDestroy() {
		System.out.println("Distrug serviciul");
		stopService(service);
		SharedVariables.mLocationClient.disconnect();
		super.onDestroy();
	}

	public class OurView extends SurfaceView implements Runnable {

		Thread t = null;
		SurfaceHolder holder;
		boolean ok = false;
		int nr;

		public OurView(Context context) {
			super(context);
			holder = getHolder();
			nr = 0;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (ok) {
				if (!holder.getSurface().isValid())
					continue;

				if (SharedVariables.venit && !SharedVariables.directions.isEmpty()) {
					SharedVariables.take = false;
					Canvas c = holder.lockCanvas();
					c.drawARGB(255, 0, 0, 0);
					Paint p = new Paint();
					p.setARGB(255, 255, 255, 255);
					p.setStrokeWidth(20);
					p.setAntiAlias(true);
					p.setDither(true);
					p.setStyle(Paint.Style.STROKE);
					p.setStrokeJoin(Paint.Join.ROUND);
					p.setStrokeCap(Paint.Cap.ROUND);

					Paint paintBlur = new Paint();
					paintBlur.set(p);
					paintBlur.setColor(Color.argb(235, 224, 224, 224));
					paintBlur.setStrokeWidth(30f);
					paintBlur.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
					float sfert = c.getWidth() / 4;

					nr++;
					float[] d = new float[1];
					Location.distanceBetween((float) SharedVariables.points.get(0).latitude,
							(float) SharedVariables.points.get(0).longitude,
							(float) SharedVariables.points.get(1).latitude,
							(float) SharedVariables.points.get(1).longitude, d);
					float ca = c.getHeight() - c.getHeight() / 16;
					c.drawLine((float) c.getWidth() / 2 - sfert, (float) c.getHeight(),
							(float) c.getWidth() / 2 - sfert, c.getHeight() - ca * d[0] / 1000, p);
					c.drawLine((float) c.getWidth() / 2 - sfert, (float) c.getHeight(),
							(float) c.getWidth() / 2 - sfert, c.getHeight() - ca * d[0] / 1000,
							paintBlur);
					c.drawLine((float) c.getWidth() / 2 + sfert, (float) c.getHeight(),
							(float) c.getWidth() / 2 + sfert, c.getHeight() - ca * d[0] / 1000, p);
					c.drawLine((float) c.getWidth() / 2 + sfert, (float) c.getHeight(),
							(float) c.getWidth() / 2 + sfert, c.getHeight() - ca * d[0] / 1000,
							paintBlur);
					float ax = c.getWidth() / 2;
					float ay = c.getHeight() - ca * d[0] / 1000;
					for (int i = 1; i < SharedVariables.directions.get(0).second - 1; i++) {
						float di = distance((float) SharedVariables.points.get(i).latitude,
								(float) SharedVariables.points.get(i).longitude,
								(float) SharedVariables.points.get(i + 1).latitude,
								(float) SharedVariables.points.get(i + 1).longitude);
						System.out.println("Distance is " + di);
						float angle = (float) angleBetween(SharedVariables.points.get(0),
								SharedVariables.points.get(i + 1), SharedVariables.points.get(i));
						System.out.println("Angle is " + angle + " cos = "
								+ Math.cos(Math.toRadians(90 - angle)) + ", sin = "
								+ Math.sin(Math.toRadians(90 - angle)));
						c.drawLine(
								ax - sfert,
								ay,
								(float) (ax + (ca * Math.cos(Math.toRadians(90 - angle)) * di) - sfert),
								(float) (ay + (ca * Math.sin(Math.toRadians(90 - angle)) * di)), p);
						c.drawLine(
								ax - sfert,
								ay,
								(float) (ax + (ca * Math.cos(Math.toRadians(90 - angle)) * di) - sfert),
								(float) (ay + (ca * Math.sin(Math.toRadians(90 - angle)) * di)),
								paintBlur);
						c.drawLine(
								ax + sfert,
								ay,
								(float) (ax + (ca * Math.cos(Math.toRadians(90 - angle)) * di) + sfert),
								(float) (ay + (ca * Math.sin(Math.toRadians(90 - angle)) * di)), p);
						c.drawLine(
								ax + sfert,
								ay,
								(float) (ax + (ca * Math.cos(Math.toRadians(90 - angle)) * di) + sfert),
								(float) (ay + (ca * Math.sin(Math.toRadians(90 - angle)) * di)),
								paintBlur);
						ax = (float) (ax + ca * Math.cos(Math.toRadians(90 - angle)) * di);
						ay = (float) (ay + ca * Math.sin(Math.toRadians(90 - angle)) * di);
					}
					int al_doilea = 0;

					for (int k = 1; k < SharedVariables.directions.size(); k++) {

						String a = SharedVariables.directions.get(k).first;
						float offset = 0;

						if (a.startsWith("Turn") || a.startsWith("Slight") || a.startsWith("Take")) {
							al_doilea++;
							if (al_doilea == 2)
								break;

							float curve = (float) angleBetween(SharedVariables.points.get(0),
									SharedVariables.points.get(SharedVariables.directions
											.get(k - 1).second + 1),
									SharedVariables.points.get(SharedVariables.directions
											.get(k - 1).second));

							System.out.println(a.substring(a.indexOf("<b>") + 3, a.length()));

							if (a.substring(a.indexOf("<b>") + 3, a.length()).startsWith("left")) {

								if (curve > 10 && curve < 100) {

									float ad = (float) 75 / 100;
									float ab = (float) 57.5 / 100;
									c.drawLine(ax - sfert, ay, ax - ad * sfert, ay - ab * sfert, p);
									c.drawLine(ax - sfert, ay, ax - ad * sfert, ay - ab * sfert,
											paintBlur);

									ad = (float) 75 / 100;
									ab = (float) 57.5 / 100;
									float add = (float) 50 / 100;
									float abb = (float) 80 / 100;
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax - add * sfert,
											ay - sfert, p);
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax - add * sfert,
											ay - sfert, paintBlur);

									ad = (float) 50 / 100;
									ab = (float) 80 / 100;
									add = (float) 25 / 100;
									abb = (float) 127 / 100;
									c.drawLine(ax - ad * sfert, ay - sfert, ax - add * sfert, ay
											- abb * sfert, p);
									c.drawLine(ax - ad * sfert, ay - sfert, ax - add * sfert, ay
											- abb * sfert, paintBlur);

									ad = (float) 25 / 100;
									ab = (float) 127 / 100;
									abb = (float) 150 / 100;
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax, ay - abb
											* sfert, p);
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax, ay - abb
											* sfert, paintBlur);

									ab = (float) 150 / 100;
									add = (float) 25 / 100;
									abb = (float) 170 / 100;
									c.drawLine(ax, ay - ab * sfert, ax + add * sfert, ay - abb
											* sfert, p);
									c.drawLine(ax, ay - ab * sfert, ax + add * sfert, ay - abb
											* sfert, paintBlur);

									ad = (float) 25 / 100;
									ab = (float) 170 / 100;
									add = (float) 50 / 100;
									abb = (float) 180 / 100;
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + add * sfert,
											ay - abb * sfert, p);
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + add * sfert,
											ay - abb * sfert, paintBlur);

									ad = (float) 50 / 100;
									ab = (float) 180 / 100;
									add = (float) 75 / 100;
									abb = (float) 195 / 100;
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + add * sfert,
											ay - abb * sfert, p);
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + add * sfert,
											ay - abb * sfert, paintBlur);

									ad = (float) 75 / 100;
									ab = (float) 195 / 100;
									abb = (float) 200 / 100;
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + sfert, ay
											- abb * sfert, p);
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + sfert, ay
											- abb * sfert, paintBlur);

									ay -= sfert;
									ax += sfert;
									offset = sfert;

								}
							}
							else {

								System.out.println("Am intrat aici" + curve);

								if ((curve > -10 && curve > -100) || (curve > 200 && curve < 300)) {

									float ad = (float) 75 / 100;
									float ab = (float) 40 / 100;
									System.out.println(ad);
									System.out.println(ab);
									c.drawLine(ax + sfert, ay, ax + ad * sfert, ay - ab * sfert, p);
									c.drawLine(ax + sfert, ay, ax + ad * sfert, ay - ab * sfert,
											paintBlur);

									ad = (float) 75 / 100;
									ab = (float) 40 / 100;
									float add = (float) 50 / 100;
									float abb = (float) 80 / 100;
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + add * sfert,
											ay - abb * sfert, p);
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + add * sfert,
											ay - abb * sfert, paintBlur);

									ad = (float) 50 / 100;
									ab = (float) 80 / 100;
									add = (float) 25 / 100;
									abb = (float) 104 / 100;
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + add * sfert,
											ay - abb * sfert, p);
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax + add * sfert,
											ay - abb * sfert, paintBlur);

									ad = (float) 25 / 100;
									ab = (float) 104 / 100;
									abb = (float) 138 / 100;
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax, ay - abb
											* sfert, p);
									c.drawLine(ax + ad * sfert, ay - ab * sfert, ax, ay - abb
											* sfert, paintBlur);

									ab = (float) 138 / 100;
									add = (float) 25 / 100;
									abb = (float) 163 / 100;
									c.drawLine(ax, ay - ab * sfert, ax - add * sfert, ay - abb
											* sfert, p);
									c.drawLine(ax, ay - ab * sfert, ax - add * sfert, ay - abb
											* sfert, paintBlur);

									ad = (float) 25 / 100;
									ab = (float) 163 / 100;
									add = (float) 50 / 100;
									abb = (float) 175 / 100;
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax - add * sfert,
											ay - abb * sfert, p);
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax - add * sfert,
											ay - abb * sfert, paintBlur);

									ad = (float) 50 / 100;
									ab = (float) 175 / 100;
									add = (float) 75 / 100;
									abb = (float) 193 / 100;
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax - add * sfert,
											ay - abb * sfert, p);
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax - add * sfert,
											ay - abb * sfert, paintBlur);

									ad = (float) 75 / 100;
									ab = (float) 193 / 100;
									abb = (float) 200 / 100;
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax - sfert, ay
											- abb * sfert, p);
									c.drawLine(ax - ad * sfert, ay - ab * sfert, ax - sfert, ay
											- abb * sfert, paintBlur);

									ay -= sfert;
									ax -= sfert;
									offset = -sfert;

								}
							}

							if (offset == 0) {

								for (int i = SharedVariables.directions.get(k - 1).second; i < SharedVariables.directions
										.get(k).second - 1; i++) {
									float di = distance(
											(float) SharedVariables.points.get(i).latitude,
											(float) SharedVariables.points.get(i).longitude,
											(float) SharedVariables.points.get(i + 1).latitude,
											(float) SharedVariables.points.get(i + 1).longitude);
									System.out.println("Distance is " + di);
									float angle = (float) angleBetween(
											SharedVariables.points.get(0),
											SharedVariables.points.get(i + 1),
											SharedVariables.points.get(i));
									System.out.println("Angle is " + angle + " cos = "
											+ Math.cos(Math.toRadians(90 - angle)) + ", sin = "
											+ Math.sin(Math.toRadians(90 - angle)));
									c.drawLine(
											ax - sfert,
											ay,
											(float) (ax
													+ (ca * Math.cos(Math.toRadians(90 - angle)) * di) - sfert),
											(float) (ay + (ca
													* Math.sin(Math.toRadians(90 - angle)) * di)),
											p);
									c.drawLine(
											ax - sfert,
											ay,
											(float) (ax
													+ (ca * Math.cos(Math.toRadians(90 - angle)) * di) - sfert),
											(float) (ay + (ca
													* Math.sin(Math.toRadians(90 - angle)) * di)),
											paintBlur);
									c.drawLine(
											ax + sfert,
											ay,
											(float) (ax
													+ (ca * Math.cos(Math.toRadians(90 - angle)) * di) + sfert),
											(float) (ay + (ca
													* Math.sin(Math.toRadians(90 - angle)) * di)),
											p);
									c.drawLine(
											ax + sfert,
											ay,
											(float) (ax
													+ (ca * Math.cos(Math.toRadians(90 - angle)) * di) + sfert),
											(float) (ay + (ca
													* Math.sin(Math.toRadians(90 - angle)) * di)),
											paintBlur);
									ax = (float) (ax + ca * Math.cos(Math.toRadians(90 - angle))
											* di);
									ay = (float) (ay + ca * Math.sin(Math.toRadians(90 - angle))
											* di);
								}

							}
							else {

								for (int i = SharedVariables.directions.get(k - 1).second; i < SharedVariables.directions
										.get(k).second - 1; i++) {

									float di = distance(
											(float) SharedVariables.points.get(i).latitude,
											(float) SharedVariables.points.get(i).longitude,
											(float) SharedVariables.points.get(i + 1).latitude,
											(float) SharedVariables.points.get(i + 1).longitude);
									System.out.println("Distance is " + di);
									float angle = (float) angleBetween(
											SharedVariables.points.get(0),
											SharedVariables.points.get(i + 1),
											SharedVariables.points.get(i));
									System.out.println("Angle is " + angle + " cos = "
											+ Math.cos(Math.toRadians(90 - angle)) + ", sin = "
											+ Math.sin(Math.toRadians(90 - angle)));
									c.drawLine(
											ax,
											ay + offset,
											(float) (ax + (ca
													* Math.cos(Math.toRadians(90 - angle)) * di)),
											(float) (ay
													+ (ca * Math.sin(Math.toRadians(90 - angle)) * di) + offset),
											p);
									c.drawLine(
											ax,
											ay + offset,
											(float) (ax + (ca
													* Math.cos(Math.toRadians(90 - angle)) * di)),
											(float) (ay
													+ (ca * Math.sin(Math.toRadians(90 - angle)) * di) + offset),
											paintBlur);
									c.drawLine(
											ax,
											ay - offset,
											(float) (ax + (ca
													* Math.cos(Math.toRadians(90 - angle)) * di)),
											(float) (ay
													+ (ca * Math.sin(Math.toRadians(90 - angle)) * di) - offset),
											p);
									c.drawLine(
											ax,
											ay - offset,
											(float) (ax + (ca
													* Math.cos(Math.toRadians(90 - angle)) * di)),
											(float) (ay
													+ (ca * Math.sin(Math.toRadians(90 - angle)) * di) - offset),
											paintBlur);
									ax = (float) (ax + ca * Math.cos(Math.toRadians(90 - angle))
											* di);
									ay = (float) (ay + ca * Math.sin(Math.toRadians(90 - angle))
											* di);
								}
							}
						}
					}

					holder.unlockCanvasAndPost(c);
					SharedVariables.venit = false;
					SharedVariables.take = true;
				}
			}
		}

		public void onPause() {
			ok = false;
			while (true) {
				try {
					t.join();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
			t = null;
		}

		public void onResume() {
			ok = true;
			t = new Thread(this);
			t.start();
		}

		public float distance(float lat_a, float lng_a, float lat_b, float lng_b) {
			double earthRadius = 3958.75;
			double latDiff = Math.toRadians(lat_b - lat_a);
			double lngDiff = Math.toRadians(lng_b - lng_a);
			double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
					+ Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b))
					* Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			double distance = earthRadius * c;
			int meterConversion = 1609;

			return (float) (distance * meterConversion) / 1000;
		}

		private double angleBetween(LatLng previous, LatLng current, LatLng center) {
			return Math.toDegrees(Math.atan2(current.longitude - center.longitude, current.latitude
					- center.latitude)
					- Math.atan2(previous.longitude - center.longitude, previous.latitude
							- center.latitude));
		}

	}

}
