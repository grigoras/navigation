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
					float ax1 = c.getWidth() / 2 - sfert;
					float ax2 = c.getWidth() / 2 + sfert;
					float ax = c.getWidth() / 2;
					float ay1 = c.getHeight() - ca * d[0] / 1000 - sfert;
					float ay2 = c.getHeight() - ca * d[0] / 1000 + sfert;
					float ay = c.getHeight() - ca * d[0] / 1000;
					for (int i = 2; i < SharedVariables.points.size() - 1; i++) {

						float di = distance((float) SharedVariables.points.get(i - 1).latitude,
								(float) SharedVariables.points.get(i - 1).longitude,
								(float) SharedVariables.points.get(i).latitude,
								(float) SharedVariables.points.get(i).longitude);
						System.out.println("distance is : " + di);
						float angle = (float) angleBetween(SharedVariables.points.get(i - 2),
								SharedVariables.points.get(i), SharedVariables.points.get(i - 1));
						System.out.println("angle is : " + angle);
						float iangle = (float) angleBetween(SharedVariables.points.get(0),
								SharedVariables.points.get(i), SharedVariables.points.get(i - 1));
						System.out.println("iangle is : " + iangle);

						float pointx = (float) (ax + (ca * Math.cos(Math.toRadians(90 - iangle)) * di));
						System.out.println("point x = " + pointx);
						float pointy = (float) (ay + (ca * Math.sin(Math.toRadians(90 - iangle)) * di));
						System.out.println("point y = " + pointy);

						System.out.println("Draw x from: " + ax2 + " to: "
								+ (pointx + (float) Math.cos(Math.toRadians(angle)) * sfert));
						System.out.println("Draw y from: " + ay2 + " to: "
								+ (pointy + (float) Math.sin(Math.toRadians(angle)) * sfert));

						c.drawLine(ax2, ay2, pointx + (float) Math.cos(Math.toRadians(180 - angle))
								* sfert, pointy + (float) Math.sin(Math.toRadians(180 - angle))
								* sfert, p);

						ax2 = pointx + (float) Math.cos(Math.toRadians(angle)) * sfert;
						ay2 = pointy + (float) Math.sin(Math.toRadians(angle)) * sfert;
						ax = pointx;
						ay = pointy;
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
