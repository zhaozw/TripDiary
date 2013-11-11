package com.yupog2003.tripdiary.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;

public class TimeAnalyzer {
	public static final int type_gpx=0;
	public static final int type_time_format3399=1;
	public static final int type_self=2;
	
	public static int getTimeInSeconds(Time time){
		 int seconds=0;
		 seconds+=time.year*365*86400;
		 seconds+=time.month*30*86400;
		 seconds+=time.monthDay*86400;
		 seconds+=time.hour*3600;
		 seconds+=time.minute*60;
		 seconds+=time.second;
		 return seconds;
	 }
	 public static Time getminusTime(Time starttime,Time stoptime){
		Time totaltime=new Time();
		long seconds=getMinusTimeInSecond(starttime, stoptime);
		totaltime.set((int)(seconds%60), (int)(seconds%3600/60), (int)(seconds%86400/3600), (int)(seconds%2592000/86400), (int)(seconds%31536000/2592000), (int)(seconds/31536000));
		return totaltime;
	}
	 public static long getMinusTimeInSecond(Time starttime,Time stoptime){
		 return (stoptime.toMillis(true)-starttime.toMillis(true))/1000;
	 }
	 public static Time getTime(String timezone,String s,int type){
		 Time time=new Time(timezone);
			String date="",t="";
			String [] datetoks,timetoks;
			switch(type){
			case type_gpx:
				date=s.substring(s.indexOf(">")+1, s.indexOf("T"));
				t=s.substring(s.indexOf("T")+1,s.indexOf("Z"));
				break;
			case type_time_format3399:
				date=s.substring(s.indexOf("=")+1, s.lastIndexOf("T"));
				t=s.substring(s.lastIndexOf("T")+1,s.lastIndexOf("."));
				break;
			case type_self:
				date=s.substring(0,s.indexOf("T"));
				t=s.substring(s.indexOf("T")+1, s.length());
				break;
			}
			datetoks=date.split("-");
			timetoks=t.split(":");
			time.set((int)(Double.parseDouble(timetoks[2])), Integer.parseInt(timetoks[1]), Integer.parseInt(timetoks[0]), Integer.parseInt(datetoks[2]), Integer.parseInt(datetoks[1])-1, Integer.parseInt(datetoks[0]));
			return time;
	 }
	 public static Time getTime(String s,int type){
			return getTime(Time.TIMEZONE_UTC, s, type);
		}
		public static boolean isTimeMatched(Time time1,Time time2,Time time3){
			return (Time.compare(time1, time2)<=0&&Time.compare(time2, time3)<=0);
		}
		public static String formatTotalTime(Time time){
			StringBuffer sb=new StringBuffer();
			if (time.year!=0)
				sb.append(time.year+"-");
			if (time.month!=0)
				sb.append(time.month+1+"-");
			if (time.monthDay!=0)
				sb.append(time.monthDay+"T");
			sb.append(time.hour+":");
			sb.append(time.minute+":");
			sb.append(time.second);
			return sb.toString();
		}
		public static String formatInCurrentTimezone(Time time){
			return formatInTimezone(time, Time.getCurrentTimezone());
		}
		public static String formatInTimezone(Time time,String timezone){
			StringBuffer s=new StringBuffer();
			if (timezone!=null)time.switchTimezone(timezone);
			s.append(time.year);s.append("-");
			s.append(time.month+1);s.append("-");
			s.append(time.monthDay);s.append("T");
			s.append(time.hour);s.append(":");
			s.append(time.minute);s.append(":");
			s.append(time.second);
			return s.toString();
		}
		public static Time getTripTime(String path,String tripName){
	    	try {
				BufferedReader br=new BufferedReader(new FileReader(path+"/"+tripName+"/"+tripName+".gpx"));
				String s;
				while((s=br.readLine())!=null){
						if (s.contains("<time>")){
							br.close();
							return TimeAnalyzer.getTime(s, TimeAnalyzer.type_gpx);
						}
					}
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	Time time=new Time();
	    	time.setToNow();
	    	time.switchTimezone(Time.TIMEZONE_UTC);
	    	return time;
	    }
		
		public static String getTimezoneFromLatlng(double lat,double lng){
			String result=Time.getCurrentTimezone();
			HttpClient client=new DefaultHttpClient();
			HttpGet get=new HttpGet("https://maps.googleapis.com/maps/api/timezone/xml?location="+String.valueOf(lat)+","+String.valueOf(lng)+"&timestamp=0&sensor=true");
			try {
				HttpResponse response=client.execute(get);
				String s=EntityUtils.toString(response.getEntity());
				if (s.contains("OVER_QUERY_LIMIT")){
					Thread.sleep(5000);
					return getTimezoneFromLatlng(lat, lng);
				}
				if (s.contains("OK")){
					result=s.substring(s.indexOf("<time_zone_id>")+14, s.indexOf("</time_zone_id>"));
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}
		public static String getTripTimeZone(Context context,String tripName){
			SharedPreferences preference=context.getSharedPreferences("tripTimezone", Context.MODE_PRIVATE);
			return preference.getString(tripName, Time.getCurrentTimezone());
		}
		public static void updateTripTimeZoneFromLatLng(Context context,String tripName,double lat,double lng){
			updateTripTimeZone(context, tripName, getTimezoneFromLatlng(lat, lng));
		}
		public static void updateTripTimeZone(Context context,String tripName,String timezone){
			SharedPreferences preference=context.getSharedPreferences("tripTimezone", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor=preference.edit();
			editor.putString(tripName, timezone);
			editor.commit();
		}
}
