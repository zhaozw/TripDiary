package com.yupog2003.tripdiary.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.widget.Toast;

import com.yupog2003.tripdiary.AddPointActivity;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.data.Trip;

public class RecordService extends Service implements LocationListener,GpsStatus.Listener,Runnable{
	double latitude;
	double longtitude;
	double elevation;
	BufferedWriter bw;
	Time time;
	NotificationCompat.Builder nb;
	final long startTime=System.currentTimeMillis()/1000;
	long totalTime=0;
	double nowSpeed=0;
	double totaldistance=0;
	float accuracy=-1;
	Location previousLocation;
	boolean run;
	StopTripReceiver stopTripReceiver;
	PauseReceiver pauseReceiver;
	String name;
	String path;
	String note;
	Trip trip;
	int recordDuration;
	int updateDuration;
	public static final String actionStopTrip="com.yupog2003.tripdiary.stopTrip";
	public static final String actionPauseTrip="com.yupog2003.tripdiary.pauseTrip";
	public static final DecimalFormat doubleFormat=new DecimalFormat("#.#");
	@Override
	public int onStartCommand(Intent intent,int flags,int startId	){
			name=intent.getStringExtra("name");
			path=intent.getStringExtra("path");
			note=intent.getStringExtra("note");
			trip=new Trip(RecordService.this,new File(path+"/"+name));
			trip.deleteCache();
			run=true;
			SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(RecordService.this);
			recordDuration=Integer.valueOf(preferences.getString("record_duration", "1000"));
			updateDuration=Integer.valueOf(preferences.getString("update_duration", "1000"));
			preferences=null;
			setupNotification(path,name,note);
			if (trip.gpxFile.exists()){
				try {
					if (trip.gpxFile.length()==0){
						bw=new BufferedWriter(new FileWriter(trip.gpxFile,true));
						bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
						bw.write("<gpx>\n");
						bw.flush();
					}else{
						StringBuffer sb=new StringBuffer();
						BufferedReader br=new BufferedReader(new FileReader(trip.gpxFile));
						String s;
						while((s=br.readLine())!=null){
							if (s.contains("<?xml")||s.contains("<gpx")||s.contains("<trkpt")||s.contains("<ele>")||s.contains("<time>")||s.contains("</trkpt")){
								sb.append(s+"\n");
							}
							if (s.contains("</gpx>")){
								break;
							}
						}
						br.close();
						bw=new BufferedWriter(new FileWriter(trip.gpxFile,false));
						bw.write(sb.toString());
						bw.flush();
						bw.close();
						bw=new BufferedWriter(new FileWriter(trip.gpxFile,true));
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				LocationManager lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, recordDuration, 0, this);
				lm.addGpsStatusListener(this);
				time=new Time(Time.TIMEZONE_UTC);
				new Thread(RecordService.this).start();
			}
		stopTripReceiver=new StopTripReceiver();
		pauseReceiver=new PauseReceiver();
		registerReceiver(stopTripReceiver, new IntentFilter(actionStopTrip));
		registerReceiver(pauseReceiver, new IntentFilter(actionPauseTrip));
		return START_REDELIVER_INTENT;
	}
	private void setupNotification(String path,String name,String note){
		nb=new NotificationCompat.Builder(this);
		nb.setContentTitle(name);
		nb.setContentText(note);
		nb.setSmallIcon(R.drawable.ic_launcher);
		nb.setTicker(getString(R.string.Start_Trip));
		Intent i2=new Intent(this,AddPointActivity.class);
		i2.putExtra("name", name);
		i2.putExtra("path", path);
		i2.putExtra("isgpsenabled", true);
		PendingIntent pi=PendingIntent.getActivity(this, 0, i2, PendingIntent.FLAG_UPDATE_CURRENT);
		nb.addAction(R.drawable.poi, getString(R.string.add_poi), pi);
		PendingIntent pi2=PendingIntent.getBroadcast(this, 0, new Intent(actionPauseTrip), PendingIntent.FLAG_UPDATE_CURRENT);
		nb.addAction(R.drawable.ic_pause, getString(R.string.pause), pi2);
		PendingIntent pi3=PendingIntent.getBroadcast(this, 0, new Intent(actionStopTrip), PendingIntent.FLAG_UPDATE_CURRENT);
		nb.addAction(R.drawable.ic_stop, getString(R.string.stop), pi3);
		nb.setOngoing(true);
		nb.setStyle(getContent());
		if (run){
			startForeground(1, nb.build());
		}
	}
	protected void updateNotification() {
		// TODO Auto-generated method stub
		totalTime=System.currentTimeMillis()/1000-startTime;
		nb.setStyle(getContent());
		if (run){
			startForeground(1, nb.build());
		}
	}
	private NotificationCompat.InboxStyle getContent(){
		String timeExpression=String.valueOf(totalTime/3600)+":"+String.valueOf(totalTime%3600/60)+":"+String.valueOf(totalTime%3600%60);
		NotificationCompat.InboxStyle content=new NotificationCompat.InboxStyle();
		content.addLine(getString(R.string.distance)+":"+doubleFormat.format(totaldistance/1000)+"km");
		content.addLine(getString(R.string.total_time)+":"+timeExpression);
		content.addLine(getString(R.string.velocity)+":"+doubleFormat.format(nowSpeed*18/5)+"km/hr");
		content.addLine(getString(R.string.accuracy)+"="+(accuracy==-1?"Infinity":(doubleFormat.format(accuracy)+"m")));
		content.addLine(getString(R.string.Altitude)+":"+doubleFormat.format(elevation));
		return content;
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onDestroy(){
		unregisterReceiver(stopTripReceiver);
		unregisterReceiver(pauseReceiver);
		super.onDestroy();
	}
	public class StopTripReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			run=false;
			try {
				bw.write("</gpx>");
				bw.flush();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			trip.deleteCache();
			stopForeground(true);
			LocationManager lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			lm.removeUpdates(RecordService.this);
			Toast.makeText(getApplicationContext(), getString(R.string.trip_has_been_stopped), Toast.LENGTH_SHORT).show();
			RecordService.this.stopSelf();
		}
	}
	public class PauseReceiver extends BroadcastReceiver{

		@SuppressLint("Wakelock")
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			run=!run;
			nb=new NotificationCompat.Builder(RecordService.this);
			nb.setContentTitle(name);
			nb.setContentText(note);
			nb.setSmallIcon(R.drawable.ic_launcher);
			nb.setTicker(getString(R.string.Start_Trip));
			Intent i2=new Intent(RecordService.this,AddPointActivity.class);
			i2.putExtra("name", name);
			i2.putExtra("path", path);
			i2.putExtra("isgpsenabled", true);
			PendingIntent pi=PendingIntent.getActivity(RecordService.this, 0, i2, PendingIntent.FLAG_UPDATE_CURRENT);
			nb.addAction(R.drawable.poi, getString(R.string.add_poi), pi);
			if (run){
				LocationManager lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, RecordService.this);
				new Thread(RecordService.this).start();
				PendingIntent pi2=PendingIntent.getBroadcast(RecordService.this, 0, new Intent(actionPauseTrip), PendingIntent.FLAG_UPDATE_CURRENT);
				nb.addAction(R.drawable.ic_pause, getString(R.string.pause), pi2);
			}else{
				LocationManager lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
				lm.removeUpdates(RecordService.this);
				PendingIntent pi2=PendingIntent.getBroadcast(RecordService.this, 0, new Intent(actionPauseTrip), PendingIntent.FLAG_UPDATE_CURRENT);
				nb.addAction(R.drawable.ic_play, getString(R.string.resume), pi2);
			}
			PendingIntent pi3=PendingIntent.getBroadcast(RecordService.this, 0, new Intent(actionStopTrip), PendingIntent.FLAG_UPDATE_CURRENT);
			nb.addAction(R.drawable.ic_stop, getString(R.string.stop), pi3);
			nb.setOngoing(true);
			nb.setStyle(getContent());
			startForeground(1, nb.build());
		}
		
	}
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (previousLocation==null){
			previousLocation=location;
		}
		totaldistance+=location.distanceTo(previousLocation);
		previousLocation=location;
		latitude=location.getLatitude();
		longtitude=location.getLongitude();
		elevation=location.getAltitude();
		nowSpeed=location.getSpeed();
		accuracy=location.getAccuracy();
		time.setToNow();
		int month=time.month;
		int year=time.year;
		month++;
		if (month==13){
			month=1;year++;
		}
		try {
			if (run){
				bw.write("<trkpt lat=\""+String.valueOf(latitude)+"\" lon=\""+String.valueOf(longtitude)+"\">\n");
				bw.write("<ele>"+String.valueOf(elevation)+"</ele>\n");
				bw.write("<time>"+String.valueOf(year)+"-"+String.valueOf(month)+"-"+String.valueOf(time.monthDay)+"T"+String.valueOf(time.hour)+":"+String.valueOf(time.minute)+":"+String.valueOf(time.second)+"Z</time>\n");
				bw.write("</trkpt>\n");
				bw.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	public void run() {
		// TODO Auto-generated method stub
		while(run){
			try {
				Thread.sleep(updateDuration);
				updateNotification();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void onGpsStatusChanged(int event) {
		// TODO Auto-generated method stub
		if (event==3){
			((Vibrator)getSystemService(Service.VIBRATOR_SERVICE)).vibrate(200);
		}
	}
}
