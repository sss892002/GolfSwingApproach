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
import android.os.SystemClock;
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
	private boolean socketConnected = false;

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
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			//AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
		}
		btAdapter.cancelDiscovery();

		for (int i = 0 ; i < 3 ; i++)
		{
			try{
				btSocket.connect();
				return true;
			}
			catch(IOException e)
			{
				SystemClock.sleep(1000);
			}
		}
		
		try
		{
			btSocket.close();
		}catch(IOException ioerror)
		{
//			out.append(ioerror.getMessage());
		}
		Log.d("BT", "Fail to connect");
		return false;
	}
	

}
