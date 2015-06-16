package com.amarzi.bluduino;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Button getDataBtn;
	private Button scanBtBtn;
	private TextView data;
	private BluetoothAdapter bluetoothAdapter;
	private ArrayAdapter<String> listAdapter;
	private ListView scanView;
	private Set<BluetoothDevice> pairedDevicesSet;
	private ArrayList<String> pairedDevicesList;
	private IntentFilter intentFilterBT;
	private BroadcastReceiver broadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initialize bluetooth adapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevicesList = new ArrayList<String>();
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					listAdapter.add(device.getName() + "\n" + device.getAddress());
				} else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
					
				} else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					
				} else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
					if(bluetoothAdapter.getState() == bluetoothAdapter.STATE_OFF) {
						turnOnBluetooth();
					}
				}
				
			}
			
		};
		
		intentFilterBT = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(broadcastReceiver, intentFilterBT);
		intentFilterBT = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(broadcastReceiver, intentFilterBT);
		intentFilterBT = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(broadcastReceiver, intentFilterBT);
		intentFilterBT = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(broadcastReceiver, intentFilterBT);
		
		if(bluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(), "No Bluetooth detected", Toast.LENGTH_LONG).show();
		} else {
			if(!bluetoothAdapter.isEnabled()) {
				turnOnBluetooth();
			}
		}
		
		getPairedDevices();

		// initialize ui widgets
		getDataBtn = (Button) this.findViewById(R.id.dataBtn);
		scanBtBtn = (Button) this.findViewById(R.id.scanBtn);
		
		scanView = (ListView) this.findViewById(R.id.scanView);
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
		scanView.setAdapter(listAdapter);
		data = (TextView) this.findViewById(R.id.dataTV);
	}

	private void turnOnBluetooth() {
		// TODO Auto-generated method stub
		
	}

	private void getPairedDevices() {
		pairedDevicesSet = bluetoothAdapter.getBondedDevices();
		if(pairedDevicesSet.size() > 0) {
			for(BluetoothDevice device : pairedDevicesSet) {
				pairedDevicesList.add(device.getName());
				//listAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		// unregister broadcastReceiver to prevent crash on pause
		unregisterReceiver(broadcastReceiver);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode== RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public  void visible(View v){
	      Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	      startActivityForResult(getVisible, 0);
	}	   
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
