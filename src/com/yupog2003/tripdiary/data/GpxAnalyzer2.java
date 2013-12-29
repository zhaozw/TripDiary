package com.yupog2003.tripdiary.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

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

public class GpxAnalyzer2 {

	private TrackCache cache;
	String gpxPath;
	Context context;
	Handler contextHandler;
	ProgressChangedListener listener;

	public GpxAnalyzer2(String gpxPath, Context context, Handler contextHandler) {
		this.gpxPath = gpxPath;
		this.context = context;
		this.contextHandler = contextHandler;
	}

	public void analyze() {
		String timezone = TimeAnalyzer.getTripTimeZone(context, gpxPath.substring(gpxPath.lastIndexOf("/") + 1, gpxPath.lastIndexOf(".gpx")));
		String tripPath = gpxPath.substring(0, gpxPath.lastIndexOf("/"));
		String rootPath = tripPath.substring(0, tripPath.lastIndexOf("/"));
		String tripName = gpxPath.substring(gpxPath.lastIndexOf("/") + 1, gpxPath.lastIndexOf("."));
		Time tempTime = TimeAnalyzer.getTripTime(rootPath, tripName);
		tempTime.switchTimezone(timezone);
		int timeZoneOffset = (int) tempTime.gmtoff;
		cache = new TrackCache();
		boolean cacheExsit = new File(gpxPath + ".cache").exists();
		ArrayList<Float> speeds = cacheExsit ? null : new ArrayList<Float>();
		boolean success = cacheExsit?getCache(gpxPath+".cache", cache):parse(gpxPath, cache, speeds, timeZoneOffset);
		if (!cacheExsit && success) {
			saveGraph(context, gpxPath + ".graph", cache.lats, speeds, contextHandler);
		}
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
	
	public void onProgressChanged(long progress) {
		if (listener != null) {
			listener.onProgressChanged(progress);
		}
	}

	public static interface ProgressChangedListener {
		public void onProgressChanged(long progress);
	}

	public TrackCache getCache() {
		return this.cache;
	}

	public void setOnProgressChangedListener(ProgressChangedListener listener) {
		this.listener = listener;
	}

	public native boolean parse(String gpxPath, TrackCache cache, ArrayList<Float> speeds, int timezoneOffset);

	public native boolean getCache(String cachePath, TrackCache cache);

	public native void stop();

}
