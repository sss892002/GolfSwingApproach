package com.zikto.ziktowalkprofiler.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import com.androidplot.xy.XYPlot;
import com.zikto.ziktowalkprofiler.AsyncUploadFile;
import com.zikto.ziktowalkprofiler.MainActivity;
import com.zikto.ziktowalkprofiler.PlotManager;
import com.zikto.ziktowalkprofiler.R;
import com.zikto.ziktowalkprofiler.UniversalSensorManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MeasureFragment extends Fragment {
	private boolean isStart=false;
	private UniversalSensorManager universalManager;
	
	private PlotManager plotManager;
	private PlotManager subplot1Manager;
	private PlotManager subplot2Manager;	
	private ViewGroup rootView;
	private View profileView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
	{
		MainActivity mainActivity = (MainActivity)this.getActivity();
		profileView = mainActivity.getMainFragment().getView();
		rootView = (ViewGroup) inflater.inflate(R.layout.measurment_main, container, false);
		
		final Button startbutton = (Button)rootView.findViewById(R.id.startbtn);
		final Button sendButton = (Button)rootView.findViewById(R.id.sendButton);
		XYPlot plot;
		XYPlot subPlot1;
		XYPlot subPlot2;

		plot = (XYPlot) rootView.findViewById(R.id.mainPlot);
		subPlot1 = (XYPlot) rootView.findViewById(R.id.subPlot1);
		subPlot2 = (XYPlot) rootView.findViewById(R.id.subPlot2);
		
		plotManager = new PlotManager(plot);
		subplot1Manager = new PlotManager(subPlot1);
		subplot2Manager = new PlotManager(subPlot2);
//
//		//DEBUG
		sendButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				sendData();
			}
			
		});
//
		startbutton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				final Button sendButton = (Button)rootView.findViewById(R.id.sendButton);


				if(isStart)
				{
					stopPhoneSensor();
					startbutton.setText("Start Tracking");
					sendButton.setEnabled(true);
//					startbutton.setBackgroundResource(R.drawable.buttonstart);
				}
				else
				{
					startPhoneSensor();
					startbutton.setText("Stop Tracking");
					sendButton.setEnabled(false);
					//startbutton.setBackgroundResource(R.drawable.buttonstop);
				}
				isStart = !isStart;
			}
		});	
		
		return rootView;
	}
	
	public void startPhoneSensor()
	{

		if( universalManager == null)
		{
			universalManager = new UniversalSensorManager((SensorManager)this.getActivity().getSystemService(Context.SENSOR_SERVICE),plotManager,subplot1Manager,subplot2Manager);		
		}
		universalManager.start();
		universalManager.draw(Sensor.TYPE_GYROSCOPE);
		
		rootView.setKeepScreenOn(true);
	}

	public void stopPhoneSensor()
	{

		if(universalManager != null)
		{
			universalManager.stop();
		}

		rootView.setKeepScreenOn(false);
	}
	
	public void DisplayServerMessage(Long response)
	{
		if(response == 200)
			Toast.makeText(this.getActivity().getApplicationContext(), (String)"Sending to server : SUCCESS!", 
					   Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this.getActivity().getApplicationContext(), (String)"Sending to server : FAIL!", 
					   Toast.LENGTH_LONG).show();
	}
	
	@SuppressLint("SimpleDateFormat")
	public void sendData()
	{
		//ViewGroup profileView = (ViewGroup) inflater.inflate(R.layout.measurment_main, container, false);

		final EditText editPelvic = (EditText)profileView.findViewById(R.id.editPelvic);
		final EditText editComment = (EditText)profileView.findViewById(R.id.editComments);
		final EditText editAge  = (EditText)profileView.findViewById(R.id.editAge);
		final EditText editWeight  = (EditText)profileView.findViewById(R.id.editWeight);
		final EditText editHeight  = (EditText)profileView.findViewById(R.id.editHeight);
		final EditText edit=(EditText)profileView.findViewById(R.id.editText1);
		
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
		final ToggleButton genderButton =  (ToggleButton)profileView.findViewById(R.id.genderButton);
		if(genderButton.isChecked())
		{
			gender = "M";
		}
		else
		{
			gender = "F";
		}
		
		final ToggleButton positionButton =  (ToggleButton)profileView.findViewById(R.id.handButton);
		
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
}
