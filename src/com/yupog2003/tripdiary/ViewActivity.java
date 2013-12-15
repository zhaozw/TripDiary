package com.yupog2003.tripdiary;

import com.viewpagerindicator.TitlePageIndicator;
import com.yupog2003.tripdiary.fragments.LocalTripsFragment;
import com.yupog2003.tripdiary.fragments.RemoteTripsFragment;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class ViewActivity extends Activity {

	LocalTripsFragment localTripsFragment;
	String path;
	MyPagerAdapter pagerAdaper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);
		path = getIntent().getStringExtra("path");
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
		pagerAdaper = new MyPagerAdapter(getFragmentManager());
		viewPager.setAdapter(pagerAdaper);
		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.titles);
		indicator.setViewPager(viewPager, 1);
	}
	public void refreshData(){
		if (pagerAdaper!=null){
			for (int i=0;i<pagerAdaper.fragments.length;i++){
				pagerAdaper.fragments[i].onResume();
			}
		}
	}
	class MyPagerAdapter extends FragmentStatePagerAdapter {

		public Fragment[] fragments;

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
			fragments = new Fragment[3];
			fragments[0] = new RemoteTripsFragment();
			Bundle personalTripFragmentBundle=new Bundle();
			personalTripFragmentBundle.putInt(RemoteTripsFragment.tag_option, RemoteTripsFragment.option_personal);
			fragments[0].setArguments(personalTripFragmentBundle);
			fragments[1] = new LocalTripsFragment();
			Bundle localTripFragmentBundle=new Bundle();
			localTripFragmentBundle.putString(LocalTripsFragment.tag_path, MainActivity.rootPath);
			fragments[1].setArguments(localTripFragmentBundle);
			fragments[2] = new RemoteTripsFragment();
			Bundle publicTripFragmentBundle=new Bundle();
			publicTripFragmentBundle.putInt(RemoteTripsFragment.tag_option, RemoteTripsFragment.option_public);
			fragments[2].setArguments(publicTripFragmentBundle);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// TODO Auto-generated method stub
			switch (position) {
			case 0:
				return getString(R.string.remote_personal_trips);
			case 1:
				return getString(R.string.local_trips);
			case 2:
				return getString(R.string.remote_public_trips);
			}
			return getTitle();
		}

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			return fragments[position];
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fragments.length;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.view_activity2, menu);
		return false;
	}

	
}
