package com.zikto.invensense.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.util.Log;

public class PacketParser {

	private ArrayList<Byte> data;
	
	private boolean isQuaternion = false;
	private boolean isDebug = false;
	private boolean isData = false;
	
	private ArrayList<Float> AccelList;
 	public PacketParser( byte[] data)
	{
 		this.data = new ArrayList<Byte>();
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
 			Log.d("Parser","Data 2 : "+this.data.get(2));
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
// 				float d[] = fourbytes(this.data.get(3),this.data.get(4),this.data.get(5),this.data.get(6)) * 1.0f/(1<<16);
// 				float d2 = fourbytes(this.data.get(7),this.data.get(8),this.data.get(9),this.data.get(10))* 1.0f/(1<<16);
// 				float d3 = fourbytes(this.data.get(11),this.data.get(12),this.data.get(13),this.data.get(14))* 1.0f/(1<<16);
 				
// 				byte[] buffer1 = {this.data.get(3),this.data.get(4),this.data.get(5),this.data.get(6)};
// 				float d1 = ByteBuffer.wrap(buffer1).order(ByteOrder.LITTLE_ENDIAN).getFloat();
// 				
// 				byte[] buffer2 = {this.data.get(7),this.data.get(8),this.data.get(9),this.data.get(10)};
// 				float d2 = ByteBuffer.wrap(buffer2).order(ByteOrder.BIG_ENDIAN).getFloat();
// 				
// 				byte[] buffer3 = {this.data.get(11),this.data.get(12),this.data.get(13),this.data.get(14)};
// 				float d3 = ByteBuffer.wrap(buffer3).order(ByteOrder.BIG_ENDIAN).getFloat();
 				
// 				Log.d("Parser","Data : " + d1 + " "+ d2 + " " +d3);
// 				setAccelData(d1, d2, d3);
 				
 				Log.d("Parser","Data : " + d[0]+ " "+ d[1] + " " +d[2]);
 				setAccelData(d[0], d[1], d[2]);
 			}
 		}
 	}
 	
 	
 	static long fourbytes(byte d1, byte d2, byte d3, byte d4)
 	{ 
 		byte data[] = {d1,d2,d3,d4};
 		ByteBuffer buffer = ByteBuffer.wrap(data);
 		
 		//long d = (d1+127)*(1<<24) + (d2+127)*(1<<16) + (d3+127)*(1<<8) + d4+127;
 		int d =((int)d1 << 24) | ((int)d2<<16) | ((int)d3<<8) | (int)d4;
 		if (d > 2147483648l)
 	        d-= 4294967296l;
 		Log.d("FB","d1: "+d1+" d2: "+d2+" d3: "+d3+" d4: "+d4+"\nD : "+d);
 	    Log.d("FB,","byte buffer : " + buffer.getFloat());
 		return d;
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
 		AccelList = new ArrayList<Float>();
 		AccelList.add(d1);
 		AccelList.add(d2);
 		AccelList.add(d3);
 	}
 	
 	public ArrayList<Float> getAccelData()
 	{
 		return AccelList;
 	} 	
 	
 	public float getAccelMag()
 	{
 		if(isData && (this.data.get(2) == 0))
 		{
 			double d1 = AccelList.get(0)*AccelList.get(0);
 	 		double d2 = AccelList.get(1)*AccelList.get(1);
 	 		double d3 = AccelList.get(2)*AccelList.get(2);

 			return (float) Math.sqrt(d1+d2+d3);
 		}
 		else
 			return -1f;
 	}
}
