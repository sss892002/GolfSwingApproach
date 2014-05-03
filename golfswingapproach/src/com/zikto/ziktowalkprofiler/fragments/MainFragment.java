package com.zikto.ziktowalkprofiler.fragments;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zikto.ziktowalkprofiler.R;
import com.zikto.ziktowalkprofiler.R.layout;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;


public class MainFragment extends Fragment {
	private ViewGroup rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
	{
		rootView = (ViewGroup) inflater.inflate(R.layout.activity_main, container, false);

		final Button loadButton = (Button)rootView.findViewById(R.id.loadButton);

		loadButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				loadData();
			}

		});

		return rootView;
	}


	public void loadData()
	{
		final EditText edit = (EditText)rootView.findViewById(R.id.editText1);
		new AsyncLoadData(this).execute(edit.getText().toString());
	}

	public class AsyncLoadData extends AsyncTask<String, Integer, Long> {


		private MainFragment mainFragment;
		private JSONObject json_data =null;

		public AsyncLoadData(MainFragment mainFragment)
		{	
			this.mainFragment = mainFragment;
		}

		@Override
		protected void onPostExecute(Long result)
		{	
			if(json_data != null)
			{
				final EditText editPelvic = (EditText)rootView.findViewById(R.id.editPelvic);
				final EditText editComment = (EditText)rootView.findViewById(R.id.editComments);
				final EditText editAge  = (EditText)rootView.findViewById(R.id.editAge);
				final EditText editWeight  = (EditText)rootView.findViewById(R.id.editWeight);
				final EditText editHeight  = (EditText)rootView.findViewById(R.id.editHeight);
				final ToggleButton buttonGender = (ToggleButton)rootView.findViewById(R.id.genderButton);

				try {
					editPelvic.setText(json_data.getString("PelvicRotation"));
					editComment.setText(json_data.getString("Meta"));
					editAge.setText(json_data.getString("Age"));
					editWeight.setText(json_data.getString("Weight"));
					editHeight.setText(json_data.getString("Height"));
					if( json_data.getString("Gender").equals( "M"))
					{
						buttonGender.setChecked(true);
					}
					else
						buttonGender.setChecked(false);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		@Override
		protected void onCancelled()
		{

		}

		@Override
		protected Long doInBackground(String... nickname) {
			JSONArray jArray = null;

			String result = null;

			StringBuilder sb = null;

			InputStream is = null;
			String upLoadServerUri = "http://www.zikto.com/gait/getid.php?name="+nickname[0];
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			//http post
			try{
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(upLoadServerUri);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}catch(Exception e){
				Log.e("log_tag", "Error in http connection"+e.toString());
			}
			//Log.d("send","send"+nickname[0]);

			//convert response to string
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
				sb = new StringBuilder();
				sb.append(reader.readLine() + "\n");

				String line="0";
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result=sb.toString();
			}catch(Exception e){
				Log.e("log_tag", "Error converting result "+e.toString());
			}

			try{
				jArray = new JSONArray(result);
				json_data=null;
				for(int i=0;i<jArray.length();i++){

					json_data = jArray.getJSONObject(i);


				}
			}
			catch(JSONException e1){
				//  Toast.makeText(getBaseContext(), "No Data Found" ,Toast.LENGTH_LONG).show();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			return 1l;
		}


	};

}
