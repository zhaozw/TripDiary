package com.yupog2003.tripdiary.fragments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.yupog2003.tripdiary.services.DownloadTripService;
import com.yupog2003.tripdiary.MainActivity;
import com.yupog2003.tripdiary.R;
import com.yupog2003.tripdiary.data.DeviceHelper;
import com.yupog2003.tripdiary.views.CheckableLayout;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteTripsFragment extends Fragment {

	public static final int option_public = 0;
	public static final int option_personal = 1;
	public static final String tag_option="optionTag";
	private static final int REQUEST_GET_TOKEN=0;
	int trip_option;
	ListView listView;
	TripAdapter adapter;
	String account;
	String token;
	Intent loginIntent;
	SearchView searchView;
	
	public RemoteTripsFragment(){
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		listView = new ListView(getActivity());
		return listView;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.trip_option=getArguments().getInt(tag_option, 0);
		setHasOptionsMenu(true);
		loaddata();
	}
	
	public void loaddata(){
		if (trip_option==option_personal) {
			Account[] accounts = AccountManager.get(getActivity()).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
			if (accounts!=null&&accounts.length>0){
				account=PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("account", accounts[0].name);
				new GetAccessTokenTask().execute();
			}
		} else {
			account="public";
			new GetTripsTask().execute();
		}
		
	}
	
	class GetTripsTask extends AsyncTask<String, String, String[]> {

		static final String phpURL = MainActivity.serverURL+"/getTrips.php";

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected String[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			String url = phpURL + "?rootPath=Trips/" + account;
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			try {
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
				DocumentBuilder builder=factory.newDocumentBuilder();
				Document document=builder.parse(entity.getContent());
				Element rootElement=(Element) document.getDocumentElement();
				NodeList tripList=rootElement.getElementsByTagName("trip");
				String[] trips=new String[tripList.getLength()];
				for (int i=0;i<trips.length;i++){
					trips[i]=((Element)tripList.item(i)).getAttribute("path");
				}
				return trips;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			// TODO Auto-generated method stub
			if (result != null) {
				adapter = new TripAdapter(result);
				listView.setAdapter(adapter);
				listView.setLongClickable(true);
				listView.setOnItemClickListener(adapter);
				listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
				listView.setMultiChoiceModeListener(adapter);
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
	}
	
	class UpdateDataTask extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String phpUrl=MainActivity.serverURL+"/UpdateData.php";
			if (params.length==6){
				String option=params[0];
				String datapath=params[1];
				String data=params[2];
				String tripname=params[3];
				String trippath=params[4];
				String token=params[5];
				phpUrl+="?option="+option+"&datapath="+datapath+"&data="+data+"&tripname="+tripname+"&trippath="+trippath+"&token="+token;
				phpUrl=phpUrl.replace(" ", "%20");
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(phpUrl);
				try {
					client.execute(get);
				} catch (ClientProtocolException e) {
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
			super.onPostExecute(result);
			adapter=null;
			RemoteTripsFragment.this.onResume();
		}
		
	}
	class GetAccessTokenTask extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				token=GoogleAuthUtil.getToken(getActivity(), account, "oauth2:https://www.googleapis.com/auth/userinfo.email");
				new GetTripsTask().execute();
			} catch (UserRecoverableAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				loginIntent=e.getIntent();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GoogleAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	class TripAdapter extends BaseAdapter implements OnItemClickListener,OnQueryTextListener,MultiChoiceModeListener{

		String[] trips;
		int dip10;
		boolean onActionMode=false;
		public TripAdapter(String[] trips) {
			this.trips = trips;
			dip10=(int)DeviceHelper.pxFromDp(getActivity(), 10);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return trips.length;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return trips[position];
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView textView = new TextView(getActivity());
			textView.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
			textView.setText(trips[position].substring(trips[position].lastIndexOf("/") + 1));
			textView.setPadding(dip10, dip10, dip10, dip10);
			CheckableLayout layout=new CheckableLayout(getActivity());
			layout.addView(textView);
			return layout;
		}

		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			// TODO Auto-generated method stub
			String tripPath=trips[position];
			String tripName=tripPath.substring(tripPath.lastIndexOf("/") + 1);
			String uri=MainActivity.serverURL+"/Trip.html?tripname="+tripName+"&trippath="+tripPath;
			Intent intent=new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(uri));
			startActivity(intent);
		}
		ArrayList<String> checksName;
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			checksName = new ArrayList<String>();
			for (int i = 0; i < checks.length; i++) {
				if (checks[i]) {
					checksName.add(trips[i]);
				}
			}
			mode.finish();
			switch(item.getItemId()){
			case R.id.download:
				for (int i=0;i<checksName.size();i++){
					String tripPath=checksName.get(i);
					String tripName=tripPath.substring(trips[i].lastIndexOf("/")+1);
					if (new File(MainActivity.rootPath+"/"+tripName).exists()){
						Toast.makeText(getActivity(), getString(R.string.explain_same_trip_when_import), Toast.LENGTH_SHORT).show();
					}else{
						Intent intent=new Intent(getActivity(),DownloadTripService.class);
						intent.putExtra("path", tripPath);
						getActivity().startService(intent);
					}
				}
				break;
			case R.id.edit:
				final String tripPath=checksName.get(0);
				final String tripName=tripPath.substring(tripPath.lastIndexOf("/")+1);
				AlertDialog.Builder ab=new AlertDialog.Builder(getActivity());
				ab.setTitle(getString(R.string.Name));
				final EditText editText=new EditText(getActivity());
				editText.setText(tripName);
				ab.setView(editText);
				ab.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						boolean conflict=false;
						String newTripName=editText.getText().toString();
						for (int i=0;i<trips.length;i++){
							if (tripPath.substring(tripPath.lastIndexOf("/") + 1).equals(newTripName)){
								conflict=true;
								break;
							}
						}
						if (!conflict){
							new UpdateDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"rename_trip",tripPath,tripPath.substring(0, tripPath.lastIndexOf("/")+1)+newTripName,tripName,tripPath,token);
						}
					}
				});
				ab.setNegativeButton(getString(R.string.cancel), null);
				ab.show();
				break;
			case R.id.delete:
				AlertDialog.Builder ab2 = new AlertDialog.Builder(getActivity());
				ab2.setTitle(getString(R.string.be_careful));
				ab2.setMessage(getString(R.string.are_you_sure_to_delete));
				ab2.setIcon(R.drawable.ic_alert);
				ab2.setPositiveButton(getString(R.string.enter), new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						for (int i=0;i<checksName.size();i++){
							String tripPath=checksName.get(i);
							String tripName=tripPath.substring(tripPath.lastIndexOf("/")+1);
							new UpdateDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"delete",tripPath,"",tripName,tripPath,token);
						}
					}
				});
				ab2.setNegativeButton(getString(R.string.cancel), null);
				ab2.show();
				break;
			case R.id.togglepublic:
				final String tripPath2=checksName.get(0);
				final String tripName2=tripPath2.substring(tripPath2.lastIndexOf("/")+1);
				new UpdateDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"toggle_public",tripPath2,tripName2,tripName2,tripPath2,token);
				break;
			case R.id.selectall:
				for (int i = 0; i < listView.getCount(); i++) {
					listView.setItemChecked(i, !checkAll);
				}
				checkAll = !checkAll;
				break;
			}
			return true;
		}

		boolean[] checks;
		boolean checkAll;
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			mode.getMenuInflater().inflate(R.menu.remote_trip_menu, menu);
			if (account.equals("public")){
				menu.removeItem(R.id.edit);
				menu.removeItem(R.id.togglepublic);
				menu.removeItem(R.id.delete);
			}
			onActionMode = true;
			checks = new boolean[trips.length];
			for (int i = 0; i < checks.length; i++)
				checks[i] = false;
			checkAll = false;
			return true;
		}

		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			mode=null;
			onActionMode=false;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
			// TODO Auto-generated method stub
			checks[position]=checked;
			int selects = 0;
			for (int i = 0; i < checks.length; i++) {
				if (checks[i])
					selects++;
			}
			if (!account.equals("public")){
				mode.getMenu().findItem(R.id.edit).setVisible(!(selects > 1));
				mode.getMenu().findItem(R.id.togglepublic).setVisible(!(selects > 1));
			}
		}

		public boolean onQueryTextChange(String newText) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean onQueryTextSubmit(String query) {
			// TODO Auto-generated method stub
			final String searchname = searchView.getQuery().toString().toLowerCase(Locale.US);
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
			searchView.clearFocus();
			if (!searchname.equals("")) {
				final ArrayList<String> founds = new ArrayList<String>();
				int adaptercount = trips.length;
				for (int i = 0; i < adaptercount; i++) {
					String itemname = trips[i];
					if (itemname.toLowerCase(Locale.US).contains(searchname)) {
						founds.add(itemname);
					}
				}
				if (founds.size() == 0) {
					Toast.makeText(getActivity(), getString(R.string.trip_not_found), Toast.LENGTH_SHORT).show();
				} else if (founds.size() == 1) {
					String tripPath=founds.get(0);
					String tripName=tripPath.substring(tripPath.lastIndexOf("/") + 1);
					String uri=MainActivity.serverURL+"/Trip.html?tripname="+tripName+"&trippath="+tripPath;
					Intent intent=new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(uri));
					startActivity(intent);
				} else {
					AlertDialog.Builder choose = new AlertDialog.Builder(getActivity());
					choose.setTitle(getString(R.string.choose_the_trip));
					choose.setSingleChoiceItems(founds.toArray(new String[0]), -1, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							String tripPath=founds.get(which);
							String tripName=tripPath.substring(tripPath.lastIndexOf("/") + 1);
							String uri=MainActivity.serverURL+"/Trip.html?tripname="+tripName+"&trippath="+tripPath;
							Intent intent=new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse(uri));
							startActivity(intent);
							dialog.dismiss();
						}
					});
					choose.show();
				}
			}
			return true;
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.fragment_remote_trip, menu);
		if (account.equals("public")||token!=null){
			menu.removeItem(R.id.login);
		}
		searchView=(SearchView)menu.findItem(R.id.searchview).getActionView();
		searchView.setQueryHint(getString(R.string.search_trip));
		searchView.setOnQueryTextListener(adapter);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.login:
			if (loginIntent!=null){
				startActivityForResult(loginIntent, REQUEST_GET_TOKEN);
			}
			break;
		}
		return true;
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode==REQUEST_GET_TOKEN&&resultCode==Activity.RESULT_OK){
			new GetAccessTokenTask().execute();
		}
	}
	
}
