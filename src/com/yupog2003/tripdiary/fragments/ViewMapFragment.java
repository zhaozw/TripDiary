package com.yupog2003.tripdiary.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.input.CountingInputStream;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.yupog2003.tripdiary.PlayPointActivity;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.ViewCostActivity;
import com.yupog2003.tripdiary.ViewPointActivity;
import com.yupog2003.tripdiary.ViewTripActivity;
import com.yupog2003.tripdiary.data.DeviceHelper;
import com.yupog2003.tripdiary.data.FileHelper;
import com.yupog2003.tripdiary.data.GpxAnalyzer2;
import com.yupog2003.tripdiary.data.GpxAnalyzer2.ProgressChangedListener;
import com.yupog2003.tripdiary.data.MyLatLng2;
import com.yupog2003.tripdiary.data.POI;
import com.yupog2003.tripdiary.data.PackageHelper;
import com.yupog2003.tripdiary.data.TimeAnalyzer;
import com.yupog2003.tripdiary.data.FileHelper.DirAdapter;
import com.yupog2003.tripdiary.services.SendTripService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ViewMapFragment extends Fragment implements OnInfoWindowClickListener, OnMapLongClickListener, OnMarkerDragListener, OnClickListener {
	MapFragment mapFragment;
	GoogleMap gmap;
	String path;
	String name;
	ArrayList<Marker> markers;
	LatLng[] lat;
	ImageButton viewInformation;
	ImageButton switchMapMode;
	ImageButton playTrip;
	ImageButton stopTrip;
	ImageButton fastforward;
	ImageButton slowforward;
	ImageButton showBar;
	ImageButton viewGraph;
	ImageButton viewCost;
	ImageButton viewNote;
	SeekBar processSeekBar;
	TextView time;
	boolean showAll = true;
	boolean uploadPublic;
	SearchView search;
	Thread playThread;
	PlayRunnable playRunnable;
	SetLocus setLocusTask;
	Handler handler;
	int trackColor;
	LinearLayout buttonBar;
	String token;
	String account;

	private static final int import_track = 0;
	private static final int update_request = 1;
	private static final int REQUEST_GET_TOKEN = 2;
	// private static final int playtriptype_normal=0;
	private static final int playtriptype_skyview = 1;
	private static final int request_write_location_to_POI = 1;
	private static final int analysis_method_jni = 0;
	private static final int analysis_method_java = 1;
	View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		rootView = inflater.inflate(R.layout.fragment_view_map, null);
		mapFragment = MapFragment.newInstance();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.maplayout, mapFragment, "mapFragment");
		ft.commit();
		buttonBar = (LinearLayout) rootView.findViewById(R.id.buttonbar);
		path = ViewTripActivity.path;
		name = ViewTripActivity.name;
		trackColor = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("trackcolor", 0xff6699cc);
		if (getActivity().getIntent().getBooleanExtra("stoptrip", false)) {
			NotificationManager nm = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(0);
		}
		if (new File(path + "/" + name + "/" + name + ".gpx").length() > 0) {
			setLocusTask = new SetLocus();
			setLocusTask.execute(0);
		} else {
			handleCannotFindGpx();
		}
		markers = new ArrayList<Marker>();
		handler = new Handler();
		viewInformation = (ImageButton) rootView.findViewById(R.id.viewinformation);
		switchMapMode = (ImageButton) rootView.findViewById(R.id.switchmapmode);
		playTrip = (ImageButton) rootView.findViewById(R.id.playtrip);
		stopTrip = (ImageButton) rootView.findViewById(R.id.stoptrip);
		fastforward = (ImageButton) rootView.findViewById(R.id.fastforward);
		slowforward = (ImageButton) rootView.findViewById(R.id.slowforward);
		showBar = (ImageButton) rootView.findViewById(R.id.showbar);
		viewGraph = (ImageButton) rootView.findViewById(R.id.viewgraph);
		viewCost = (ImageButton) rootView.findViewById(R.id.viewcost);
		viewNote = (ImageButton) rootView.findViewById(R.id.viewnote);
		time = (TextView) rootView.findViewById(R.id.time);
		processSeekBar = (SeekBar) rootView.findViewById(R.id.playProcess);
		viewInformation.setOnClickListener(this);
		playTrip.setOnClickListener(this);
		stopTrip.setOnClickListener(this);
		fastforward.setOnClickListener(this);
		slowforward.setOnClickListener(this);
		switchMapMode.setOnClickListener(this);
		showBar.setOnClickListener(this);
		viewGraph.setOnClickListener(this);
		viewCost.setOnClickListener(this);
		viewNote.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setHasOptionsMenu(true);
		if (gmap == null) {
			gmap = mapFragment.getMap();
			if (gmap != null) {
				gmap.setMyLocationEnabled(true);
				gmap.setOnInfoWindowClickListener(this);
				gmap.setOnMapLongClickListener(this);
				gmap.setOnMarkerDragListener(this);
				if (ViewTripActivity.trip != null)
					setPOIs();
			}
		}
		if (playRunnable != null) {
			playRunnable.onResume();
		}

	}

	@Override
	public void onPause() {
		if (playRunnable != null) {
			playRunnable.onPause();
		}
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.fragment_view_map, menu);
		search = (SearchView) menu.findItem(R.id.searchviewmap).getActionView();
		search.setQueryHint(getString(R.string.search_poi));
		search.setOnQueryTextListener(new OnQueryTextListener() {

			public boolean onQueryTextSubmit(String query) {
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
				search.clearFocus();
				final String searchname = search.getQuery().toString();
				if (!searchname.equals("")) {
					final ArrayList<Marker> founds = new ArrayList<Marker>();
					final int markersSize = markers.size();
					for (int i = 0; i < markersSize; i++) {
						if (markers.get(i).getTitle().contains(searchname)) {
							founds.add(markers.get(i));
						}
					}
					if (founds.size() == 0) {
						Toast.makeText(getActivity(), getString(R.string.poi_not_found), Toast.LENGTH_SHORT).show();
					} else if (founds.size() == 1) {
						gmap.animateCamera(CameraUpdateFactory.newLatLng(founds.get(0).getPosition()));
						founds.get(0).showInfoWindow();
					} else {
						AlertDialog.Builder choose = new AlertDialog.Builder(getActivity());
						choose.setTitle(getString(R.string.choose_the_poi));
						String[] foundsname = new String[founds.size()];
						for (int j = 0; j < founds.size(); j++) {
							foundsname[j] = founds.get(j).getTitle();
						}
						choose.setSingleChoiceItems(foundsname, -1, new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								gmap.animateCamera(CameraUpdateFactory.newLatLng(founds.get(which).getPosition()));
								founds.get(which).showInfoWindow();
								dialog.dismiss();
							}
						});
						choose.show();
					}
				}
				return false;
			}

			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		return;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getActivity().finish();
		} else if (item.getItemId() == R.id.clearcache) {
			if (ViewTripActivity.trip != null) {
				ViewTripActivity.trip.deleteCache();
				Toast.makeText(getActivity(), getString(R.string.cache_has_been_cleared), Toast.LENGTH_SHORT).show();
				EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "clear_cache", ViewTripActivity.trip.dir.getName(), null).build());
			}
		} else if (item.getItemId() == R.id.streetview) {
			if (PackageHelper.isAppInstalled(getActivity(), PackageHelper.StreetViewPackageNmae)) {
				final RelativeLayout mapLayout = (RelativeLayout) rootView.findViewById(R.id.maplayout);
				final ImageButton streetman = new ImageButton(getActivity());
				streetman.setImageResource(R.drawable.ic_streetman);
				streetman.setBackgroundColor(Color.TRANSPARENT);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.CENTER_IN_PARENT);
				mapLayout.addView(streetman, params);
				streetman.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						// TODO Auto-generated method stub
						mapLayout.removeView(streetman);
						LatLng latlng = gmap.getCameraPosition().target;
						Uri uri = Uri.parse("google.streetview:cbll=" + String.valueOf(latlng.latitude) + "," + String.valueOf(latlng.longitude));
						Intent intent2 = new Intent(Intent.ACTION_VIEW, uri);
						if (intent2.resolveActivity(getActivity().getPackageManager()) != null) {
							getActivity().startActivity(intent2);
						} else {
							Toast.makeText(getActivity(), getString(R.string.street_view_is_not_available), Toast.LENGTH_SHORT).show();
						}
					}
				});
				Toast.makeText(getActivity(), getString(R.string.explain_how_to_use_street_view), Toast.LENGTH_LONG).show();
				EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "streetview", ViewTripActivity.trip.dir.getName(), null).build());
			} else {
				PackageHelper.askForInstallApp(getActivity(), PackageHelper.StreetViewPackageNmae, getString(R.string.street_view));
			}
		} else if (item.getItemId() == R.id.sharetrackby) {
			AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
			ab.setTitle(getString(R.string.share_track_by___));
			String[] bys = new String[] { getString(R.string.gpx), getString(R.string.kml), getString(R.string.app_name) };
			ab.setSingleChoiceItems(bys, -1, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					switch (which) {
					case 0:
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("application/gpx+xml");
						intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(ViewTripActivity.trip.gpxFile));
						getActivity().startActivity(intent);
						EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "share_track_by_gpx", ViewTripActivity.trip.dir.getName(), null).build());
						break;
					case 1:
						AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
						ab.setTitle(getString(R.string.share_track_kml));
						final EditText filepath = new EditText(getActivity());
						filepath.setText(path + "/" + name + "/" + name + ".kml");
						ab.setView(filepath);
						ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								File kmlFile = new File(filepath.getText().toString());
								FileHelper.convertToKml(markers, lat, kmlFile, ViewTripActivity.trip.note);
								Intent intent = new Intent(Intent.ACTION_SEND);
								intent.setType("application/vnd.google-earth.kml+xml");
								intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(kmlFile));
								getActivity().startActivity(intent);
								EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "share_track_by_kml", ViewTripActivity.trip.dir.getName(), null).build());
							}
						});
						ab.setNegativeButton(getString(R.string.cancel), null);
						ab.show();
						break;
					case 2:
						Account[] accounts = AccountManager.get(getActivity()).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
						if (accounts != null && accounts.length > 0) {
							account = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("account", accounts[0].name);
							AlertDialog.Builder ab2 = new AlertDialog.Builder(getActivity());
							ab2.setTitle(getString(R.string.make_it_public));
							ab2.setMessage(getString(R.string.make_it_public_to_share));
							ab2.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									uploadPublic = true;
									new GetAccessTokenTask().execute();
								}
							});
							ab2.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									uploadPublic = false;
									new GetAccessTokenTask().execute();
								}
							});
							ab2.show();
						}
						break;
					}
					dialog.dismiss();
				}
			});
			ab.show();
		} else if (item.getItemId() == R.id.importmemory) {
			AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
			ab.setTitle(getString(R.string.import_memory));
			ab.setMessage(getString(R.string.import_memory_explanation));
			ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
					ListView listView = new ListView(getActivity());
					final DirAdapter adapter = new DirAdapter(getActivity(), false, Environment.getExternalStorageDirectory());
					listView.setAdapter(adapter);
					listView.setOnItemClickListener(adapter);
					ab.setTitle(getString(R.string.select_a_directory));
					ab.setView(listView);
					ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "import_memory", ViewTripActivity.trip.dir.getName(), null).build());
							new ImportMemoryTask(ViewTripActivity.trip.dir, adapter.getRoot()).execute();
						}
					});
					ab.setNegativeButton(getString(R.string.cancel), null);
					ab.show();
				}
			});
			ab.setNegativeButton(getString(R.string.cancel), null);
			ab.show();
		}
		return true;
	}

	class ImportMemoryTask extends AsyncTask<File, String, String> {

		File tripFile;
		File memory;
		TextView message;
		ProgressBar progress;
		TextView progressMessage;
		AlertDialog dialog;
		boolean cancel = false;

		public ImportMemoryTask(File trip, File memory) {
			this.tripFile = trip;
			this.memory = memory;

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
			ab.setTitle(getString(R.string.importing));
			LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.progressdialog_import_memory, null);
			message = (TextView) layout.findViewById(R.id.message);
			progress = (ProgressBar) layout.findViewById(R.id.progressBar);
			progressMessage = (TextView) layout.findViewById(R.id.progress);
			ab.setView(layout);
			ab.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					cancel = true;
				}
			});
			dialog = ab.create();
			dialog.show();
		}

		@Override
		protected String doInBackground(File... params) {
			// TODO Auto-generated method stub
			publishProgress("Sorting...", "0");
			if (ViewTripActivity.trip.pois.length < 1)
				return null;
			HashMap<Long, POI> pois = new HashMap<Long, POI>();
			for (int i = 0; i < ViewTripActivity.trip.pois.length; i++) {
				Time time = ViewTripActivity.trip.pois[i].time;
				time.switchTimezone(ViewTripActivity.trip.timezone);
				pois.put(time.toMillis(true), ViewTripActivity.trip.pois[i]);
			}
			Set<Long> set = pois.keySet();
			Long[] poiTimes = set.toArray(new Long[0]);
			Arrays.sort(poiTimes);
			File[] memories = memory.listFiles(new FileFilter() {

				public boolean accept(File pathname) {
					// TODO Auto-generated method stub
					if (pathname.isDirectory())
						return false;
					String mime = FileHelper.getMimeFromFile(pathname);
					if (mime.startsWith("image") || mime.startsWith("video") || mime.startsWith("audio"))
						return true;
					return false;
				}
			});
			publishProgress("setMax", String.valueOf(memories.length));
			HashMap<Long, File> mems = new HashMap<Long, File>();
			for (int i = 0; i < memories.length; i++) {
				String mime = FileHelper.getMimeFromFile(memories[i]);
				if (mime.equals("image/jpeg")) {
					try {
						ExifInterface exif = new ExifInterface(memories[i].getPath());
						String datetime = exif.getAttribute(ExifInterface.TAG_DATETIME);
						if (datetime != null) {
							String date = datetime.split(" ")[0];
							String time = datetime.split(" ")[1];
							String[] dates = date.split(":");
							String[] times = time.split(":");
							Time time1 = new Time(ViewTripActivity.trip.timezone);
							time1.set(Integer.valueOf(times[2]), Integer.valueOf(times[1]), Integer.valueOf(times[0]), Integer.valueOf(dates[2]), Integer.valueOf(dates[1]) - 1, Integer.valueOf(dates[0]));
							mems.put(time1.toMillis(false), memories[i]);
							continue;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mems.put(memories[i].lastModified(), memories[i]);
			}
			set = mems.keySet();
			Long[] memTimes = set.toArray(new Long[0]);
			Arrays.sort(memTimes);
			int key = 0;
			long[] poisMiddleTime = new long[poiTimes.length - 1];
			for (int i = 0; i < poisMiddleTime.length; i++) {
				poisMiddleTime[i] = (poiTimes[i] + poiTimes[i + 1]) / 2;
			}
			boolean coppied = false;
			for (int i = 0; i < memTimes.length; i++) {
				if (cancel)
					break;
				for (int j = key; j < poisMiddleTime.length; j++) {
					if (memTimes[i] <= poisMiddleTime[j]) {
						File infile = mems.get(memTimes[i]);
						String mime = FileHelper.getMimeFromFile(infile);
						String path = "";
						if (mime.startsWith("image"))
							path = "pictures";
						else if (mime.startsWith("video"))
							path = "videos";
						else if (mime.startsWith("audio"))
							path = "audios";
						publishProgress(infile.getName(), String.valueOf(i));
						FileHelper.copyFile(infile, new File(pois.get(poiTimes[j]).dir.getPath() + "/" + path + "/" + infile.getName()));
						key = j;
						coppied = true;
						break;
					}
				}
				if (!coppied) {
					File infile = mems.get(memTimes[i]);
					String mime = FileHelper.getMimeFromFile(infile);
					String path = "";
					if (mime.startsWith("image"))
						path = "pictures";
					else if (mime.startsWith("video"))
						path = "videos";
					else if (mime.startsWith("audio"))
						path = "audios";
					publishProgress(infile.getName(), String.valueOf(i));
					FileHelper.copyFile(infile, new File(pois.get(poiTimes[poiTimes.length - 1]).dir.getPath() + "/" + path + "/" + infile.getName()));
				}
				coppied = false;
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			if (values[0].equals("setMax")) {
				progress.setMax(Integer.valueOf(values[1]));
				progressMessage.setText("0/" + values[1]);
			} else {
				message.setText(values[0]);
				progress.setProgress(Integer.valueOf(values[1]));
				progressMessage.setText(values[1] + "/" + String.valueOf(progress.getMax()));
			}
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			ViewTripActivity.trip.refreshPOIs();
			AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
			ab.setTitle(getString(R.string.finish));
			ab.setMessage(getString(R.string.finish_import_memory));
			ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					FileHelper.deletedir(memory.getPath());
				}
			});
			ab.setNegativeButton(getString(R.string.cancel), null);
			ab.show();
		}
	}

	private void handleCannotFindGpx() {
		AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
		ab.setTitle(getString(R.string.cannot_find_gpx_data));
		ab.setMessage(getString(R.string.ask_import_gpx_file));
		ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				importGpx();
			}
		});
		ab.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Toast.makeText(getActivity(), getString(R.string.there_is_no_data_to_display), Toast.LENGTH_LONG).show();
				getActivity().finish();
			}
		});
		ab.show();
	}

	private void importGpx() {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.setType("file/*");
		Intent i2 = Intent.createChooser(i, getString(R.string.select_the_gpx));
		startActivityForResult(i2, import_track);
	}

	private void updateAll() {
		if (gmap != null && lat != null) {
			gmap.clear();
			gmap.addPolyline(new PolylineOptions().add(lat).width(5).color(trackColor));
			setPOIs();
		}
	}

	private void setPOIs() {
		if (markers == null || gmap == null || ViewTripActivity.trip.pois == null || !isAdded())
			return;
		markers.clear();
		ViewTripActivity.trip.refreshPOIs();
		for (int i = 0; i < ViewTripActivity.trip.pois.length; i++) {
			markers.add(gmap.addMarker(new MarkerOptions().position(new LatLng(ViewTripActivity.trip.pois[i].latitude, ViewTripActivity.trip.pois[i].longitude)).title(ViewTripActivity.trip.pois[i].title).snippet(TimeAnalyzer.formatInTimezone(ViewTripActivity.trip.pois[i].time, ViewTripActivity.trip.timezone)).draggable(true)));
		}
		List<String> markerNames = new ArrayList<String>();
		markerNames.add(getString(R.string.select_poi));
		final int markersSize = markers.size();
		for (int i = 0; i < markersSize; i++) {
			markerNames.add(markers.get(i).getTitle());
		}
		SpinnerAdapter POIsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, markerNames);
		getActivity().getActionBar().setListNavigationCallbacks(POIsAdapter, new OnNavigationListener() {

			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				// ViewTripActivity.trip
				if (itemPosition > 0) {
					itemPosition--;
					gmap.animateCamera(CameraUpdateFactory.newLatLng(markers.get(itemPosition).getPosition()));
					markers.get(itemPosition).showInfoWindow();
				}
				return true;
			}
		});
	}

	private void writeLocationToPoint(ArrayList<MyLatLng2> locus) {
		ArrayList<Time> times = new ArrayList<Time>();
		for (int i = 0; i < locus.size(); i++) {
			Time time = TimeAnalyzer.getTime(ViewTripActivity.trip.timezone, locus.get(i).time, TimeAnalyzer.type_self);
			times.add(time);
		}
		for (int i = 0; i < ViewTripActivity.trip.pois.length; i++) {
			Time nowTime = new Time(Time.TIMEZONE_UTC);
			nowTime.set(ViewTripActivity.trip.pois[i].time);
			nowTime.switchTimezone(ViewTripActivity.trip.timezone);
			Time previousTime = times.get(0);
			Time nextTime = times.get(times.size() - 1);
			if (TimeAnalyzer.isTimeMatched(previousTime, nowTime, nextTime)) {
				for (int j = 0; j < times.size(); j++) {
					nextTime = times.get(j);
					if (TimeAnalyzer.isTimeMatched(previousTime, nowTime, nextTime)) {
						ViewTripActivity.trip.pois[i].updateBasicInformation(null, null, locus.get(j).getLatitude(), locus.get(j).getLongitude(), (double) locus.get(j).getAltitude());
						break;
					}
					previousTime = nextTime;
				}
			}
		}
	}

	class SetLocus extends AsyncTask<Integer, Integer, LatLng[]> {
		int option;
		int analysisMethod;
		ProgressBar progressBar;
		GpxAnalyzer2.ProgressChangedListener listener;
		long fileSize;
		double latitude, longitude;
		boolean animated;
		CountingInputStream cis;

		public void stop() {
			if (ViewTripActivity.trip != null) {
				ViewTripActivity.trip.stopGetCache();
			}
		}

		@Override
		protected void onPreExecute() {
			analysisMethod = analysis_method_jni;
			animated = false;
			fileSize = ViewTripActivity.trip.cacheFile.exists() ? ViewTripActivity.trip.cacheFile.length() : ViewTripActivity.trip.gpxFile.length();
			progressBar = (ProgressBar) rootView.findViewById(R.id.analysis_progress);
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setMax(100);
			progressBar.setProgress(0);
			listener = new ProgressChangedListener() {

				public void onProgressChanged(long progress) {
					// TODO Auto-generated method stub
					if (fileSize != 0)
						publishProgress(0, (int) (progress * 100 / fileSize));
				}
			};
			try {
				BufferedReader br = new BufferedReader(new FileReader(ViewTripActivity.trip.gpxFile));
				String s;
				while ((s = br.readLine()) != null) {
					if (s.contains("<trkpt")) {
						String[] toks = s.split("\"");
						if (s.indexOf("lat") > s.indexOf("lon")) {
							latitude = Double.parseDouble(toks[3]);
							longitude = Double.parseDouble(toks[1]);
						} else {
							latitude = Double.parseDouble(toks[1]);
							longitude = Double.parseDouble(toks[3]);
						}
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
			getActivity().setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected LatLng[] doInBackground(Integer... arg0) {
			// TODO Auto-generated method stub
			option = arg0[0];
			if (ViewTripActivity.trip.cacheFile.exists()) {
				if (isAdded()) {
					publishProgress(1);
					long startTime = System.currentTimeMillis();
					switch (analysisMethod) {
					case analysis_method_jni:
						ViewTripActivity.trip.getCache2(getActivity(), handler, listener);
						break;
					case analysis_method_java:
						ViewTripActivity.trip.getCache(listener);
						break;
					}
					Log.i("trip", String.valueOf(System.currentTimeMillis() - startTime));
				}
			} else {
				if (isAdded()) {
					publishProgress(2);
					long startTime = System.currentTimeMillis();
					switch (analysisMethod) {
					case analysis_method_jni:
						ViewTripActivity.trip.getCache2(getActivity(), handler, listener);
						break;
					case analysis_method_java:
						ViewTripActivity.trip.updateCacheFromGpxFile(getActivity(), handler, listener);
						break;
					}
					Log.i("trip", String.valueOf(System.currentTimeMillis() - startTime));
					if (option == request_write_location_to_POI) {
						if (ViewTripActivity.trip.cache != null && isAdded()) {
							publishProgress(3);
							writeLocationToPoint(ViewTripActivity.trip.cache.lats);
						} else if (isAdded()) {
							Toast.makeText(getActivity(), getString(R.string.gpx_doesnt_contain_time_information), Toast.LENGTH_LONG).show();
						}
					}
					System.gc();
				}
			}
			try {
				if (ViewTripActivity.trip.cache != null) {
					final int latsSize = ViewTripActivity.trip.cache.lats.size();
					lat = new LatLng[latsSize];
					for (int i = 0; i < latsSize; i++) {
						MyLatLng2 latlng = ViewTripActivity.trip.cache.lats.get(i);
						lat[i] = new LatLng(latlng.getLatitude(), latlng.getLongitude());
					}
					System.gc();
				}
			} catch (Exception e) {
				ViewTripActivity.trip.cacheFile.delete();
				e.printStackTrace();
			}
			return lat;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			if (isAdded()) {
				switch (progress[0]) {
				case 0:
					progressBar.setProgress(progress[1]);
					if (!animated && mapFragment.getMap() != null && gmap != null) {
						gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
						animated = true;
					}
					break;
				case 1:
					Toast.makeText(getActivity(), getString(R.string.analysis_gpx), Toast.LENGTH_LONG).show();
					break;
				case 2:
					Toast.makeText(getActivity(), getString(R.string.first_analysis_gpx), Toast.LENGTH_LONG).show();
					break;
				case 3:
					Toast.makeText(getActivity(), getString(R.string.setup_pois), Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}
		}

		@Override
		protected void onPostExecute(LatLng[] result) {
			if (isAdded()) {
				if (result != null && result.length > 0) {
					gmap.addPolyline(new PolylineOptions().add(result).width(5).color(trackColor));
					if (!animated && mapFragment.getMap() != null && gmap != null) {
						gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
						animated = true;
					}
				} else {
					Toast.makeText(getActivity(), getString(R.string.invalid_gpx_file), Toast.LENGTH_LONG).show();
					ViewTripActivity.trip.deleteCache();
				}
				progressBar.setVisibility(View.GONE);
				getActivity().setProgressBarIndeterminateVisibility(false);
			}
		}
	}

	public void onInfoWindowClick(Marker marker) {
		// TODO Auto-generated method stub
		String pointtitle = marker.getTitle();
		if (new File(path + "/" + name + "/" + pointtitle).exists()) {
			EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "view_poi", ViewTripActivity.trip.dir.getName() + "-" + pointtitle, null).build());
			Intent intent = new Intent(getActivity(), ViewPointActivity.class);
			intent.putExtra("path", path + "/" + name + "/" + pointtitle);
			intent.putExtra("request_code", update_request);
			getActivity().startActivityForResult(intent, update_request);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Uri uri = data.getData();
			switch (requestCode) {
			case update_request:
				if (data.getBooleanExtra("update", false)) {
					updateAll();
				}
				break;
			case import_track:
				FileHelper.copyFile(new File(uri.getPath()), new File(path + "/" + name + "/" + name + ".gpx"));
				setLocusTask = new SetLocus();
				setLocusTask.execute(0);
				break;
			case REQUEST_GET_TOKEN:
				if (resultCode == Activity.RESULT_OK) {
					new GetAccessTokenTask().execute();
				}
				break;
			}
		}
	}

	public void onMapLongClick(final LatLng latlng) {
		// TODO Auto-generated method stub
		AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
		ab.setTitle(getString(R.string.add_poi));
		View layout = getActivity().getLayoutInflater().inflate(R.layout.edit_poi, null);
		ab.setView(layout);
		MyLatLng2 position = getLatLngInTrack(latlng);
		final EditText edittitle = (EditText) layout.findViewById(R.id.edit_poi_title);
		final EditText editlatitude = (EditText) layout.findViewById(R.id.edit_poi_latitude);
		editlatitude.setText(String.valueOf(position.latitude));
		final EditText editlongitude = (EditText) layout.findViewById(R.id.edit_poi_longitude);
		editlongitude.setText(String.valueOf(position.longitude));
		final EditText editaltitude = (EditText) layout.findViewById(R.id.edit_poi_altitude);
		editaltitude.setText(String.valueOf(position.altitude));
		final DatePicker editdate = (DatePicker) layout.findViewById(R.id.edit_poi_date);
		Time time = TimeAnalyzer.getTime(position.time, TimeAnalyzer.type_self);
		editdate.updateDate(time.year, time.month, time.monthDay);
		final TimePicker edittime = (TimePicker) layout.findViewById(R.id.edit_poi_time);
		edittime.setIs24HourView(true);
		edittime.setCurrentHour(time.hour);
		edittime.setCurrentMinute(time.minute);
		ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String title = edittitle.getText().toString();
				LatLng location = new LatLng(Double.parseDouble(editlatitude.getText().toString()), Double.parseDouble(editlongitude.getText().toString()));
				double altitude = Double.parseDouble(editaltitude.getText().toString());
				Time time = new Time(Time.getCurrentTimezone());
				time.set(0, edittime.getCurrentMinute(), edittime.getCurrentHour(), editdate.getDayOfMonth(), editdate.getMonth(), editdate.getYear());
				time.switchTimezone(Time.TIMEZONE_UTC);
				if (title.equals("")) {
					Toast.makeText(getActivity(), getString(R.string.input_the_poi_title), Toast.LENGTH_LONG).show();
				} else {
					final String newPointPath = path + "/" + name + "/" + title;
					File file = new File(newPointPath);
					if (file.exists()) {
						Toast.makeText(getActivity(), getString(R.string.there_is_a_same_poi), Toast.LENGTH_LONG).show();
					} else {
						addPoint(newPointPath, location, altitude, time);
					}
				}
			}
		});
		ab.setNegativeButton(getString(R.string.cancel), null);
		ab.show();
	}

	private void addPoint(String newPointPath, LatLng latlng, double altitude, Time time) {
		POI poi = new POI(new File(newPointPath));
		poi.updateBasicInformation(null, time, latlng.latitude, latlng.longitude, altitude);
		updateAll();
	}

	private MyLatLng2 getLatLngInTrack(LatLng latlng) {
		MyLatLng2 result = new MyLatLng2(latlng.latitude, latlng.longitude, 0, TimeAnalyzer.formatInCurrentTimezone(new Time()));
		if (ViewTripActivity.trip.cache == null || lat == null)
			return result;
		if (ViewTripActivity.trip.cache.lats == null)
			return result;
		double minDistance = Double.MAX_VALUE;
		int resultIndex = 0;
		int latLength = lat.length;
		double distance;
		for (int i = 0; i < latLength; i++) {
			distance = Math.pow((latlng.latitude - lat[i].latitude), 2) + Math.pow(latlng.longitude - lat[i].longitude, 2);
			if (distance < minDistance) {
				minDistance = distance;
				resultIndex = i;
			}
		}
		if (resultIndex < ViewTripActivity.trip.cache.lats.size())
			result = ViewTripActivity.trip.cache.lats.get(resultIndex);
		return result;
	}

	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub

	}

	public void onMarkerDragEnd(Marker marker) {
		// TODO Auto-generated method stub
		POI poi = new POI(new File(path + "/" + name + "/" + marker.getTitle()));
		poi.updateBasicInformation(null, null, marker.getPosition().latitude, marker.getPosition().longitude, null);
		updateAll();
	}

	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == null)
			return;
		if (v.equals(viewInformation)) {
			if (ViewTripActivity.trip.cache == null) {
				Toast.makeText(getActivity(), getString(R.string.no_data_can_be_displayed), Toast.LENGTH_SHORT).show();
				return;
			}
			AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
			ab.setTitle(getString(R.string.statistics));
			LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.map_view_basicinformation, null);
			TextView starttime = (TextView) layout.findViewById(R.id.starttime);
			starttime.setText(ViewTripActivity.trip.cache.startTime);
			TextView stoptime = (TextView) layout.findViewById(R.id.stoptime);
			stoptime.setText(ViewTripActivity.trip.cache.endTime);
			TextView totaltime = (TextView) layout.findViewById(R.id.totaltime);
			totaltime.setText(":" + ViewTripActivity.trip.cache.totalTime);
			TextView distance = (TextView) layout.findViewById(R.id.distance);
			distance.setText(":" + String.valueOf(ViewTripActivity.trip.cache.distance / 1000) + "km");
			TextView avgspeed = (TextView) layout.findViewById(R.id.avgspeed);
			avgspeed.setText(":" + String.valueOf(ViewTripActivity.trip.cache.avgSpeed) + "km/hr");
			TextView maxspeed = (TextView) layout.findViewById(R.id.maxspeed);
			maxspeed.setText(":" + String.valueOf(ViewTripActivity.trip.cache.maxSpeed) + "km/hr");
			TextView totalclimb = (TextView) layout.findViewById(R.id.totalclimb);
			totalclimb.setText(":" + String.valueOf(ViewTripActivity.trip.cache.climb) + "m");
			TextView maxaltitude = (TextView) layout.findViewById(R.id.maxaltitude);
			maxaltitude.setText(":" + String.valueOf(ViewTripActivity.trip.cache.maxAltitude));
			TextView minaltitude = (TextView) layout.findViewById(R.id.minaltitude);
			minaltitude.setText(":" + String.valueOf(ViewTripActivity.trip.cache.minAltitude));
			ab.setView(layout);
			ab.setPositiveButton(getString(R.string.enter), null);
			ab.show();
			EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "view_basicinformation", ViewTripActivity.trip.dir.getName(), null).build());
		} else if (v.equals(switchMapMode)) {
			if (gmap == null)
				return;
			switch (gmap.getMapType()) {
			case GoogleMap.MAP_TYPE_NORMAL:
				gmap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;
			case GoogleMap.MAP_TYPE_SATELLITE:
				gmap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				break;
			case GoogleMap.MAP_TYPE_HYBRID:
				gmap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				break;
			case GoogleMap.MAP_TYPE_TERRAIN:
				gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
			default:
				gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
			}
		} else if (v.equals(playTrip)) {
			if (playRunnable == null) {
				playTrip.setImageResource(R.drawable.ic_pause);
				stopTrip.setVisibility(View.VISIBLE);
				fastforward.setVisibility(View.VISIBLE);
				slowforward.setVisibility(View.VISIBLE);
				viewInformation.setVisibility(View.GONE);
				viewGraph.setVisibility(View.GONE);
				viewCost.setVisibility(View.GONE);
				viewNote.setVisibility(View.GONE);
				time.setVisibility(View.VISIBLE);
				processSeekBar.setVisibility(View.VISIBLE);
				BitmapFactory.Options option = new BitmapFactory.Options();
				option.inJustDecodeBounds = true;
				BitmapFactory.decodeResource(getResources(), R.drawable.runpoint, option);
				playRunnable = new PlayRunnable(option.outWidth);
				playThread = new Thread(playRunnable);
				playThread.start();
				EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "play", ViewTripActivity.trip.dir.getName(), null).build());
			} else if (playRunnable.pause) {
				playRunnable.onResume();
			} else {
				playRunnable.onPause();
			}
		} else if (v.equals(stopTrip)) {
			if (playRunnable != null) {
				playRunnable.onStop();
			}
		} else if (v.equals(showBar)) {
			showAll = !showAll;
			if (showAll) {
				buttonBar.setVisibility(View.VISIBLE);
				getActivity().getActionBar().show();
				WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
				attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
				getActivity().getWindow().setAttributes(attrs);
			} else {
				buttonBar.setVisibility(View.GONE);
				getActivity().getActionBar().hide();
				WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
				attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				getActivity().getWindow().setAttributes(attrs);
			}
			EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "toggle_bar", ViewTripActivity.trip.dir.getName(), null).build());
		} else if (v.equals(fastforward)) {
			if (playRunnable != null) {
				playRunnable.changeForward(PlayRunnable.fast);
			}
		} else if (v.equals(slowforward)) {
			if (playRunnable != null) {
				playRunnable.changeForward(PlayRunnable.slow);
			}
		} else if (v.equals(viewGraph)) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(ViewTripActivity.trip.graphicFile), "image/*");
			getActivity().startActivity(intent);
			EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "view_graph", ViewTripActivity.trip.dir.getName(), null).build());
		} else if (v.equals(viewCost)) {
			Intent intent2 = new Intent(getActivity(), ViewCostActivity.class);
			intent2.putExtra("option", ViewCostActivity.optionTrip);
			intent2.putExtra("path", ViewTripActivity.trip.dir.getPath());
			intent2.putExtra("title", ViewTripActivity.trip.tripName);
			startActivity(intent2);
			EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "view_cost", ViewTripActivity.trip.dir.getName(), null).build());
		} else if (v.equals(viewNote)) {
			AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
			ab.setTitle(getString(R.string.Note));
			final String noteStr = ViewTripActivity.trip.note;
			TextView textView = new TextView(getActivity());
			textView.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
			textView.setText(noteStr);
			int dip10 = (int) DeviceHelper.pxFromDp(getActivity(), 10);
			textView.setPadding(dip10, dip10, dip10, dip10);
			ab.setView(textView);
			ab.setPositiveButton(getString(R.string.enter), null);
			ab.setNegativeButton(getString(R.string.edit), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					AlertDialog.Builder ab2 = new AlertDialog.Builder(getActivity());
					ab2.setTitle(getString(R.string.edit) + " " + getString(R.string.Note));
					final EditText editText = new EditText(getActivity());
					editText.setText(noteStr);
					ab2.setView(editText);
					ab2.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							ViewTripActivity.trip.updateNote(editText.getText().toString());
						}
					});
					ab2.setNegativeButton(getString(R.string.cancel), null);
					ab2.show();
				}
			});
			ab.show();
			EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "view_note", ViewTripActivity.trip.dir.getName(), null).build());
		}
	}

	class PlayRunnable implements Runnable, OnSeekBarChangeListener {
		private Object pauselock;
		public boolean pause;
		public boolean stop;
		int pointSize;
		MediaPlayer mp;
		String title;
		int playMode;
		Marker playPoint;
		LatLng[] markersPosition;
		String[] times;
		ImageView runPointImage;
		float bearing;
		RelativeLayout mapLayout;
		int markersSize;
		int latlength;
		int trackCacheSize;
		int i, add = 1;
		int interval;
		boolean[] passed;
		public static final int fast = 0;
		public static final int slow = 1;

		public PlayRunnable(int pointSize) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
			playMode = Integer.parseInt(preferences.getString("playingtripmode", "0"));
			mapLayout = (RelativeLayout) rootView.findViewById(R.id.maplayout);
			interval = Integer.parseInt(preferences.getString("playtripspeed", "10"));
			if (ViewTripActivity.trip.cache != null && ViewTripActivity.trip.cache.lats != null)
				trackCacheSize = ViewTripActivity.trip.cache.lats.size();
			if (lat != null)
				latlength = lat.length;
			if (markers != null)
				markersSize = markers.size();
			pauselock = new Object();
			pause = false;
			stop = false;
			this.pointSize = pointSize / 2;
			if (latlength > 0)
				processSeekBar.setMax(latlength - 1);
			processSeekBar.setOnSeekBarChangeListener(this);
			markersPosition = new LatLng[markersSize];
			for (int i = 0; i < markersSize; i++) {
				markersPosition[i] = markers.get(i).getPosition();
			}
			times = new String[trackCacheSize];
			for (int i = 0; i < trackCacheSize; i++) {
				times[i] = ViewTripActivity.trip.cache.lats.get(i).getTime();
			}
		}

		public void onPause() {
			synchronized (pauselock) {
				pause = true;
				playTrip.setImageResource(R.drawable.ic_play);
			}
		}

		public void onResume() {
			synchronized (pauselock) {
				playTrip.setImageResource(R.drawable.ic_pause);
				pause = false;
				pauselock.notifyAll();
			}
		}

		public void onStop() {
			stop = true;
			stopTrip.setVisibility(View.GONE);
			fastforward.setVisibility(View.GONE);
			slowforward.setVisibility(View.GONE);
			viewInformation.setVisibility(View.VISIBLE);
			viewGraph.setVisibility(View.VISIBLE);
			viewCost.setVisibility(View.VISIBLE);
			viewNote.setVisibility(View.VISIBLE);
			time.setVisibility(View.GONE);
			processSeekBar.setVisibility(View.GONE);
			playTrip.setImageResource(R.drawable.ic_play);
			handler.post(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					if (playPoint != null)
						playPoint.remove();
					if (playMode == playtriptype_skyview) {
						mapLayout.removeView(runPointImage);
					}
					if (processSeekBar != null) {
						mapLayout.removeView(processSeekBar);
					}
				}
			});
			if (mp != null) {
				mp.stop();
				mp.release();
				mp = null;
			}
			playRunnable = null;
			playThread = null;
			System.gc();
		}

		private void resetPassed() {
			for (int i = 0; i < passed.length; i++)
				passed[i] = false;
		}

		public void changeForward(int option) {
			switch (option) {
			case fast:
				if (interval == 1)
					add *= 2;
				else
					interval /= 2;
				break;
			case slow:
				if (add == 1)
					interval *= 2;
				else
					add /= 2;
				break;
			default:
				break;
			}
		}

		public void run() {
			// TODO Auto-generated method stub
			mp = null;
			if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("playmusic", false)) {
				String musicpath = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("musicpath", "");
				if (musicpath != "") {
					File file = new File(musicpath);
					if (file.exists() && file.isFile()) {
						mp = MediaPlayer.create(getActivity(), Uri.fromFile(file));
						mp.setLooping(true);
						mp.start();
					}
				}
			}
			if (latlength > 0 && lat != null) {
				handler.post(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						if (playMode == playtriptype_skyview) {
							runPointImage = new ImageView(getActivity());
							runPointImage.setImageResource(R.drawable.runpoint);
							RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
							params.addRule(RelativeLayout.CENTER_IN_PARENT);
							mapLayout.addView(runPointImage, params);
						} else {
							playPoint = gmap.addMarker(new MarkerOptions().position(lat[0]).icon(BitmapDescriptorFactory.fromResource(R.drawable.runpoint)).anchor((float) 0.5, (float) 0.5));
						}
					}

				});
			}
			passed = new boolean[markersSize];
			resetPassed();
			for (i = 0; i < latlength; i += add) {
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				synchronized (pauselock) {
					while (pause) {
						try {
							pauselock.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				if (stop || i >= latlength || i >= trackCacheSize)
					break;
				final LatLng position = lat[i];
				processSeekBar.setProgress(i);
				bearing += 0.05;
				if (bearing > 360)
					bearing = 0;
				handler.post(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						setPosition(i);
					}
				});
				for (int j = 0; j < markersSize; j++) {
					final int index = j;
					if (!passed[index] && Math.abs(position.latitude - markersPosition[index].latitude) <= 0.0001 && Math.abs(position.longitude - markersPosition[index].longitude) <= 0.0001) {
						passed[index] = true;
						Intent intent = new Intent(getActivity(), PlayPointActivity.class);
						synchronized (handler) {
							handler.post(new Runnable() {

								public void run() {
									// TODO Auto-generated method stub
									title = markers.get(index).getTitle();
									markers.get(index).showInfoWindow();
									synchronized (handler) {
										handler.notifyAll();
									}
								}
							});
							try {
								handler.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						intent.putExtra("path", path + "/" + name + "/" + title);
						passed[j] = true;
						getActivity().startActivity(intent);
					}
				}
			}
			handler.post(new Runnable() {

				public void run() {
					// TODO Auto-generated method stub
					if (playRunnable != null)
						playRunnable.onStop();
				}
			});
		}

		private void setPosition(int index) {
			if (index < trackCacheSize && times[index] != null) {
				time.setText(times[index]);
			}
			if (index < latlength) {
				if (playMode == playtriptype_skyview) {
					gmap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(lat[index], gmap.getCameraPosition().zoom, 90, bearing)), interval, null);
				} else {
					playPoint.setPosition(lat[index]);
				}
			}
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			resetPassed();
			playRunnable.onResume();
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			playRunnable.onPause();
		}

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
			i = progress;
			setPosition(i);
		}
	}

	class GetAccessTokenTask extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				token = GoogleAuthUtil.getToken(getActivity(), account, "oauth2:https://www.googleapis.com/auth/userinfo.email");
				Intent intent = new Intent(getActivity(), SendTripService.class);
				intent.putExtra(SendTripService.filePathTag, ViewTripActivity.trip.dir.getPath());
				intent.putExtra(SendTripService.accountTag, account);
				intent.putExtra(SendTripService.tokenTag, token);
				intent.putExtra(SendTripService.publicTag, uploadPublic);
				getActivity().startService(intent);
				EasyTracker.getInstance(getActivity()).send(MapBuilder.createEvent("Trip", "share_track_by_tripdiary", ViewTripActivity.trip.dir.getName(), null).build());
			} catch (UserRecoverableAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				startActivityForResult(e.getIntent(), REQUEST_GET_TOKEN);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GoogleAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (setLocusTask != null && !setLocusTask.isCancelled()) {
			setLocusTask.stop();
		}
		super.onDestroy();
	}

}
