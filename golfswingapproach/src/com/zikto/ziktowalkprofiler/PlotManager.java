package com.zikto.ziktowalkprofiler;

import java.util.ArrayList;

import android.graphics.Color;
import android.util.Log;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class PlotManager {
	private static final int DISPLAY_SIZE = 500; 
	private static final int HISTORY_SIZE = 30000; 
	private XYPlot plot;
	private SimpleXYSeries magSeries = null; ///Acceleration Magnitude Series
	private ArrayList<Float> magList = new ArrayList<Float>();
	
	private static Integer lineColor = Color.argb(70, 0,0,0);
	private static Integer pointColor = Color.argb(200, 250, 250, 250);
	
	public PlotManager(XYPlot plot )
	{
		this.plot = plot;
		magSeries = new SimpleXYSeries("mag");
		magSeries.useImplicitXVals();
		LineAndPointFormatter magFormat = new LineAndPointFormatter(lineColor, null,null,null);
		plot.addSeries(magSeries, magFormat);
		plot.setTicksPerRangeLabel(3);
		plot.getGraphWidget().setDomainLabelOrientation(-45);
		plot.setRangeBoundaries(0,30, BoundaryMode.FIXED);
		plot.setDomainBoundaries(0, DISPLAY_SIZE, BoundaryMode.FIXED);
	}

	public void addValue(float value)
	{
		if(magList.size() > HISTORY_SIZE)
		{
			magList.remove(0);
		}
		if(magSeries.size() > DISPLAY_SIZE)
		{
			magSeries.removeFirst();
		}

		magList.add(value);
		magSeries.addLast(null, value);
		plot.redraw();
	}

	public void clear()
	{
		for(int  i = 0 ; i < HISTORY_SIZE;i++)
		{
			if(magSeries.size() != 0)
				magSeries.removeFirst();
		}
		magList.clear();
	}

	public ArrayList<Float> getMagList()
	{
		return magList;
	}
}
