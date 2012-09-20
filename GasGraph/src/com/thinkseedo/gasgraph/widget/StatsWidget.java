package com.thinkseedo.gasgraph.widget;

//import ArrayList;
//import BasicNameValuePair;
//import ClientProtocolException;
//import DefaultHttpClient;
//import HttpClient;
//import HttpPost;
//import HttpResponse;
//import NameValuePair;
//import UrlEncodedFormEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.lang.String;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.EditRecord;
import com.thinkseedo.gasgraph.EditService;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.util.CostRecordList;
import com.thinkseedo.gasgraph.util.CustomAdapterView;
import com.thinkseedo.gasgraph.util.CustomGallery;
//import com.thinkseedo.gasgraph.util.DrawableManager;
import com.thinkseedo.gasgraph.util.FillupHolder;
import com.thinkseedo.gasgraph.util.GasRecordList;
import com.thinkseedo.gasgraph.util.GenericListAdapter;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.util.CustomAdapterView.OnItemLongClickListener;
import com.thinkseedo.gasgraph.util.ServiceHolder;
import com.thinkseedo.gasgraph.util.StdStats;
import com.thinkseedo.gasgraph.widget.ImportWidget;
import com.thinkseedo.gasgraph.widget.TimeBarWidget.OnChangedListener;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class StatsWidget extends LinearLayout { 
	
	public StatsWidget(Context context) {
		super(context);
	}
	public StatsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	int[][] rowIds = {
			{R.id.r00, R.id.r01},
			{R.id.r10, R.id.r11},
			{R.id.r20, R.id.r21},
			{R.id.r30, R.id.r31},
			{R.id.r40, R.id.r41},
			{R.id.r50, R.id.r51},
			{R.id.r60, R.id.r61},
			{R.id.r70, R.id.r71},
			{R.id.r80, R.id.r81},
			{R.id.r90, R.id.r91},
			{R.id.r100, R.id.r101},
			{R.id.r110, R.id.r111},
			{R.id.r120, R.id.r121},
			{R.id.r130, R.id.r131},
			{R.id.r140, R.id.r141},
			{R.id.r150, R.id.r151},
	};
	
	String[] labels = { 
			"Average <MPG>",
			"Maximum <MPG>",
			"Minimum <MPG>",
			"Total Costs",
			"Total <DIST>",
			"Total <VOL>",
			"Total Fill Ups",
			"Total Days",
			"Cost per <DIST>",
			"Cost per <VOL>",
			"Cost per Day",
			"Costs per Fill Up",
			"<DIST> per Fill Up",
			"<VOL> per Fill Up",
			"<DIST> per Day",
			"<DIST> per <CUR>",
	};
	enum COL { AVERAGE_MPG, MAX_MPG, MIN_MPG, TOTAL_COST, 
			  TOTAL_DIST, TOTAL_VOL, TOTAL_FILLUPS, TOTAL_DAYS, 
			  COST_PER_DIST, COST_PER_VOL, COST_PER_DAY, 
			  COST_PER_FILLUP, DIST_PER_FILLUP, VOL_PER_FILLUP, 
			  DIST_PER_DAY, DIST_PER_CUR
	};
	Map<COL, Double> enumMap1 = new TreeMap<COL, Double>();

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater mInflater = LayoutInflater.from(getContext());
		mInflater.inflate(R.layout.widget_stats, this);
	}
	
	Date getFirstDate() {
		Date d;
		List<Date> dl = App.mRecords.getDatesSince(null);
		if ( dl.size() > 0 ) {
			d = dl.get(dl.size()-1);
		} else {
			d = Calendar.getInstance().getTime();
		}
		return d;
	}
	
	String getVal(int row, Date since) {
		if ( App.mRecords == null || App.mRecords.mList.size() == 0 ) {
			return "0";
		}
		Double 	d = 0.0;
		String[] units;
		String precision="2";
		boolean price=false;
		switch(COL.values()[row]){
			case AVERAGE_MPG:
				d = StdStats.mean(App.mRecords.getValuesFor(App.units_cn, since));
				units = App.mUnitAbbv.get(App.units_cn);
				precision = units[2];
				price = units[4].matches("CUR");
				enumMap1.put(COL.AVERAGE_MPG, d);
				break;
			case MAX_MPG:
				d = StdStats.max(App.mRecords.getValuesFor(App.units_cn, since));
				units = App.mUnitAbbv.get(App.units_cn);
				precision = units[2];
				price = units[4].matches("CUR");
				enumMap1.put(COL.MAX_MPG, d);
				break;
			case MIN_MPG:
				d = StdStats.min(App.mRecords.getValuesFor(App.units_cn, since));
				units = App.mUnitAbbv.get(App.units_cn);
				precision = units[2];
				price = units[4].matches("CUR");
				enumMap1.put(COL.MIN_MPG, d);
				break;
			case TOTAL_COST:
				d = StdStats.sum(App.mRecords.getValuesFor("totalprice", since));
				precision = "2";
				price = true;
				enumMap1.put(COL.TOTAL_COST, d);
				break;
			case TOTAL_DIST:
				List<Double> l = App.mRecords.getValuesFor(App.distance, since, 2);
				units = App.mUnitAbbv.get(App.distance);
				precision = units[2];
				price = units[4].matches("CUR");
				if ( l.size() > 0 ) { 
					d = (double)(l.get(0) - l.get(l.size()-1));
				} else {
					d = 0.0;
				}
				enumMap1.put(COL.TOTAL_DIST, d);
				break;
			case TOTAL_VOL:
				d = StdStats.sum(App.mRecords.getValuesFor(App.volume, since));
				units = App.mUnitAbbv.get(App.volume);
				precision = units[2];
				price = units[4].matches("CUR");
				enumMap1.put(COL.TOTAL_VOL, d);
				break;
			case TOTAL_FILLUPS:
				d = (double)App.mRecords.getDatesSince(since).size();
				precision = "0";
				price = false;
				enumMap1.put(COL.TOTAL_FILLUPS, d);
				break;
			case TOTAL_DAYS:
				List<Date> dl = App.mRecords.getDatesSince(since,2);
				if ( dl.size() > 0 ) {
					long x = dl.get(0).getTime() - dl.get(dl.size()-1).getTime();
					d = (double)(x/1000.0/(60*60*24));
				} else {
					d = 0.0;
				}
				precision = "0";
				price=false;
				enumMap1.put(COL.TOTAL_DAYS, d);
				break;
				
			case COST_PER_DIST: 
				Lg.d("f2:" + enumMap1.get(COL.TOTAL_DIST));
				Lg.d("f1:" + enumMap1.get(COL.TOTAL_COST));
				d = enumMap1.get(COL.TOTAL_COST)/enumMap1.get(COL.TOTAL_DIST);
				precision = "2";
				price=true;
				enumMap1.put(COL.COST_PER_DIST, d);
				break;
			case COST_PER_VOL:
				d = enumMap1.get(COL.TOTAL_COST)/enumMap1.get(COL.TOTAL_VOL);
				precision = "2";
				price=true;
				enumMap1.put(COL.COST_PER_VOL, d);
				break;
			case COST_PER_DAY:
				d = enumMap1.get(COL.TOTAL_COST)/enumMap1.get(COL.TOTAL_DAYS);
				precision = "2";
				price=true;
				enumMap1.put(COL.COST_PER_DAY, d);
				break;
			case COST_PER_FILLUP:
				d = enumMap1.get(COL.TOTAL_COST)/enumMap1.get(COL.TOTAL_FILLUPS);
				precision = "2";
				price=true;
				enumMap1.put(COL.COST_PER_FILLUP, d);
				break;
			case DIST_PER_FILLUP:
				d = enumMap1.get(COL.TOTAL_DIST)/enumMap1.get(COL.TOTAL_FILLUPS);
				precision = "1";
				price=false;
				enumMap1.put(COL.DIST_PER_FILLUP, d);
				break;
			case VOL_PER_FILLUP:
				d = enumMap1.get(COL.TOTAL_VOL)/enumMap1.get(COL.TOTAL_FILLUPS);
				precision = "1";
				price=false;
				enumMap1.put(COL.VOL_PER_FILLUP, d);
				break;
			case DIST_PER_DAY:
				d = enumMap1.get(COL.TOTAL_DIST)/enumMap1.get(COL.TOTAL_DAYS);
				precision = "1";
				price=false;
				enumMap1.put(COL.DIST_PER_DAY, d);
				break;
			case DIST_PER_CUR:
				d = enumMap1.get(COL.TOTAL_DIST)/enumMap1.get(COL.TOTAL_COST);
				precision = "1";
				price=false;
				enumMap1.put(COL.DIST_PER_CUR, d);
				break;
		}
		if ( d.isInfinite() || d.isNaN() ) {
			if ( price ) {
				return App.getCurrencySymbol() + "0.00";
			} else {
				return "0";
			}
		} else {
			String fmt = "%." + precision + "f";
			String ret=null;
			if ( price ) {
				NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
				nf.setMaximumFractionDigits(2);
				nf.setMinimumFractionDigits(2);
				ret = nf.format(d);
				ret = App.getCurrencySymbol() + ret;
				//ret = String.format(App.getCurrencySymbol() + fmt, d);
			} else {
				NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
				int prec = Integer.parseInt(precision);
				nf.setMaximumFractionDigits(prec);
				nf.setMinimumFractionDigits(prec);
				ret = nf.format(d);
				//ret = String.format(fmt, d);
			}
			//if ( precision.equals("0") ) {
			//	ret += ".0";
			//} else if ( precision.equals("1") ) {
			//	ret += "";
			//} else if ( precision.equals("2") ) {
			//	ret += "";
			//} else if ( precision.equals("3") ) {
			//	ret = ret.substring(0,ret.length()-1);
			//}
			return ret;
		}
	}
	
	boolean runOnce=true;
	/* 
	   init stats -> initTB -> stats 
	 
	 */
	public void updateStats() {
		TimeBarWidget tb = (TimeBarWidget)findViewById(R.id.timeBar);
		if ( tb != null ) {
			updateStats(tb.getDateForCurrentPosition());
		}
		
	}
	
	public void updateStats(Date since) {
		Lg.e("updateStats() date: " + since);
		if ( runOnce ) {
			TimeBarWidget tb = (TimeBarWidget)findViewById(R.id.timeBar);
			if ( tb != null ) {
            	tb.setOnChangedListener(new OnChangedListener() {
					@Override
					public void onChanged(int pos, final Date since) {
						Lg.e("onChange() pos: " + pos + " date: " + since);
						// post update so button responds instantly.
						postDelayed(new Runnable() {
							@Override
							public void run() {
								updateStats(since);
								App.deleteImages();
							}
						}, 100);
					}
               	});
				tb.initTimeBar();
			}
			runOnce=false;
			//return;
		}
		TextView title = (TextView)findViewById(R.id.textTitleCar);
		TextView titleDate = (TextView)findViewById(R.id.textTitleDate);
		if ( since == null ) {
			title.setText(App.getVehicleSpec()); //+ " \u2022 " + 
			titleDate.setText(App.formatDate(getFirstDate(), App.DATE_DISPLAY) + " - " + 
							  App.formatDate(null, App.DATE_DISPLAY));
		} else {
			title.setText(App.getVehicleSpec()); // + " \u2022 " + 
			titleDate.setText(App.formatDate(since, App.DATE_DISPLAY) + " - " + 
					  		  App.formatDate(null, App.DATE_DISPLAY) );
		}
		for ( int i=0; i < labels.length; i++) {
			Lg.d("row: " + i + " col 1 " + "val: " +  getVal(i, since));
			App.setText(this, rowIds[i][0], format(labels[i]));
			App.setText(this, rowIds[i][1], getVal(i, since));
		}
	}
	
    public String format(String temp) {
    	temp = temp.replaceAll("<MPG>",  App.mUnitAbbv.get(App.units_cn)[3]);
    	temp = temp.replaceAll("<DIST>", App.mUnitAbbv.get(App.distance)[3]);
    	temp = temp.replaceAll("<VOL>",  App.mUnitAbbv.get(App.volume)[3]);
    	temp = temp.replace("<CUR>",  App.getCurrencySymbol());
    	return temp;
    }
    
    
}