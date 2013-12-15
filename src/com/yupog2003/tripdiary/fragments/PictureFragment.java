package com.yupog2003.tripdiary.fragments;

import java.io.File;
import java.util.ArrayList;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.ViewPointActivity;
import com.yupog2003.tripdiary.data.DeviceHelper;
import com.yupog2003.tripdiary.data.FileHelper;
import com.yupog2003.tripdiary.views.CheckableLayout;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

public class PictureFragment extends Fragment implements OnItemClickListener{
	GridView layout;
	PictureAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		layout=(GridView)inflater.inflate(R.layout.fragment_picture, container,false);
		layout.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		layout.setMultiChoiceModeListener(new MyMultiChoiceModeListener());
		layout.setOnItemClickListener(this);
		adapter=new PictureAdapter();
		layout.setAdapter(adapter);
		return layout;
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		//adapter.run=false;
		super.onPause();
	}

	@Override
	public void onResume(){
		super.onResume();
		if (ViewPointActivity.poi.picDir.listFiles(FileHelper.getPictureFileFilter()).length!=adapter.getCount()){
			adapter.reFresh();
		}
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		if (outState.isEmpty()){
			outState.putBoolean("bug:fix", true);
		}
	}
	class PictureAdapter extends BaseAdapter{
		File[] files;
		int width;
		DisplayImageOptions options;
		Bitmap[] bitmaps;
		public PictureAdapter(){
			int screenWidth=DeviceHelper.getScreenWidth(getActivity());
			int screenHeight=DeviceHelper.getScreenHeight(getActivity());
			if (screenWidth>screenHeight){
				width=screenWidth/5;
				layout.setNumColumns(5);
			}else{
				width=screenWidth/3;
				layout.setNumColumns(3);
			}
			options=new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisc(false)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
			files=ViewPointActivity.poi.picFiles;
			bitmaps=new Bitmap[files.length];
			
		}
		public void reFresh(){
			ViewPointActivity.poi.updateAllFields();
			files=ViewPointActivity.poi.picFiles;
			bitmaps=new Bitmap[files.length];
			notifyDataSetChanged();
			
		}
		public int getCount() {
			// TODO Auto-generated method stub
			return files.length;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return files[position];
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView==null){
				ImageView i=new ImageView(getActivity());
				i.setMaxWidth(width);
				i.setMaxHeight(width);
				CheckableLayout l=new CheckableLayout(getActivity());
				l.setLayoutParams(new AbsListView.LayoutParams(width,width));
				l.setPadding(10, 10, 10, 10);
				l.addView(i);
				convertView=l;
				convertView.setTag(i);
			}
			if (bitmaps[position]==null){
				ImageLoader.getInstance().displayImage("file://"+files[position].getPath(), (ImageView)convertView.getTag(), options,new ImageLoadingListener() {
					
					public void onLoadingStarted(String imageUri, View view) {
						// TODO Auto-generated method stub
						
					}
					
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						// TODO Auto-generated method stub
						
					}
					
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						// TODO Auto-generated method stub
						bitmaps[position]=loadedImage;
					}
					
					public void onLoadingCancelled(String imageUri, View view) {
						// TODO Auto-generated method stub
						
					}
				});
			}else{
				((ImageView)convertView.getTag()).setImageBitmap(bitmaps[position]);
			}
			return convertView;
		}
		
	}
	
	class MyMultiChoiceModeListener implements GridView.MultiChoiceModeListener{
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
						adapter.reFresh();
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
							adapter.reFresh();
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
			intent.setType("image/*");
			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			shareProvider.setShareIntent(intent);
		}
		
	}

	public void onItemClick(AdapterView<?> av, View view, int position, long id) {
		// TODO Auto-generated method stub
		Intent intent=new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile((File)adapter.getItem(position)), "image/*");
		getActivity().startActivity(intent);
	}
	
}
