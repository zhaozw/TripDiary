package com.yupog2003.tripdiary;

import java.io.File;
import java.text.DecimalFormat;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.viewpagerindicator.TitlePageIndicator;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.data.FileHelper;
import com.yupog2003.tripdiary.data.POI;
import com.yupog2003.tripdiary.data.TimeAnalyzer;
import com.yupog2003.tripdiary.fragments.AudioFragment;
import com.yupog2003.tripdiary.fragments.PictureFragment;
import com.yupog2003.tripdiary.fragments.TextFragment;
import com.yupog2003.tripdiary.fragments.VideoFragment;

public class ViewPointActivity extends Activity {
	String path;
	String name;
	public static POI poi;
	public static final DecimalFormat latlngFormat = new DecimalFormat("#.######");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_point);
		path = getIntent().getStringExtra("path");
		name = path.substring(path.lastIndexOf("/") + 1);
		poi = new POI(new File(path));
		ImageLoaderConfiguration conf = new ImageLoaderConfiguration.Builder(ViewPointActivity.this).build();
		ImageLoader.getInstance().init(conf);
		setTitle(name);
		initialtab(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}

	private void initialtab(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		viewPager.setAdapter(new MyPagerAdapter(getFragmentManager()));
		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.titles);
		indicator.setViewPager(viewPager);
		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_view_point, menu);
		return true;
	}

	class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			Fragment fragment = null;
			switch (arg0) {
			case 0:
				fragment = new TextFragment();
				break;
			case 1:
				fragment = new PictureFragment();
				break;
			case 2:
				fragment = new VideoFragment();
				break;
			case 3:
				fragment = new AudioFragment();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			// TODO Auto-generated method stub
			String title = "";
			switch (position) {
			case 0:
				title = getString(R.string.diary);
				break;
			case 1:
				title = getString(R.string.photo);
				break;
			case 2:
				title = getString(R.string.video);
				break;
			case 3:
				title = getString(R.string.sound);
				break;
			}
			return title;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.importpicture) {
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.setType("image/*");
			startActivityForResult(Intent.createChooser(i, getString(R.string.select_the_picture_by)), R.id.importpicture);
		} else if (item.getItemId() == R.id.importvideo) {
			Intent i2 = new Intent(Intent.ACTION_GET_CONTENT);
			i2.setType("video/*");
			startActivityForResult(Intent.createChooser(i2, getString(R.string.select_the_video_by)), R.id.importvideo);
		} else if (item.getItemId() == R.id.importaudio) {
			Intent i3 = new Intent(Intent.ACTION_GET_CONTENT);
			i3.setType("audio/*");
			startActivityForResult(Intent.createChooser(i3, getString(R.string.select_the_audio_by)), R.id.importaudio);
		} else if (item.getItemId() == R.id.editdiary) {
			this.editText();
		} else if (item.getItemId() == R.id.playpoint) {
			Intent intent = new Intent(ViewPointActivity.this, PlayPointActivity.class);
			intent.putExtra("path", path);
			ViewPointActivity.this.startActivity(intent);
		} else if (item.getItemId() == android.R.id.home) {
			ViewPointActivity.this.finish();
		} else if (item.getItemId() == R.id.viewbasicinformation) {
			AlertDialog.Builder ab = new AlertDialog.Builder(ViewPointActivity.this);
			ab.setTitle(poi.title);
			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.view_basicinformation, null);
			TextView location = (TextView) layout.findViewById(R.id.location);
			location.setText("(" + latlngFormat.format(poi.latitude) + "," + latlngFormat.format(poi.longitude) + ")");
			TextView altitude = (TextView) layout.findViewById(R.id.altitude);
			altitude.setText(String.valueOf(poi.altitude) + "m");
			TextView time = (TextView) layout.findViewById(R.id.time);
			time.setText(TimeAnalyzer.formatInTimezone(poi.time, ViewMapActivity.trip.timezone));
			ab.setView(layout);
			ab.setPositiveButton(getString(R.string.edit), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					AlertDialog.Builder ab2 = new AlertDialog.Builder(ViewPointActivity.this);
					ab2.setTitle(getString(R.string.edit));
					View layout = getLayoutInflater().inflate(R.layout.edit_poi, null);
					final EditText edittitle = (EditText) layout.findViewById(R.id.edit_poi_title);
					edittitle.setText(poi.title);
					final EditText editlatitude = (EditText) layout.findViewById(R.id.edit_poi_latitude);
					editlatitude.setText(String.valueOf(poi.latitude));
					final EditText editlongitude = (EditText) layout.findViewById(R.id.edit_poi_longitude);
					editlongitude.setText(String.valueOf(poi.longitude));
					final EditText editaltitude = (EditText) layout.findViewById(R.id.edit_poi_altitude);
					editaltitude.setText(String.valueOf(poi.altitude));
					final DatePicker editdate = (DatePicker) layout.findViewById(R.id.edit_poi_date);
					final TimePicker edittime = (TimePicker) layout.findViewById(R.id.edit_poi_time);
					Time time = poi.time;
					time.switchTimezone(ViewMapActivity.trip.timezone);
					editdate.updateDate(time.year, time.month, time.monthDay);
					edittime.setIs24HourView(true);
					edittime.setCurrentHour(time.hour);
					edittime.setCurrentMinute(time.minute);

					ab2.setView(layout);
					ab2.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Time time = new Time(ViewMapActivity.trip.timezone);
							time.set(0, edittime.getCurrentMinute(), edittime.getCurrentHour(), editdate.getDayOfMonth(), editdate.getMonth(), editdate.getYear());
							time.switchTimezone(Time.TIMEZONE_UTC);
							poi.renamePOI(edittitle.getText().toString());
							poi.updateBasicInformation(edittitle.getText().toString(), time, Double.parseDouble(editlatitude.getText().toString()), Double.parseDouble(editlongitude.getText().toString()), Double.parseDouble(editaltitude.getText().toString()));
							Intent data = new Intent();
							data.putExtra("update", true);
							setResult(getIntent().getIntExtra("request_code", 1), data);
							ViewPointActivity.this.finish();
						}
					});
					ab2.setNegativeButton(getString(R.string.cancel), null);
					ab2.show();
				}
			});
			ab.setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					AlertDialog.Builder ab2 = new AlertDialog.Builder(ViewPointActivity.this);
					ab2.setMessage(getString(R.string.are_you_sure_to_delete));
					ab2.setTitle(getString(R.string.be_careful));
					ab2.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							poi.deleteSelf();
							Intent data = new Intent();
							data.putExtra("update", true);
							setResult(getIntent().getIntExtra("request_code", 2), data);
							ViewPointActivity.this.finish();
						}

					});
					ab2.setNegativeButton(getString(R.string.cancel), null);
					AlertDialog ad = ab2.create();
					ad.setIcon(R.drawable.ic_alert);
					ad.show();
				}
			});
			ab.show();
		} else if (item.getItemId() == R.id.viewcost) {
			Intent intent2 = new Intent(ViewPointActivity.this, ViewCostActivity.class);
			intent2.putExtra("path", path);
			intent2.putExtra("title", name);
			intent2.putExtra("option", ViewCostActivity.optionPOI);
			startActivity(intent2);
		}
		return false;
	}

	public void editText() {
		AlertDialog.Builder ab = new AlertDialog.Builder(ViewPointActivity.this);
		ab.setTitle(getString(R.string.edit_diary));
		final EditText diary = new EditText(ViewPointActivity.this);
		diary.setText(poi.diary);
		ab.setView(diary);
		ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				poi.updateDiary(diary.getText().toString());
			}
		});
		ab.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Uri uri = data.getData();
			Cursor cursor;
			String filepath, filename;
			if (requestCode == R.id.importpicture) {
				cursor = getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.Media.DATA }, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();
					filepath = cursor.getString(0);
					if (filepath != null) {
						filename = filepath.substring(filepath.lastIndexOf("/") + 1);
						FileHelper.copyFile(new File(filepath), new File(path + "/pictures/" + filename));
						// ViewPointActivity.this.recreate();
					}
				} else {
					Toast.makeText(ViewPointActivity.this, getString(R.string.not_a_picture), Toast.LENGTH_SHORT).show();
				}
			} else if (requestCode == R.id.importvideo) {
				cursor = getContentResolver().query(uri, new String[] { android.provider.MediaStore.Video.Media.DATA }, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();
					filepath = cursor.getString(0);
					if (filepath != null) {
						filename = filepath.substring(filepath.lastIndexOf("/") + 1);
						FileHelper.copyFile(new File(filepath), new File(path + "/videos/" + filename));
					}
				} else {
					Toast.makeText(ViewPointActivity.this, getString(R.string.not_a_video), Toast.LENGTH_SHORT).show();
				}
			} else if (requestCode == R.id.importaudio) {
				cursor = getContentResolver().query(uri, new String[] { android.provider.MediaStore.Audio.Media.DATA }, null, null, null);
				if (cursor != null) {
					cursor.moveToFirst();
					filepath = cursor.getString(0);
					if (filepath != null) {
						filename = filepath.substring(filepath.lastIndexOf("/") + 1);
						FileHelper.copyFile(new File(filepath), new File(path + "/audios/" + filename));
					}
				} else {
					Toast.makeText(ViewPointActivity.this, getString(R.string.not_a_audio), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

}
