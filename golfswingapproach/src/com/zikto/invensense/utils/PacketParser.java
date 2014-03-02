package com.zikto.invensense.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.util.Log;

public class PacketParser {

	public static final float NAN = -9999f;
	
	private ArrayList<Byte> data;

	private boolean isQuaternion = false;
	private boolean isDebug = false;
	private boolean isData = false;

	private ArrayList<Float> AccelList;
	public PacketParser( byte[] data)
	{
		this.data = new ArrayList<Byte>();
		this.AccelList = new ArrayList<Float>();

		for(byte b : data)
		{
			this.data.add(b);
			//Log.d("Parser", ""+b);
		}
		this.Parse();
	}

	private void Parse()
	{
		if(this.data.get(0) == '$')
		{
			Log.d("STAGE1", "Type : "+this.data.get(1));
			switch( this.data.get(1))
			{
			case 1:
				isDebug = true;
				break;
			case 2:
				isQuaternion = true;
				break;
			case 3:
				isData = true;
				ParseData();
				break;
			}
		}
		else
		{
			Log.d("PacketParser", "Init Failed");
		}
	}

	private void ParseData()
	{
		if(isData)
		{
			// 			Log.d("Parser","Data 2 : "+this.data.get(2));
			switch(this.data.get(2))
			{
			case 0: //Accel
				float[] d={0,0,0};
				final int base = 3;

				for (int i = 0 ; i < 3 ; i++)
				{
					int index = i*4+base;
					d[i] = fourbytes(this.data.get(index),this.data.get(index+1),this.data.get(index+2),this.data.get(index+3)) * 1.0f/(1<<16);
				} 				
				Log.d("Parser","Data : " + d[0]+ " "+ d[1] + " " +d[2]);
				setAccelData(d[0], d[1], d[2]);
			}
		}
	}
	
	static private String byteTobinary(byte b)
	{
		return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}


	static int fourbytes(byte d1, byte d2, byte d3, byte d4)
	{ 
		byte data[] = {d1,d2,d3,d4};
		ByteBuffer buffer = ByteBuffer.wrap(data);
		Log.d("BT3","d1: "+ byteTobinary(d1) +" d2: "+byteTobinary(d2)+" d3: "+byteTobinary(d3)+" d4: "+byteTobinary(d4));
		return buffer.getInt();
		
//
//		//long d = (d1+127)*(1<<24) + (d2+127)*(1<<16) + (d3+127)*(1<<8) + d4+127;
//		//
//		
//		//Log.d("OP","d1 : "+d1);
//		int sign = (d1 & 0x8000);
//		//Log.d("OP","d  : "+sign);
//		int maskD1 = d1;
//		if(sign == 32768)
//		{
//			maskD1 = d1 & 0x7fff;
//		}
//		int d =((int)maskD1 << 24) + ((int)d2<<16) + ((int)d3<<8) + (int)d4;
//		// 		if (d > 2147483648l)
//		// 	        d-= 4294967296l;
//		Log.d("BT3,","byte buffer : " + buffer.getInt()*1.0f/65535);
//		return d;
	}

	public boolean isQuaternion()
	{
		return this.isQuaternion;
	}

	public boolean isDebug()
	{
		return this.isDebug;
	}

	public boolean isData()
	{
		return this.isData;
	}

	private void setAccelData(float d1, float d2, float d3)
	{
		AccelList.add(d1);
		AccelList.add(d2);
		AccelList.add(d3);
	}

	public ArrayList<Float> getAccelData()
	{
		if(isData)
			return AccelList;
		else
			return null;
	} 	

	public float getAccelMag()
	{
		if(isData && (this.data.get(2) == 0))
		{
			double d1 = AccelList.get(0)*AccelList.get(0);
			double d2 = AccelList.get(1)*AccelList.get(1);
			double d3 = AccelList.get(2)*AccelList.get(2);
			
			float mag = (float) Math.sqrt(d1+d2+d3);
			
			if(mag > 6.0f)
				return NAN;
			else 
				return mag;
		}
		else
			return NAN;
	}
}
