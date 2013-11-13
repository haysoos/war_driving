package com.yahoo.wardriver;

import java.util.ArrayList;
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
			arrayAdapter.clear();
			arrayAdapter.addAll(wifiList);
			arrayAdapter.notifyDataSetChanged();
		}
	}

}
