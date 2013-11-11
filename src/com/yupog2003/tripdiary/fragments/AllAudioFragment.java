package com.yupog2003.tripdiary.fragments;

import java.io.File;
import java.util.ArrayList;

import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.ViewAllActivity;
import com.yupog2003.tripdiary.data.POI;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class AllAudioFragment extends Fragment{
	POI[] pois;
	ExpandableListView listView;
	public AllAudioFragment(){
		this.pois=ViewAllActivity.trip.pois;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if (listView==null){
			listView=new ExpandableListView(getActivity());
			POIAdapter adapter=new POIAdapter(pois);
			listView.setAdapter(adapter);
			listView.setOnChildClickListener(adapter);
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
	class POIAdapter extends BaseExpandableListAdapter implements OnChildClickListener{
		POI[] pois;
		ArrayList<File[]> audios;
		public POIAdapter(POI[] pois){
			this.pois=pois;
			audios=new ArrayList<File[]>();
			for (int i=0;i<pois.length;i++){
				audios.add(pois[i].audioFiles);
			}
		}
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return audios.get(groupPosition)[childPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return groupPosition*1000+childPosition;
		}

		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView textView=new TextView(getActivity());
			textView.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
			textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_music, 0, 0, 0);
			textView.setText(((File)getChild(groupPosition, childPosition)).getName());
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setPadding(50, 0, 0, 0);
			return textView;
		}

		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return audios.get(groupPosition).length;
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
			textView.setText(pois[groupPosition].title+"("+String.valueOf(audios.get(groupPosition).length)+")");
			textView.setGravity(Gravity.CENTER_VERTICAL);
			return textView;
		}

		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			Intent intent=new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile((File)getChild(groupPosition, childPosition)), "audio/*");
			getActivity().startActivity(intent);
			return true;
		}
		
	}
}
