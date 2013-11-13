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
		tvScanResult.setText(scanResult.toString());

		return v;
	}

}
