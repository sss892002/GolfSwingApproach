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
	private Sensor gyroscope;
	private ArrayList<LinkedList<Float>> gyroList = new ArrayList<LinkedList<Float>>();
	private Sensor rotationvector;
	private ArrayList<LinkedList<Float>> rotationList = new ArrayList<LinkedList<Float>>();
	
	public UniversalSensorManager(Activity activity)
	{
		sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		accelrometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		rotationvector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		
		
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
//		Log.d("Sensor","Time " + event.timestamp);
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			for(int i = 0 ; i < 3; i++)
			{
				accelList.get(i).add( event.values[i]);
			}
			accelList.get(3).add((float)event.timestamp);
		}
		else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
		{
			for(int i = 0 ; i < 3; i++)
			{
				gyroList.get(i).add( event.values[i]);
			}
			gyroList.get(3).add((float)event.timestamp);	
		}
		else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
		{
			for(int i = 0 ; i < 3; i++)
			{
				rotationList.get(i).add(event.values[i]);
			}
			rotationList.get(3).add((float)event.timestamp);
		}
	}
	
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
	
	public void start()
	{
		sensorManager.registerListener(this, accelrometer, RATE);
		sensorManager.registerListener(this, gyroscope, RATE);
		sensorManager.registerListener(this, rotationvector, RATE);

		accelList.clear();
		gyroList.clear();
		rotationList.clear();
		
		for(int i = 0  ; i < 4 ; i++) //  
		{
			accelList.add(new LinkedList<Float>());
			gyroList.add(new LinkedList<Float>());
			rotationList.add(new LinkedList<Float>());
		}
	}
	
	public void stop()
	{
		sensorManager.unregisterListener(this);
	}

}
