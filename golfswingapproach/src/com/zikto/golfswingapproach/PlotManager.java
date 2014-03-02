package com.zikto.golfswingapproach;

import java.util.ArrayList;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class PlotManager {
	private static final int HISTORY_SIZE = 500; 
	private XYPlot plot;
    private SimpleXYSeries magSeries = null; ///Acceleration Magnitude Series
   // private ArrayList<Float> magList = new ArrayList<Float>();
	
	public PlotManager(XYPlot plot)
	{
		this.plot = plot;
		magSeries = new SimpleXYSeries("mag");
		magSeries.useImplicitXVals();
		
		 // add a new series' to the xyplot:
        plot.addSeries(magSeries, new LineAndPointFormatter());
 
        // same as above:
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
//        series2Format.configure(getApplicationContext(),
//                R.xml.line_point_formatter_with_plf2);
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
	}
	
	public void addValue(float value)
	{
		//magList.add(value);

		if(magSeries.size() > HISTORY_SIZE)
		{
			magSeries.removeFirst();
		}

		magSeries.addLast(null, value);
		plot.redraw();
	}
	public void clear()
	{
		for(int  i = 0 ; i < magSeries.size();i++)
		{
			magSeries.removeFirst();
		}
		///plot.clear();
	}
}
