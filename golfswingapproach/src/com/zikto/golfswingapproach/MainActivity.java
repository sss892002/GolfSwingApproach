package com.zikto.golfswingapproach;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Intent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import com.example.golfswingapproach.R;
import com.zikto.invensense.utils.PacketParser;
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


		CheckBTState();
		
		
		out.append("\n\n\nAttempting client connect...");		
		
	}
	
	public boolean AttemptConnect()
	{
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

		out.invalidate();

		try {
			btSocket.connect();
			out.append("\n...Connection established and data link opened..\n");
			out.append("Start Reading...");
//			btThread = new BluetoothThread(btSocket);
//			btThread.start();
			BluetoothReadThread(btSocket);
			return true;
		} catch (IOException e) {
			
			out.append("\nConnection Failed!!!Reconnect in 3 seconds");
			try
			{
				btSocket.close();
			}catch(IOException ioerror)
			{
				out.append(ioerror.getMessage());
			}
			
			return false;//SystemClock.sleep(3000);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		out.append("\n...In onStart()...");
		for(int i = 0 ; i < 10 ; i ++)
		{
			if(!AttemptConnect())
			{
				out.append("\nReconnect in 3 seconds");
				out.invalidate();
				SystemClock.sleep(3000);
			}
			else
			{
				break;
			}
			
		}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();

		 
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
	
	private void BluetoothReadThread(BluetoothSocket btSocket)
	{
		final BluetoothSocket socket = btSocket;
		final Handler handler = new Handler();
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				int bytes;
				//TODO:Fix reading module.Looks like it's missing tons of data
				byte[] buffer = new byte[30];
				InputStream inStream;
				try {
					inStream = socket.getInputStream();
					
					while(true)
					{
						try
						{
							Log.d("ALIVE","Read");
							bytes = inStream.read(buffer);
							String message =  Arrays.toString(buffer);
							//out.append("Data : ");
							
							if( buffer[0] == 36)
							{
								Log.d("BT1",message);
								final PacketParser p = new PacketParser(buffer);
								if(p.getAccelData()!=null)
								{
									Log.d("BT2",p.getAccelData().toString());
									handler.post(new Runnable()
									{

										@Override
										public void run() {
											out.append(p.getAccelData().toString());
										}
										
									});
									//out.append(p.getAccelData().toString());
								}
							}
//							for (int i =  0 ; i < bytes ; i++)
//							{
//								//out.append(" "+ buffer[i]);
//								Log.d("BT1",buffer[i]);
//							}
							//out.append("\n");
							
						}catch(IOException e )
						{
							Log.d("Error",e.getMessage());
						}
						
						
					} 
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				
			}
			
		};
		
		new Thread(runnable).start();
		
	}
}



