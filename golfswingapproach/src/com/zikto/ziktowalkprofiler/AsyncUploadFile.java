package com.zikto.ziktowalkprofiler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.zikto.ziktowalkprofiler.fragments.MeasureFragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncUploadFile extends AsyncTask<String, Integer, Long> {
	
	final MeasureFragment fragment;
	
	public AsyncUploadFile()
	{
		this.fragment = null;
	}
	
	public AsyncUploadFile(MeasureFragment fragment)
	{
		this.fragment=fragment;
	}
	
	@Override
	protected void onPostExecute(Long result)
	{	
		if(fragment!=null)
		fragment.DisplayServerMessage(result);
	}
	
	@Override
	protected void onCancelled()
	{
		
	}

	@Override
	protected Long doInBackground(String... urls) {
		String upLoadServerUri = "http://www.zikto.com/gait/uploadall.php";
		int serverResponseCode = 0;

		/**********  File Path *************/
		//				final String uploadFilePath = "/mnt/sdcard/zikto";
		//				final String uploadFileName = "walk.csv";
		//		URL[] url = urls;
		String fileName = urls[0];

		HttpURLConnection conn = null;
		DataOutputStream dos = null;  
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024; 
		File sourceFile = new File(fileName); 

		if (!sourceFile.isFile()) {
			Log.e("uploadFile", "Source File not exist :");
		}
		else
		{
			try {   // open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(upLoadServerUri);

				// Open a HTTP  connection to  the URL
				conn = (HttpURLConnection) url.openConnection(); 
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file", fileName); 
				//						conn.connect();

				//OutputStream test = conn.getOutputStream();

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd); 
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
						+ fileName + "\"" + lineEnd);

				dos.writeBytes(lineEnd);

				// create a buffer of  maximum size
				bytesAvailable = fileInputStream.available(); 

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);  

				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
					
				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				Log.i("uploadFile", "HTTP Response is : "
						+ serverResponseMessage + ": " + serverResponseCode);

				//close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();
				conn.disconnect();

			} catch (MalformedURLException ex) {


				Log.e("Upload file to server", "error: ");  
			} catch (Exception e) {


				Log.e("Upload file to server Exception", "Exception : "
						+ e.getMessage(), e);  
			}   
		}

		return (long)serverResponseCode;
		
	}
	}
