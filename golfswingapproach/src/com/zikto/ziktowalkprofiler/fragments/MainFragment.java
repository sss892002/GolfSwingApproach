package com.zikto.ziktowalkprofiler.fragments;


import com.zikto.ziktowalkprofiler.R;
import com.zikto.ziktowalkprofiler.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MainFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
	{
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.activity_main, container, false);
		return rootView;
	}
	
	
}
