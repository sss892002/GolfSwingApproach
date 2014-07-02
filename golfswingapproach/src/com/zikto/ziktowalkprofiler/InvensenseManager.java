package com.zikto.ziktowalkprofiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

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
		MAKING_TEMPLATE,
		MATCHING_TEMPLATE,
		IDLE
	}

	private Status status;

	private Activity activity;
	private PlotManager plotManager;
	private BluetoothSocket btSocket;
	private BluetoothDataReader mBluetoothDataReader;
	/** Bluetooth input stream to read the incoming data from the device */
	private InputStream mBluetoothIS = null;
	private OutputStream mBluetoothOS = null;
	private MeasureFragment measureFragment;
	public InvensenseManager(Activity activity, PlotManager plotManager, MeasureFragment measureFragment)
	{
		this.activity = activity;
		this.plotManager = plotManager;
		this.measureFragment = measureFragment;

		status = Status.IDLE;
	}

	public void start()
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
				//
			}

		}

		Global.CASDKUtilityActivityHandler = new Handler()
		{
			public void handleMessage(Message msg) {

				switch(msg.arg1)
				{
				case 1:
					switch (msg.arg2) {

					case BluetoothDataReader.PACKET_DATA_GYRO:
						plotManager.addValue(Global.Gyro[2]);
						break;
					}
					break;
					//Debug Packet	
				case 0:
					if (status == Status.MATCHING_TEMPLATE)
					{
						String debug_msg = (String)msg.obj;
						debug_msg = debug_msg.replaceAll("\\D+","");
						Log.d("matching score", debug_msg);
						plotManager.addValue(Float.parseFloat(debug_msg));
						if(Float.parseFloat(debug_msg) < 350)
						{
							measureFragment.changeSmileyFace(true);
						}
						else
						{
							measureFragment.changeSmileyFace(false);
						}
					}
					break;

				}

			}
		};

	}

	public void stop()
	{
		//TODO stop the thread , disconnect from Bluetooth

	}

	public void sendCommand(String command) {
		try {
			if (btSocket.isConnected()) {
				byte[] cmd = command.getBytes();
				for (int ii = 0; ii < cmd.length; ii++) {
					mBluetoothOS.write(cmd[ii]);
					long currentTime = System.currentTimeMillis();
					while(System.currentTimeMillis() - currentTime < 50){ ; }
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

	private void BluetoothReadThread(BluetoothSocket btSocket)
	{
		final BluetoothSocket socket = btSocket;
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				int bytes;
				//TODO:Fix reading module.Looks like it's missing tons of data
				byte[] buffer1 = new byte[23];
				byte[] buffer2 = new byte[23];

				InputStream inStream;
				boolean doublebuffer = true;
				try {
					inStream = socket.getInputStream();
					while(true)
					{
						try
						{
							if(doublebuffer)
							{
								bytes = inStream.read(buffer1);
								String readMessage = new String(buffer1, 0, bytes);
								mHandler.obtainMessage(MESSAGE_READ, bytes, -1, readMessage).sendToTarget();
							}
							else
							{
								bytes = inStream.read(buffer2);
								String readMessage = new String(buffer2, 0, bytes);
								mHandler.obtainMessage(MESSAGE_READ, bytes, -1, readMessage).sendToTarget();
							}
							doublebuffer = !doublebuffer;
						}
						catch(IOException e )
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
