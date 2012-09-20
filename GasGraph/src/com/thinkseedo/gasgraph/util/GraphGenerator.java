package com.thinkseedo.gasgraph.util;

import java.io.File;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.DataUtil;
import com.googlecode.charts4j.Fills;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.LegendPosition;
import com.googlecode.charts4j.LineStyle;
import com.googlecode.charts4j.Marker;
import com.googlecode.charts4j.Markers;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.Shape;
import com.googlecode.charts4j.XYLine;
import com.googlecode.charts4j.XYLineChart;
import com.thinkseedo.gasgraph.App;

import static com.googlecode.charts4j.Color.*;

import android.app.Activity;

public class GraphGenerator {
    @SuppressWarnings("unused")
	private Activity mActivity;
	private double[] mData;
	private Date[] mDates;
	
	static final int MS_PER_DAY = (1000*60*60*24);
	
    
    public String hash (String url) {
        try {
    	    MessageDigest m=MessageDigest.getInstance("MD5");
            m.update(url.getBytes(),0,url.length());
            return new BigInteger(1,m.digest()).toString(16);
        } catch (Exception e) {
            return "";
        }
    }
    
    public File fileFor(String url) {
    	File dir = App.getContext().getFilesDir();
    	File img = new File(dir, hash(url) + ".png");
    	return img;
    }
    
    public String getGraphURL(int w, int h, String title, String ylabel, List<Double> data, List<Date> datelist) {
        //Log.d("gasgraph", data.toString());
        //Log.d("gasgraph", datelist.toString());
        //Log.d("gasgraph", "dl: " + data.size());
        //Log.d("gasgraph", "Dl: " + datelist.size());
    	if ( data.size() == 0 || datelist.size() == 0 ) {
    		return null;
    	}
        mData = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            mData[i] = data.get(i);
        }
        mDates = new Date[datelist.size()];
        for (int i=0; i < datelist.size(); i++) {
        	mDates[i] = datelist.get(i);
        }
        return getGraphURL(title, w, h, ylabel);
    }
    
    private int monthsBetween(Date start, Date end){
    	if ( start.getYear() == end.getYear() ) {
    		return end.getMonth() - start.getMonth() + 2;
    	} else if ( start.getYear() == end.getYear()-1 ) {
    		//Log.d("gasgraph", "years off by 1");
    		//Log.d("gasgraph", "me: " + end.getMonth());
    		//Log.d("gasgraph", "ms: " + (13-start.getMonth()));
    		return end.getMonth() + (13-start.getMonth());
    		
    	} else if ( start.getYear() < end.getYear()-1 ) {
    		
    		return 12*(end.getYear() - start.getYear() -1) + end.getMonth() + (13-start.getMonth());
    	}
		return 0;
    }
    
    private Object[] getXData(Date[] dates) {
    	if ( dates== null ) { return null; }
    	if ( dates.length == 0 ) { return null; }
    	
    	// all X coords (in days)
    	Date d_earliest = dates[dates.length-1];
    	Date d_latest   = dates[0];
    	Date d_origin   = new Date(d_earliest.getTime()); // a copy
    	d_origin.setDate(1);
    	long origin = d_origin.getTime()/(MS_PER_DAY);
    	double[] x = new double[dates.length];
    	for (int j=(dates.length-1), i=0; j >= 0; j--, i++ ) {
    		x[i] = (double)((dates[j].getTime()/(MS_PER_DAY))-origin);
    	}
    	
    	// for month labels
    	List<String> yearLabels = new ArrayList<String>();
    	List<String> monthLabels = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat sdf_year = new SimpleDateFormat("yyyy", Locale.getDefault());
    	int months_apart = monthsBetween(d_earliest, d_latest);
    	
    	
    	// for grid lines
    	double[] gx = new double[2*months_apart+8];
    	double[] gy = new double[2*months_apart+8];
    	
    	// allows natural, monthly increment
    	Calendar c = Calendar.getInstance();
    	c.setTime(d_origin);
    	c.setLenient(true);
    	monthLabels.add(sdf.format(c.getTime()));
    	
    	// external vals d&i get the last values of the second loop 
    	// for grid lines.
    	double	d = 0;
    	int 	i = 0;
    	
    	// collect: monthLables, grid lines
    	for (i=0; i < months_apart; i++ ) {
    		c.add(Calendar.MONTH, 1);
    		d = (double)((c.getTime().getTime()/(MS_PER_DAY))-origin);
    		if ( months_apart > 12 ) {
    			if ( i % 2 == 1 ) {
    				monthLabels.add(sdf.format(c.getTime()));
    			} else {
    				monthLabels.add("");
    			}
    		} else {
    			monthLabels.add(sdf.format(c.getTime()));
    		}
    		
    		if ( ! yearLabels.contains(sdf_year.format(c.getTime())) ) {
    			yearLabels.add(sdf_year.format(c.getTime()));
    		}
    		
    		// gx is the same, but copied for x,y pairs.
    		if  ( i % 2 == 0 ) {
    			gx[i*2]   = d; gy[i*2]   = 0;
    			gx[i*2+1] = d; gy[i*2+1] = 100;
    		} else {
    			gx[i*2]   = d; gy[i*2]   = 100;
    			gx[i*2+1] = d; gy[i*2+1] = 0;
    		}
    	}
    	
    	// add horizontal lines.
    	gx[i*2]   = d; gy[i*2]   = 75;
    	gx[i*2+1] = 0; gy[i*2+1] = 75;
    	gx[i*2+2] = 0; gy[i*2+2] = 50;
    	gx[i*2+3] = d; gy[i*2+3] = 50;
    	gx[i*2+4] = d; gy[i*2+4] = 25;
    	gx[i*2+5] = 0; gy[i*2+5] = 25;
    	// cover top of graph to hide uneven dotting.
    	gx[i*2+6] = 0; gy[i*2+6] = 99.9;
    	gx[i*2+7] = d; gy[i*2+7] = 99.9;
    	
    	// min and max dates (in days)
    	double[] range = new double[] { 0, 0 };
    	range[1] = (double)(c.getTime().getTime()/(1000*24*60*60)-origin);
    	
    	return new Object[] { x, range, monthLabels, gx, gy, yearLabels };
    }

    private String sLab(double d, boolean price){
    	if ( price ) {
    		return String.format("%.2f", d);
    	} else {
    		return String.format("%.1f", d).replaceAll("\\.0$", "");
    	}
    }
    
    private List<String> doubleToString(double[] data, boolean price) {
    	List<String> axisList = new ArrayList<String>();
    	for (int i=0; i < data.length; i++ ) {
    		if ( Double.compare(data[i], 0) == 0 ) {
    			axisList.add("");
    		} else {
    			axisList.add(sLab(data[i], price));
    		}
    	}
    	if ( axisList.size() == 1 ) {
    		axisList.add(sLab(data[0] + 1, price));
    	}
    	return axisList;
    }
    private Object[] getYData(double[] data, boolean price) {
    	double[] y = new double[data.length];
    	for (int i=data.length-1, j=0; i >= 0; i--, j++) {
    		y[j] = data[i];
    	}
    	double[] y_labels = getYLabels(data);
    	return new Object[] { y, getRange(y_labels), doubleToString(y_labels, price) };
    }
    
    private double[] rangeFromWindow(double b, double t, double window){
    	double w_top = Math.ceil(t);
    	double w_bot = Math.floor(b);
    	
    	Lg.d("window: " + window);
    	// based on window magnitude, find 
    	// the nearest 'nice' spots.
    	if ( 0.10*(w_top - w_bot) > window )
    	{
    		// if 10% of diff is more than the existing window,
    		// then this is a terrible guess, so use
    		w_top = Math.ceil(t*10)/10;
    		w_bot = Math.floor(b/10)*10;
    		Lg.d("10%: " + window);
    		if ( 0.10*(w_top - w_bot) > window )
    		{
    			w_top = Math.ceil(t*100)/100;
    			w_bot = Math.floor(b/100)*100;
    			Lg.d("100%: " + window);
    		}
    	}
    	if ( t+window*0.10 < 1.0 ) {
    	    w_top = Math.ceil((t+window*0.10)*10)/10;
    	    w_bot = Math.floor(b-window*0.10);
    	} else {
    	    w_top = Math.ceil((t+window*0.10)/4)*4;
    	    w_bot = Math.floor((b-window*0.10)/4)*4;
    	}
    	return new double[] { w_bot, w_top };
    }
    
    private double[] getYLabels(double[] data) {
    	if ( data == null ) { return null; }
    	double max = 0;
    	double min = Integer.MAX_VALUE;
    	//final double[] steps = { 0.125, 0.25, 0.5, 1.0, 1.25, 2, 2.5, 5, 7.5, 10, 12.5, 15, 20, 25, 
    	//				   30, 40, 50, 75,100,150,200,Integer.MAX_VALUE };
    	for (int i=0; i < data.length; i ++ ) {
    		min = Math.min(data[i], min);
    		max = Math.max(data[i], max);
    	}
    	double[] range = rangeFromWindow(min, max, max-min);
    	Lg.d("b: " + range[0] + " t: " + range[1]);
    	return new double[] { 0,
    						(range[1])/4.0,
    						(range[1])/2.0,
    						(range[1])*3.0/4.0,
    						 range[1] };
    	//return new double[] { range[0], 
    	//					  range[0]+(range[1]-range[0])/4.0, 
    	//					  range[0]+(range[1]-range[0])/2.0, 
    	//					  range[0]+(range[1]-range[0])*3.0/4.0, 
    	//					  range[1] };
    }
    
    public double[] getRange(double[] data){
    	if ( data == null ) { return null; }
    	double min = Integer.MAX_VALUE;
    	double max = 0;
    	for (int i=0; i < data.length; i ++ ) {
    		max = Math.max(data[i], max);
    		min = Math.min(data[i], min);
    	}
    	Lg.d("mn: " + min + " mx: " + max);
    	if ( min == max ) {
    		return new double[] { min-1, max + 1};
    	}
    	return new double[] { min, max };
    }
    double getAvgOf(double[] data, int start, int end) {
    	double avg = 0;
    	if ( end > data.length ) { 
    		end = data.length;
    	}
    	if ( start < 0 ) {
    		start = 0;
    	}
    	for ( int i=start; i < end; i++ ) {
    		avg += data[i];
    	}
    	return avg/(end-start);
    }
    List<Double> getAvg(double[] data, int win) {
        ArrayList<Double> avg = new ArrayList<Double>(); // double[data.length];
        int half_win = win/2;
        for ( int i=0; i < data.length; i++) {
        	avg.add(getAvgOf(data, i-half_win, i+half_win));
        }
        return avg;
    }
    
    private String getGraphURL(String title, int w, int h, String label) {

    	if ( mData == null ) { return null; }
    	
    	boolean graphSmooth = App.getPrefs().getBoolean("graphsmooth", true);
    	boolean price = false;
    	
        if ( label.equals("price") || label.equals("totalprice") || 
             label.equals("currency_mile") || label.equals("currency_km") ) {
        	price = true;
        }
        
        int font_size = 14;
        int line_size = 3;
        int point_size = 8;
        if ( w <= 400 ) {
        	font_size = 12;
        	line_size = 3;
        	point_size = 8;
        } else if ( w > 400 && w <= 800 ) {
        	font_size = 16;
        	line_size = 5;
        	point_size = 10;
        } else {
        	font_size = 20;
        	line_size = 6;
        	point_size = 12;
        }
        
        // make w*h less than 300,000 (which is google's chart limit.)
        while ( w*h > 300000 ) {
        	w = (int)(w*0.95);
        	h = (int)(h*0.95);
        } 
        
        Lg.i(label + " w: " + w + " h: " + h + " f: " + font_size + 
        	 " l: " + line_size + " p: " + point_size);
    	
    	// x, range, monthLabels, gx, gy 
        Object[] x_spec  = getXData(mDates);
        double[] x 		 = (double[])x_spec[0];
        double[] x_range = (double[])x_spec[1];
        @SuppressWarnings("unchecked")
		List<String> x_labels = (List<String>)x_spec[2];
		double[] grid_x = (double[])x_spec[3];
		double[] grid_y = (double[])x_spec[4];
        @SuppressWarnings("unchecked")
		List<String> x_years = (List<String>)x_spec[5];
        
    	// y, range, labels
        Object[] y_spec   = getYData(mData, price);
        double[] y		  = (double[]) y_spec[0];
        double[] y_range  = (double[]) y_spec[1];
        @SuppressWarnings("unchecked")
        List<String> y_labels = (List<String>)y_spec[2];
        
        // scale raw x & y values to chart's strict 0-100 range.
        Data data_x = DataUtil.scaleWithinRange(0, x_range[1], x);
        Data data_y = DataUtil.scaleWithinRange(0, y_range[1], y);
        
        String diamond=null;
        String grid=null;
        Color foreground=null; 
        String background=null; 
        int sdev_opac =0;
        int opac;
        
        if ( App.getPrefs().getBoolean("graphdark", true) ) {
        	grid 	   = "AAAAAA";
        	diamond    = "FFFFFF";
        	background = "000000";
        	foreground = WHITE;
        	sdev_opac  = 30;
        	opac 	   = 100;
        } else {
        	grid 	   = "555555";
        	diamond    = "444444";
        	background = "FFFFFF";
        	foreground = BLACK;
        	sdev_opac  = 15;
        	opac 	   = 100;
        }
        
        Color line_color;
        Color avg_color;
        Color sdev_color;
        Color diamond_color = Color.newColor(foreground, 60);
        Color color = Color.newColor(App.color, 50);
        
        if ( graphSmooth ) {
        	line_color = Color.newColor(background, 0);
        	avg_color  = Color.newColor(App.color, opac);
        	sdev_color = Color.newColor(App.color, sdev_opac);
        } else {
        	line_color = Color.newColor(App.color, 60);
            avg_color  = Color.newColor(App.color, opac);
        	sdev_color = Color.newColor(App.color, sdev_opac);
        }
        
        // data line.
        String title2;
        XYLine line;
       	title2 = title.replaceAll("CUR", "\\" + App.getCurrencySymbol()); 
       	line = Plots.newXYLine(data_x, data_y);
       	line.setLineStyle(LineStyle.newLineStyle(line_size, 1, 0));
       	line.addShapeMarkers(Shape.DIAMOND, color, point_size);
       	line.addShapeMarkers(Shape.DIAMOND, Color.newColor(diamond, 60), point_size/2);
        
        // grey grid lines.
        XYLine line_grid = Plots.newXYLine(
        				   DataUtil.scaleWithinRange(0, x_range[1], grid_x),
        				   Data.newData(grid_y), 
        				   Color.newColor(grid,40));
        line_grid.setLineStyle(LineStyle.newLineStyle(1,2,5));
        
        // Avg line
        double avg  = StdStats.mean(y);
        double sdev = StdStats.stddev(y);
       	Data y_avg = Data.newData(getAvg(data_y.getData(), 10));
       	XYLine line_avg; 
       	XYLine line_sdev; 
       	double[] range; 
       	if ( y.length == 1 || sdev == 0 ) sdev = 0.01;
       	
       	Lg.d(label + "RAW avg:" + avg + " sdev:" + sdev);
       	
        if ( graphSmooth ) {
        	// smooth average
        	line_avg = Plots.newXYLine(data_x, y_avg);
        	range = DataUtil.scaleWithinRange(0, y_range[1], new double[] {Math.max(0, 2*sdev)}).getData();
        	line_sdev = Plots.newXYLine(data_x, y_avg);
        	line_avg.setLineStyle(LineStyle.newLineStyle(line_size, 1, 0));
        } else {
            // flat average
            Data sx = new Data(new double[] {0, 100});
            Data sy = DataUtil.scaleWithinRange(0, y_range[1], new double[] {avg, avg});
            line_avg  = Plots.newXYLine(sx, sy);
            //range = DataUtil.scaleWithinRange(0, y_range[1], new double[] {Math.max(0, avg-sdev), avg+sdev}).getData();
        	range = DataUtil.scaleWithinRange(0, y_range[1], new double[] {Math.max(0, 2*sdev)}).getData();
        	line_sdev = Plots.newXYLine(sx, sy);
        	line_avg.setLineStyle(LineStyle.newLineStyle(line_size/2, 1, 0));
        }
       	line_sdev.setLineStyle(LineStyle.newLineStyle((int)(2*range[0])+1, 1, 0));
       	
        line.setColor(line_color);
        line_avg.setColor(avg_color);
        line_sdev.setColor(sdev_color);
        
        // Actually create chart.
        XYLineChart chartxy = GCharts.newXYLineChart(line_avg, line_sdev, line, line_grid);
        chartxy.setSize(w, h);
        
        int legendY = 7;
        int legendX = 5;
        
        String strTitle = URLEncoder.encode(title2 + "  (" + App.getVehicleSpec().replace("\n", "") + ")");
        String strDev = URLEncoder.encode("+/-Stddev") + "++(" + App.formatDouble(sdev, "2") + ")";
        String strAvg = "++(" + App.formatDouble(avg, "2") + ")";
        
        chartxy.addMarker(Markers.newShapeMarker(Shape.DIAMOND, color, (int)(point_size*1.5)), legendX, 3*legendY);
        chartxy.addMarker(Markers.newShapeMarker(Shape.DIAMOND, diamond_color, (int)(point_size*0.75)), legendX,3*legendY);
        chartxy.addMarker(Markers.newTextMarker(strTitle, foreground, font_size), legendX+4, 3*legendY-2); 
        
        chartxy.addMarker(Markers.newShapeMarker(Shape.SQUARE, avg_color, (int)(point_size*1.1)), legendX, 2*legendY);
        chartxy.addMarker(Markers.newTextMarker("Average" + strAvg, foreground, font_size-2), legendX+4, 2*legendY-2);
        
        chartxy.addMarker(Markers.newShapeMarker(Shape.SQUARE, sdev_color, (int)(point_size*1.1)), legendX, legendY);
        chartxy.addMarker(Markers.newTextMarker(strDev, foreground, font_size-2), legendX+4, legendY-2);

        // Defining axis info and styles
        AxisStyle axisStyle = AxisStyle.newAxisStyle(foreground, font_size, AxisTextAlignment.CENTER);
        AxisLabels xAxis = AxisLabelsFactory.newAxisLabels(x_labels);
        xAxis.setAxisStyle(axisStyle);
        AxisLabels xAxis2 = AxisLabelsFactory.newAxisLabels(x_years);
        xAxis2.setAxisStyle(axisStyle);
        AxisLabels yAxis = AxisLabelsFactory.newAxisLabels(y_labels); 
        yAxis.setAxisStyle(axisStyle);
        
        // Adding axis info to chart.
        chartxy.addXAxisLabels(xAxis);
        chartxy.addXAxisLabels(xAxis2);
        chartxy.addYAxisLabels(yAxis);
        
        // if the graph is of prices, add a currency symbol to Y axis
        if ( price ) {
        	AxisLabels yAxis2 = AxisLabelsFactory.newAxisLabels(new String[] { "", App.getCurrencySymbol(), "" }); 
        	yAxis2.setAxisStyle(axisStyle);
        	chartxy.addYAxisLabels(yAxis2);
        }
        
        // Defining background and chart fills.
        chartxy.setBackgroundFill(Fills.newSolidFill(Color.newColor(background)));
        String url = chartxy.toURLString();
        
        // strip out the legend label, since it is added to the graph above.
        url = url.replaceAll("&chdl=[^&]+", "");
        Lg.i("url: " + url);
        return url;
    }
}
