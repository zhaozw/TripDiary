package com.yupog2003.tripdiary.fragments;

import java.io.File;

import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.ViewPointActivity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class TextFragment extends Fragment {
	TextView text;
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_text, container,false);
	}
	@Override
	public void onResume(){
		super.onResume();
		setText();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		if (outState.isEmpty()){
			outState.putBoolean("bug:fix", true);
		}
	}
	public void setText() {
		// TODO Auto-generated method stub
		text=(TextView)getView().findViewById(R.id.Text);
		text.setText("");
		File fontFile=new File(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("diaryfont", ""));
		if (fontFile!=null){
			if (fontFile.exists()&&fontFile.isFile()){
				try{
					text.setTypeface(Typeface.createFromFile(fontFile));
				}catch(RuntimeException e){
					Toast.makeText(getActivity(), getString(R.string.invalid_font), Toast.LENGTH_SHORT).show();
				}
			}
		}
		text.setTextSize(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("diaryfontsize", 20));
		
			text.setText(ViewPointActivity.poi.diary);
    	text.setLongClickable(true);
			text.setOnLongClickListener(new OnLongClickListener() {
				
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					Toast.makeText(getActivity(), getString(R.string.share_diary), Toast.LENGTH_LONG).show();
					Intent i=new Intent(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
					getActivity().startActivity(i);
					return false;
				}
			});
	}
	
}
