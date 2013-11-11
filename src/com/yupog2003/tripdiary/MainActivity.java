package com.yupog2003.tripdiary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.data.ColorHelper;
import com.yupog2003.tripdiary.data.PackageHelper;
import com.yupog2003.tripdiary.data.TimeAnalyzer;
import com.yupog2003.tripdiary.data.Trip;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends Activity implements Button.OnClickListener {
	Button startTrip;
	Button resumeTrip;
	Button viewHistory;
	public static String rootPath;
	public static final String serverURL = "http://140.113.121.20/TripDiary";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			rootPath = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("rootpath", Environment.getExternalStorageDirectory() + "/TripDiary");
			File file = new File(rootPath);
			if (!file.exists()) {
				file.mkdir();
				File nomedia = new File(rootPath + "/.nomedia");
				try {
					nomedia.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			File[] trips = file.listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					// TODO Auto-generated method stub
					return pathname.isDirectory() && !pathname.getName().startsWith(".");
				}
			});
			for (int i = 0; i < trips.length; i++) {
				String tripName = trips[i].getName();
				new File(trips[i].getPath() + "/" + tripName + ".gpx.lats").delete();
				new File(trips[i].getPath() + "/" + tripName + ".gpx.statistics").delete();
			}
		}
		SharedPreferences.Editor editor = getSharedPreferences("category", MODE_PRIVATE).edit();
		editor.putString("nocategory", String.valueOf(Color.WHITE));
		editor.commit();
		if (!PackageHelper.isAppInstalled(MainActivity.this, PackageHelper.GoogleMapPackageName)) {
			PackageHelper.askForInstallApp(MainActivity.this, PackageHelper.GoogleMapPackageName, getString(R.string.google_map));
		}
		startTrip = (Button) findViewById(R.id.starttrip);
		startTrip.setOnClickListener(this);
		viewHistory = (Button) findViewById(R.id.viewhistory);
		viewHistory.setOnClickListener(this);
		resumeTrip = (Button) findViewById(R.id.resume_trip);
		resumeTrip.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private boolean isGpsEnabled() {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	@SuppressWarnings("unchecked")
	private void startTripDialog() {
		final SharedPreferences categorysp = getSharedPreferences("category", MODE_PRIVATE);
		Map<String, String> map = (Map<String, String>) categorysp.getAll();
		Set<String> keyset = map.keySet();
		final String[] categories = keyset.toArray(new String[0]);
		AlertDialog.Builder ab2 = new AlertDialog.Builder(MainActivity.this);
		View layout = getLayoutInflater().inflate(R.layout.edit_trip, null);
		final RadioGroup rg = (RadioGroup) layout.findViewById(R.id.categories);
		final TextView category = (TextView) layout.findViewById(R.id.category);
		for (int i = 0; i < categories.length; i++) {
			RadioButton rb = new RadioButton(MainActivity.this);
			rb.setText(categories[i]);
			rb.setId(i);
			rg.addView(rb);
			if (categories[i].equals("nocategory")) {
				rg.check(i);
				String color = categorysp.getString(categories[i], String.valueOf(Color.WHITE));
				category.setCompoundDrawablesWithIntrinsicBounds(ColorHelper.getColorDrawable(MainActivity.this, 100, Integer.valueOf(color)), null, null, null);
			}
		}
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				String color = categorysp.getString(categories[checkedId], "Gray");
				category.setCompoundDrawablesWithIntrinsicBounds(ColorHelper.getColorDrawable(MainActivity.this, 100, Integer.valueOf(color)), null, null, null);
			}
		});
		ab2.setTitle(getString(R.string.Start_Trip));
		ab2.setView(layout);
		final EditText tripName = (EditText) layout.findViewById(R.id.tripname);
		final EditText tripNote = (EditText) layout.findViewById(R.id.tripnote);
		Calendar c = Calendar.getInstance();
		tripName.setText(String.valueOf(c.get(Calendar.YEAR)) + "-" + String.valueOf(c.get(Calendar.MONTH) + 1) + "-" + String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
		tripName.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				tripName.setText("");
			}
		});
		ab2.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				final String name = tripName.getText().toString();
				final String note = tripNote.getText().toString();
				final String category = categories[rg.getCheckedRadioButtonId()];
				if (name.length() > 0) {
					if (!new File(rootPath + "/" + name).exists()) {
						startTrip(name, note, category);
					} else {
						AlertDialog.Builder ab2 = new AlertDialog.Builder(MainActivity.this);
						ab2.setTitle(getString(R.string.same_trip));
						ab2.setMessage(getString(R.string.explain_same_trip));
						ab2.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								startTrip(name, note, category);
							}
						});
						ab2.setNegativeButton(getString(R.string.cancel), null);
						ab2.show();
					}
				} else {
					Toast.makeText(MainActivity.this, getString(R.string.input_the_trip_name), Toast.LENGTH_SHORT).show();
				}
			}
		});
		ab2.setNegativeButton(getString(R.string.cancel), null);
		ab2.show();

	}

	private void startTrip(String name, String note, String category) {
		Trip trip = new Trip(MainActivity.this, new File(rootPath + "/" + name));
		trip.setCategory(MainActivity.this, category);
		trip.updateNote(note);
		if (isGpsEnabled()) {
			Intent i = new Intent(MainActivity.this, RecordService.class);
			i.putExtra("name", name);
			i.putExtra("path", rootPath);
			i.putExtra("note", note);
			startService(i);
		} else {
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			NotificationCompat.Builder nb = new NotificationCompat.Builder(MainActivity.this);
			nb.setSmallIcon(R.drawable.ic_launcher);
			nb.setContentText(note);
			nb.setContentTitle(name);
			nb.setTicker(getString(R.string.Start_Trip));
			Intent i2 = new Intent(MainActivity.this, AddPointActivity.class);
			i2.putExtra("name", name);
			i2.putExtra("path", rootPath);
			i2.putExtra("isgpsenabled", false);
			PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, i2, PendingIntent.FLAG_UPDATE_CURRENT);
			nb.addAction(R.drawable.poi, getString(R.string.add_poi), pi);
			Intent i3 = new Intent(MainActivity.this, ViewMapActivity.class);
			i3.putExtra("name", name);
			i3.putExtra("path", rootPath);
			i3.putExtra("stoptrip", true);
			PendingIntent pi2 = PendingIntent.getActivity(MainActivity.this, 0, i3, PendingIntent.FLAG_UPDATE_CURRENT);
			nb.addAction(R.drawable.ic_stop, getString(R.string.stop), pi2);
			nb.setOngoing(true);
			nm.notify(0, nb.build());
		}
		EasyTracker.getInstance(MainActivity.this).send(MapBuilder.createEvent("Trip", "start", name, null).build());
		MainActivity.this.finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (isGpsEnabled()) {
			if (requestCode == 0)
				startTripDialog();
			else if (requestCode == 1)
				resumeTripDialog();
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.setting) {
			Intent intent = new Intent(MainActivity.this, PreferActivity.class);
			intent.putExtra("path", rootPath);
			startActivity(intent);
		}
		return false;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.equals(startTrip)) {
			if (isGpsEnabled()) {
				startTripDialog();
			} else {
				AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
				ab.setTitle(getString(R.string.gps_is_disabled));
				ab.setMessage(getString(R.string.explain_gpx));
				ab.setPositiveButton(getString(R.string.start_with_gps), new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Toast.makeText(getBaseContext(), getString(R.string.please_enable_the_gps_provider), Toast.LENGTH_SHORT).show();
						startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
					}
				});
				ab.setNegativeButton(getString(R.string.start_without_gps), new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						startTripDialog();
					}
				});
				ab.show();
			}
		} else if (v.equals(viewHistory)) {
			Intent i = new Intent(MainActivity.this, ViewActivity.class);
			i.putExtra("path", rootPath);
			MainActivity.this.startActivity(i);
		} else if (v.equals(resumeTrip)) {
			if (isGpsEnabled()) {
				resumeTripDialog();
			} else {
				Toast.makeText(getBaseContext(), getString(R.string.please_enable_the_gps_provider), Toast.LENGTH_SHORT).show();
				startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
			}
		}
	}

	class TripInformation {
		File file;
		String name;
		Time time;
	}

	public void resumeTripDialog() {
		AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
		File[] files = new File(rootPath).listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (pathname.getName().startsWith("."))
					return false;
				return pathname.isDirectory();
			}
		});
		ArrayList<TripInformation> trips = new ArrayList<MainActivity.TripInformation>();
		for (int i = 0; i < files.length; i++) {
			TripInformation trip = new TripInformation();
			trip.file = files[i];
			trip.name = files[i].getName();
			trip.time = TimeAnalyzer.getTripTime(rootPath, trip.name);
			trips.add(trip);
		}
		Collections.sort(trips, new Comparator<TripInformation>() {

			public int compare(TripInformation lhs, TripInformation rhs) {
				// TODO Auto-generated method stub
				if (lhs.time == null || rhs.time == null)
					return 0;
				else if (lhs.time.after(rhs.time))
					return -1;
				else if (rhs.time.after(lhs.time))
					return 1;
				else
					return 0;
			}
		});
		final String[] strs = new String[trips.size()];
		for (int i = 0; i < strs.length; i++) {
			strs[i] = trips.get(i).name;
		}
		ab.setSingleChoiceItems(strs, -1, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				Intent i = new Intent(MainActivity.this, RecordService.class);
				i.putExtra("name", strs[which]);
				i.putExtra("path", rootPath);
				StringBuffer sb = new StringBuffer();
				try {
					BufferedReader br = new BufferedReader(new FileReader(rootPath + "/" + strs[which] + "/note"));
					String s;
					while ((s = br.readLine()) != null) {
						sb.append(s + "\n");
					}
					br.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String result = sb.toString();
				if (result.endsWith("\n")) {
					result = result.substring(0, result.length() - 1);
				}
				i.putExtra("note", result);
				startService(i);
				EasyTracker.getInstance(MainActivity.this).send(MapBuilder.createEvent("Trip", "resume", strs[which], null).build());
				MainActivity.this.finish();
			}
		});
		ab.setTitle(getString(R.string.resume_trip));
		ab.setNegativeButton(getString(R.string.cancel), null);
		ab.show();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

}
