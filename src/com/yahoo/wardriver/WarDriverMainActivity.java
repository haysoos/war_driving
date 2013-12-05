package com.yahoo.wardriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class WarDriverMainActivity extends Activity {

	private Button btnScan;
	private WifiManager mainWifi;
	private WifiReceiver receiverWifi;
	public List<ScanResult> wifiList = new ArrayList<ScanResult>();
	private ListView lvScanResults;
	private ScanResultArrayAdapter arrayAdapter;
	private Spinner spinner;
	private Button btnSave;
	private boolean externalStorageAvailable;
	private boolean externalStorageWriteable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.war_driver_activity_main);

		btnScan = (Button) findViewById(R.id.btnScan);
		lvScanResults = (ListView) findViewById(R.id.lvScanResults);
		arrayAdapter = new ScanResultArrayAdapter(getApplicationContext(), wifiList);
		lvScanResults.setAdapter(arrayAdapter);
		
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mainWifi.startScan();

		spinner = (Spinner) findViewById(R.id.spinner1);
		loadSpinner(spinner);
		
		btnScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				scanButtonClicked();
			}
		});
		
		btnSave = (Button) findViewById(R.id.button1);
		btnSave.setText("Save");
		
		btnSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveToFile();
			}
		});
		

	}

	private void saveToFile() {
		checkStateOfExternalStorage();
		
		if (externalStorageAvailable && externalStorageWriteable) {
			File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File logFile = new File(downloadDirectory, "wifi_routers.txt");
			
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(logFile, true);
				
				String string = formatScanResults(mainWifi.getScanResults());
				fos.write(string.getBytes());
				Toast.makeText(getApplicationContext(), "Successfully Saved to wifi_routers.txt", Toast.LENGTH_SHORT).show();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
		}
		
	}

	private String formatScanResults(List<ScanResult> scanResults) {
		
		StringBuilder sb = new StringBuilder();
		
		for (ScanResult s : scanResults) {
			sb.append(System.getProperty("line.separator"));
			sb.append(spinner.getSelectedItem().toString());
			sb.append(',');
			sb.append(" MAC: ");
			sb.append(getMacFromBssid(s.BSSID));
			sb.append(',');
			sb.append(" BSSID: ");
			sb.append(s.BSSID);
			sb.append(',');
			sb.append(" SSID: ");
			sb.append(s.SSID);
			sb.append(',');
			sb.append(" Level: ");
			sb.append((s.level + 100));
			sb.append(',');
			sb.append(" Frequency: ");
			sb.append(s.frequency);
			sb.append(',');
			sb.append(" Timestamp: ");
			sb.append(s.timestamp);
			sb.append(',');
			sb.append(" Capabilities: ");
			sb.append(s.capabilities);
		}
		
		return  sb.toString();
	}
	
	private void checkStateOfExternalStorage() {
		externalStorageAvailable = false;
		externalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    externalStorageAvailable = externalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    externalStorageAvailable = true;
		    externalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    externalStorageAvailable = externalStorageWriteable = false;
		}
	}

	private void loadSpinner(Spinner spinner) {
		
		// Array of choices
		String routers[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10","11", "12", "13", "14", "15", "16"};
		
		// Application of the Array to the Spinner
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, routers);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
		spinner.setAdapter(spinnerArrayAdapter);
	}

	protected void scanButtonClicked() {
		Toast.makeText(getApplicationContext(), "Scanning", Toast.LENGTH_SHORT).show();
		mainWifi.startScan();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mainWifi.startScan();
		return super.onMenuItemSelected(featureId, item);
	}

	protected void onPause() {
		unregisterReceiver(receiverWifi);
		super.onPause();
	}

	protected void onResume() {
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			wifiList = mainWifi.getScanResults();
			//removeFrequenciesGreaterThan5GHz(wifiList);
			//removeDuplicateSSIDForEachBSSID(wifiList);
			Collections.sort(wifiList, new Comparator<ScanResult>() {

				@Override
				public int compare(ScanResult lhs, ScanResult rhs) {
					return (rhs.level - lhs.level);
				}
				
			});
			arrayAdapter.clear();
			arrayAdapter.addAll(wifiList);
			arrayAdapter.notifyDataSetChanged();
		}

		private void removeFrequenciesGreaterThan5GHz(List<ScanResult> wifiList) {
			List<Integer> remove = new ArrayList<Integer>(80);
			
			for (int i=0; i < wifiList.size(); i++) {
				if (wifiList.get(i).frequency > 5000) {
					remove.add(i);
				}
			}
			
			for (int i=remove.size() - 1 ; i>=0; i--) {
				wifiList.remove(remove.get(i).intValue());
			}
		}

		private void removeDuplicateSSIDForEachBSSID(List<ScanResult> wifiList) {
			Collections.sort(wifiList, new Comparator<ScanResult>() {

				@Override
				public int compare(ScanResult lhs, ScanResult rhs) {
					return lhs.BSSID.compareTo(rhs.BSSID);
				}
				
			});
			
			String tempBssid = "";
			List<Integer> remove = new ArrayList<Integer>(80);
			for (int i=0; i < wifiList.size(); i++) {
				if (!areMacAddressesEquals(wifiList.get(i), tempBssid)) {
					tempBssid = wifiList.get(i).BSSID;
				} else {
					remove.add(i);
				}
			}
			
			for (int i=remove.size() - 1 ; i>=0; i--) {
				Toast.makeText(getApplicationContext(), "removing " + wifiList.get(remove.get(i)).SSID, Toast.LENGTH_SHORT).show();
				wifiList.remove(remove.get(i).intValue());
			}
		}

		private boolean areMacAddressesEquals(ScanResult scanResult1, String bssid2) {
			
			if (bssid2 == null || bssid2.isEmpty()) {
				return false;
			}
			
			String mac1 = getMacFromBssid(scanResult1.BSSID);
			String mac2 = getMacFromBssid(bssid2);
			
			return mac1.equals(mac2);
		}
	}

	public static String getMacFromBssid(String bssid) {
		
		int lastIndexOf = bssid.lastIndexOf(':');
		String mac = bssid.substring(0, lastIndexOf);
		return mac;
	}
}
