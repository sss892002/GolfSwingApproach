package com.zikto.golfswingapproach;

import java.util.ArrayList;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class PlotManager {
	private static final int HISTORY_SIZE = 500; 
	private XYPlot plot;
    private SimpleXYSeries magSeries = null; ///Acceleration Magnitude Series
    private ArrayList<Float> magList = new ArrayList<Float>();
	
	public PlotManager(XYPlot plot)
	{
		this.plot = plot;
		magSeries = new SimpleXYSeries("mag");
		magSeries.useImplicitXVals();
	}
	
	public void addValue(float value)
	{
		magList.add(value);

		if(magSeries.size() > HISTORY_SIZE)
		{
			magSeries.removeFirst();
		}

		magSeries.addLast(null, value);
		plot.redraw();
	}
}
