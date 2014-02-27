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

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.example.golfswingapproach.R;
import com.zikto.invensense.BluetoothModule;
import com.zikto.invensense.utils.PacketParser;
//import com.example.bttest.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private XYPlot plot; ///Main Plot
    private SimpleXYSeries magSeries = null; ///Acceleration Magnitude Serires
	//	private final ListAdapter mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.id.new_devices);
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		out = (TextView) findViewById(R.id.out);

		out.append("\n...In onCreate()...");
		
		//checkBTState();
		
		Button connectButton = (Button)findViewById(R.id.buttonConnect);
		connectButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				//AttemptConnect();
			}
			
		});
	}
	
	public void checkBTState()
	{
		if(!BluetoothModule.getInstance().isReady())
		{
			//Prompt user to turn on Bluetooth
			Intent enableBtIntent = new Intent(BluetoothModule.getInstance().getAdapter().ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, BluetoothModule.REQUEST_ENABLE_BT);
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
	
	
		
}



