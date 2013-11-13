package com.yupog2003.tripdiary.fragments;

import java.io.File;

import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.ViewTripActivity;
import com.yupog2003.tripdiary.data.DeviceHelper;
import com.yupog2003.tripdiary.data.POI;
import com.yupog2003.tripdiary.views.UnScrollableGridView;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class AllVideoFragment extends Fragment{
	POIAdapter adapter;
	POI[] pois;
	ExpandableListView listView;
	int width;
	public AllVideoFragment(){
		this.pois=ViewTripActivity.trip.pois;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (listView==null){
			adapter=new POIAdapter(pois);
			listView=new ExpandableListView(getActivity());
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
	class VideoAdapter extends BaseAdapter implements OnItemClickListener{
		
		File[] videos;
		
		MediaMetadataRetriever mmr;
		Canvas canvas;
		int left,top;
		final Bitmap playbitmap=BitmapFactory.decodeResource(getResources(), R.drawable.ic_play);
   
		public VideoAdapter(File[] videos){
			this.videos=videos;
			mmr=new MediaMetadataRetriever();
	   left=playbitmap.getWidth()/2;
	   top=playbitmap.getHeight()/2;
		}
		public int getCount() {
			// TODO Auto-generated method stub
			if (videos==null)return 0;
			return videos.length;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return videos[position];
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View view, ViewGroup viewGroup) {
			// TODO Auto-generated method stub
			ImageView image=new ImageView(getActivity());
			mmr.setDataSource(videos[position].getPath());
			Bitmap b=mmr.getFrameAtTime();
			Bitmap bitmap=b==null?Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888):Bitmap.createScaledBitmap(mmr.getFrameAtTime(), width, width, true);
			canvas=new Canvas(bitmap);
			canvas.drawBitmap(playbitmap, bitmap.getWidth()/2-left,bitmap.getHeight()/2-top,null);
			image.setImageBitmap(bitmap);
			return image;
		}
		public void onItemClick(AdapterView<?> adapterView, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Intent intent=new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(videos[position]), "video/*");
			getActivity().startActivity(intent);
		}
		
	}
	class POIAdapter extends BaseExpandableListAdapter{
		
		POI[] pois;
		UnScrollableGridView[] videos;
		int numColums;
		public POIAdapter(POI[] pois){
			this.pois=pois;
			videos=new UnScrollableGridView[pois.length];
			int screenWidth=DeviceHelper.getScreenWidth(getActivity());
			int screenHeight=DeviceHelper.getScreenHeight(getActivity());
			if (screenWidth>screenHeight){
				width=screenWidth/5;
				numColums=5;
			}else{
				width=screenWidth/3;
				numColums=3;
			}
			for (int i=0;i<videos.length;i++){
				videos[i]=new UnScrollableGridView(getActivity());
				VideoAdapter adapter=new VideoAdapter(pois[i].videoFiles);
				videos[i].setAdapter(adapter);
				videos[i].setOnItemClickListener(adapter);
				videos[i].setNumColumns(numColums);
			}
			
		}
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return videos[groupPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return groupPosition*1000+childPosition;
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
			textView.setText(pois[groupPosition].title+"("+String.valueOf(pois[groupPosition].videoFiles.length)+")");
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
