package com.yupog2003.tripdiary.data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.achartengine.ChartFactory;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.yupog2003.tripdiary.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.text.format.Time;
import android.view.View;
import android.widget.Toast;

public class GpxAnalyzer {
	GPXHandler handler;
	TrackCache cache;
	private static final double earthRadius = 6378.1 * 1000;

	public GpxAnalyzer(String gpxPath, Context context, Handler contextHandler) throws ParserConfigurationException, SAXException, IOException {

		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		handler = new GPXHandler(context, gpxPath);
		BufferedReader br = new BufferedReader(new FileReader(new File(gpxPath)));
		StringBuffer sb = new StringBuffer();
		String s;
		while ((s = br.readLine()) != null) {
			sb.append(s + "\n");
		}
		br.close();
		s = sb.toString();
		if (!s.contains("</gpx>")) {
			sb.append("</gpx>");
			s = sb.toString();
		}
		sb = null;
		System.gc();
		sp.parse(new ByteArrayInputStream(s.getBytes()), handler);
		ArrayList<Time> times = handler.getTimes();
		float totaldistance = handler.getDiatance();
		float maxSpeed = handler.getMaxSpeed();
		float totalAltitude = handler.getTotalAltitude();
		float maxAltitude = handler.getMaxAltitude();
		float minAltitude = handler.getMinAltitude();
		ArrayList<MyLatLng2> lats = handler.getLats();
		ArrayList<Float> speeds = handler.getSpeeds();
		if (times.size() > 0 && totaldistance > 0 && lats.size() > 0) {
			cache = new TrackCache();
			saveTrack(lats);
			saveStatistics(times, totaldistance, maxSpeed, totalAltitude, maxAltitude, minAltitude);
			saveGraph(context, gpxPath + ".graph", lats, speeds, contextHandler);
			saveCache(gpxPath + ".cache");
		}
	}

	public ArrayList<Time> getTimes() {
		return handler.getTimes();
	}

	public float getDistance() {
		return handler.getDiatance();
	}

	public ArrayList<MyLatLng2> getLats() {
		return handler.getLats();
	}

	public class GPXHandler extends DefaultHandler {
		Context context;
		String path;
		ArrayList<MyLatLng2> lats;
		ArrayList<Time> times;
		ArrayList<Float> speeds;
		MyLatLng2 latlng;
		MyLatLng2 previousLatLng;
		Time time;
		long timeZoneOffset = 0; // in seconds
		float previousAltitude;
		StringBuffer sb;
		String timezone = null;
		float distance = 0;
		float maxSpeed = 0;
		float totalAltitude = 0;
		float maxAltitude = -Float.MAX_VALUE;
		float minAltitude = Float.MAX_VALUE;

		public ArrayList<Time> getTimes() {
			return times;
		}

		public float getDiatance() {
			return distance;
		}

		public ArrayList<MyLatLng2> getLats() {
			return lats;
		}

		public float getMaxAltitude() {
			return maxAltitude;
		}

		public float getMinAltitude() {
			return minAltitude;
		}

		public float getTotalAltitude() {
			return totalAltitude;
		}

		public float getMaxSpeed() {
			return maxSpeed;
		}

		public ArrayList<Float> getSpeeds() {
			return speeds;
		}

		public GPXHandler(Context context, String path) {
			this.context = context;
			this.path = path;
			lats = new ArrayList<MyLatLng2>();
			times = new ArrayList<Time>();
			sb = new StringBuffer();
			speeds = new ArrayList<Float>();
			timezone = TimeAnalyzer.getTripTimeZone(context, path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".gpx")));
			Time tempTime = new Time(Time.TIMEZONE_UTC);
			tempTime.switchTimezone(timezone);
			timeZoneOffset = tempTime.gmtoff;
		}

		@Override
		public void startDocument() {

		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			if (localName.equals("trkpt")) {
				latlng = new MyLatLng2(Double.parseDouble(attributes.getValue("lat")), Double.parseDouble(attributes.getValue("lon")), 0, null);
			}
			sb.setLength(0);

		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			sb.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			if (localName.equals("trkpt")) {
				if (latlng != null) {
					float altitude = latlng.getAltitude();
					if (altitude > maxAltitude)
						maxAltitude = altitude;
					if (altitude < minAltitude)
						minAltitude = altitude;
					if (previousLatLng != null) {
						float altitudeDiffer = altitude - previousAltitude;
						distance += distFrom(previousLatLng.latitude, previousLatLng.longitude, latlng.latitude, latlng.longitude);
						if (Math.abs(altitudeDiffer) > 15) {
							if (altitudeDiffer > 0)
								totalAltitude += altitudeDiffer;
							previousAltitude = altitude;
						}
					} else {
						previousLatLng = new MyLatLng2();
						previousAltitude = altitude;
					}
					previousLatLng.setMyLatLng2(latlng);
					lats.add(latlng);
					latlng = null;
				}
			} else if (localName.equals("time")) {
				if (sb.indexOf("-") != -1 && sb.indexOf("T") != -1 && sb.indexOf(":") != -1 && sb.indexOf("Z") != -1) {
					time = new Time(Time.TIMEZONE_UTC);
					String year = sb.substring(0, sb.indexOf("-"));
					String month = sb.substring(sb.indexOf("-") + 1, sb.lastIndexOf("-"));
					String day = sb.substring(sb.lastIndexOf("-") + 1, sb.indexOf("T"));
					String hour = sb.substring(sb.indexOf("T") + 1, sb.indexOf(":"));
					String minute = sb.substring(sb.indexOf(":") + 1, sb.lastIndexOf(":"));
					String second = sb.substring(sb.lastIndexOf(":") + 1, sb.indexOf("Z"));
					time.set((int) (Double.parseDouble(second)), Integer.parseInt(minute), Integer.parseInt(hour), Integer.parseInt(day), Integer.parseInt(month) - 1, Integer.parseInt(year));
					time.set(time.toMillis(false) + timeZoneOffset * 1000);
					times.add(time);
					if (latlng != null) {
						latlng.setTime(TimeAnalyzer.formatInTimezone(time, null));
					}
				}
			} else if (localName.equals("ele")) {
				if (latlng != null) {
					latlng.setAltitude(Float.parseFloat(sb.toString()));
				}
			}
			sb.setLength(0);

		}

		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub
			maxSpeed = Float.MIN_VALUE;
			int latsSize = lats.size();
			int timesSize = times.size();
			for (int i = 0; i < latsSize; i += 20) {
				if (i + 20 < latsSize && i + 20 < timesSize) {
					float dist = distFrom(lats.get(i).latitude, lats.get(i).longitude, lats.get(i + 20).latitude, lats.get(i + 20).longitude);
					float time = (times.get(i + 20).toMillis(false) - times.get(i).toMillis(false)) / 1000;
					float speed = dist / time * 18 / 5;
					speeds.add(speed);
					if (maxSpeed < speed)
						maxSpeed = speed;
				}
			}
			super.endDocument();
		}
	}

	public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLng / 2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		return (float) dist;
	}

	private void saveTrack(ArrayList<MyLatLng2> lats) {
		cache.lats = lats;
	}

	private void saveStatistics(ArrayList<Time> times, float totaldistance, float maxSpeed, float totalAltitude, float maxAltitude, float minAltitude) {
		cache.endTime = TimeAnalyzer.formatInTimezone(times.get(times.size() - 1), null);
		cache.startTime = TimeAnalyzer.formatInTimezone(times.get(0), null);
		cache.totalTime = TimeAnalyzer.formatTotalTime(TimeAnalyzer.getminusTime(times.get(0), times.get(times.size() - 1)));
		cache.distance = totaldistance;
		cache.avgSpeed = cache.distance / TimeAnalyzer.getMinusTimeInSecond(times.get(0), times.get(times.size() - 1)) * 18 / 5;
		cache.maxSpeed = maxSpeed;
		cache.maxAltitude = maxAltitude;
		cache.climb = totalAltitude;
		cache.minAltitude = minAltitude;
	}

	private void saveGraph(final Context context, final String path, final ArrayList<MyLatLng2> lats, final ArrayList<Float> speeds, Handler handler) {
		if (context == null)
			return;
		final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		XYSeries altitudeSeries = new XYSeries(context.getString(R.string.Altitude), 0);
		final int latsSize = lats.size();
		for (int i = 0; i < latsSize; i++) {
			altitudeSeries.add(i, lats.get(i).getAltitude());
		}
		dataset.addSeries(0, altitudeSeries);
		XYSeries speedSeries = new XYSeries(context.getString(R.string.velocity), 1);
		final int speedsSize = speeds.size();
		for (int i = 0; i < speedsSize; i++) {
			speedSeries.add(i, speeds.get(i));
		}
		dataset.addSeries(1, speedSeries);
		final XYMultipleSeriesRenderer render = new XYMultipleSeriesRenderer(2);
		render.setAntialiasing(true);
		XYSeriesRenderer r1 = new XYSeriesRenderer();
		r1.setColor(Color.GREEN);
		render.addSeriesRenderer(r1);
		XYSeriesRenderer r2 = new XYSeriesRenderer();
		r2.setColor(Color.BLUE);
		render.addSeriesRenderer(r2);
		render.setBackgroundColor(Color.BLACK);
		render.setApplyBackgroundColor(true);
		render.setLabelsTextSize(30);
		render.setLegendTextSize(30);
		render.setYLabels(10);
		render.setXLabels(10);
		render.setYTitle(context.getString(R.string.Altitude) + "(m)", 0);
		render.setYAxisAlign(Align.LEFT, 0);
		render.setYTitle(context.getString(R.string.velocity) + "(km/hr)", 1);
		render.setYAxisAlign(Align.RIGHT, 1);
		render.setYLabelsAlign(Align.RIGHT, 1);
		render.setXTitle(context.getString(R.string.Time) + "(s)");
		render.setAxisTitleTextSize(30);
		String fileName = new File(path).getName();
		render.setChartTitle(fileName.substring(0, fileName.indexOf(".")));
		render.setChartTitleTextSize(40);
		if (handler == null)
			return;
		handler.post(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				View view = ChartFactory.getLineChartView(context, dataset, render);
				view.setDrawingCacheEnabled(true);
				view.measure(1920, 1080);
				view.layout(0, 0, 1920, 1080);
				view.buildDrawingCache();
				Bitmap bitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				view.draw(canvas);
				new File(path).delete();
				try {
					if (bitmap != null) {
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
						bos.flush();
						bos.close();
						bitmap.recycle();
						System.gc();
					} else {
						Toast.makeText(context, "bitmap is null", Toast.LENGTH_SHORT).show();
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}

	public void saveCache(String path) {
		File file = new File(path);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(cache);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}