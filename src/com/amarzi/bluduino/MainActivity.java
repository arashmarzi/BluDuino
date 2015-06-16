package com.amarzi.bluduino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener {
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9834FB");
	protected static final int SUCCESS_CONNECT = 0;
	public static final int MESSAGE_READ = 1;
	private Button getDataBtn;
	private Button scanBtBtn;
	private TextView data;
	private BluetoothAdapter bluetoothAdapter;
	private ArrayAdapter<String> listViewAdapter;
	private ListView scanListView;
	private Set<BluetoothDevice> pairedDevicesSet;
	private ArrayList<String> pairedDevicesList;
	private ArrayList<BluetoothDevice> bluetoothDevices;
	private IntentFilter intentFilterBT;
	private BroadcastReceiver broadcastReceiver;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initialize bluetooth adapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevicesList = new ArrayList<String>();
		bluetoothDevices = new ArrayList<BluetoothDevice>();
		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					bluetoothDevices.add(device);
					String pairedStr = "";
					String deviceStr = device.getName() + "\n" + device.getAddress();
					for(int i = 0; i < pairedDevicesList.size(); i++) {
						if(deviceStr.equals(pairedDevicesList.get(i))) {
							pairedStr = "(PAIRED)";
							break;
						}
						
					}
					
					listViewAdapter.add(device.getName() + " " + pairedStr + "\n" + device.getAddress());
				} else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
					// to be implemented
				} else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					// to be implemented
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
		startDiscovery();

		// initialize ui widgets
		getDataBtn = (Button) this.findViewById(R.id.dataBtn);
		scanBtBtn = (Button) this.findViewById(R.id.scanBtn);
		
		scanListView = (ListView) this.findViewById(R.id.scanListView);
		scanListView.setOnItemClickListener(this);
		listViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
		scanListView.setAdapter(listViewAdapter);
		data = (TextView) this.findViewById(R.id.dataTV);
		
		// initialize handler for ConnectThread
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
				case SUCCESS_CONNECT:
					ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
					Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_LONG).show();
					String ack = "ACK";
					connectedThread.write(ack.getBytes());
					break;
				case MESSAGE_READ:
					byte[] readBuffer = (byte[])msg.obj;
					String readStr = new String(readBuffer);
					Toast.makeText(getApplicationContext(), readStr, Toast.LENGTH_LONG).show();
					break;
				}
			}
			
		};
	}

	private void startDiscovery() {
		bluetoothAdapter.cancelDiscovery();
		bluetoothAdapter.startDiscovery();
		
	}

	private void turnOnBluetooth() {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);
	}

	private void getPairedDevices() {
		pairedDevicesSet = bluetoothAdapter.getBondedDevices();
		if(pairedDevicesSet.size() > 0) {
			for(BluetoothDevice device : pairedDevicesSet) {
				pairedDevicesList.add(device.getName() + "\n" + device.getAddress());
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
		}
		
		if(listViewAdapter.getItem(arg2).contains("(PAIRED)")) {
			BluetoothDevice selectedDevice = bluetoothDevices.get(arg2);
			ConnectThread connectThread = new ConnectThread(selectedDevice);
			connectThread.start();
			
			
			Toast.makeText(getApplicationContext(), "Device is already paired", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), "Device is not paired", Toast.LENGTH_SHORT).show();
		}
	}
	
	private class ConnectThread extends Thread {
	    
		private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        bluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	        // Do work to manage the connection (in a separate thread), ALWAYS NEED sendToTarget()!
	        mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
	    }

		/** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	            	// Clear buffer
	            	buffer = new byte[1024];	            	
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                        .sendToTarget();
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
}
