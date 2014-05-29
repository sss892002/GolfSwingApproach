package com.zikto.ziktowalkprofiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.zikto.invensense.BluetoothModule;
import com.zikto.invensense.utils.PacketParser;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class InvensenseManager {

	final int MESSAGE_READ=-9999;

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

				for (int i = 0 ; i < (Integer)msg.arg1 - 23 ; i ++ )
				{
					if( buffer[i] == '$' && buffer[i+21]=='\r' && buffer[i+22]=='\n')
					{
						byte[] packet = Arrays.copyOfRange(buffer, i, i+23);
						PacketParser p = new PacketParser(packet);
						if(p.isData())
						{
							//Log.d("BT2","Data " + p.getAccelData().toString());
							//Log.d("BT2","Mag "+ p.getAccelMag());
							float value = p.getGyroY();
							if(value != PacketParser.NAN)
								plotManager.addValue(value);
						}
						i = i + 22;
					}
				}
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
				byte[] buffer1 = new byte[64];
				byte[] buffer2 = new byte[64];
				
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
								String readMessage = new String(Arrays.copyOf(buffer1, bytes), 0, bytes);
								mHandler.obtainMessage(MESSAGE_READ, bytes, -1, readMessage).sendToTarget();
							}
							else
							{
								bytes = inStream.read(buffer2);
								String readMessage = new String(Arrays.copyOf(buffer2, bytes), 0, bytes);
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
