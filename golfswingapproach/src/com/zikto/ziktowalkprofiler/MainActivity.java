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
import java.util.Date;

import com.androidplot.xy.XYPlot;
import com.zikto.ziktowalkprofiler.R;
import com.zikto.invensense.BluetoothModule;
import com.zikto.utils.server.ServerTools;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;


public class MainActivity extends Activity {
	private TextView out;
	private AccelerometerManager accelManager;
	private ThreeAxisAccelManager threeAccelManager;
	
	private InvensenseManager invenManager;
	private PlotManager plotManager;
	private PlotManager subplot1Manager;
	private PlotManager subplot2Manager;
	private boolean isStart=false;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//Remove Titlebar
	    requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);
		
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//
//		StrictMode.setThreadPolicy(policy); 

		//mheadtext = (TextView)findViewById(R.id.headtext);

		//RadioGroup mrgroup = (RadioGroup)findViewById(R.id.rgroup);
		

		final Button startbutton = (Button)findViewById(R.id.startbtn);
//		final Button connectButoon = (Button)findViewById(R.id.connectButton);
		final Button sendButton = (Button)findViewById(R.id.sendButton);

		XYPlot plot;
		XYPlot subPlot1;
		XYPlot subPlot2;
		//	private final ListAdapter mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.id.new_devices);
		/** Called when the activity is first created. */

		out = (TextView) findViewById(R.id.out);
		plot = (XYPlot) findViewById(R.id.mainPlot);
		subPlot1 = (XYPlot) findViewById(R.id.subPlot1);
		subPlot2 = (XYPlot) findViewById(R.id.subPlot2);
		
		out.append("Ready...");

		plotManager = new PlotManager(plot);
		subplot1Manager = new PlotManager(subPlot1);
		subplot2Manager = new PlotManager(subPlot2);

		//DEBUG
		sendButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				sendData();
			}
			
		});

		startbutton.setOnClickListener(new Button.OnClickListener() {

			@SuppressLint("SimpleDateFormat")
			public void onClick(View v) {
				

				if(isStart)
				{
					stopPhoneSensor();
					startbutton.setBackgroundResource(R.drawable.buttonstart);
				}
				else
				{
					startPhoneSensor();
					startbutton.setBackgroundResource(R.drawable.buttonstop);
				}
				isStart = !isStart;
			}
		});
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
//		accelManager = new AccelerometerManager(this, plotManager);
//		accelManager.start();
		threeAccelManager = new ThreeAxisAccelManager(this, plotManager, subplot1Manager, subplot2Manager);
		threeAccelManager.start();
	}

	public void stopPhoneSensor()
	{
//		if(accelManager != null)
//		{
//			accelManager.stop();
//		}
		
		if(threeAccelManager != null)
		{
			threeAccelManager.stop();
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

	public void sendData()
	{
		EditText edit=(EditText)findViewById(R.id.editText1);
		String filename = edit.getText().toString();
		if(filename=="")
		{
			filename="default";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String currentDateandTime = sdf.format(new Date());
		
		filename = filename+currentDateandTime+".csv";
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
//				if(response == 200)
//					out.append("\nSending to server : SUCCESS!");
//				else
//					out.append("\nSending to server : FAIL");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
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
		}
	};
}



