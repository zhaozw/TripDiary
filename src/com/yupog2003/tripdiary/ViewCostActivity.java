package com.yupog2003.tripdiary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.yupog2003.tripdiary.fragments.CostBarChartFragment;
import com.yupog2003.tripdiary.fragments.CostListFragment;
import com.yupog2003.tripdiary.fragments.CostPieChartFragment;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.DialogInterface;

public class ViewCostActivity extends Activity{
	int option;
	public static final int optionPOI=0;
	public static final int optionTrip=1;
	String path;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_cost);
		this.option=getIntent().getIntExtra("option", 0);
		this.path=getIntent().getStringExtra("path");
		setTitle(getIntent().getStringExtra("title")+" "+getString(R.string.cost));
		ViewPager viewPager=(ViewPager) findViewById(R.id.viewpager);
		  ActionBar actionBar=getActionBar();
		  actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		  actionBar.setDisplayShowTitleEnabled(true);
		  actionBar.setDisplayHomeAsUpEnabled(true);
		  TabsAdapter tabsAdapter=new TabsAdapter(this, viewPager);
		  tabsAdapter.addTab(actionBar.newTab().setIcon(R.drawable.ic_money), CostListFragment.class, null);
		  tabsAdapter.addTab(actionBar.newTab().setIcon(R.drawable.ic_piechart), CostPieChartFragment.class, null);
		  tabsAdapter.addTab(actionBar.newTab().setIcon(R.drawable.ic_barchart), CostBarChartFragment.class, null);
		  if (savedInstanceState!=null){
			  actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab"));
		  		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_view_cost, menu);
		if (getIntent().getIntExtra("option", 0)==optionTrip){
			menu.findItem(R.id.addcost).setVisible(false);
		}
		return true;
	}
	@Override
	  protected void onSaveInstanceState(Bundle outState){
		  super.onSaveInstanceState(outState);
		  outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	  }
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId() == android.R.id.home) {
			ViewCostActivity.this.finish();
		} else if (item.getItemId() == R.id.addcost) {
			if (option==optionPOI){
				AlertDialog.Builder ab=new AlertDialog.Builder(ViewCostActivity.this);
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
							try {
								BufferedWriter bw=new BufferedWriter(new FileWriter(new File(path+"/costs/"+name),false));
								bw.write("type="+String.valueOf(type)+"\n");
								bw.write("dollar="+dollar);
								bw.flush();
								bw.close();
								ViewCostActivity.this.recreate();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
				ab.setNegativeButton(getString(R.string.cancel), null);
				ab.show();
			}
		}
		return false;
	}
	public static class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,ViewPager.OnPageChangeListener{
		Context context;
		ActionBar actionBar;
		ViewPager viewPager;
		ArrayList<TabInfo> tabs=new ArrayList<TabInfo>();
		static final class TabInfo{
			private final Class<?> clss;
			private final Bundle args;
			TabInfo(Class<?> _clss,Bundle _args){
				clss=_clss;
				args=_args;
			}
		}
		public TabsAdapter(Activity activity,ViewPager viewPager) {
			// TODO Auto-generated constructor stub
			super(activity.getFragmentManager());
			context=activity;
			actionBar=activity.getActionBar();
			this.viewPager=viewPager;
			this.viewPager.setAdapter(this);
			this.viewPager.setOnPageChangeListener(this);
		}
		public void addTab(ActionBar.Tab tab,Class<?> clss,Bundle args){
			TabInfo info=new TabInfo(clss,args);
			tab.setTag(info);
			tab.setTabListener(this);
			tabs.add(info);
			actionBar.addTab(tab);
			notifyDataSetChanged();
		}
		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			TabInfo info=tabs.get(position);
			return Fragment.instantiate(context, info.clss.getName(),info.args);
			
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return tabs.size();
		}

		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
			actionBar.setSelectedNavigationItem(position);
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			TabInfo tag=(TabInfo) tab.getTag();
			for (int i=0;i<tabs.size();i++){
				if (tabs.get(i).equals(tag)){
					viewPager.setCurrentItem(i);
				}
			}
		}
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
