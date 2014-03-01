package com.zikto.golfswingapproach;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.zikto.invensense.BluetoothModule;
import com.zikto.invensense.utils.PacketParser;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class InvensenseManager {

	private Activity activity;
	private PlotManager plotManager;
	private BluetoothSocket btSocket;
	public InvensenseManager(Activity activity, PlotManager plotManager)
	{
		this.activity = activity;
		this.plotManager = plotManager;
	}
	
	public void start()
	{
		if(BluetoothModule.getInstance().AttemptConnect())
		{
			btSocket = BluetoothModule.getInstance().getSocket();
			BluetoothReadThread(btSocket);
		}
		
	}
	
	public void stop()
	{
		//TODO stop the thread , disconnect from Bluetooth
	}
	
	private void BluetoothReadThread(BluetoothSocket btSocket)
	{
		final BluetoothSocket socket = btSocket;
		final Handler handler = new Handler();
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				int bytes;
				//TODO:Fix reading module.Looks like it's missing tons of data
				byte[] buffer = new byte[30];
				InputStream inStream;
				try {
					inStream = socket.getInputStream();
					
					while(true)
					{
						try
						{
							Log.d("ALIVE","Read");
							bytes = inStream.read(buffer);
							String message =  Arrays.toString(buffer);
							//out.append("Data : ");
							
							if( buffer[0] == 36)
							{
								Log.d("BT1",message);
								final PacketParser p = new PacketParser(buffer);
								if(p.getAccelData()!=null)
								{
									Log.d("BT2","Data " + p.getAccelData().toString());
									Log.d("BT2","Mag "+ p.getAccelMag());
									handler.post(new Runnable()
									{

										@Override
										public void run() {
											plotManager.addValue(p.getAccelMag());
										}
										
									});
								}
							}
							
						}catch(IOException e )
						{
							Log.d("Error",e.getMessage());
						}
						
						
					} 
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				
			}
			
		};
		
		new Thread(runnable).start();
	}
}
