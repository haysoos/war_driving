package com.yahoo.wardriver;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ScanResultArrayAdapter extends ArrayAdapter<ScanResult> {

	public ScanResultArrayAdapter(Context context, List<ScanResult> objects) {
		super(context, R.layout.war_driver_activity_main, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout v = (RelativeLayout) vi.inflate(R.layout.scan_result_listview_item, null);
		ScanResult scanResult = this.getItem(position);
		TextView tvScanResult = (TextView) v.findViewById(R.id.tvScanResult);
		StringBuilder sb = new StringBuilder();
		sb.append((position + 1));
		sb.append(". BSSID:");
		sb.append(scanResult.BSSID);
		sb.append("\nSSID:");
		sb.append(scanResult.SSID);
		sb.append(", level: ");
		sb.append(scanResult.level + 100);
		sb.append(", frequency:");
		sb.append(scanResult.frequency);
		sb.append(", timestamp: ");
		sb.append(scanResult.timestamp);
		tvScanResult.setText(sb.toString());

		return v;
	}

}
