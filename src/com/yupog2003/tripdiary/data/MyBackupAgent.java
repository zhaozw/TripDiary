package com.yupog2003.tripdiary.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupManager;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MyBackupAgent extends BackupAgentHelper{
	static final String defaultPreference="com.yupog2003.tripdiary_preferences";
	static final String tripPreference="trip";
	static final String categoryPreference="category";
	static final String categoryExpandPreference="categoryExpand";
	static final String tripTimeZonePreference="tripTimezone";
	static final String backupKey="com.yupog2003.tripdiary.backupkey";
	@Override
	public void onCreate(){
		SharedPreferencesBackupHelper helper=new SharedPreferencesBackupHelper(this, defaultPreference,tripPreference,categoryPreference,categoryExpandPreference,tripTimeZonePreference);
		addHelper(backupKey, helper);
	}
	public static void requestBackup(Context context){
		BackupManager bm = new BackupManager(context);
		bm.dataChanged();
	}
	public static boolean saveSharedPreferencesToFile(Context context,String prefName,File dst) {
	    boolean res = false;
	    ObjectOutputStream output = null;
	    try {
	        output = new ObjectOutputStream(new FileOutputStream(dst));
	        SharedPreferences pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
	        output.writeObject(pref.getAll());
	        res = true;
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }finally {
	        try {
	            if (output != null) {
	                output.flush();
	                output.close();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return res;
	}

	@SuppressWarnings({ "unchecked" })
	public static boolean loadSharedPreferencesFromFile(Context context,String prefName,File src) {
	    boolean res = false;
	    ObjectInputStream input = null;
	    try {
	        input = new ObjectInputStream(new FileInputStream(src));
	            Editor prefEdit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
	            prefEdit.clear();
	            Map<String, ?> entries = (Map<String, ?>) input.readObject();
	            for (Entry<String, ?> entry : entries.entrySet()) {
	                Object v = entry.getValue();
	                String key = entry.getKey();

	                if (v instanceof Boolean)
	                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
	                else if (v instanceof Float)
	                    prefEdit.putFloat(key, ((Float) v).floatValue());
	                else if (v instanceof Integer)
	                    prefEdit.putInt(key, ((Integer) v).intValue());
	                else if (v instanceof Long)
	                    prefEdit.putLong(key, ((Long) v).longValue());
	                else if (v instanceof String)
	                    prefEdit.putString(key, ((String) v));
	            }
	            prefEdit.commit();
	        res = true;         
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }finally {
	        try {
	            if (input != null) {
	                input.close();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return res;
	}
}
