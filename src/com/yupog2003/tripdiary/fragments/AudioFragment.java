package com.yupog2003.tripdiary.fragments;

import java.io.File;
import java.util.ArrayList;

import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.ViewPointActivity;
import com.yupog2003.tripdiary.views.CheckableLayout;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

public class AudioFragment extends Fragment{
	ListView layout;
	AudioAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		return inflater.inflate(R.layout.fragment_audio, container,false);
	}
	@Override
	public void onResume(){
		super.onResume();
		setAudio();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		if (outState.isEmpty()){
			outState.putBoolean("bug:fix", true);
		}
	}
	public void setAudio(){
		layout=(ListView)getView().findViewById(R.id.audiolistview);
		ViewPointActivity.poi.updateAllFields();
		adapter=new AudioAdapter();
		layout.setAdapter(adapter);
		layout.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		layout.setMultiChoiceModeListener(new MyMultiChoiceModeListener());
		layout.setOnItemClickListener(adapter);
	}
	class MyMultiChoiceModeListener implements ListView.MultiChoiceModeListener{
		boolean [] checks;
		boolean checkAll;
		ShareActionProvider shareProvider;
		public MyMultiChoiceModeListener(){
			
		}
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			final ArrayList<File> checksName=new ArrayList<File>();
			for (int i=0;i<checks.length;i++){
				if (checks[i]){
					checksName.add((File)adapter.getItem(i));
				}
			}
			if (item.getItemId() == R.id.delete) {
				AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
				ab.setTitle(getString(R.string.be_careful));
				ab.setMessage(getString(R.string.are_you_sure_to_delete));
				ab.setIcon(R.drawable.ic_alert);
				ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						for (int i=0;i<checksName.size();i++){
							checksName.get(i).delete();
						}
						setAudio();
					}
				});
				ab.setNegativeButton(getString(R.string.cancel), null);
				ab.show();
				mode.finish();
			} else if (item.getItemId() == R.id.rename) {
				if (checksName.size()>1){
					
				}else{
					final File checkName=checksName.get(0);
					AlertDialog.Builder ab2=new AlertDialog.Builder(getActivity());
					ab2.setTitle(getString(R.string.filename));
					final EditText name=new EditText(getActivity());
					name.setText(checkName.getName());
					ab2.setView(name);
					ab2.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							File file=checkName;
							String s=name.getText().toString();
							file.renameTo(new File(checkName.getParent()+"/"+s));
							setAudio();
						}
					});
					ab2.show();
					mode.finish();
				}
			} else if (item.getItemId() == R.id.selectall) {
				for (int i=0;i<layout.getCount();i++){
					layout.setItemChecked(i, !checkAll);
				}
				checkAll=!checkAll;
			}
			return true;
		}

		public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			mode.getMenuInflater().inflate(R.menu.poi_menu, menu);
			shareProvider=(ShareActionProvider)mode.getMenu().findItem(R.id.share).getActionProvider();
			shareProvider.setOnShareTargetSelectedListener(new OnShareTargetSelectedListener() {
				
				public boolean onShareTargetSelected(ShareActionProvider source,
						Intent intent) {
					// TODO Auto-generated method stub
					mode.finish();
					return false;
				}
			});
			checks=new boolean[adapter.getCount()];
			for (int i=0;i<checks.length;i++){
				checks[i]=false;
			}
			checkAll=false;
			return true;
		}

		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			mode=null;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return true;
		}

		public void onItemCheckedStateChanged(final ActionMode mode, int position,
				long id, boolean checked) {
			// TODO Auto-generated method stub
			checks[position]=checked;
			ArrayList<Uri> uris=new ArrayList<Uri>();
			int selects=0;
			for (int i=0;i<checks.length;i++){
				if (checks[i]){
					uris.add(Uri.fromFile((File)adapter.getItem(i)));
					selects++;
				}
			}
			mode.getMenu().findItem(R.id.rename).setVisible(!(selects>1));
			Intent intent=new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("audio/*");
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			shareProvider.setShareIntent(intent);
		}
		
	}
	
	class AudioAdapter extends BaseAdapter implements OnItemClickListener{
		File [] audios;
		public AudioAdapter(){
			audios=ViewPointActivity.poi.audioFiles;
		}
		public int getCount() {
			// TODO Auto-generated method stub
			if (audios==null)return 0;
			return audios.length;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return audios[position];
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView==null){
				TextView textView=new TextView(getActivity());
				textView.setTextSize(30);
				textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_music, 0, 0, 0);
				textView.setText(audios[position].getName());
				CheckableLayout l=new CheckableLayout(getActivity());
				l.addView(textView);
				convertView=l;
			}else{
				convertView.setTag(convertView.getTag());
			}
			return convertView;
		}
		public void onItemClick(AdapterView<?> av, View view, int position, long id) {
			// TODO Auto-generated method stub
			Intent intent=new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(audios[position]), "audio/*");
			getActivity().startActivity(intent);
		}
	}

	
	
}
