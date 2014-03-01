/**
 * 
 */
package com.zikto.invensense;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import com.zikto.invensense.utils.PacketParser;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

/**
 * @author kyungtae
 *
 */
public class BluetoothModule {

	private static BluetoothModule instance;

	public static BluetoothModule getInstance()
	{
		if(instance == null)
		{
			instance = new BluetoothModule();
		}
		return instance;
	}

	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private InputStream inStream = null;
	public static final int REQUEST_ENABLE_BT = 1;

	// Well known SPP UUID
	private static final UUID MY_UUID =
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Insert your server's MAC address
	private static String address = "00:A0:96:3B:E5:4D";

	private BluetoothModule()
	{
		btAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public BluetoothAdapter getAdapter()
	{
		return btAdapter;
	}
	
	public BluetoothSocket getSocket()
	{
		return btSocket;
	}

	public boolean isReady()
	{
		// Check for Bluetooth support and then check to make sure it is turned on

		// Emulator doesn't support Bluetooth and will return null
		if(btAdapter==null) { 
			return false;
		} else {
			if (btAdapter.isEnabled()) {
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean AttemptConnect()
	{
		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		// Two things are needed to make a connection:
		//   A MAC address, which we got above.
		//   A Service ID or UUID.  In this case we are using the
		//     UUID for SPP.
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			//AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
		}

		// Discovery is resource intensive.  Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection.  This will block until it connects.

//		out.invalidate();

		try {
			btSocket.connect();
//			out.append("\n...Connection established and data link opened..\n");
//			out.append("Start Reading...");
			//		btThread = new BluetoothThread(btSocket);
			//		btThread.start();
			//BluetoothReadThread(btSocket);
			return true;
		} catch (IOException e) {

//			out.append("\nConnection Failed!!!Reconnect in 3 seconds");
			try
			{
				btSocket.close();
			}catch(IOException ioerror)
			{
//				out.append(ioerror.getMessage());
			}

			return false;//SystemClock.sleep(3000);
		}
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
				byte[] buffer = new byte[23];
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
									Log.d("BT2",p.getAccelData().toString());
									handler.post(new Runnable()
									{

										@Override
										public void run() {
											//out.append("Accel : " + p.getAccelData().toString()+"\n");
										}
										
									});
									//out.append(p.getAccelData().toString());
								}
							}
//							for (int i =  0 ; i < bytes ; i++)
//							{
//								//out.append(" "+ buffer[i]);
//								Log.d("BT1",buffer[i]);
//							}
							//out.append("\n");
							
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
