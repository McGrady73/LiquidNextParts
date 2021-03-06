package com.suxsem.liquidnextparts.activities;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.suxsem.liquidnextparts.LiquidSettings;
import com.suxsem.liquidnextparts.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class SDMAN extends PreferenceActivity {
	
	private CheckBoxPreference sdman_app;
	private CheckBoxPreference sdman_data;
	private CheckBoxPreference sdman_dalvik;
	private CheckBoxPreference sdman_download;
	private CheckBoxPreference sdman_swap;
	private Preference sdman_sdext_recovery;
	private Preference sdman_swap_recovery;
	private Preference sdman_update;
	private EditTextPreference sdman_swappyness;
	private boolean sdman_ext_exist_value;
	private boolean sdman_swap_exist_value;
	private boolean sdman_app_value;
	private boolean sdman_data_value;
	private boolean sdman_dalvik_value;
	private boolean sdman_download_value;
	private boolean sdman_swap_value;
	private Integer sdman_swappyness_value;
	private ProgressDialog waitdialog;
	private SDMAN myactivity;
	private String swappiness;
	private String sdman_update_value;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		myactivity = this;
		addPreferencesFromResource(R.xml.sdman);
		sdman_swappyness = (EditTextPreference)findPreference("sdman_swappyness");
		sdman_sdext_recovery = findPreference("sdman_sdext_recovery");
		sdman_swap_recovery = findPreference("sdman_swap_recovery");
		sdman_update = findPreference("sdman_update");
		sdman_app = (CheckBoxPreference)findPreference("sdman_app");
		sdman_data = (CheckBoxPreference)findPreference("sdman_data");
		sdman_dalvik = (CheckBoxPreference)findPreference("sdman_dalvik");
		sdman_download = (CheckBoxPreference)findPreference("sdman_download");
		sdman_swap = (CheckBoxPreference)findPreference("sdman_swap");
		
		checkstatus();

		sdman_sdext_recovery.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
					return sdman_set("recovery");
			}
		});
		sdman_swap_recovery.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
					return sdman_set("recovery");
			}
		});
		sdman_update.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ConnectivityManager connManager =  (ConnectivityManager)myactivity.getSystemService(Context.CONNECTIVITY_SERVICE);
				android.net.NetworkInfo netInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				android.net.NetworkInfo wifiInfo= connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (netInfo.getState() == android.net.NetworkInfo.State.CONNECTED ||
						wifiInfo.getState() == android.net.NetworkInfo.State.CONNECTED  ) {
					return sdman_set("update");
				}else{
					Toast.makeText(myactivity, "ERROR: NO CONNECTIONS!", 4000).show();
					return false;
				}
			}
		});
		sdman_app.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_app.isChecked()){
					return sdman_set("appon");
				}else{
					return sdman_set("appoff");
				}
			} 
		});
		sdman_data.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_data.isChecked()){
					return sdman_set("dataon");
				}else{
					return sdman_set("dataoff");
				}
			}
		});
		sdman_dalvik.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_dalvik.isChecked()){
					return sdman_set("dalvikon");
				}else{
					return sdman_set("dalvikoff");
				}
			}
		});
		sdman_download.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_download.isChecked()){
					return sdman_set("downloadon");
				}else{
					return sdman_set("downloadoff");
				}
			}
		});
		sdman_swap.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!sdman_swap.isChecked()){
					return sdman_set("swapon");
				}else{
					return sdman_set("swapoff");
				}
			}
		});
		sdman_swappyness.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				swappiness = newValue.toString();
				new Thread()
				{
					public void run() 
					{
						LiquidSettings.runRootCommand("sdman -e swpy "+swappiness);
						myactivity.runOnUiThread(new Runnable() {
							public void run() {
								waitdialog.dismiss();
								checkstatus();
							}
						});
					}}.start();
				return true;
			}
		});
		
	}
	@Override
	public void onStop() {
		super.onStop();
		this.finish();
		return;
	}
	@Override
	public void onBackPressed() {
		Intent myintent = new Intent (Intent.ACTION_VIEW);
		myintent.setClassName(this.getBaseContext(), settings.class.getName());
		startActivity(myintent);
	this.finish();
	return;
	}
	
	private void checkstatus(){
		waitdialog = ProgressDialog.show(myactivity, "", 
				"Checking status...", true);
		new Thread()
		{
			public void run() 
			{
				String result = "";
				try {

					Process process = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(process
							.getOutputStream());
					DataInputStream osRes = new DataInputStream(process
							.getInputStream());

					String commandtemp = "echo ";
					commandtemp += "$(cat \"/data/.sdman\" | grep \"SDPA\" | awk '{print $2}')";
					commandtemp += "$(cat \"/data/.sdman\" | grep \"SDSW\" | awk '{print $2}')";
					commandtemp += "$(cat \"/data/.sdman\" | grep \"APXT\" | awk '{print $2}')";
					commandtemp += "$(cat \"/data/.sdman\" | grep \"DTXT\" | awk '{print $2}')";
					commandtemp += "$(cat \"/data/.sdman\" | grep \"DCXT\" | awk '{print $2}')";
					commandtemp += "$(cat \"/data/.sdman\" | grep \"DLDT\" | awk '{print $2}')";
					commandtemp += "$(cat \"/data/.sdman\" | grep \"SWEN\" | awk '{print $2}')";
					commandtemp += "\n";
					os.writeBytes(commandtemp);

					result = osRes.readLine();

					os.flush();
					os.writeBytes("exit\n");
					os.flush();
					process.waitFor();

				} catch (IOException e) {
				} catch (InterruptedException e) {
				}
				sdman_ext_exist_value = result.substring(0, 1).equals("1");
				sdman_swap_exist_value = result.substring(1, 2).equals("1");
				sdman_app_value = result.substring(2, 3).equals("1");
				sdman_data_value = result.substring(3, 4).equals("1");
				sdman_dalvik_value = result.substring(4, 5).equals("1");
				sdman_download_value = result.substring(5, 6).equals("1");
				sdman_swap_value = result.substring(6, 7).equals("1");
				
				result = "";
				try {

					Process process = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(process
							.getOutputStream());
					DataInputStream osRes = new DataInputStream(process
							.getInputStream());

					os.writeBytes("cat /proc/sys/vm/swappiness\n");

					result = osRes.readLine();					
					os.flush();
					os.writeBytes("exit\n");
					os.flush();
					process.waitFor();

				} catch (IOException e) {
				} catch (InterruptedException e) {
				}
				sdman_swappyness_value = Integer.valueOf(result);
				
				result = "";
				try {

					Process process = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(process
							.getOutputStream());
					DataInputStream osRes = new DataInputStream(process
							.getInputStream());

					os.writeBytes("sdman -v | head -n2 | tail -n1 | awk -F'v.' '{print $2}'\n");

					result = osRes.readLine();					
					os.flush();
					os.writeBytes("exit\n");
					os.flush();
					process.waitFor();

				} catch (IOException e) {
				} catch (InterruptedException e) {
				}
				sdman_update_value = result;

				myactivity.runOnUiThread(new Runnable() {
					public void run() {
						updatemenu();
					}
				});
			}
		}.start();
	}
	
	private void updatemenu(){
		waitdialog.dismiss();
		if(sdman_ext_exist_value){
			sdman_sdext_recovery.setEnabled(false);
		}else{
			sdman_app.setEnabled(false);
			sdman_data.setEnabled(false);
			sdman_dalvik.setEnabled(false);
			sdman_download.setEnabled(false);
		}
		if(sdman_swap_exist_value){
			sdman_swap_recovery.setEnabled(false);
		}else{
			sdman_swap.setEnabled(false);
			sdman_swappyness.setEnabled(false);
		}
		sdman_swappyness.setText(String.valueOf(sdman_swappyness_value));
		sdman_swappyness.setSummary("Current value: "+String.valueOf(sdman_swappyness_value));
		sdman_update.setSummary("Current version: "+String.valueOf(sdman_update_value));
		sdman_app.setChecked(sdman_app_value);
		sdman_data.setChecked(sdman_data_value);
		sdman_dalvik.setChecked(sdman_dalvik_value);
		sdman_download.setChecked(sdman_download_value);
		sdman_swap.setChecked(sdman_swap_value);
		
		sdman_dalvik.setSummary("Feature temporarily disabled");
		sdman_dalvik.setEnabled(false);
	}	
	
	private boolean sdman_set(String arg){
		waitdialog = ProgressDialog.show(myactivity, "", 
				"Applying changes... This can take a long time... PLEASE BE PATIENT", true);
		if(arg.equals("update")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -u");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("appon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e mvap");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("appoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d mvap");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("dataon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e mvdt");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("dataoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d mvdt");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}		
		if(arg.equals("dalvikon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e mvdc");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("dalvikoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d mvdc");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}		
		if(arg.equals("downloadon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e mvdl");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("downloadoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d mvdl");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							Toast.makeText(myactivity, "No need to reboot. Done", 4000).show();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}		
		if(arg.equals("swapon")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -e swap");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("swapoff")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("sdman -d swap");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}
		if(arg.equals("recovery")){
			new Thread()
			{
				public void run() 
				{
					LiquidSettings.runRootCommand("reboot recovery");
					myactivity.runOnUiThread(new Runnable() {
						public void run() {
							waitdialog.dismiss();
							checkstatus();
						}
					});
				}}.start();
			return true;
		}		
		return false;
		
	}
}