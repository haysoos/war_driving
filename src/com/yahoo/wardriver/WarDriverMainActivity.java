package com.yahoo.wardriver;

import java.util.ArrayList;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class WarDriverMainActivity extends Activity {

	private Button btnScan;
	private WifiManager mainWifi;
	private WifiReceiver receiverWifi;
	public List<ScanResult> wifiList = new ArrayList<ScanResult>();
	private ListView lvScanResults;
	private ScanResultArrayAdapter arrayAdapter;

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

		btnScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				scanButtonClicked();
			}
		});

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
			removeFrequenciesGreaterThan5GHz(wifiList);
			removeDuplicateSSIDForEachBSSID(wifiList);
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

		private String getMacFromBssid(String bssid) {
			
			int lastIndexOf = bssid.lastIndexOf(':');
			String mac = bssid.substring(0, lastIndexOf);
			return mac;
		}
	}

}
