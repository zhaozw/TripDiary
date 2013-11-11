package com.yupog2003.tripdiary.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.format.Time;

public class Trip {
	public Context context;
	public File dir;
	public File gpxFile;
	public File cacheFile;
	public File graphicFile;
	public File noteFile;
	public POI[] pois;
	public TrackCache cache;
	public String note;
	public String tripName;
	public String category;
	public String timezone;
	
	public Trip(Context context,File dir){
		this.dir=dir;
		if (!dir.exists()){
			dir.mkdirs();
			TimeAnalyzer.updateTripTimeZone(context, dir.getName(), Time.getCurrentTimezone());
		}
		this.context=context;
		refreshAllFields();
	}
	
	public void refreshAllFields(){
		this.gpxFile=new File(dir.getPath()+"/"+dir.getName()+".gpx");
		if (!gpxFile.exists()){
			try {
				gpxFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.cacheFile=new File(gpxFile.getPath()+".cache");
		this.graphicFile=new File(gpxFile.getPath()+".graph");
		this.noteFile=new File(dir.getPath()+"/note");
		if (!noteFile.exists()){
			try {
				noteFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		refreshPOIs();
		try{
			this.cache=(TrackCache)FileHelper.readObjectFromFile(cacheFile);
		}catch(Exception e){
			this.cache=null;
			e.printStackTrace();
		}
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(noteFile));
			String s;StringBuffer sb=new StringBuffer();
			while((s=br.readLine())!=null){
				sb.append(s+"\n");
			}
			note=sb.toString();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.tripName=dir.getName();
		SharedPreferences p=context.getSharedPreferences("trip", Context.MODE_PRIVATE);
		category=p.getString(tripName, "nocategory");
		timezone=TimeAnalyzer.getTripTimeZone(context, tripName);
	}
	public void deleteCache(){
		cacheFile.delete();
		graphicFile.delete();
	}
	public void updateCacheFromGpxFile(Context context,Handler handler){
		try {
			new GpxAnalyzer(gpxFile.getPath(), context, handler);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			this.cache=(TrackCache)FileHelper.readObjectFromFile(cacheFile);
		}catch(Exception e){
			this.cache=null;
			e.printStackTrace();
		}
	}
	public void updateNote(String note){
		if (note!=null){
			try {
				BufferedWriter bw=new BufferedWriter(new FileWriter(noteFile,false));
				bw.write(note);
				bw.flush();
				bw.close();
				this.note=note;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void refreshPOIs(){
		File[] poiFiles=dir.listFiles(FileHelper.getDirFilter());
		this.pois=new POI[poiFiles.length];
		for (int i=0;i<pois.length;i++){
			pois[i]=new POI(poiFiles[i]);
		}
	}
	public void renameTrip(Context context,String name){
		SharedPreferences p=context.getSharedPreferences("trip", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor=p.edit();
		editor.remove(tripName);
		editor.putString(name, category);
		editor.commit();
		p=context.getSharedPreferences("tripTimezone", Context.MODE_PRIVATE);
		editor=p.edit();
		editor.remove(tripName);
		editor.putString(name, timezone);
		editor.commit();
		String gpxName=gpxFile.getName();
		String cacheName=cacheFile.getName();
		String graphicName=graphicFile.getName();
		dir.renameTo(new File(dir.getParent()+"/"+name));
		dir=new File(dir.getParent()+"/"+name);
		gpxFile=new File(dir.getPath()+"/"+gpxName);
		gpxFile.renameTo(new File(dir.getPath()+"/"+name+".gpx"));
		gpxFile=new File(dir.getPath()+"/"+name+".gpx");
		cacheFile=new File(dir.getPath()+"/"+cacheName);
		cacheFile.renameTo(new File(gpxFile.getPath()+".cache"));
		cacheFile=new File(gpxFile.getPath()+".cache");
		graphicFile=new File(dir.getPath()+"/"+graphicName);
		graphicFile.renameTo(new File(gpxFile.getPath()+".graph"));
		graphicFile=new File(gpxFile.getPath()+".graph");
		refreshAllFields();
	}
	public void setCategory(Context context,String category){
		this.category=category;
		SharedPreferences.Editor editor=context.getSharedPreferences("trip", Context.MODE_PRIVATE).edit();
		editor.putString(tripName, category);
		editor.commit();
	}
	public void deleteSelf(){
		FileHelper.deletedir(dir.getPath());
	}
}
