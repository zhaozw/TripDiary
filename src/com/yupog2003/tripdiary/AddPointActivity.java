package com.yupog2003.tripdiary;

import java.io.File;
import java.io.IOException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.data.POI;

import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class AddPointActivity extends Activity implements Button.OnClickListener{
	ImageButton takePicture;
	ImageButton takeVideo;
	ImageButton takeAudio;
	ImageButton takeText;
	ImageButton takePaint;
	ImageButton takeMoney;
	Location location;
	Marker POIMarker;
	MapFragment mapFragment;
	String picturePath;
	String videoPath;
	String audioPath;
	String textPath;
	String costsPath;
	Time time;
	boolean isGpsEnabled;
	POI poi;
	private static final int REQUEST_PICTURE=0;
	private static final int REQUEST_VIDEO=1;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_point);
        isGpsEnabled=getIntent().getBooleanExtra("isgpsenabled", false);
        AlertDialog.Builder builder=new AlertDialog.Builder(AddPointActivity.this);
        builder.setTitle(getString(R.string.input_the_poi_title));
        final EditText pointTitle=new EditText(this);
        builder.setView(pointTitle);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(pointTitle.getText().toString().equals("")){
					AddPointActivity.this.finish();
				}
				final String title=pointTitle.getText().toString();
				setTitle(title);
				final String path=getIntent().getStringExtra("path");
				final String name=getIntent().getStringExtra("name");
				String newPointPath=path+"/"+name+"/"+title;
				poi=new POI(new File(newPointPath));
				picturePath=poi.picDir.getPath();
				videoPath=poi.videoDir.getPath();
				audioPath=poi.audioDir.getPath();
				textPath=poi.diaryFile.getPath();
				costsPath=poi.costDir.getPath();
			}
		});
        builder.show();
        mapFragment=MapFragment.newInstance();
					FragmentTransaction ft=getFragmentManager().beginTransaction();
					ft.add(R.id.addmaplayout, mapFragment, "mapFragment");
					ft.commit();
        takePicture=(ImageButton)findViewById(R.id.takepicture);
        takePicture.setOnClickListener(this);
        takeVideo=(ImageButton)findViewById(R.id.takevideo);
        takeVideo.setOnClickListener(this);
        takeAudio=(ImageButton)findViewById(R.id.takeaudio);
        takeAudio.setOnClickListener(this);
        takeText=(ImageButton)findViewById(R.id.taketext);
        takeText.setOnClickListener(this);
        takePaint=(ImageButton)findViewById(R.id.takepaint);
        takePaint.setOnClickListener(this);
        takeMoney=(ImageButton)findViewById(R.id.takemoney);
        takeMoney.setOnClickListener(this);
        time=new Time();
        time.switchTimezone(Time.TIMEZONE_UTC);
        time.setToNow();
        location=null;
        if (isGpsEnabled){
        		LocationManager locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        		location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        		if (location==null){
        			Toast.makeText(AddPointActivity.this, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show();
        			AddPointActivity.this.finish();
        					}
        			}
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_point, menu);
        return true;
    }
    @Override
    public void onResume(){
    	super.onResume();
    	if (isGpsEnabled){
    		GoogleMap gmap=mapFragment.getMap();
      gmap.setMyLocationEnabled(true);
      gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),16));
      POIMarker=gmap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).draggable(true));
    		}
      }
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Time time=new Time(Time.getCurrentTimezone());
		time.setToNow();
		String fileName=time.format3339(false);
		fileName=fileName.substring(0,fileName.lastIndexOf("."));
		fileName=fileName.replace("-", "");
		fileName=fileName.replace("T", "");
		fileName=fileName.replace(":", "");
		if (v.equals(takePicture)){
			Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			String filePath=picturePath+"/"+fileName+".jpg";
			File file=new File(filePath);
			Uri uri=Uri.fromFile(file);
			i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			if (i.resolveActivity(getPackageManager())!=null){
				AddPointActivity.this.startActivityForResult(i, REQUEST_PICTURE);
			}else{
				Toast.makeText(AddPointActivity.this, getString(R.string.camera_is_not_available), Toast.LENGTH_SHORT).show();
			}
			
		}else if (v.equals(takeVideo)){
			Intent i=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			String filePath=videoPath+"/"+fileName+".3gp";
			File file=new File(filePath);
			Uri uri=Uri.fromFile(file);
			i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			if (i.resolveActivity(getPackageManager())!=null){
				AddPointActivity.this.startActivityForResult(i, REQUEST_VIDEO);
			}else{
				Toast.makeText(AddPointActivity.this, getString(R.string.camera_is_not_available), Toast.LENGTH_SHORT).show();
			}
			
		}else if (v.equals(takeAudio)){
			new RecordAudioTask(fileName).execute();
		}else if (v.equals(takeText)){
			final EditText getText=new EditText(AddPointActivity.this);
			getText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			AlertDialog.Builder ab=new AlertDialog.Builder(this);
			ab.setTitle(getString(R.string.input_diary));
			ab.setView(getText);
			ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener(){

				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					poi.updateDiary(getText.getText().toString());
				}
			});
			ab.setNegativeButton(getString(R.string.cancel), null);
			ab.show();
		}else if (v.equals(takePaint)){
			Intent intent=new Intent(AddPointActivity.this,PaintActivity.class);
			intent.putExtra("path", picturePath+"/"+fileName+".png");
			startActivityForResult(intent, REQUEST_PICTURE);
		}else if (v.equals(takeMoney)){
			AlertDialog.Builder ab=new AlertDialog.Builder(AddPointActivity.this);
			ab.setTitle(getString(R.string.cost));
			final LinearLayout layout=(LinearLayout)getLayoutInflater().inflate(R.layout.take_money, null);
			ab.setView(layout);
			ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText costName=(EditText) layout.findViewById(R.id.costname);
					RadioGroup costType=(RadioGroup)layout.findViewById(R.id.costtype);
					EditText costDollar=(EditText)layout.findViewById(R.id.costdollar);
					String name=costName.getText().toString();
					String dollar=costDollar.getText().toString();
					if (!name.equals("")&&!dollar.equals("")){
						int type=-1;
						if (costType.getCheckedRadioButtonId() == R.id.food) {
							type=0;
						} else if (costType.getCheckedRadioButtonId() == R.id.lodging) {
							type=1;
						} else if (costType.getCheckedRadioButtonId() == R.id.transportation) {
							type=2;
						} else if (costType.getCheckedRadioButtonId() == R.id.other) {
							type=3;
						} else {
							type=0;
						}
						poi.addCost(type, name, Float.parseFloat(dollar));
					}
				}
			});
			ab.setNegativeButton(getString(R.string.cancel), null);
			ab.show();
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId() == R.id.save) {
			if (isGpsEnabled){
				poi.updateBasicInformation(null, time, POIMarker.getPosition().latitude, POIMarker.getPosition().longitude, location.getAltitude());
			}else{
				poi.updateBasicInformation(null, time, null, null, null);
			}
			AddPointActivity.this.finish();
		}
		return false;
	}
			@Override
			protected void onActivityResult(int requestCode,int resultCode,Intent data){
				super.onActivityResult(requestCode, resultCode, data);
			}
			class RecordAudioTask extends AsyncTask<Integer, Integer, Integer>{
				MediaRecorder mr;
				ProgressBar pb;
				TextView time;
				boolean run=true;
				long startTime;
				String filePath;
				static final int base=40;
				public RecordAudioTask(String fileName){
					filePath=audioPath+"/"+fileName+".mp3";
				}
				@Override
				protected void onPreExecute() {
					// TODO Auto-generated method stub
					mr=new MediaRecorder();
					mr.setAudioSource(MediaRecorder.AudioSource.MIC);
					mr.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
					mr.setOutputFile(filePath);
					mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					try {
						mr.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					LinearLayout layout=(LinearLayout)getLayoutInflater().inflate(R.layout.record_audio, null);
					pb=(ProgressBar)layout.findViewById(R.id.volum);
					pb.setMax((int)(20*Math.log10(32767))-base);
					pb.setProgress(0);
					time=(TextView)layout.findViewById(R.id.time);
					time.setText("00:00:00");
					AlertDialog.Builder ab=new AlertDialog.Builder(AddPointActivity.this);
					ab.setTitle(getString(R.string.record_audio));
					ab.setView(layout);
					ab.setPositiveButton(getString(R.string.finish), new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							run=false;
							dialog.dismiss();
						}
					});
					ab.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							run=false;
							new File(filePath).delete();
							dialog.dismiss();
						}
					});
					ab.setCancelable(false);
					ab.show();
					startTime=System.currentTimeMillis();
					mr.start();
				}
				@Override
				protected Integer doInBackground(Integer... params) {
					// TODO Auto-generated method stub
					while(run){
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						publishProgress(mr.getMaxAmplitude());
					}
					return null;
				}
				@Override
				protected void onProgressUpdate(Integer... values) {
					// TODO Auto-generated method stub
					time.setText(convertSecondToTime(System.currentTimeMillis()-startTime));
					pb.setProgress((int)(20*Math.log10(values[0]))-base);
				}
				private String convertSecondToTime(long mSecond){
					int second=(int)(mSecond/1000);
					StringBuffer sb=new StringBuffer();
					sb.append(String.valueOf(second/3600)+":");
					second=second%3600;
					sb.append(String.valueOf(second/60)+":");
					second=second%60;
					sb.append(String.valueOf(second));
					return sb.toString();
				}
				@Override
				protected void onPostExecute(Integer result) {
					// TODO Auto-generated method stub
					mr.stop();
					mr.release();
					
				}
			}
}
