package com.yupog2003.tripdiary.fragments;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.yupog2003.tripdiary.CategoryActivity;
import com.yupog2003.tripdiary.MainActivity;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.data.DeviceHelper;
import com.yupog2003.tripdiary.data.MyBackupAgent;
import com.yupog2003.tripdiary.data.FileHelper.DirAdapter;
import com.yupog2003.tripdiary.data.TimeAnalyzer;
import com.yupog2003.tripdiary.preferences.SeekBarPreference;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PreferFragment extends PreferenceFragment implements OnPreferenceChangeListener,OnPreferenceClickListener{
	Preference musicpath;
	Preference help;
	Preference backupsetting;
	Preference restoresetting;
	Preference tripcategory;
	Preference diaryfont;
	Preference tripTimeZone;
	Preference account;
	Preference rootpath;
	ListPreference playingtripmode;
	ListPreference playtripspeed;
	ListPreference playpoispeed;
	SeekBarPreference diaryfontsize;
	
	private static final int selectmusicpath=0;
	private static final int selectdiaryfont=1;
	private static final String backedupPreferencePath=MainActivity.rootPath+"/.settings";
	private static final String categorySettingName="category";
	private static final String tripSettingName="trip";
	private static final String tripExpandSettingName="categoryExpand";
	private static final String tripTimezoneSettingName="tripTimezone";
	private static final String defaultSettingName="com.yupog2003.tripdiary_preferences";
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		File file=new File(backedupPreferencePath);
		if (!file.exists())file.mkdirs();
		musicpath=(Preference)findPreference("musicpath");
		musicpath.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("musicpath", getString(R.string.select_music_path)));
		musicpath.setOnPreferenceClickListener(this);
		help=(Preference)findPreference("help");
		help.setOnPreferenceClickListener(this);
		tripcategory=(Preference)findPreference("tripcategory");
		tripcategory.setOnPreferenceClickListener(this);
		diaryfont=(Preference)findPreference("diaryfont");
		diaryfont.setOnPreferenceClickListener(this);
		diaryfont.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("diaryfont",getString(R.string.select_diary_font)));
		diaryfontsize=(SeekBarPreference)findPreference("diaryfontsize");
		diaryfontsize.setSummary(String.valueOf(diaryfontsize.getProgress())+" pixels");
		diaryfontsize.setOnPreferenceChangeListener(this);
		playingtripmode=(ListPreference)findPreference("playingtripmode");
		playingtripmode.setSummary(playingtripmode.getEntry());
		playingtripmode.setOnPreferenceChangeListener(this);
		playtripspeed=(ListPreference)findPreference("playtripspeed");
		playtripspeed.setSummary(playtripspeed.getEntry());
		playtripspeed.setOnPreferenceChangeListener(this);
		playpoispeed=(ListPreference)findPreference("playpoispeed");
		playpoispeed.setSummary(playpoispeed.getEntry());
		playpoispeed.setOnPreferenceChangeListener(this);
		backupsetting=(Preference)findPreference("backupsetting");
		backupsetting.setOnPreferenceClickListener(this);
		restoresetting=(Preference)findPreference("restoresetting");
		restoresetting.setOnPreferenceClickListener(this);
		rootpath=(Preference)findPreference("rootpath");
		rootpath.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("rootpath", Environment.getExternalStorageDirectory().getPath()));
		rootpath.setOnPreferenceChangeListener(this);
		rootpath.setOnPreferenceClickListener(this);
		tripTimeZone=(Preference)findPreference("triptimezone");
		tripTimeZone.setOnPreferenceClickListener(this);
		account=(Preference)findPreference("account");
		account.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("account", ""));
		account.setOnPreferenceClickListener(this);
	}
	@SuppressLint("DefaultLocale")
	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent data){
		if (data!=null){
			SharedPreferences setting=getPreferenceManager().getSharedPreferences();
			SharedPreferences.Editor editor=setting.edit();
			switch(requestCode){
			case selectmusicpath:
				Cursor cursor=getActivity().getContentResolver().query(data.getData(), new String []{android.provider.MediaStore.Video.Media.DATA}, null, null, null);
				if (cursor!=null){
					cursor.moveToFirst();
					String s=cursor.getString(0);
					if (s!=null){
						editor.putString("musicpath", s);
						editor.commit();
						musicpath.setSummary(s);
					}
				}else{
					Toast.makeText(getActivity(), getString(R.string.not_a_music), Toast.LENGTH_SHORT).show();
				}
				break;
			case selectdiaryfont:
				String path=data.getData().getPath();
				if (path.toLowerCase().endsWith(".ttf")){
					editor.putString("diaryfont", path);
					editor.commit();
					diaryfont.setSummary(path);
				}else{
					Toast.makeText(getActivity(), getString(R.string.not_a_ttf_file), Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	}
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		if (preference.equals(musicpath)){
			Intent i3=new Intent(Intent.ACTION_GET_CONTENT);
			i3.setType("audio/*");
			startActivityForResult(Intent.createChooser(i3, getString(R.string.select_music_path)), selectmusicpath);
		}else if (preference.equals(help)){
			AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
    		ab.setTitle(getString(R.string.help));
    		TextView helptext=new TextView(getActivity());
    		helptext.setTextSize(20);
    		InputStream is=getResources().openRawResource(R.raw.help);
    		ByteArrayOutputStream bs=new ByteArrayOutputStream();
    		int i;
				try {
				i = is.read();
				while(i!=-1){
	    			bs.write(i);
	    			i=is.read();
	    		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		helptext.setText(bs.toString());
    		ScrollView scroll=new ScrollView(getActivity());
    		scroll.addView(helptext);
    		ab.setView(scroll);
    		ab.setPositiveButton(getString(R.string.enter), null);
    		ab.show();
		}else if (preference.equals(tripcategory)){
			getActivity().startActivity(new Intent(getActivity(),CategoryActivity.class));
		}else if (preference.equals(diaryfont)){
			Intent i=new Intent(Intent.ACTION_GET_CONTENT);
			i.setType("file/*");
			startActivityForResult(Intent.createChooser(i, getString(R.string.diary_font)), selectdiaryfont);
		}else if (preference.equals(backupsetting)){
			boolean b1=MyBackupAgent.saveSharedPreferencesToFile(getActivity(),defaultSettingName, new File(backedupPreferencePath+"/"+defaultSettingName));
			boolean b2=MyBackupAgent.saveSharedPreferencesToFile(getActivity(),categorySettingName, new File(backedupPreferencePath+"/"+categorySettingName));
			boolean b3=MyBackupAgent.saveSharedPreferencesToFile(getActivity(),tripSettingName, new File(backedupPreferencePath+"/"+tripSettingName));
			boolean b4=MyBackupAgent.saveSharedPreferencesToFile(getActivity(),tripExpandSettingName, new File(backedupPreferencePath+"/"+tripExpandSettingName));
			boolean b5=MyBackupAgent.saveSharedPreferencesToFile(getActivity(),tripTimezoneSettingName, new File(backedupPreferencePath+"/"+tripTimezoneSettingName));
			if (b1&&b2&&b3&b4&&b5)
				Toast.makeText(getActivity(), getString(R.string.setting_has_been_backed_up), Toast.LENGTH_SHORT).show();
		}else if (preference.equals(restoresetting)){
			boolean b1=MyBackupAgent.loadSharedPreferencesFromFile(getActivity(),defaultSettingName, new File(backedupPreferencePath+"/"+defaultSettingName));
			boolean b2=MyBackupAgent.loadSharedPreferencesFromFile(getActivity(),categorySettingName, new File(backedupPreferencePath+"/"+categorySettingName));
			boolean b3=MyBackupAgent.loadSharedPreferencesFromFile(getActivity(),tripSettingName, new File(backedupPreferencePath+"/"+tripSettingName));
			boolean b4=MyBackupAgent.loadSharedPreferencesFromFile(getActivity(),tripExpandSettingName, new File(backedupPreferencePath+"/"+tripExpandSettingName));
			boolean b5=MyBackupAgent.loadSharedPreferencesFromFile(getActivity(),tripTimezoneSettingName, new File(backedupPreferencePath+"/"+tripTimezoneSettingName));
			if (b1&&b2&&b3&&b4&&b5)
				Toast.makeText(getActivity(), getString(R.string.setting_has_been_restored), Toast.LENGTH_SHORT).show();
		}else if (preference.equals(rootpath)){
			AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
			ListView listView=new ListView(getActivity());
			final DirAdapter adapter=new DirAdapter(getActivity(), false, Environment.getExternalStorageDirectory());
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(adapter);
			ab.setTitle(getString(R.string.select_a_directory));
			ab.setView(listView);
			ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String newPath=adapter.getRoot().getPath();
					SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
					editor.putString("rootpath", newPath);
					editor.commit();
					rootpath.setSummary(newPath);
					MainActivity.rootPath=newPath;
				}
			});
			ab.setNegativeButton(getString(R.string.cancel), null);
			ab.show();
		}else if (preference.equals(tripTimeZone)){
			AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
			ab.setTitle(getString(R.string.update_trip_timezone));
			ab.setMessage(getString(R.string.explain_update_trip_timezone));
			ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (DeviceHelper.isMobileNetworkAvailable(getActivity())){
						new UpdateTripTimeZoneTask().execute();
					}else{
						Toast.makeText(getActivity(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
					}
				}
			});
			ab.setNegativeButton(getString(R.string.cancel), null);
			ab.show();
		}else if (preference.equals(account)){
			Account[] accounts = AccountManager.get(getActivity()).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
			final String[] names = new String[accounts.length];
			for (int i = 0; i < names.length; i++) {
				names[i] = accounts[i].name;
			}
			AlertDialog.Builder ab2 = new AlertDialog.Builder(getActivity());
			ab2.setTitle(getString(R.string.choose_a_account));
			ab2.setSingleChoiceItems(names, -1, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
					editor.putString("account", names[which]);
					editor.commit();
					account.setSummary(names[which]);
					dialog.dismiss();
				}
			});
			ab2.show();
		}
		return false;
	}
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		if (preference.equals(diaryfontsize)){
			diaryfontsize.setSummary(String.valueOf(newValue)+" pixels");
		}
		if (preference instanceof ListPreference){
			((ListPreference) preference).setValue((String)newValue);
			preference.setSummary(((ListPreference) preference).getEntry());
		}
		if (preference.equals(rootpath)){
			String newPath=String.valueOf(newValue);
			if (new File(newPath).isDirectory()){
				rootpath.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("rootpath", Environment.getExternalStorageDirectory().getPath()));
				SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
				editor.putString("rootpath", newPath);
				editor.commit();
				MainActivity.rootPath=newPath;
			}else{
				Toast.makeText(getActivity(), newPath+" "+getString(R.string.is_not_a_valid_directory), Toast.LENGTH_SHORT).show();
			}
		}
		return false;
	}
	class UpdateTripTimeZoneTask extends AsyncTask<String, String, String>{
		TextView message;
		ProgressBar progress;
		TextView progressMessage;
		AlertDialog dialog;
		boolean cancel=false;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
			ab.setTitle(getString(R.string.updating));
			LinearLayout layout=(LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.progressdialog_import_memory, null);
			message=(TextView)layout.findViewById(R.id.message);
			progress=(ProgressBar)layout.findViewById(R.id.progressBar);
			progressMessage=(TextView)layout.findViewById(R.id.progress);
			ab.setView(layout);
			ab.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					cancel=true;
				}
			});
			dialog=ab.create();
			dialog.show();
			super.onPreExecute();
		}
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			File[] trips=new File(MainActivity.rootPath).listFiles(new FileFilter() {
				
				public boolean accept(File pathname) {
					// TODO Auto-generated method stub
					return pathname.isDirectory()&&!pathname.getName().startsWith(".");
				}
			});
			publishProgress("setMax",String.valueOf(trips.length));
			for (int i=0;i<trips.length;i++){
				if (cancel)break;
				String tripName=trips[i].getName();
				publishProgress(tripName,String.valueOf(i));
				try {
					BufferedReader br=new BufferedReader(new FileReader(new File(trips[i].getPath()+"/"+tripName+".gpx")));
					String s;
					while((s=br.readLine())!=null){
						if (s.startsWith("<trkpt ")){
							String[] toks=s.split("\"");
							double lat,lng;
							if (s.indexOf("lat")>s.indexOf("lon")){
								lat=Double.parseDouble(toks[3]);
								lng=Double.parseDouble(toks[1]);
							}else{
								lat=Double.parseDouble(toks[1]);
								lng=Double.parseDouble(toks[3]);
							}
							TimeAnalyzer.updateTripTimeZoneFromLatLng(getActivity(), tripName, lat, lng);
							new File(trips[i].getPath()+"/"+tripName+".gpx.cache").delete();
							break;
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
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			super.onPostExecute(result);
		}

		

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			if (values[0].equals("setMax")){
				progress.setMax(Integer.valueOf(values[1]));
				progressMessage.setText("0/"+values[1]);
			}else{
				message.setText(values[0]);
				progress.setProgress(Integer.valueOf(values[1]));
				progressMessage.setText(values[1]+"/"+String.valueOf(progress.getMax()));
			}
			super.onProgressUpdate(values);
		}
		
	}
	
}
