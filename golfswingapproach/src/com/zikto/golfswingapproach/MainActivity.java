package com.zikto.golfswingapproach;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import com.example.golfswingapproach.R;
//import com.example.bttest.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;


public class MainActivity extends Activity {
	TextView out;
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private InputStream inStream = null;
	private BluetoothThread btThread;

	// Well known SPP UUID
	private static final UUID MY_UUID =
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Insert your server's MAC address
	private static String address = "00:A0:96:3B:E5:4D";
	//	private final ListAdapter mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.id.new_devices);
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		out = (TextView) findViewById(R.id.out);

		out.append("\n...In onCreate()...");

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		//Create a reciever for the Intent

		//IntentFilter will match the action specified
		//IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		//broadcast reciever for any matching filter
		//this.registerReceiver(mReciever, filter);

		//filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		//this.registerReceiver(mReciever, filter);

		//ListView foundDevicesListView = (ListView) findViewById(R.id.new_devices);
		//foundDevicesListView.setAdapter(mNewDevicesArrayAdapter);

		CheckBTState();
		
		
		out.append("\n...In onResume...\n...Attempting client connect...");

		//TODO : need to search and choose
		//btAdapter.startDiscovery();

		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		// Two things are needed to make a connection:
		//   A MAC address, which we got above.
		//   A Service ID or UUID.  In this case we are using the
		//     UUID for SPP.
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
		}

		// Discovery is resource intensive.  Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection.  This will block until it connects.
		try {
			btSocket.connect();
			out.append("\n...Connection established and data link opened..\n");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
			}
		}

		
		//TODO : Read data packet
		
		btThread = new BluetoothThread(btSocket);
		btThread.start();

	}

	@Override
	public void onStart() {
		super.onStart();
		out.append("\n...In onStart()...");
	}
	
	@Override
	public void onResume() {
		super.onResume();
/*
		out.append("\n...In onResume...\n...Attempting client connect...");

		//TODO : need to search and choose
		//btAdapter.startDiscovery();

		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		// Two things are needed to make a connection:
		//   A MAC address, which we got above.
		//   A Service ID or UUID.  In this case we are using the
		//     UUID for SPP.
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
		}

		// Discovery is resource intensive.  Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection.  This will block until it connects.
		try {
			btSocket.connect();
			out.append("\n...Connection established and data link opened..\n");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
			}
		}

		
		//TODO : Read data packet
		
		btThread = new BluetoothThread(btSocket);
		btThread.start();
		// Create a data stream so we can talk to server.
		//		out.append("\n...Sending message to server...");
		//
		//		try {
		//			outStream = btSocket.getOutputStream();
		//		} catch (IOException e) {
		//			AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
		//		}
		//
		//		String message = "Hello from Android.\n";
		//		byte[] msgBuffer = message.getBytes();
		//		try {
		//			outStream.write(msgBuffer);
		//		} catch (IOException e) {
		//			String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
		//			if (address.equals("00:00:00:00:00:00")) 
		//				msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
		//			msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
		//
		//			AlertBox("Fatal Error", msg);       
		//		}
		 * */
		 
	}

	@Override
	public void onPause() {
		super.onPause();

		out.append("\n...In onPause()...");

	}

	@Override
	public void onStop() {
		super.onStop();
		out.append("\n...In onStop()...");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		out.append("\n...In onDestroy()...");
	}

	private void CheckBTState() {
		// Check for Bluetooth support and then check to make sure it is turned on

		// Emulator doesn't support Bluetooth and will return null
		if(btAdapter==null) { 
			AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
		} else {
			if (btAdapter.isEnabled()) {
				out.append("\n...Bluetooth is enabled...");
			} else {
				//Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
	}

	public void AlertBox( String title, String message ){
		new AlertDialog.Builder(this)
		.setTitle( title )
		.setMessage( message + " Press OK to exit." )
		.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		}).show();
	}


	public final BroadcastReceiver mReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			String action = intent.getAction();

			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//	  			
				if(btDevice.getBondState() != BluetoothDevice.BOND_BONDED){
					//TODO : find and MAC 
					out.append(btDevice.getName()+"\n"+btDevice.getAddress());
					//((ArrayAdapter<String>) mNewDevicesArrayAdapter).add(btDevice.getName()+"\n"+btDevice.getAddress());
				}
				out.append(btDevice.getName()+"\n"+btDevice.getAddress());
				out.append("ACTION_FOUND\n");
			}
			else{

				out.append("ACTION_DISCOVERY\n");
			}
			//	  			if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
			//	  				setProgressBarIndeterminateVisibility(false);
			//	  				//setTitle(R.string.select_device);
			//	  				if(mNewDevicesArrayAdapter.getCount() == 0){
			//	  					String noDevice = getResources().getText(R.string.none_paired).toString();
			//	  					((Object) mNewDevicesArrayAdapter).add(noDevice);
			//	  				}
			//	  			}

		}
	};
	public class BluetoothThread extends Thread{
		private final BluetoothSocket socket;
		private InputStream inStream;
		public BluetoothThread(BluetoothSocket socket)
		{
			this.socket = socket;
			try{
				inStream = socket.getInputStream();
			} catch(IOException e)
			{
				Log.e("TAG", "socket created failed",e);
			}

		}

		public void run()
		{
			int bytes;
			byte[] buffer = new byte[30];

			while(true)
			{
				try
				{
					bytes = inStream.read(buffer);
					String message =  Arrays.toString(buffer);
					//out.append("Data : ");
					
					if( buffer[0] == 36)
						Log.d("BT1",message);	
					
//					for (int i =  0 ; i < bytes ; i++)
//					{
//						//out.append(" "+ buffer[i]);
//						Log.d("BT1",buffer[i]);
//					}
					//out.append("\n");
					
				}catch(IOException e )
				{
					AlertBox("IO Exception",e.getMessage());
				}
			} 
		}
	}
}



