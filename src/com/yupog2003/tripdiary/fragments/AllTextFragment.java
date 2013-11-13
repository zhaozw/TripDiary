package com.yupog2003.tripdiary.fragments;

import java.io.File;

import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.ViewTripActivity;
import com.yupog2003.tripdiary.data.POI;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class AllTextFragment extends Fragment{
	POI[] pois;
	ExpandableListView listView;
	public AllTextFragment(){
		this.pois=ViewTripActivity.trip.pois;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
	class POIAdapter extends BaseExpandableListAdapter{
		POI[] pois;
		String [] diarys;
		public POIAdapter(POI[] pois){
			this.pois=pois;
			diarys=new String[pois.length];
			for (int i=0;i<diarys.length;i++){
				diarys[i]=pois[i].diary;
				if (diarys[i].length()>0){
					diarys[i]=diarys[i].substring(0, diarys[i].length()-1);
				}
			}
		}
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return diarys[groupPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView textView=new TextView(getActivity());
			textView.setText(diarys[groupPosition]);
			textView.setPadding(50, 0, 0, 0);
			File fontFile=new File(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("diaryfont", ""));
			if (fontFile!=null){
				if (fontFile.exists()&&fontFile.isFile()){
					try{
						textView.setTypeface(Typeface.createFromFile(fontFile));
					}catch(RuntimeException e){
						Toast.makeText(getActivity(), getString(R.string.invalid_font), Toast.LENGTH_SHORT).show();
					}
				}
			}
			textView.setTextSize(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("diaryfontsize", 20));
			return textView;
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
			textView.setText(pois[groupPosition].title+"("+String.valueOf(diarys[groupPosition].length())+")");
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
