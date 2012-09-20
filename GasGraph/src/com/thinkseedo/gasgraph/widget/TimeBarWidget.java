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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class TimeBarWidget extends LinearLayout { 
	
	public TimeBarWidget(Context context) {
		super(context);
	}
	public TimeBarWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	final static int MONTH_ONE = 1;
	final static int WEEK_ONE = 0;
	final static int MONTH_THREE = 2;
	final static int MONTH_SIX = 3;
	final static int YEAR_ONE = 4;
	final static int ALL = 5;
	
	ToggleButton[] toggleList = new ToggleButton[6];

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater mInflater = LayoutInflater.from(getContext());
		mInflater.inflate(R.layout.widget_time, this);
		toggleList[WEEK_ONE]   = (ToggleButton)findViewById(R.id.toggleWeek);
		toggleList[MONTH_ONE]  = (ToggleButton)findViewById(R.id.toggleMonth);
		toggleList[MONTH_THREE]= (ToggleButton)findViewById(R.id.toggleThreeMonth);
		toggleList[MONTH_SIX]  = (ToggleButton)findViewById(R.id.toggleSixMonth);
		toggleList[YEAR_ONE]   = (ToggleButton)findViewById(R.id.toggleYear);
		toggleList[ALL] 	   = (ToggleButton)findViewById(R.id.toggleAll);
		//setChecked(ALL);
		//init();
	}
	
	int mCurrentPosition=5;
	
	public int getSelection() {
		return mCurrentPosition;
	}
    public interface OnChangedListener {
       void onChanged(int pos, Date since); 
    }	
    OnChangedListener mOCL=null;
    public void setOnChangedListener(OnChangedListener ocl) {
    	mOCL=ocl;
    }
    public Date getDateForPosition(int pos) {
					
		Calendar c = Calendar.getInstance();
		switch(pos) {
			case WEEK_ONE:
				c.add(Calendar.WEEK_OF_YEAR, -1);
				break;
			case MONTH_ONE:
				c.add(Calendar.MONTH, -1);
				break;
			case MONTH_THREE:
				c.add(Calendar.MONTH, -3);
				break;
			case MONTH_SIX:
				c.add(Calendar.MONTH, -6);
				break;
			case YEAR_ONE:
				c.add(Calendar.YEAR, -1);
				break;
			case ALL:
				c = null;
				break;
		}
		if ( c == null ) {
			return null;
		}
		return c.getTime();
    }
    
    public Date getDateForCurrentPosition() {
		Integer pos = App.getPrefs().getInt("timebar_position", 5);
		return getDateForPosition(pos);
    }
	
	OnCheckedChangeListener checked = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Integer pos = (Integer)buttonView.getTag();
			Lg.d("checked: " + pos);
			if ( isChecked || pos == mCurrentPosition ) {
				mCurrentPosition=pos;
				setChecked(pos);
				if ( mOCL != null ) {
					App.getPrefs().edit().putInt("timebar_position", pos).commit();
					mOCL.onChanged(pos, getDateForPosition(pos));
				}
			}
		}
	};
	
	void init() {
		for ( int i=0; i < toggleList.length; i++ ) {
			toggleList[i].setTag(Integer.valueOf(i));
			toggleList[i].setOnCheckedChangeListener(checked);
		}
		Integer pos = App.getPrefs().getInt("timebar_position", 5);
		mCurrentPosition=pos;
		setChecked(pos);
	}
	
	public void setChecked(int pos) {
		for ( int i=0; i < toggleList.length; i ++ ) {
			toggleList[i].setChecked((pos==i));
		}
	}
	
	public void initTimeBar() {
		init();
	}
}	