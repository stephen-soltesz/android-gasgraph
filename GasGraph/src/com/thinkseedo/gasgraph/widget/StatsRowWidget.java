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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class StatsRowWidget extends TableRow { 
	
	public StatsRowWidget(Context context) {
		super(context);
	}
	public StatsRowWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	int[] rows = {R.id.rLabel, R.id.rValue};
	TextView label=null;
	TextView value=null;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater mInflater = LayoutInflater.from(getContext());
		mInflater.inflate(R.layout.widget_stats_row, this);
		Lg.d("count: " + this.getChildCount());
		label = (TextView)findViewById(R.id.rLabel);
		value = (TextView)findViewById(R.id.rValue);
	}
	
	public void setLabel(String lab) {
		//App.setText(this, R.id.rLabel, lab);
		label.setText(lab);
	}
	
	public void setValue(String val) {
		//App.setText(this, R.id.rValue, val);
		value.setText(val);
	}
	
}