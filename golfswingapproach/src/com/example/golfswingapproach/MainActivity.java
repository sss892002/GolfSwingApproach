package com.example.golfswingapproach;

import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();
	mBTAdapter.startDiscovery();

	
	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	
	this.registerReceiver(mReceiver,filter);
	filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	this.registerReceiver(mReceiver, filter);
	}
	private final BroadcastReceiver mReceiver= new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent){
	String action = intent.getAction();
	if (BluetoothDevice.ACTION_FOUND.equals(action)){
		BluetoothDevice device =intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		
		if (device.getBondState() !=BluetoothDevice.BOND_BONDED){
			mNewDevicesArrayAdapter.add(device.getName()+ "\n"+ device.getAddress());
			
		}
		
	}
	else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
	setProgressBarIndeterminateVisibility(false);
	setTitle(R.string.select_device);
	if (mNewDeviceArrayAdapter.getCount()==0){
		String noDevices= getText(R.String.none_found).toString();
		mNewDevicesArrayAdapter.add(noDevices);
		
	}
	}
	}

	
	mBTAdapter.cancelDiscovery ();
	BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
	private static final UUID MY_UUID = UUID.fromString("00001108-0000-1000-8000-00805F9B34FB");
	mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
	mmSocket.connect();
	};	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
}

