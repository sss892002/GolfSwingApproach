package com.zikto.ziktowalkprofiler;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ThreeAxisAccelManager implements SensorEventListener {

	private static final float NOISE = (float) 2.0;

	private float[] mLastMag ={0,0,0};
	private boolean mInitialized = false;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Activity activity;
	private PlotManager[] plotManager = new PlotManager[3];
	static private int RATE = SensorManager.SENSOR_DELAY_FASTEST;

	public ThreeAxisAccelManager(Activity activity,
			PlotManager plotManager1, 
			PlotManager plotManager2,
			PlotManager plotManager3)
	{
		this.activity = activity;
		this.plotManager[0] = plotManager1;
		this.plotManager[1] = plotManager2;
		this.plotManager[2] = plotManager3;

		mSensorManager = (SensorManager) this.activity.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {


		for(int i = 0 ; i < 3; i ++)
		{
			plotManager[i].addValue(event.values[i]);

			//			if (!mInitialized) {
			//				mLastMag[i] = event.values[i]
			//				mInitialized = true;
			//			} else {
			//				
			//				float deltaMag = Math.abs(mLastMag[i] - event.values[i]);
			//				if (deltaMag < NOISE) deltaMag = (float)0.0;
			//					deltaMag = ;
			//				
			//				//tvX.setText(Float.toString(deltaX));
			//			}
		}

	}


	public void start()
	{

		for(int i = 0 ; i < 3; i ++)
		{
			plotManager[i].clear();
		}
		mSensorManager.registerListener(this, mAccelerometer, RATE);
	}

	public void stop()
	{
		mSensorManager.unregisterListener(this);
	}
}
