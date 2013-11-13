package com.yupog2003.tripdiary.fragments;

import java.io.File;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.ViewTripActivity;
import com.yupog2003.tripdiary.data.DeviceHelper;
import com.yupog2003.tripdiary.data.POI;
import com.yupog2003.tripdiary.views.UnScrollableGridView;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class AllPictureFragment extends Fragment{
	POI[] pois;
	ExpandableListView listView;
	int width;
	int numColums;
	DisplayImageOptions options;
	public AllPictureFragment(){
		this.pois=ViewTripActivity.trip.pois;
		options=new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.cacheOnDisc(false)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		int screenWidth=DeviceHelper.getScreenWidth(getActivity());
		int screenHeight=DeviceHelper.getScreenHeight(getActivity());
		if (screenWidth>screenHeight){
			width=screenWidth/5;
			numColums=5;
		}else{
			width=screenWidth/3;
			numColums=3;
		}
		if (listView==null){
			listView=new ExpandableListView(getActivity());
			POIAdapter adapter=new POIAdapter(pois);
			listView.setAdapter(adapter);
		}
		return listView;
	}
	public void expandAll(){
		int groupCount=listView.getExpandableListAdapter().getGroupCount();
		for (int i=0;i<groupCount;i++){
			listView.expandGroup(i);
		}
	}
	public void collapseAll(){
		int groupCount=listView.getExpandableListAdapter().getGroupCount();
		for (int i=0;i<groupCount;i++){
			listView.collapseGroup(i);
		}
	}
	class PictureAdapter extends BaseAdapter implements OnItemClickListener{
		File[] pictures;
		
		public PictureAdapter(File[] pictures){
			this.pictures=pictures;
		}
		public int getCount() {
			// TODO Auto-generated method stub
			if (pictures==null)return 0;
			return pictures.length;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return pictures[position];
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView==null){
				ImageView imageView=new ImageView(getActivity());
				imageView.setLayoutParams(new AbsListView.LayoutParams(width, width));
				imageView.setMaxHeight(width);
				imageView.setMaxWidth(width);
				convertView=imageView;
			}
			ImageLoader.getInstance().displayImage("file://"+pictures[position].getPath(), (ImageView)convertView, options);
			return convertView;
		}
		public void onItemClick(AdapterView<?> adapterView, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Intent intent=new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(pictures[position]), "image/*");
			getActivity().startActivity(intent);
		}
		
	}
	class POIAdapter extends BaseExpandableListAdapter{
		POI[] pois;
		UnScrollableGridView[] pictures;
		public POIAdapter(POI[] pois){
			this.pois=pois;
			pictures=new UnScrollableGridView[pois.length];
			for (int i=0;i<pictures.length;i++){
				pictures[i]=new UnScrollableGridView(getActivity());
				PictureAdapter adapter=new PictureAdapter(pois[i].picFiles);
				pictures[i].setAdapter(adapter);
				pictures[i].setOnItemClickListener(adapter);
				pictures[i].setNumColumns(numColums);
			}
		}
		
		@Override
		public void onGroupExpanded(int groupPosition) {
			// TODO Auto-generated method stub
			super.onGroupExpanded(groupPosition);
			
		}

		@Override
		public void onGroupCollapsed(int groupPosition) {
			// TODO Auto-generated method stub
			super.onGroupCollapsed(groupPosition);
		}

		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return pictures[groupPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return (UnScrollableGridView)getChild(groupPosition, childPosition);
		}

		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return 1;
		}

		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return pois[groupPosition];
		}

		public int getGroupCount() {
			// TODO Auto-generated method stub
			if (pois==null)return 0;
			return pois.length;
		}

		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView textView=new TextView(getActivity());
			textView.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
			textView.setCompoundDrawablesWithIntrinsicBounds(isExpanded?R.drawable.indicator_expand:R.drawable.indicator_collapse, 0,0,0);
			textView.setText(pois[groupPosition].title+"("+String.valueOf(pois[groupPosition].picFiles.length)+")");
			textView.setGravity(Gravity.CENTER_VERTICAL);
			return textView;
		}

		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
