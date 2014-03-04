package com.zikto.ziktowalkprofiler;

import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.zikto.golfswingapproach.R;
import com.zikto.invensense.BluetoothModule;
import com.zikto.invensense.utils.PacketParser;
import com.zikto.utils.server.ServerTools;
//import com.example.bttest.R;





import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;


public class MainActivity extends Activity {

	private TextView mheadtext;
	private TextView out;
	private AccelerometerManager accelManager;
	private InvensenseManager invenManager;
	private PlotManager plotManager;
	private boolean isStart=false;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 

		mheadtext = (TextView)findViewById(R.id.headtext);

		RadioGroup mrgroup = (RadioGroup)findViewById(R.id.rgroup);
		mrgroup.setOnCheckedChangeListener(mRadioCheck);


		final Button startbutton = (Button)findViewById(R.id.startbtn);
		final Button connectButoon = (Button)findViewById(R.id.connectButton);

		XYPlot plot;
		//	private final ListAdapter mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.id.new_devices);
		/** Called when the activity is first created. */


		out = (TextView) findViewById(R.id.out);
		plot = (XYPlot) findViewById(R.id.mainPlot);

		out.append("ZIKTO, Ready...");

		plotManager = new PlotManager(plot);



		//DEBUG


		connectButoon.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				plotManager.clear();				
			}

		});


		startbutton.setOnClickListener(new Button.OnClickListener() {

			@SuppressLint("SimpleDateFormat")
			public void onClick(View v) {
				EditText edit=(EditText)findViewById(R.id.editText1);
				String filename = edit.getText().toString();
				if(filename=="")
				{
					filename="default";
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String currentDateandTime = sdf.format(new Date());
				
				filename = filename+currentDateandTime+".csv";

				if(isStart)
				{
					stopPhoneSensor();
					startbutton.setText("Start Tracking");
					String message = "";

					ArrayList<Float> walkList = plotManager.getMagList();

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
							File file = new File(directory, filename);
							FileOutputStream fOut = new FileOutputStream(file);
							OutputStreamWriter osw = new OutputStreamWriter(fOut);
							osw.write(message);
							osw.flush();
							osw.close();
							int response = ServerTools.uploadFile(file.getAbsolutePath());
							if(response == 200)
								out.append("\nSending to server : SUCCESS!");
							else
								out.append("\nSending to server : FAIL");
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch(IOException e) {
							e.printStackTrace();
						}

					}

				}
				else
				{
					startPhoneSensor();
					startbutton.setText("Stop Tracking");
				}
				isStart = !isStart;
			}

		});

		//DEBUG
		//startPhoneSensor();
		//startInvensenseSensor();

		//
	}

	RadioGroup.OnCheckedChangeListener mRadioCheck = new RadioGroup.OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (group.getId()==R.id.rgroup) {

				//plotManager.clear();
				switch (checkedId) 
				{

				case R.id.phonebtn:
					// Put phoneBluetooth Module Here
					Toast.makeText(MainActivity.this,"Phone Sensor Selected",Toast.LENGTH_SHORT).show();
					stopInvensenseSensor();
					startPhoneSensor();
					break;
				case R.id.invensensebtn:
					Toast.makeText(MainActivity.this,"Invensense Sensor Selected",Toast.LENGTH_SHORT).show();
					stopPhoneSensor();
					startInvensenseSensor();
					break;
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

	public void stopPhoneSensor()
	{
		if(accelManager != null)
		{
			accelManager.stop();
		}
	}

	public void startInvensenseSensor()
	{
		checkBTState();
		invenManager = new InvensenseManager(this, plotManager);
		invenManager.start();
	}

	public void stopInvensenseSensor()
	{
		if(invenManager!=null)
		{
			invenManager.stop();
		}
	}


	@Override
	public void onStart() {
		super.onStart();
	//	out.append("\n...In onStart()...");
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	//	out.append("\n...In onPause()...");
	}

	@Override
	public void onStop() {
		super.onStop();
	//	out.append("\n...In onStop()...");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	//	out.append("\n...In onDestroy()...");
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



