package com.zikto.golfswingapproach;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.golfswingapproach.R;
//import com.example.bttest.R;




public class MainActivity extends Activity {
	TextView out;
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private InputStream inStream = null;
	private BluetoothThread btThread;
	private static final int HISTORY_SIZE = 500; 
	
	private float mLastX, mLastY, mLastZ;
	private boolean mInitialized;
	
    private final float NOISE = (float) 2.0;
    
    private XYPlot plot;
    
    private SimpleXYSeries xSeries = null;
    private SimpleXYSeries ySeries = null;
    private SimpleXYSeries zSeries = null;
    
    private ArrayList<Float> walkList = new ArrayList<Float>();

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
		
		xSeries = new SimpleXYSeries("x");
        xSeries.useImplicitXVals();
        ySeries = new SimpleXYSeries("y");
        ySeries.useImplicitXVals();
        zSeries = new SimpleXYSeries("z");
        zSeries.useImplicitXVals();
        
        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
 
        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
        Number[] series2Numbers = {4, 6, 3, 8, 2, 10};
        
        plot.setRangeBoundaries(0, 60, BoundaryMode.FIXED);
        //plot.setRangeBoundaries(0, 10==-	fffwef0, BoundaryMode.FIXED);
        
        /*
        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
        		xSeries,          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Series1");                             // Set the display title of the series
 
        // same as above
        XYSeries series2 = new SimpleXYSeries(yList, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");
 
 */
        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        //LineAndPointFormatter series1Format = new LineAndPointFormatter();
        //series1Format.setPointLabelFormatter(new PointLabelFormatter());
        //series1Format.configure(getApplicationContext(), 
         //       R.xml.line_point_formatter_with_plf1);
 
        // add a new series' to the xyplot:
        plot.addSeries(xSeries, new LineAndPointFormatter());
 
        // same as above:
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
   


	


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
	
	
	private boolean isStart = false;

	public void onStartClick(View view)
	{
		//Toggle Click
		final Button button = (Button)findViewById(R.id.buttonStart);
		
		if(isStart)
		{
			button.setText("Start");
			String message = "";
			for(float value  : walkList)
			{
				message=message+","+value;
			}
			
			String state = Environment.getExternalStorageState();
		    if (Environment.MEDIA_MOUNTED.equals(state)) {
		       
				try {
					//
			    	//This will get the SD Card directory and create a folder named MyFiles in it.
			    	File sdCard = Environment.getExternalStorageDirectory();
			    	File directory = new File (sdCard.getAbsolutePath() + "/zikto");
			    	directory.mkdirs();

			    	//Now create the file in the above directory and write the contents into it
			    	File file = new File(directory, "walk.csv");
			    	FileOutputStream fOut = new FileOutputStream(file);
					OutputStreamWriter osw = new OutputStreamWriter(fOut);
			    	osw.write(message);
			    	osw.flush();
			    	osw.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				}
		    	
		    }
			
		}
		else
		{
			button.setText("Stop");
			walkList.clear();
			
		}
		isStart = !isStart;
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
	public void onSensorChanged(SensorEvent event)//should be changed...
	{
		TextView tvX= (TextView)findViewById(R.id.x_axis);
		TextView tvY= (TextView)findViewById(R.id.y_axis);
		TextView tvZ= (TextView)findViewById(R.id.z_axis);
		ImageView iv = (ImageView)findViewById(R.id.image);
		
		float x = event.values[0];//should be changed later
		float y = event.values[1];
		float z = event.values[2];
		
		float mag = (float)Math.sqrt(x*x + y*y + z*z);
		
		walkList.add(mag);
		
		
		
		//xQueue.add(x);
		//yQueue.add(y);
		//zQueue.add(z);
		
		if(xSeries.size() > HISTORY_SIZE)
		{
			xSeries.removeFirst();
			ySeries.removeFirst();
			zSeries.removeFirst();
		}

		xSeries.addLast(null, mag);
		ySeries.addLast(null, y);
		zSeries.addLast(null, z);
		
		plot.redraw();
		
		
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			tvX.setText("0.0");
			tvY.setText("0.0");
			tvZ.setText("0.0");
			mInitialized = true;
		} else {
			float deltaX = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);
			float deltaZ = Math.abs(mLastZ - z);
			if (deltaX < NOISE) deltaX = (float)0.0;
			if (deltaY < NOISE) deltaY = (float)0.0;
			if (deltaZ < NOISE) deltaZ = (float)0.0;
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			
			}
		}
		
		//print out messages


    

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



