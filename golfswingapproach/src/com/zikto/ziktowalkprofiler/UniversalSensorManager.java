package com.zikto.ziktowalkprofiler;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class UniversalSensorManager implements SensorEventListener {
	
	static private int RATE = SensorManager.SENSOR_DELAY_FASTEST;
	
	private SensorManager sensorManager;
	
	private Sensor accelrometer;
	private ArrayList<LinkedList<Float>> accelList = new ArrayList<LinkedList<Float>>();
	private LinkedList<Long >accelTime = new LinkedList<Long>(); 
	private Sensor gyroscope;
	private ArrayList<LinkedList<Float>> gyroList = new ArrayList<LinkedList<Float>>();
	private LinkedList<Long > gyroTime = new LinkedList<Long>();
	private Sensor rotationvector;
	private ArrayList<LinkedList<Float>> rotationList = new ArrayList<LinkedList<Float>>();
	private LinkedList<Long >rotationTime = new LinkedList<Long>();
	private Sensor linearAccelrometer;
	private ArrayList<LinkedList<Float>> linearAccelList = new ArrayList<LinkedList<Float>>();
	private LinkedList<Long> linearAccelTime = new LinkedList<Long>();
	private ArrayList<PlotManager> plotList = new ArrayList<PlotManager>();
	
	private int drawingPlot=0;
	
	public UniversalSensorManager(Activity activity, PlotManager plot1, PlotManager plot2 , PlotManager plot3)
	{
		sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		accelrometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		rotationvector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		linearAccelrometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		
		plotList.add(plot1);
		plotList.add(plot2);
		plotList.add(plot3);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
//		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
//		{
//			Log.d("Sensor", "Accel " + event.values[0] + " " +event.values[1]+" "+event.values[2]);
//			
//		}
//		else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
//		{
//			Log.d("Sensor", "Gyroscope " + event.values[0] + " " +event.values[1]+" "+event.values[2]);
//			
//		}
//		else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
//		{
//			Log.d("Sensor", "Rot Vec " + event.values[0] + " " +event.values[1]+" "+event.values[2]);
//		}
//		
		
		
		int resolution = 50000;
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			for(int i = 0 ; i < 3; i++)
			{
				accelList.get(i).add( event.values[i]);
				if( drawingPlot == Sensor.TYPE_ACCELEROMETER )
				{
					plotList.get(i).addValue(event.values[i]);
				}
			}
			accelTime.add(event.timestamp);
		}
		else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
		{
			for(int i = 0 ; i < 3; i++)
			{
				gyroList.get(i).add( event.values[i]);
				if( drawingPlot == Sensor.TYPE_GYROSCOPE )
				{
					plotList.get(i).addValue(event.values[i]);
				}
			}
			gyroTime.add(event.timestamp);
		}
		else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
		{
			for(int i = 0 ; i < 4; i++)
			{
				Log.d("len", ""+event.values.length);
				if(event.values.length >=4)
				{
				rotationList.get(i).add(event.values[i]);
					if( drawingPlot == Sensor.TYPE_ROTATION_VECTOR )
					{
						if(i < 3)
							plotList.get(i).addValue(event.values[i]);
					}
				}
			}
			rotationTime.add(event.timestamp);
		}
		else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
		{
			for(int i = 0 ; i < 3; i++)
			{
				linearAccelList.get(i).add(event.values[i]);
//				if( drawingPlot == Sensor.TYPE_ROTATION_VECTOR )
//				{
//					if(i < 3)
//						plotList.get(i).addValue(event.values[i]);
//				}
			}
			linearAccelTime.add(event.timestamp);
		}
	}
	
	public void draw(int i )
	{
		drawingPlot = i;
	}
	
	//Getters
	
	public LinkedList<Float> getAccelData(int index)
	{
		return accelList.get(index);
	}
	public LinkedList<Float> getGyroData(int index)
	{
		return gyroList.get(index);
	}
	public LinkedList<Float> getRotationData(int index)
	{
		return rotationList.get(index);
	}
	
	public LinkedList<Float> getLinearAccelData(int index)
	{
		return linearAccelList.get(index);
	}
	
	public LinkedList<Long> getAccelTime()
	{
		return accelTime;
	}
	public LinkedList<Long> getGyroTime()
	{
		return gyroTime;
	}
	public LinkedList<Long> getRotationTime()
	{
		return rotationTime;
	}
	public LinkedList<Long> getLinearAccelTime()
	{
		return linearAccelTime;
	}
	
	public void start()
	{
		sensorManager.registerListener(this, accelrometer, RATE);
		sensorManager.registerListener(this, gyroscope, RATE);
		sensorManager.registerListener(this, rotationvector, RATE);
		sensorManager.registerListener(this, linearAccelrometer, RATE);

		accelList.clear();
		gyroList.clear();
		rotationList.clear();
		linearAccelList.clear();
		accelTime.clear();
		gyroTime.clear();
		rotationTime.clear();
		linearAccelTime.clear();
		
		
		for(int i = 0  ; i < 3 ; i++) //  
		{
			accelList.add(new LinkedList<Float>());
			gyroList.add(new LinkedList<Float>());
			rotationList.add(new LinkedList<Float>());
			linearAccelList.add(new LinkedList<Float>());
			plotList.get(i).clear();
		}
		rotationList.add(new LinkedList<Float>());
	}
	
	public void stop()
	{
		sensorManager.unregisterListener(this);
		
	}

}
