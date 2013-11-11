package com.yupog2003.tripdiary.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.ViewCostActivity;
import android.app.Fragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class CostBarChartFragment extends Fragment{
	String path;
	String title;
	int option;
	int [] colors;
	float [] totals;
	boolean hasData=false;
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_cost_barchart, container,false);
		
	}
	@Override
	public void onResume(){
		super.onResume();
		this.option=getActivity().getIntent().getIntExtra("option", 0);
		this.path=getActivity().getIntent().getStringExtra("path");
		this.title=getActivity().getIntent().getStringExtra("title");
		TypedArray array=getActivity().getResources().obtainTypedArray(R.array.cost_type_colors);
		colors=new int[array.length()];
		for (int i=0;i<colors.length;i++)colors[i]=array.getColor(i, 0);
		totals=new float[array.length()];
		switch(option){
		case ViewCostActivity.optionPOI:
			File file=new File(path+"/costs");
			File [] files=file.listFiles();
			for (int i=0;i<files.length;i++){
				readData(files[i]);
				hasData=true;
			}
			break;
		case ViewCostActivity.optionTrip:
			File file2=new File(path);
			File [] files2=file2.listFiles();
			for (int i=0;i<files2.length;i++){
				File file3=new File(files2[i].getPath()+"/costs");
				if (!file3.exists())file3.mkdir();
				File [] files3=file3.listFiles();
				if (files3!=null){
					for (int j=0;j<files3.length;j++){
						readData(files3[j]);
						hasData=true;
					}
				}
			}
			break;
		}
		XYMultipleSeriesRenderer render=new XYMultipleSeriesRenderer();
		render.setXAxisMax(4.5);
		render.setXAxisMin(0.5);
		render.setLegendTextSize(30);
		String [] titles=getActivity().getResources().getStringArray(R.array.cost_types);
		XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset();
		for (int i=0;i<totals.length;i++){
			CategorySeries series=new CategorySeries(titles[i]);
			for (int j=0;j<totals.length;j++){
				if (i==j){
					series.add(titles[i],totals[j]);
				}else{
					series.add(0);
				}
			}
			dataset.addSeries(series.toXYSeries());
			SimpleSeriesRenderer r=new SimpleSeriesRenderer();
			r.setColor(colors[i]);
			render.addSeriesRenderer(r);
		}
		FrameLayout layout=(FrameLayout) getView();
		layout.addView(ChartFactory.getBarChartView(getActivity(), dataset, render, Type.DEFAULT));
	}
	private void readData(File file){
		try {
			BufferedReader br=new BufferedReader(new FileReader(file));
			String s;int type=-1;
			while((s=br.readLine())!=null){
				if (s.startsWith("type=")){
					type=Integer.valueOf(s.substring(s.indexOf("=")+1));
				}else if (s.startsWith("dollar=")){
					totals[type]+=Float.valueOf(s.substring(s.indexOf("=")+1));
					type=-1;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
