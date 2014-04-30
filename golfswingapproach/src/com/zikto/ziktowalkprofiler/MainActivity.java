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
import java.util.LinkedList;
import java.util.concurrent.Callable;

import com.androidplot.xy.XYPlot;
import com.zikto.ziktowalkprofiler.R;
import com.zikto.invensense.BluetoothModule;
import com.zikto.utils.server.MultitouchPlot;
import com.zikto.utils.server.ServerTools;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Sensor;


public class MainActivity extends Activity {
	private TextView out;
	private AccelerometerManager accelManager;
	private ThreeAxisAccelManager threeAccelManager;
	private UniversalSensorManager universalManager;
	
	private InvensenseManager invenManager;
	private PlotManager plotManager;
	private PlotManager subplot1Manager;
	private PlotManager subplot2Manager;
	private boolean isStart=false;
	private AsyncUploadFile sendingManager;

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
		final Button fpglogo = (Button) findViewById(R.id.fpglogo);

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
					startbutton.setText("Start Tracking");
//					startbutton.setBackgroundResource(R.drawable.buttonstart);
				}
				else
				{
					startPhoneSensor();
					startbutton.setText("Stop Tracking");
					//startbutton.setBackgroundResource(R.drawable.buttonstop);
				}
				isStart = !isStart;
			}
		});
		
		sendingManager = new AsyncUploadFile(this);
		
		
		fpglogo.setOnClickListener(new Button.OnClickListener()
		{

			@Override
			public void onClick(View v) {
				closeKeyboard();
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
//		threeAccelManager = new ThreeAxisAccelManager(this, plotManager, subplot1Manager, subplot2Manager);
//		threeAccelManager.start();
		if( universalManager == null)
		{
			universalManager = new UniversalSensorManager(this,plotManager,subplot1Manager,subplot2Manager);
			
		}
		universalManager.start();
		universalManager.draw(Sensor.TYPE_GYROSCOPE);
	}

	public void stopPhoneSensor()
	{
//		if(accelManager != null)
//		{
//			accelManager.stop();
//		}
		
//		if(threeAccelManager != null)
//		{
//			threeAccelManager.stop();
//		}
		if(universalManager != null)
		{
			universalManager.stop();
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

	public void clearAllField()
	{
		final EditText editPelvic = (EditText)findViewById(R.id.editPelvic);
		final EditText editComment = (EditText)findViewById(R.id.editComments);
		final EditText editAge  = (EditText)findViewById(R.id.editAge);
		final EditText editWeight  = (EditText)findViewById(R.id.editWeight);
		final EditText editHeight  = (EditText)findViewById(R.id.editHeight);
		final EditText edit=(EditText)findViewById(R.id.editText1);
		
		editPelvic.getText().clear();
		editComment.getText().clear();
		editAge.getText().clear();
		editWeight.getText().clear();
		editHeight.getText().clear();
		edit.getText().clear();
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

	@SuppressLint("SimpleDateFormat")
	public void sendData()
	{
		final EditText editPelvic = (EditText)findViewById(R.id.editPelvic);
		final EditText editComment = (EditText)findViewById(R.id.editComments);
		final EditText editAge  = (EditText)findViewById(R.id.editAge);
		final EditText editWeight  = (EditText)findViewById(R.id.editWeight);
		final EditText editHeight  = (EditText)findViewById(R.id.editHeight);
		final EditText edit=(EditText)findViewById(R.id.editText1);
		
		String filename = edit.getText().toString(); 
		String pelvicRotation = editPelvic.getText().toString();
		String meta = editComment.getText().toString();
		String age = editAge.getText().toString();
		String height = editHeight.getText().toString();
		String weight = editWeight.getText().toString();
		
		//if(!ValidateInputs())return;
		
		if(filename=="")
		{
			filename="default";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String currentDateandTime = sdf.format(new Date());
		
		filename = filename+currentDateandTime+".csv";
		String message = "";
		
		ArrayList<LinkedList<Float>> sensorData = new ArrayList<LinkedList<Float>>();
		ArrayList<LinkedList<Long>> timeStamps = new ArrayList<LinkedList<Long>>();
		for(int i = 0 ; i < 3 ; i ++)
		{
			sensorData.add(universalManager.getAccelData(i));
		}
		for(int i = 0 ; i < 3 ; i ++)
		{
			sensorData.add(universalManager.getGyroData(i));
		}
		for(int i = 0 ; i < 4 ; i ++)
		{
			sensorData.add(universalManager.getRotationData(i));
		}
		for(int i = 0 ; i < 3; i++)
		{
			sensorData.add(universalManager.getLinearAccelData(i));
		}
		
		timeStamps.add(universalManager.getAccelTime());
		timeStamps.add(universalManager.getGyroTime());
		timeStamps.add(universalManager.getRotationTime());
		timeStamps.add(universalManager.getLinearAccelTime());
		
		StringBuilder buffer = new StringBuilder();
		
		//String Nickname, Gender, Device, Rotation, Meta, Position,Status,Age,weight,height
		
		String name = edit.getText().toString();
		String gender,position;
		final ToggleButton genderButton =  (ToggleButton)findViewById(R.id.genderButton);
		if(genderButton.isChecked())
		{
			gender = "M";
		}
		else
		{
			gender = "F";
		}
		
		final ToggleButton positionButton =  (ToggleButton)findViewById(R.id.handButton);
		
		if(positionButton.isChecked())
		{
			position = "right_wrist";
		}
		else
		{
			position = "left_wrist";
		}
		
		String device = "galaxy gear";
		
		for( LinkedList<Float> list : sensorData)
		{
			for( Float value : list)
			{
				buffer.append( ","+value);
			}
			buffer.append("\n");
		}
		
		for( LinkedList<Long> list : timeStamps)
		{
			for( Long value : list)
			{
				buffer.append( ","+value);
			}
			buffer.append("\n");
		}
		
		buffer.append(name+";"+gender+";"+device+";"+pelvicRotation+";"+meta+";"+position+";casual walking;"+age+";"+weight+";"+height+"\n");
		
		message = buffer.toString();
		
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
				//int response = ServerTools.uploadFile(file.getAbsolutePath());
				new AsyncUploadFile(this).execute(file.getAbsolutePath());

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(Exception e)
			{
				e.printStackTrace();
			}

		}

	}
	
	public void DisplayServerMessage(Long response)
	{
		if(response == 200)
			Toast.makeText(getApplicationContext(), (String)"Sending to server : SUCCESS!", 
					   Toast.LENGTH_LONG).show();
		else
			Toast.makeText(getApplicationContext(), (String)"Sending to server : FAIL!", 
					   Toast.LENGTH_LONG).show();
	}

	public void AlertBox( String title, String message ){
		new AlertDialog.Builder(this)
		.setTitle( title )
		.setMessage( message ).show();
//		.setPositiveButton("OK", new OnClickListener() {
//			public void onClick(DialogInterface arg0, int arg1) {
//				finish();
//			}
//		}
		
	}
	
	public boolean ValidateInputs()
	{
		final EditText editPelvic = (EditText)findViewById(R.id.editPelvic);
		final EditText editComment = (EditText)findViewById(R.id.editComments);
		final EditText editAge  = (EditText)findViewById(R.id.editAge);
		final EditText editWeight  = (EditText)findViewById(R.id.editWeight);
		final EditText editHeight  = (EditText)findViewById(R.id.editHeight);
		final EditText edit=(EditText)findViewById(R.id.editText1);
		
		String filename = edit.getText().toString(); 
		String pelvicRotation = editPelvic.getText().toString();
		String meta = editComment.getText().toString();
		String age = editAge.getText().toString();
		String height = editHeight.getText().toString();
		String weight = editWeight.getText().toString();
		
		if(filename.isEmpty())
		{
			AlertBox("Sorry", "Enter Name.");
			return false;
		}
		
		if(pelvicRotation.isEmpty())
		{
			AlertBox("Sorry", "Enter Pelvic Rotation Data.");
			return false;
		}
		
		if(age.isEmpty())
		{
			AlertBox("Sorry", "Enter Age.");
			return false;
		}
		
		if(weight.isEmpty())
		{
			AlertBox("Sorry", "Enter Weight.");
			return false;
		}
		if(height.isEmpty())
		{
			AlertBox("Sorry", "Enter Height.");
			return false;
		}
		
		return true;
	}
	
	public void closeKeyboard()
	{
		Log.d("d","d");
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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



