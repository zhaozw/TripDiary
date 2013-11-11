package com.yupog2003.tripdiary;

import java.util.Arrays;
import java.util.Comparator;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.slidingmenu.lib.SlidingMenu;
import com.yupog2003.tripdiary.data.DeviceHelper;
import com.yupog2003.tripdiary.data.POI;
import com.yupog2003.tripdiary.data.Trip;
import com.yupog2003.tripdiary.fragments.AllAudioFragment;
import com.yupog2003.tripdiary.fragments.AllPictureFragment;
import com.yupog2003.tripdiary.fragments.AllTextFragment;
import com.yupog2003.tripdiary.fragments.AllVideoFragment;

import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class ViewAllActivity extends Activity implements OnClickListener{
	
	public static final String tag_trip_path="tag_path";
	public static final String tag_mode="tag_mode";
	public static final int text_mode=0;
	public static final int photo_mode=1;
	public static final int video_mode=2;
	public static final int audio_mode=3;
	int mode;
	boolean showMenu=false;
	public static Trip trip;
	SlidingMenu menu;
	Button diary;
	Button photo;
	Button video;
	Button audio;
	AllAudioFragment allAudioFragment;
	AllTextFragment allTextFragment;
	AllPictureFragment allPictureFragment;
	AllVideoFragment allVideoFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_all);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		trip=ViewMapActivity.trip;
		mode=getIntent().getIntExtra(tag_mode, text_mode);
		this.setTitle(trip.tripName);
		ImageLoaderConfiguration conf=new ImageLoaderConfiguration.Builder(ViewAllActivity.this)
		.build();
		ImageLoader.getInstance().init(conf);
		trip.pois=setupPOIs(trip.pois);
		allAudioFragment=new AllAudioFragment();
		allVideoFragment=new AllVideoFragment();
		allPictureFragment=new AllPictureFragment();
		allTextFragment=new AllTextFragment();
		setMode(mode);
		LinearLayout menuLayout=(LinearLayout)getLayoutInflater().inflate(R.layout.menu_view_all, null);
		diary=(Button)menuLayout.findViewById(R.id.diary);
		diary.setOnClickListener(this);
		photo=(Button)menuLayout.findViewById(R.id.photo);
		photo.setOnClickListener(this);
		video=(Button)menuLayout.findViewById(R.id.video);
		video.setOnClickListener(this);
		audio=(Button)menuLayout.findViewById(R.id.audio);
		audio.setOnClickListener(this);
		diary.measure(DeviceHelper.getScreenWidth(ViewAllActivity.this), DeviceHelper.getScreenHeight(ViewAllActivity.this));
		int width=diary.getMeasuredWidth();
		menu=new SlidingMenu(ViewAllActivity.this);
		menu.setMode(SlidingMenu.LEFT);
		menu.setFadeEnabled(true);
		menu.setFadeDegree(0.2f);
		menu.setBehindOffset(DeviceHelper.getScreenWidth(ViewAllActivity.this)-width);
		menu.attachToActivity(ViewAllActivity.this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(menuLayout);
		
	}
	private POI[] setupPOIs(POI[] pois){
		Arrays.sort(pois, new Comparator<POI>() {

			public int compare(POI lhs, POI rhs) {
				// TODO Auto-generated method stub
				if (lhs.time==null||rhs.time==null)return 0;
				if (lhs.time.after(rhs.time))return 1;
				else if (rhs.time.after(lhs.time))return -1;
				else return 0;
			}
		});
		return pois;
	}
	
	private void setMode(int mode){
		FragmentTransaction ft=getFragmentManager().beginTransaction();
		this.mode=mode;
		switch(mode){
		case text_mode:
			this.setTitle(trip.tripName+"-"+getString(R.string.diary));
			ft.replace(R.id.fragment, allTextFragment);
			break;
		case photo_mode:
			this.setTitle(trip.tripName+"-"+getString(R.string.photo));
			ft.replace(R.id.fragment, allPictureFragment);
			break;
		case video_mode:
			this.setTitle(trip.tripName+"-"+getString(R.string.video));
			ft.replace(R.id.fragment, allVideoFragment);
			break;
		case audio_mode:
			this.setTitle(trip.tripName+"-"+getString(R.string.sound));
			ft.replace(R.id.fragment, allAudioFragment);
			break;
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_view_all, menu);
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
		if (v.equals(diary)){
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
}
