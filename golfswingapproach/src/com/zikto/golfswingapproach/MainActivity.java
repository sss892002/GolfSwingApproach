package com.zikto.golfswingapproach;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.zikto.golfswingapproach.R;
import com.zikto.invensense.BluetoothModule;
import com.zikto.invensense.utils.PacketParser;
//import com.example.bttest.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;


public class MainActivity extends Activity {
	TextView out;
	AccelerometerManager accelManager;
	InvensenseManager invenManager;
	PlotManager plotManager;
	
	XYPlot plot;
	//	private final ListAdapter mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.id.new_devices);
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		out = (TextView) findViewById(R.id.out);
		plot = (XYPlot) findViewById(R.id.mainPlot);

		out.append("\n...In onCreate()...");
		
		plotManager = new PlotManager(plot);
		
		Button connectButton = (Button)findViewById(R.id.buttonConnect);
		connectButton.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
			}
		});
		
		//DEBUG
		//startPhoneSensor();
		startInvensenseSensor();
	}
	
	public void checkBTState()
	{
		if(!BluetoothModule.getInstance().isReady())
		{
			BluetoothModule.getInstance().getAdapter();
			//Prompt user to turn on Bluetooth
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, BluetoothModule.REQUEST_ENABLE_BT);
		}
	}
	
	public void startPhoneSensor()
	{
		//Accelerometer Manager
		accelManager = new AccelerometerManager(this, plotManager);
		accelManager.start();
	}
	
	public void startInvensenseSensor()
	{
		checkBTState();
		invenManager = new InvensenseManager(this, plotManager);
		invenManager.start();
	}
	

	@Override
	public void onStart() {
		super.onStart();
		out.append("\n...In onStart()...");
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
		}
	};
}



