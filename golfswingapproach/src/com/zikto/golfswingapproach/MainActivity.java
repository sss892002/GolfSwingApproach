package com.zikto.golfswingapproach;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Intent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;


public class MainActivity extends Activity {

	TextView mheadtext;
	TextView out;
	AccelerometerManager accelManager;
	PlotManager plotManager;
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mheadtext = (TextView)findViewById(R.id.headtext);

		RadioGroup mrgroup = (RadioGroup)findViewById(R.id.rgroup);
		mrgroup.setOnCheckedChangeListener(mRadioCheck);


		final Button startbutton = (Button)findViewById(R.id.startbtn);


		XYPlot plot;
		//	private final ListAdapter mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.id.new_devices);
		/** Called when the activity is first created. */


		out = (TextView) findViewById(R.id.out);
		plot = (XYPlot) findViewById(R.id.mainPlot);

		out.append("\n...In onCreate()...");

		plotManager = new PlotManager(plot);



		//DEBUG

		startPhoneSensor();


		startbutton.setOnClickListener(new Button.OnClickListener() {

			private boolean isStart=false;

			public void onClick(View v) {
				EditText edit=(EditText)findViewById(R.id.editText1);
				String filename = edit.getText().toString();

				if(isStart)
				{
					startbutton.setText("Start Tracking");
					String message = "";

					message="File Writing Test";

					String state = Environment.getExternalStorageState();
					if (Environment.MEDIA_MOUNTED.equals(state)) {

						try {
							//
							//This will get the SD Card directory and create a folder named MyFiles in it.
							File sdCard = Environment.getExternalStorageDirectory();
							File directory = new File (sdCard.getAbsolutePath() + "/ziktoshawn");
							directory.mkdirs();

							//Now create the file in the above directory and write the contents into it
							File file = new File(directory, filename+".csv");
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
					startbutton.setText("Stop Tracking");

				}
				isStart = !isStart;



				//save as filename 

			}

		});
	}

	RadioGroup.OnCheckedChangeListener mRadioCheck = new RadioGroup.OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (group.getId()==R.id.rgroup) {
				switch (checkedId) {
				case R.id.phonebtn:
					// Put phoneBluetooth Module Here
					Toast.makeText(MainActivity.this,"Phone Sensor Selected",Toast.LENGTH_SHORT).show();

					break;


				case R.id.invensensebtn:
					Toast.makeText(MainActivity.this,"Invensense Sensor Selected",Toast.LENGTH_SHORT).show();
					//  Put invensense Parsing module here
//					Button connectButton = (Button)findViewById(R.id.buttonConnect);
//					connectButton.setOnClickListener(new Button.OnClickListener(){
//						@Override
//						public void onClick(View v) {
//						}
//					});
//
//					break;
				}
			}
		}
	};


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
	}

	public void setAccel(float value)
	{

	}

	@Override
	public void onStart() {
		super.onStart();
		out.append("\n...In onStart()...");
		for(int i = 0 ; i < 10 ; i ++)
		{
			//			if(!AttemptConnect())
			//			{
			//				out.append("\nReconnect in 3 seconds");
			//				out.invalidate();
			//				SystemClock.sleep(3000);
			//			}
			//			else
			//			{
			//				break;
			//			}

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



