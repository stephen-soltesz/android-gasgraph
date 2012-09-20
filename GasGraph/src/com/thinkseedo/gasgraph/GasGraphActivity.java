package com.thinkseedo.gasgraph;

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
import java.util.Calendar;
import java.util.SortedSet;
import java.lang.String;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.util.CustomAdapterView;
import com.thinkseedo.gasgraph.util.CustomGallery;
import com.thinkseedo.gasgraph.util.DrawableManager;
import com.thinkseedo.gasgraph.util.FillupHolder;
import com.thinkseedo.gasgraph.util.GasRecordList;
import com.thinkseedo.gasgraph.util.GenericListAdapter;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.util.CustomAdapterView.OnItemLongClickListener;
import com.thinkseedo.gasgraph.widget.ImportWidget;


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
import android.widget.TextView;
import android.widget.Toast;

public class GasGraphActivity extends ListActivity { 
    /** Called when the activity is first created. */
	//private EfficientAdapter mListAdapter;
	private GenericListAdapter mListAdapter;
	
    ProgressDialog mDialog = null;
    boolean mWaitDialogShowing;
    boolean mLandscape = false;
    GasRecord mGasRecord=null;
    DrawableManager mDrawableManager;
    OrmLiteSqliteOpenHelper mDatabaseHelper = null;
    CustomGallery mGallery=null;
    Dialog mWelcome=null;
    Dialog mReview=null;
    Dialog mProgress=null;

    // the add/edit record dialog 
    int mGalleryPosition = 0;
    Handler mHandler = new Handler();

    static final int CONFIRM_RESET_DIALOG_ID = 1;
    static final int IMPORT_EXPORT_DIALOG_ID = 2;
    static final int WAIT_DIALOG_ID = 3;
    static final int SAMPLE_DIALOG_ID = 5;
    static final int IO_LAYOUT_ID = 6;
    static final int WELCOME_DIALOG_ID = 7;
    static final int USAGE_DIALOG_ID = 8;
    static final int REVIEW_DIALOG_ID = 9;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fillup_list_simple);
	    LinearLayout landscapeView = (LinearLayout) findViewById(R.id.layoutLandscape);
	    if ( landscapeView == null && App.actCount == 0 ) {
    		checkForRedirect();
    		App.actCount += 1;
    		int runCount = App.getPrefs().getInt("runCount", 0);
    		App.getPrefs().edit().putInt("runCount", runCount + 1).commit();
		  	if ( runCount > 10 && runCount % 15 == 0 ) {
		  		if ( !App.getPrefs().getBoolean(App.PREF_MARKET_REVIEW, false) ) {
					if ( !App.getPrefs().getBoolean(App.PREF_PLEASE_REVIEW, false) ) {
			 			showDialog(REVIEW_DIALOG_ID);
			 		}
				}
		 	}
        }
	    //if ( false ) {
        //TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        //if ( tabHost != null ) {
        	//tabHost.addTab(tabHost.newTabSpec("tab1")
        //	TabSpec ts = tabHost.newTabSpec("blue");
        //	assert ts != null;
        //	ts = ts.setIndicator("test");
        //	assert ts != null;
        //	assert tabHost != null;
        //	ts = ts.setContent(new Intent(getBaseContext(), ImportCSV.class));
        //	assert ts != null;
        //	tabHost.addTab(ts);
        //} else {
        //	Lg.e("tabHost: NULL");
        //}
	    //}
        //tabHost.addTab(tabHost.newTabSpec("tab2")
         //       .setIndicator("tab2", getResources().getDrawable(R.drawable.ic_maint))
          //      .setContent(R.id.tab2));
    }
    
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {         
            Lg.e("in Main - RECEIVED BROADCAST - str: " + intent);
        	App.mRecords = GasRecordList.simpleReadFromDB(App.getHelper());
		    App.mRecords.updateCalculatedValues();
		    mListAdapter.mRedraw = true;
            mListAdapter.notifyDataSetChanged();
		    mListAdapter.mRedraw = false;
        }
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Lg.e("onDestroy()");
        App.closeHelper();
        doUnbindService();
        mGallery.reloadAll();
        stopService(new Intent(App.getContext(), Background.class));
    }
    
    public void onStart(){
    	super.onStart();
    	
    	Lg.d("onStart()");
        setupFirstRun();
        if ( App.preferencesChanged ) {
        	Lg.e("PREFERNCES CHANGED");
        	App.preferencesChanged = false;
       		App.flushCache();
        	App.deleteImages();
        }
        setupTitle();
        setupHeader();
        setupDatabase();
        
        // runs once but slow...
	   	GasRecordList.copySampleDB();
        // in case preferences were changed.
		App.setupGraphSets();
		App.mRecords = GasRecordList.simpleReadFromDB(App.getHelper());
		App.mRecords.updateCalculatedValues();
		//mListAdapter = new EfficientAdapter(this);
		mListAdapter = new GenericListAdapter(new FillupHolder(this, R.layout.fillup_record_view));
		setListAdapter(mListAdapter);
        
		mListAdapter.mRedraw = true;
        mListAdapter.notifyDataSetChanged();
		mListAdapter.mRedraw = false;
		
	    App.act=this;
	    LinearLayout landscapeView = (LinearLayout) findViewById(R.id.layoutLandscape);
	    if ( landscapeView == null ) {
	    	mLandscape = false;
	    	Lg.d("setup portrait()");
	    	setupPortrait();
    	} else {
    		mLandscape = true;
    		Lg.d("setup landscape()");
    		setupLandscape();
        }
	    /* First, get the Display from the WindowManager */
	    //Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
	    /* Now we can retrieve all display-related infos */
	    //int orientation = display.getOrientation();
	    //Lg.e("orientation: " + orientation);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        //unregisterReceiver(receiver);
    	Lg.e("onPause()");
    	if ( mWelcome != null ) {
    		mWelcome.dismiss();
    		mWelcome = null;
    	}
    	if ( mReview != null ) {
    		mReview.dismiss();
    		mReview = null;
    	}
    }
    
    public void onResume() {
    	super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(App.BROADCAST_RELOAD);
        registerReceiver(receiver, filter);
    	Lg.d("onResume()");
    }
    
    void setupHeader() {
		View header = findViewById(R.id.listHeader);
		
        String[] units = App.mUnitAbbv.get(App.units_cn);
		if ( header != null && units != null && units.length > 1 ) {
			String s = units[1].replace("CUR", App.getCurrencySymbol());
		    ((TextView) header.findViewById(R.id.textCN_lv)).setText(s);
		}
        units = App.mUnitAbbv.get(App.units_br);
		if ( header != null && units != null && units.length > 1 ) {
			String s = units[1].replace("CUR", App.getCurrencySymbol());
		    ((TextView) header.findViewById(R.id.textBR_lv)).setText(s);
		}
        units = App.mUnitAbbv.get(App.units_bl);
		if ( header != null && units != null && units.length > 1 ) {
			String s = units[1].replace("CUR", App.getCurrencySymbol());
		    ((TextView) header.findViewById(R.id.textBL_lv)).setText(s);
		}
        units = App.mUnitAbbv.get(App.units_tr);
		if ( header != null && units != null && units.length > 1 ) {
			String s = units[1].replace("CUR", App.getCurrencySymbol());
			((TextView) header.findViewById(R.id.textTR_lv)).setText(s);
		}
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Lg.d("req:" + requestCode + " res:" + resultCode + " " + data );
        Lg.d("ok:" + RESULT_OK + " cancel:" + RESULT_CANCELED);
        if ( data != null ) { 
        	if ( data.hasExtra("return") ) {
        		int r = data.getIntExtra("return", -1);
        		Lg.d("extra:" + r);
        	}
        }
        if ( requestCode == EditRecord.ADD_ID ) {
            // If the request was cancelled, then we are cancelled as well.
            if (resultCode == RESULT_CANCELED) {
            	Lg.d("RESULT_CANCELED");
                // Otherwise, there now should be text...  reload the prefs,
                // and show our UI.  (Optionally we could verify that the text
                // is now set and exit if it isn't.)
            }
        } else if ( requestCode == EditRecord.EDIT_ID ) {
            // In this case we are just changing the text, so if it was
            // cancelled then we can leave things as-is.
            if (resultCode != RESULT_CANCELED) {
            	Lg.d("result:" + resultCode);
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	AlertDialog.Builder builder;
        switch (id) {
            case USAGE_DIALOG_ID:
            	if ( App.MARKET == App.MARKET_AMAZON ) {
            		return createWebViewDialog("How-to...", "web_usage_amz.html");
            	} else {
            		return createWebViewDialog("How-to...", "web_usage.html");
            	}

            case REVIEW_DIALOG_ID:
            	if ( mReview != null ) {
            		mReview.dismiss();
            		mReview = null;
            	}
            	String url;
            	if ( App.MARKET == App.MARKET_AMAZON ) {
            		url = "web_review_amz.html";
            	} else {
            		url = "web_review.html";
            	}
            	mReview = createWebViewDialog("What do you like?", url, false, new Runnable() {
					@Override
					public void run() {
						App.getPrefs().edit().putBoolean(App.PREF_PLEASE_REVIEW, true).commit();
					}
            	}, false);
            	return mReview;
            case WELCOME_DIALOG_ID:
            	if ( mWelcome != null ) {
            		mWelcome.dismiss();
            		mWelcome = null;
            	}
            	mWelcome = createWebViewDialog("Welcome!", "welcome.html", false, new Runnable() {
					@Override
					public void run() {
						App.getPrefs().edit().putBoolean(App.PREF_WELCOME_SHOWN, true).commit();
					}
            	}, true);
            	//mHandler.postDelayed(new Runnable() {
			   	//		@Override
				//	   	public void run() {
			   	//			Lg.e("running copysampleDB()");
				//		   	App.mRecords = GasRecordList.simpleReadFromDB(App.getHelper());
				//			App.mRecords.updateCalculatedValues();
				//			mListAdapter.mRedraw = true;
				//			mListAdapter.notifyDataSetChanged();
				//			mListAdapter.mRedraw = false;
				//	   	}
        	   	//	}, 1000);
            	return mWelcome;
            case IMPORT_EXPORT_DIALOG_ID:
            	builder = new AlertDialog.Builder(this);
            	builder.setMessage("- Import loads a fill-up history from a *.csv file.\n\n" +
            					   "- Export saves your fill-up history to a *.csv file.\n\n" +
            					   "- More details are on the following screens."
            				)
            	       .setCancelable(true)
            	       .setNegativeButton("Import", new DialogInterface.OnClickListener() {
            	    	   public void onClick(DialogInterface dialog, int id) {
            	        	   Lg.d("Import Data");
            	        	   Intent importCSV = new Intent(getBaseContext(), ImportWidget.class);
            	        	   startActivity(importCSV);
            	           }
            	       })
            	       .setPositiveButton("Export", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	        	    //GasRecordList.exportRecords("gasgraph.csv");
            	        	    Intent exportCSV = new Intent(getBaseContext(), ExportCSV.class);
            	        	    startActivity(exportCSV);
            	           }
            	       });
            	return builder.create();
            case SAMPLE_DIALOG_ID:
            	builder = new AlertDialog.Builder(this);
            	builder.setMessage("Loading the sample data will erase any existing data. Continue?")
            	       .setCancelable(false)
            	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            	        public void onClick(DialogInterface dialog, int id) {
            	        	   Lg.d("load sample data");
            	               GasRecordList.loadSampleData(App.getHelper());
            	               mListAdapter.notifyDataSetChanged();
            	               App.deleteImages();
            	           }
            	       })
            	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	                dialog.cancel();
            	           }
            	       });
            	return builder.create();
            case WAIT_DIALOG_ID:
            	mDialog = ProgressDialog.show(this, "", "Loading now. Please wait...", true);
            	return mDialog;
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        }
    }    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
    	if ( App.getPrefs().getBoolean(App.PREF_SAMPLES_CLEAR, true) ) {
    		menu.findItem(R.id.itemReset).setVisible(false);
    	} else {
    		menu.findItem(R.id.itemReset).setVisible(true);
    	}
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.itemImportExport:
        		//showDialog(IMPORT_EXPORT_DIALOG_ID);
                Intent ioact = new Intent(getBaseContext(), IOApp.class);
                startActivity(ioact);
                return true;
        		//break;
            case R.id.itemUsage:
        		showDialog(USAGE_DIALOG_ID);
            	break;
            case R.id.itemReset:
            	Lg.d("loading host data before import");
            	App.getPrefs().edit().putBoolean(App.PREF_SAMPLES_CLEAR, true).commit(); 
            	setupDatabase();
            	App.mRecords = GasRecordList.simpleReadFromDB(App.getHelper());
            	App.mRecords.updateCalculatedValues();
            	mListAdapter.mRedraw = true;
            	mListAdapter.notifyDataSetChanged();
            	mListAdapter.mRedraw = false;
            	App.deleteImages();
            	break;
            case R.id.itemSettings:
                Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
                startActivity(settingsActivity);
                return true;
        }       
        return super.onOptionsItemSelected(item);
    }     
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Toast.makeText(this, "ListItemClick-" + String.valueOf(position), Toast.LENGTH_SHORT).show();
  	}
    
  	// bind to service.
    private boolean mIsBound=false;
    private Background mDownloadService=null;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
    //        // This is called when the connection with the service has been
    //        // established, giving us the service object we can use to
    //        // interact with the service.  Because we have bound to a explicit
    //        // service that we know is running in our own process, we can
    //        // cast its IBinder to a concrete class and directly access it.
            mDownloadService = ((Background.BoundPipe)service).getService();
      		//mDownloadService.setupSizes(dim[0], dim[1]);
			//String[] graphTypes = getGraphTypes();
			if ( mGallery != null ) {
				mGallery.setAdapter(new ImageAdapter(GasGraphActivity.this));
			}

            // Tell the user about this for our demo.
            // Toast.makeText(GasGraphActivity.this, "service connected", 
            //       Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mDownloadService = null;
            //Toast.makeText(GasGraphActivity.this, "service disconnected",
             //       Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this,
                Background.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
  	
    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;
      	private LayoutInflater mInflater;
        DrawableManager mDrawManager=null; 
        private String[] graphTypes;
        
        public class ImageHolder {
        	public String			name;
      		public ImageView 		v;
      		public ImageView 		a;
      		public ProgressBar		p;
      		public RelativeLayout	r;
      		public TextView 		t;
      		public WebView  		w;
        }
      		
        public ImageAdapter(Context c) {
        	mContext = c;
        	mInflater = LayoutInflater.from(mContext);
      		
			graphTypes = getGraphTypes();
        }
        public int getCount() {
        	SortedSet<String> s;
            s = App.mGraphSets.get(App.distance + App.volume); 
            return s.size(); 
        }
        public Object getItem(int position) {
            return position;
        }
        public long getItemId(int position) {
            return position;
        }

      	public View getView(final int position, View convertView, ViewGroup parent) {
    		// A ViewHolder keeps references to children views to avoid
      		// unneccessary calls to findViewById() on each row.
      		
   			// When convertView is not null, we can reuse it directly, there is 
      		// no need to reinflate it. We only inflate a new View when the 
      		// convertView supplied is null.
   			if ( convertView == null ) {
   				Lg.w("creating new imageholder");
   		  		convertView = mInflater.inflate(R.layout.imagecell, null);
      			ImageHolder ih = new ImageHolder();
      			ih.v = (ImageView)convertView.findViewById(R.id.imageGraph);
      			ih.a = (ImageView)convertView.findViewById(R.id.imageAlert);
      			ih.p = (ProgressBar)convertView.findViewById(R.id.progressSpin);
      			ih.r = (RelativeLayout)convertView.findViewById(R.id.spinnerLayout);
      			ih.t = (TextView)convertView.findViewById(R.id.textGraphTitle);
      			//ih.w = (WebView)convertView.findViewById(R.id.webGraph);
      			//ih.w.getSettings().setJavaScriptEnabled(true);
      			//ih.w.setOnTouchListener(new View.OnTouchListener() {
      			//    @Override
      			//    public boolean onTouch(View v, MotionEvent event) {
      			//        return false;
      			//    }
      			//});
      			//ih.w.setWebViewClient(new WebViewClient() {
      			//    @Override
      			//    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
      			//        Lg.i("WEB_VIEW_TEST: error code:" + errorCode);
      			//        super.onReceivedError(view, errorCode, description, failingUrl);
      			//        view.loadUrl("file:///android_asset/web_error.html");
      			//    }
      			//    @Override
      			//    public void onPageFinished(WebView view, String url) {
      			//        Lg.i("WEB_VIEW_TEST: page finished:" + url);
      			//    }
      			//});
      			//ih.w.setBackgroundColor(0xFF000000);
      			//ih.w.getSettings().setDefaultZoom(ZoomDensity.MEDIUM);
      			//ih.w.setClickable(false);
      			//ih.w.setScrollContainer(false);
      			//ih.w.setVisibility(View.GONE);
      			convertView.setTag(ih);
      		} 
   			ImageHolder holder = (ImageHolder)convertView.getTag();
			String graphtag = graphTypes[position];
  			holder.t.setBackgroundColor(Color.parseColor("#"+App.color));
      		
   			assert (mDownloadService != null );
   			Lg.d("downloading: " + graphtag);
   			//GraphView gv = new GraphView(holder, graphtag);
   			//gv.execute("");
   			mDownloadService.setupImageWithTag(graphtag, holder);
   			//mDownloadService.setupImageInWebview(graphtag, holder);
      		return convertView;
      	}
    }
    
    //public void closeHelper() {
    //   // release the helper before exiting.
    //  if (mDatabaseHelper != null) {
            //OpenHelperManager.releaseHelper();
    //     	App.closeHelper();
    //    	//mDatabaseHelper.close();
    //       mDatabaseHelper = null;
    //  }
    //}

    /** Get the helper from the manager once per class. */
    //public DatabaseHelper getHelper() {
    //    if (mDatabaseHelper == null) {
    //        mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
    //    }
    //private OrmLiteSqliteOpenHelper getHelper() {
     //   if (mDatabaseHelper == null) {
      //  	mDatabaseHelper = App.getHelper();
       // }
        //if (mDatabaseHelper == null) {
        //	boolean samplesClear = App.getPrefs().getBoolean(App.PREF_SAMPLES_CLEAR, true);
        //	if ( !samplesClear ) { 
        //		Lg.e("getHelper for Samples");
         //   	mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelperSample.class);
        	//} else {
        	//	Lg.e("getHelper for Default");
            //	mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        	//}
        //}
    //    return mDatabaseHelper;
    //}
    
    void setupDatabase() {
    	Lg.d("setup db ");
        App.closeHelper();
        App.getHelper();
    }
    
    public void setupTitle() {
    	TextView t = (TextView)findViewById(R.id.textTitle);
       	if ( t != null ) {
       		((TextView)findViewById(R.id.textTitle)).setText(R.string.main_title);
       	}
    }
    
    void checkForRedirect() {
        if ( !App.getPrefs().getBoolean(App.PREF_START_ON_LIST, true) ) {
        	EditRecord.addRecordView(this);
        }
    }
    
	public void setupFirstRun() {
        // Check to see if this is the initial run.
        boolean welcomeShown = App.getPrefs().getBoolean(App.PREF_WELCOME_SHOWN, false);
        if ( ! welcomeShown ) {
        	showDialog(WELCOME_DIALOG_ID);
        }
        if ( !App.getPrefs().contains(App.PREF_START_ON_LIST) ) {
        	boolean startOnList = !App.getPrefs().getBoolean(App.PREF_START_ON_ADD, false);
        	App.getPrefs().edit().putBoolean(App.PREF_START_ON_LIST, startOnList).commit();
        }
        if ( !App.getPrefs().contains(App.PREF_SAMPLES_CLEAR) ) {
        	boolean samplesClear = !App.getPrefs().getBoolean(App.PREF_SAMPLE_DATA, true);
        	App.getPrefs().edit().putBoolean(App.PREF_SAMPLES_CLEAR, samplesClear).commit();
        }
		
        
		// performed only once.
        String firstrun = App.getPrefs().getString(App.PREF_FIRST_RUN, null);
        if ( firstrun == null || Integer.parseInt(firstrun) < App.VERSION_MAX ) {
        	if ( firstrun == null ) {
        		// really only ever do this once.
        		App.getPrefs().edit().putBoolean(App.PREF_SAMPLES_CLEAR, false).commit();
        		//App.setSampleDataFlag(true);
        	}
        	App.getPrefs().edit().putString(App.PREF_FIRST_RUN, App.VERSION_MAX.toString()).commit();
        	App.getPrefs().edit().putLong(App.PREF_FIRST_DATE,Calendar.getInstance().getTime().getTime()).commit();
        	App.postDataFirst(this);
        }
	}
	
    void setupPortrait() {
        //View view = (View) findViewById(R.id.imageViewAdd);
        View view = (View) findViewById(R.id.buttonAdd);
        if ( view != null ) {
        	view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditRecord.addRecordView(GasGraphActivity.this);
				}
        	});
        	mListAdapter.notifyDataSetChanged();
       		//getListView().smoothScrollToPosition(0);
       		getListView().setSelectionFromTop(0,0);
        }
        view = (View) findViewById(R.id.imageViewService);
        if ( view != null ) {
        	view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditService.addServiceView(GasGraphActivity.this);
				}
        	});
        }
    }
    
    String[] getGraphTypes(){
		String set_key = App.distance + App.volume; 
		SortedSet<String> s = App.mGraphSets.get(set_key);
		String[] graphTypes = new String[s.size()];
		s.toArray(graphTypes);
		for (int i=0,j=graphTypes.length-1; i < graphTypes.length/2+1; i++,j-- ) {
			String x = graphTypes[j];
			graphTypes[j] = graphTypes[i];
			graphTypes[i] = x;
		}
		return graphTypes;
    }
    
    void pause (int ms) {
      	try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    void setupLandscape() {
   		Long current = Calendar.getInstance().getTime().getTime();
   		Long firstrun = App.getPrefs().getLong(App.PREF_FIRST_DATE, Calendar.getInstance().getTime().getTime());
   		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
   							 WindowManager.LayoutParams.FLAG_FULLSCREEN);
   			
       	mGallery = (CustomGallery) findViewById(R.id.gallery1);
       	if ( mGallery != null ) {
      		doBindService();
      		
       		int[] dim = App.checkWinSize(GasGraphActivity.this);
			String[] graphTypes = getGraphTypes();
			Lg.d("DOWNLOAD ALL");
      		Background.downloadAllImages(graphTypes, dim[0], dim[1]);
   			mGallery.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(CustomAdapterView<?> parent,
						View view,  int position, long id) {
					//originalClick(position);
					insertClick(position);
					return true;
				}
   			});
       	}
    }
	void insertClick(int position) {
		Toast.makeText(App.getContext(), "Loading share list..", Toast.LENGTH_LONG).show();    
   		
   		String[] types = getGraphTypes(); 
   		File f = Background.getFileFor(types[position]);
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
        mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/png");
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(intent , "Send options")); 
			}
        }, 500);
	}
	void originalClick(int position) {
		Toast.makeText(App.getContext(), "Loading share list..", Toast.LENGTH_LONG).show();    
   		Intent intent = new Intent(Intent.ACTION_SEND);
   		String[] l = App.iUnitAbbv.get(position);
   		File f = Background.getFileFor(l[0]);
   		File odir = GasRecordList.getImportExportDir();
   		File o = new File(odir, f.getName());
   		try {
   			GasRecordList.copyIStoOS(new FileInputStream(f), new FileOutputStream(o));
		} catch (FileNotFoundException e) { Lg.e("no file trace: " + e);
		} catch (IOException e) { Lg.e("ioe trace: " + e); 
		}
   		intent.setType("image/png");
   		String url=null;
   		try {
   			url = MediaStore.Images.Media.insertImage(getContentResolver(), 
								o.toString(), o.getName(),null);
   			Lg.e("url: " + url);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(o));
   		startActivity(Intent.createChooser(intent , "Send options")); 
	}
    public Dialog createWebViewDialog(String title, String filename) {
    	return createWebViewDialog(title, filename, true, null, true);
    }
    
    public Dialog createWebViewDialog(String title, String filename, boolean cancelable, final Runnable r, boolean showButton) {
    	final Dialog mInfo;
    	mInfo = new Dialog(this,android.R.style.Theme_Black_NoTitleBar);
    	mInfo.setContentView(R.layout.webviewdialog);
    	mInfo.setCancelable(cancelable);
    	
        //TextView titleView = (TextView)mInfo.findViewById(R.id.textDialogTitle);
    	final Button dismiss = (Button)mInfo.findViewById(R.id.buttonDialogDismiss);
    	WebView w = (WebView)mInfo.findViewById(R.id.webViewDialog);
    	
        //titleView.setText(title);
    	dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mInfo.dismiss();
				if ( r != null ) {
					r.run();
				}
			}
    	});
    	if ( !showButton ) {
    		dismiss.setEnabled(false);
    		dismiss.postDelayed(new Runnable() {
				@Override
				public void run() {
					updateButton(10,dismiss);
				}
    		}, 1000);
    	}
    	w.setBackgroundColor(0xFF000000);
        w.loadUrl("file:///android_asset/" + filename);
        w.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    		
        return mInfo;
    }
    void updateButton(final int count, final Button b ) {
    	if ( count == 0 ) {
    		b.setEnabled(true);
    		b.setText(getString(R.string.dialog_dismiss));
    	} else {
    		b.setText(getString(R.string.dialog_dismiss) + " (" + count + ")" );
    		b.postDelayed(new Runnable() {
				@Override
				public void run() {
					updateButton(count-1, b);
				}
    		}, 1000);
    	}
    }
    
       		//gallery.setOnItemSelectedListener(new OnItemSelectedListener(){
			//	@Override
			//	public void onItemSelected(CustomAdapterView<?> parent,
			//			View view, int position, long id) {
			//		//Log.d("ItemSelected", "pos: " + position);
			//		mGalleryPosition = position;
			//	}
			//	@Override
			//	public void onNothingSelected(CustomAdapterView<?> parent) { }
       		//});
}
