package com.zikto.golfswingapproach;

import java.util.ArrayList;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerManager implements SensorEventListener{
	private static final int HISTORY_SIZE = 500; 
    private static final float NOISE = (float) 2.0;
    
	private float mLastMag;
	private boolean mInitialized = false;
	

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Activity activity;
	
	private XYPlot plot;
    private SimpleXYSeries magSeries = null; ///Acceleration Magnitude Series
    private ArrayList<Float> magList = new ArrayList<Float>();


	public AccelerometerManager(Activity activity,XYPlot plot)
	{
		this.activity = activity;
		this.plot = plot;
		
		mSensorManager = (SensorManager) this.activity.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		//mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		
		magSeries = new SimpleXYSeries("mag");
		magSeries.useImplicitXVals();
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		float mag = (float)Math.sqrt(x*x + y*y + z*z);

		magList.add(mag);

		if(magSeries.size() > HISTORY_SIZE)
		{
			magSeries.removeFirst();
		}

		magSeries.addLast(null, mag);

		plot.redraw();

		if (!mInitialized) {
			mLastMag = mag;
			//tvX.setText("0.0");
			mInitialized = true;
		} else {
			float deltaMag = Math.abs(mLastMag - mag);
			if (deltaMag < NOISE) deltaMag = (float)0.0;
			deltaMag = mag;
			
			//tvX.setText(Float.toString(deltaX));
		}		
	}
	
	public void start()
	{
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void stop()
	{
        mSensorManager.unregisterListener(this);
	}
}
