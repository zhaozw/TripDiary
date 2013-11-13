package com.yupog2003.tripdiary;

import java.io.File;

import com.google.analytics.tracking.android.EasyTracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.slidingmenu.lib.SlidingMenu;
import com.yupog2003.tripdiary.data.DeviceHelper;
import com.yupog2003.tripdiary.data.Trip;
import com.yupog2003.tripdiary.fragments.AllAudioFragment;
import com.yupog2003.tripdiary.fragments.AllPictureFragment;
import com.yupog2003.tripdiary.fragments.AllTextFragment;
import com.yupog2003.tripdiary.fragments.AllVideoFragment;
import com.yupog2003.tripdiary.fragments.ViewMapFragment;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class ViewTripActivity extends Activity implements OnClickListener{
	public static String path;
	public static String name;
	public static Trip trip;
	public static final int map_mode=0;
	public static final int text_mode=1;
	public static final int photo_mode=2;
	public static final int video_mode=3;
	public static final int audio_mode=4;
	ViewMapFragment viewMapFragment;
	AllAudioFragment allAudioFragment;
	AllTextFragment allTextFragment;
	AllPictureFragment allPictureFragment;
	AllVideoFragment allVideoFragment;
	Button diary;
	Button photo;
	Button video;
	Button audio;
	Button map;
	SlidingMenu menu;
	int mode=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_trip);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		path = getIntent().getStringExtra("path");
		name = getIntent().getStringExtra("name");
		trip = new Trip(ViewTripActivity.this, new File(path + "/" + name));
		allAudioFragment=new AllAudioFragment();
		allVideoFragment=new AllVideoFragment();
		allPictureFragment=new AllPictureFragment();
		allTextFragment=new AllTextFragment();
		viewMapFragment=new ViewMapFragment();
		FragmentTransaction ft=getFragmentManager().beginTransaction();
		ft.add(R.id.fragment, viewMapFragment);
		ft.add(R.id.fragment, allAudioFragment);
		ft.add(R.id.fragment, allPictureFragment);
		ft.add(R.id.fragment, allTextFragment);
		ft.add(R.id.fragment, allVideoFragment);
		ft.commit();
		LinearLayout menuLayout=(LinearLayout)getLayoutInflater().inflate(R.layout.menu_view_trip, null);
		map=(Button)menuLayout.findViewById(R.id.map);
		map.setOnClickListener(this);
		diary=(Button)menuLayout.findViewById(R.id.diary);
		diary.setOnClickListener(this);
		photo=(Button)menuLayout.findViewById(R.id.photo);
		photo.setOnClickListener(this);
		video=(Button)menuLayout.findViewById(R.id.video);
		video.setOnClickListener(this);
		audio=(Button)menuLayout.findViewById(R.id.audio);
		audio.setOnClickListener(this);
		diary.measure(DeviceHelper.getScreenWidth(ViewTripActivity.this), DeviceHelper.getScreenHeight(ViewTripActivity.this));
		int width=diary.getMeasuredWidth();
		menu=new SlidingMenu(ViewTripActivity.this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setFadeEnabled(true);
		menu.setFadeDegree(0.2f);
		menu.setBehindOffset(DeviceHelper.getScreenWidth(ViewTripActivity.this)-width);
		menu.attachToActivity(ViewTripActivity.this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(menuLayout);
		setMode(0);
		ImageLoaderConfiguration conf=new ImageLoaderConfiguration.Builder(ViewTripActivity.this)
		.build();
		ImageLoader.getInstance().init(conf);
	}
	private void setMode(int mode){
		FragmentTransaction ft=getFragmentManager().beginTransaction();
		ft.hide(viewMapFragment);
		ft.hide(allTextFragment);
		ft.hide(allAudioFragment);
		ft.hide(allPictureFragment);
		ft.hide(allVideoFragment);
		ft.commit();
		ft=getFragmentManager().beginTransaction();
		this.mode=mode;
		switch(mode){
		case map_mode:
			this.setTitle(trip.tripName);
			ft.show(viewMapFragment);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			break;
		case text_mode:
			this.setTitle(trip.tripName+"-"+getString(R.string.diary));
			ft.show(allTextFragment);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			break;
		case photo_mode:
			this.setTitle(trip.tripName+"-"+getString(R.string.photo));
			ft.show(allPictureFragment);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			break;
		case video_mode:
			this.setTitle(trip.tripName+"-"+getString(R.string.video));
			ft.show(allVideoFragment);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			break;
		case audio_mode:
			this.setTitle(trip.tripName+"-"+getString(R.string.sound));
			ft.show(allAudioFragment);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			break;
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_view_trip, menu);
		if (mode==map_mode){
			menu.removeItem(R.id.expandall);
			menu.removeItem(R.id.collapseall);
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case android.R.id.home:
			menu.toggle(true);
			break;
		case R.id.expandall:
			switch(mode){
			case text_mode:
				allTextFragment.expandAll();
				break;
			case photo_mode:
				allPictureFragment.expandAll();
				break;
			case video_mode:
				allVideoFragment.expandAll();
				break;
			case audio_mode:
				allAudioFragment.expandAll();
				break;
			}
			break;
		case R.id.collapseall:
			switch(mode){
			case text_mode:
				allTextFragment.collapseAll();
				break;
			case photo_mode:
				allPictureFragment.collapseAll();
				break;
			case video_mode:
				allVideoFragment.collapseAll();
				break;
			case audio_mode:
				allAudioFragment.collapseAll();
				break;
			}
			break;
		}
		return true;
	}
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.equals(map)){
			setMode(map_mode);
			menu.toggle(true);
		}else if (v.equals(diary)){
			setMode(text_mode);
			menu.toggle(true);
		}else if (v.equals(photo)){
			setMode(photo_mode);
			menu.toggle(true);
		}else if (v.equals(video)){
			setMode(video_mode);
			menu.toggle(true);
		}else if (v.equals(audio)){
			setMode(audio_mode);
			menu.toggle(true);
		}
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
}
