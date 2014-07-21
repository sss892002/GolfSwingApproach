package com.zikto.ziktowalkprofiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import com.invensense.casdk.client.BluetoothDataReader;
import com.zikto.invensense.BluetoothModule;
import com.zikto.invensense.Global;
import com.zikto.invensense.utils.PacketParser;
import com.zikto.ziktowalkprofiler.fragments.MeasureFragment;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class InvensenseManager {

	static final int MESSAGE_READ=-9999;

	public enum Status{
		DOWNLOADING,
		IDLE
	}

	private Status status;

	private Boolean ACK = false;

	private Activity activity;
	private PlotManager plotManager;
	private BluetoothSocket btSocket;
	private BluetoothDataReader mBluetoothDataReader;
	/** Bluetooth input stream to read the incoming data from the device */
	private InputStream mBluetoothIS = null;
	private OutputStream mBluetoothOS = null;
	private MeasureFragment measureFragment;

	private ArrayList<LinkedList<Float>> accelList = new ArrayList<LinkedList<Float>>();
	private ArrayList<LinkedList<Float>> gyroList = new ArrayList<LinkedList<Float>>();

	public InvensenseManager(Activity activity, PlotManager plotManager, MeasureFragment measureFragment)
	{
		this.activity = activity;
		this.plotManager = plotManager;
		this.measureFragment = measureFragment;

		status = Status.IDLE;
		
		Global.CASDKUtilityActivityHandler = new Handler()
		{
			public void handleMessage(Message msg) {

				switch(msg.arg1)
				{
				case 1:
					switch (msg.arg2) {

					case BluetoothDataReader.PACKET_DATA_GYRO:
						break;
					}
					break;
					//Debug Packet	
				case 0:
					String debug_msg = (String)msg.obj;
					//Matching Score
					if (debug_msg.equals("ACK"))
					{
						ACK = true;
					}
					
					if(debug_msg.equals("DONE"))
					{
						//
					}

					switch(debug_msg.charAt(1))
					{

					case 'A':
						//State Machine
						if(status == Status.IDLE)
						{
							InitSensorDataList();
						}
						status = Status.DOWNLOADING;

						if(status ==  Status.DOWNLOADING)
						{
							AddAccelData(debug_msg);
						}

						break;

					case 'G':
						if(status ==  Status.DOWNLOADING)
						{
							AddGyroData(debug_msg);
						}
						break;


					}

					break;

				}

			}
		};
	}

	public boolean start()
	{
		if(BluetoothModule.getInstance().AttemptConnect())
		{
			btSocket = BluetoothModule.getInstance().getSocket();
			try
			{
				mBluetoothIS = btSocket.getInputStream();
				mBluetoothOS = btSocket.getOutputStream();
				mBluetoothDataReader = new BluetoothDataReader();
				mBluetoothDataReader.execute(mBluetoothIS);
				//BluetoothReadThread(btSocket);
				Log.d("InvensenseManager", "Bluetooth Initiated...");
			}catch(Exception e)
			{
				return false;
			}

		}

		return true;

	}

	public void setStatus(Status now)
	{
		status = now;
	}

	public void stop()
	{
		//TODO stop the thread , disconnect from Bluetooth
		try {
			btSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendCommand(String command) {
		try {
			if (btSocket.isConnected()) {
				byte[] cmd = command.getBytes();
				for (int ii = 0; ii < cmd.length; ii++) {
					mBluetoothOS.write(cmd[ii]);
					long currentTime = System.currentTimeMillis();
					while(System.currentTimeMillis() - currentTime < 100){ ; }
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg) {
			switch (msg.what) {
			//			case SOCKET_CONNECTED: {
			//				mBluetoothConnection = (ConnectionThread) msg.obj;
			//				if (!mServerMode)
			//					mBluetoothConnection.write("this is a message".getBytes());
			//				break;
			//			}
			//			case DATA_RECEIVED: {
			//				data = (String) msg.obj;
			//				tv.setText(data);
			//				if (mServerMode)
			//					mBluetoothConnection.write(data.getBytes());
			//			}
			case MESSAGE_READ:
				// your code goes here
				String readMessage = (String) msg.obj;
				byte[] buffer =readMessage.getBytes();

				//for (int i = 0 ; i < (Integer)msg.arg1 - 23 ; i ++ )
				//{
				if ( msg.arg1 == 23)
				{
					int i = 0;
					if( buffer[i] == '$' && buffer[i+21]=='\r' && buffer[i+22]=='\n')
					{
						byte[] packet = Arrays.copyOfRange(buffer, i, i+23);
						PacketParser p = new PacketParser(packet);
						if(p.isData())
						{
							//Log.d("BT2","Data " + p.getAccelData().toString());
							Log.d("BT2","Valid Data");
							float value = p.getGyroY();
							if(value != PacketParser.NAN)
								plotManager.addValue(value);
						}
						i = i + 22;
					}
				}


				//}
			}
		}
	};

	private void InitSensorDataList()
	{
		accelList.clear();
		gyroList.clear();

		for(int i = 0  ; i < 3 ; i++) //  
		{
			accelList.add(new LinkedList<Float>());
			gyroList.add(new LinkedList<Float>());
		}
	}

	private void AddGyroData(String msg)
	{
		String[] splitMessage = msg.split(",");
		String value;
		float data;
		int index;
		if(splitMessage.length == 2)
		{
			value = ""+splitMessage[0].charAt(2);

			try{
				data = Float.parseFloat(splitMessage[1]);
				index = Integer.parseInt(value);
				if(index == 2 )
				{
					plotManager.addValue(data);
				}
				Log.d("Gyro" , ""+data);

				gyroList.get(index).add(data);			
			} catch(NumberFormatException e)
			{

			}
		}

	}

	private void AddAccelData(String msg)
	{
		String[] splitMessage = msg.split(",");
		String value;
		float data;
		int index;
		if(splitMessage.length == 2)
		{
			value = ""+splitMessage[0].charAt(2);

			try{
				data = Float.parseFloat(splitMessage[1]);
				index = Integer.parseInt(value);
				

				accelList.get(index).add(data);		

				Log.d("Accel" , ""+data);
			} catch(NumberFormatException e)
			{

			}
		}
	}

	public Boolean isACK()
	{
		return ACK;
	}

	//LinearAccel
	public LinkedList<Float> getLinearAccelData(int index)
	{
		return accelList.get(index);
	}
	public LinkedList<Float> getGyroData(int index)
	{
		return gyroList.get(index);
	}
}
