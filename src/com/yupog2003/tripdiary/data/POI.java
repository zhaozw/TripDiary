package com.yupog2003.tripdiary.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.text.format.Time;

public class POI {
	
	public File parentTrip;
	public File dir;
	public File picDir;
	public File audioDir;
	public File videoDir;
	public File costDir;
	public File diaryFile;
	public File basicInformationFile;
	public File[] picFiles;
	public File[] audioFiles;
	public File[] videoFiles;
	public File[] costFiles;
	public String title;
	public Time time;
	public double latitude;
	public double longitude;
	public double altitude;
	public String diary;
	
	public POI(File dir){
		this.dir=dir;
		if (!dir.exists())dir.mkdirs();
		updateAllFields();
	}
	public void updateAllFields(){
		this.parentTrip=dir.getParentFile();
		this.picDir=new File(dir.getPath()+"/pictures");
		if (!picDir.exists())picDir.mkdirs();
		this.audioDir=new File(dir.getPath()+"/audios");
		if (!audioDir.exists())audioDir.mkdirs();
		this.videoDir=new File(dir.getPath()+"/videos");
		if (!videoDir.exists())videoDir.mkdirs();
		this.costDir=new File(dir.getPath()+"/costs");
		if (!costDir.exists())costDir.mkdirs();
		this.diaryFile=new File(dir.getPath()+"/text");
		if (!diaryFile.exists())
			try {
				diaryFile.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		this.basicInformationFile=new File(dir.getPath()+"/basicinformation");
		if (!basicInformationFile.exists()){
			try {
				basicInformationFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.picFiles=picDir.listFiles(FileHelper.getPictureFileFilter());
		this.audioFiles=audioDir.listFiles(FileHelper.getAudioFileFilter());
		this.videoFiles=videoDir.listFiles(FileHelper.getVideoFileFilter());
		this.costFiles=costDir.listFiles();
		this.title=dir.getName();
		try {
			BufferedReader br=new BufferedReader(new FileReader(basicInformationFile));
			String s;
			this.time=new Time();
			time.setToNow();
			this.latitude=0;
			this.longitude=0;
			this.altitude=0;
			while((s=br.readLine())!=null){
				if (s.startsWith("Title")){
					this.title=s.substring(s.indexOf("=")+1);
				}else if (s.startsWith("Time")){
					this.time=TimeAnalyzer.getTime(s, TimeAnalyzer.type_time_format3399);
				}else if (s.startsWith("Latitude")){
					this.latitude=Double.parseDouble(s.substring(s.indexOf("=")+1));
				}else if (s.startsWith("Longitude")){
					this.longitude=Double.parseDouble(s.substring(s.indexOf("=")+1));
				}else if (s.startsWith("Altitude")){
					this.altitude=Double.parseDouble(s.substring(s.indexOf("=")+1));
				}
			}
			br.close();
			br=new BufferedReader(new FileReader(diaryFile));
			StringBuffer sb=new StringBuffer();
			while((s=br.readLine())!=null){
				sb.append(s+"\n");
			}
			diary=sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateBasicInformation(String title,Time time,Double latitude,Double longitude,Double altitude){
		if (title!=null)this.title=title;
		if (time!=null)this.time=time;
		if (latitude!=null)this.latitude=latitude;
		if (longitude!=null)this.longitude=longitude;
		if (altitude!=null)this.altitude=altitude;
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(basicInformationFile, false));
			bw.write("Title="+this.title+"\n");
			bw.write("Time="+this.time.format3339(false)+"\n");
			bw.write("Latitude="+String.valueOf(this.latitude)+"\n");
			bw.write("Longitude="+String.valueOf(this.longitude)+"\n");
			bw.write("Altitude="+String.valueOf(this.altitude)+"\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateDiary(String text){
		if (text!=null){
			try {
				BufferedWriter bw=new BufferedWriter(new FileWriter(diaryFile, false));
				bw.write(text);
				bw.flush();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void copyPicture(File picture){
		if (FileHelper.isPicture(picture)){
			FileHelper.copyFile(picture, new File(picDir.getPath()+"/"+picture.getName()));
			this.picFiles=picDir.listFiles(FileHelper.getPictureFileFilter());
		}
	}
	public void copyVideo(File video){
		if (FileHelper.isVideo(video)){
			FileHelper.copyFile(video, new File(videoDir.getPath()+"/"+video.getName()));
			this.videoFiles=videoDir.listFiles(FileHelper.getVideoFileFilter());
		}
	}
	public void copyAudio(File audio){
		if (FileHelper.isAudio(audio)){
			FileHelper.copyFile(audio, new File(audioDir.getPath()+"/"+audio.getName()));
			this.audioFiles=audioDir.listFiles(FileHelper.getAudioFileFilter());
		}
	}
	public void addCost(int type,String name,float dollar){
		File cost=new File(costDir.getPath()+"/"+name);
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(cost,false));
			bw.write("type="+String.valueOf(type)+"\n");
			bw.write("dollar="+String.valueOf(dollar));
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deletePicture(File file){
		if (file.isFile()){
			file.delete();
			this.picFiles=picDir.listFiles(FileHelper.getPictureFileFilter());
		}
	}
	public void deleteVideo(File file){
		if (file.isFile()){
			file.delete();
			this.videoFiles=videoDir.listFiles(FileHelper.getVideoFileFilter());
		}
	}
	public void deleteAudio(File file){
		if (file.isFile()){
			file.delete();
			this.audioFiles=audioDir.listFiles(FileHelper.getAudioFileFilter());
		}
	}
	public void deleteCost(File file){
		if (file.isFile()){
			file.delete();
			this.costFiles=costDir.listFiles();
		}
	}
	public void renamePOI(String name){
		dir.renameTo(new File(dir.getParent()+"/"+name));
		dir=new File(dir.getParent()+"/"+name);
		updateAllFields();
	}
	public void deleteSelf(){
		FileHelper.deletedir(dir.getPath());
	}
}
