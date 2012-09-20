package com.thinkseedo.gasgraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.SortedSet;

import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.database.Record;
import com.thinkseedo.gasgraph.util.CustomAdapterView;
import com.thinkseedo.gasgraph.util.CustomGallery;
import com.thinkseedo.gasgraph.util.GasRecordList;
import com.thinkseedo.gasgraph.util.ImageAdapter;
import com.thinkseedo.gasgraph.util.ImageFlowAdapter;
import com.thinkseedo.gasgraph.util.ImageHolder;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.util.CustomAdapterView.OnItemLongClickListener;
import com.thinkseedo.gasgraph.widget.FillupListWidget;
import com.thinkseedo.gasgraph.widget.ServiceListWidget;
import com.thinkseedo.gasgraph.widget.StatsWidget;
import com.thinkseedo.gasgraph.widget.TimeBarWidget;
import com.thinkseedo.gasgraph.widget.TimeBarWidget.OnChangedListener;

import taptwo.widget.CircleFlowIndicator;
import taptwo.widget.TitleFlowIndicator;
import taptwo.widget.ViewFlow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class ListExample extends Activity {

	private ViewFlow viewFlow=null;
	LocalActivityManager mLam=null;
	public static ListAppAdapter mAdapter=null;
	private TitleFlowIndicator indicator=null;
	View mRootLandscape = null;
	View mRootPortrait  = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        
        mLam = null; 
        mAdapter = new ListAppAdapter(this);
        
        int orientation = getResources().getConfiguration().orientation;
       	Lg.d("orientation start: " + orientation);
   		requestWindowFeature(Window.FEATURE_NO_TITLE);
       	if ( orientation == Configuration.ORIENTATION_PORTRAIT ) {
			setupPortrait(true);
       	} else {
			setupLandscape();
       	}
       	App.runAfterLoad = new Runnable() {
			@Override
			public void run() {
				Lg.e("onAfterLoad");
				App.reloadViews();
				App.runAfterLoad = null;
			}
       	};
		App.loadFillupsFromDB();
       	setupFirstRun();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
        Lg.e("onStart");
        if ( App.preferencesChanged ) {
			if ( mGallery != null ) {
				mGallery.reloadAll();
				App.deleteImages();
				App.flushCache();
				App.reloadViews();
			}
			TimeBarWidget tb = (TimeBarWidget) findViewById(R.id.timeBar);
			if ( tb != null ) {
				Background.downloadAllImages(tb.getDateForCurrentPosition());
			}
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(App.BROADCAST_RELOAD);
        //registerReceiver(receiver, filter);
        if ( mLam != null ) { mLam.dispatchResume(); }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	super.onSaveInstanceState(savedInstanceState);
    	Lg.e("onSave: " + savedInstanceState);
		Lg.d("vf position: " + viewFlow.getSelectedItemPosition());
		selectedView = viewFlow.getSelectedItemPosition();
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	Lg.e("onRestore: " + savedInstanceState);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Lg.d("change orient: " + newConfig.orientation + " port: " + Configuration.ORIENTATION_PORTRAIT);
        Lg.d("change orient: " + newConfig.orientation + " land: " + Configuration.ORIENTATION_LANDSCAPE);
        if ( viewFlow != null ) {
        	selectedView = viewFlow.getSelectedItemPosition();
        }
        if ( mGallery != null ) {
        	mGalleryPosition = mGallery.getSelectedItemPosition();
        }
        if ( newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ) {
        	setupPortrait(false);
        } else {
        	setupLandscape();
        }
       	App.reloadViews();
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
		boolean copied = App.getPrefs().getBoolean(App.SAMPLE_COPIED, false);
		if ( ! copied ) {
			App.copySamples();
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
	
    static final int WELCOME_DIALOG_ID = 7;
    static final int REVIEW_DIALOG_ID = 9;
    Dialog mWelcome=null;
    Dialog mReview=null;
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	AlertDialog.Builder builder;
        switch (id) {
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
            	return mWelcome;
        }
        return null;
    }

    void setupPortrait(boolean first) {
    	doUnbindService();
   		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        if ( mRootPortrait == null ) {
        	setContentView(R.layout.main_portrait);
        	mRootPortrait = findViewById(R.id.rootPortrait);
        	viewFlow  = (ViewFlow) findViewById(R.id.viewflow);
        	indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
        	indicator.setTitleProvider(mAdapter);
        	viewFlow.setFlowIndicator(indicator);
        	
        	int start_view = App.getPrefs().getInt("start_view", 3);
        	viewFlow.setAdapter(mAdapter, start_view);
        	App.getPrefs().edit().putInt("start_view", 1).commit();
        	Lg.e("set fixed");
        	viewFlow.setFixed(true);
        } else {
        	setContentView(mRootPortrait);
        }
		Lg.d("vf position: " + viewFlow.getSelectedItemPosition());
    }
    
    int selectedView = 1;
    Handler mHandler = new Handler();
    ViewFlow mGallery=null;
    TimeBarWidget landscapeTimeBar; 
    int mGalleryPosition = 0;
    void setupLandscape() {
   		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
   							 WindowManager.LayoutParams.FLAG_FULLSCREEN);
   		
   		if ( mRootLandscape == null ) {
   			setContentView(R.layout.main_flow);
   			mRootLandscape = findViewById(R.id.rootLandscape);
   			mGallery = (ViewFlow) findViewById(R.id.gallery1);
   			/* if ( App.mRecords == null ) {
   				mGallery.post(new Runnable() {
   					@Override
   					public void run() {
   						App.loadFillupsFromDB();
				    }
   				});
   			} */
       		final int[] dim = checkWinSize();
       		Background.setupSizes(dim[0], dim[1]);
			landscapeTimeBar = (TimeBarWidget) findViewById(R.id.timeBar);
			landscapeTimeBar.initTimeBar();
			CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
			indic.addBar(landscapeTimeBar);
			landscapeTimeBar.setOnChangedListener(new OnChangedListener() {
				@Override
				public void onChanged(int pos, Date since) {
					App.deleteImages();
					if ( mDownloadService != null ) {
						mDownloadService.flushCache();
						mGallery.reloadAll();
					}
					Background.downloadAllImages(since);
				}
			});
   		} else {
   			setContentView(mRootLandscape);
   		}
   			
       	if ( mGallery != null ) {
      		doBindService();
			Lg.d("DOWNLOAD ALL");
      		Background.downloadAllImages(landscapeTimeBar.getDateForCurrentPosition());
       	}
    }
    
    public int[] checkWinSize() { 
        // get the screen size
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);   
        int screenHeight = metrics.heightPixels;
        int screenWidth = metrics.widthPixels;
        return new int[] { screenWidth, screenHeight };
    } 
    
	void insertClick(int position) {
		Toast.makeText(App.getContext(), "Loading share list..", Toast.LENGTH_LONG).show();    
   		 
   		String[] types = App.getGraphTypes(); 
   		final File f = Background.getFileFor(types[position]);
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
        mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				String uri=null;
				try {
					uri=MediaStore.Images.Media.insertImage(getContentResolver(), f.getAbsolutePath(), f.getName(), null);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				//Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/png");
				intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
				startActivity(Intent.createChooser(intent , "Send options")); 
			}
        }, 500);
	}
  	// bind to service.
    private Background mDownloadService=null;
    private boolean mIsBound=false;
    ImageFlowAdapter mImageFlowAdapter=null;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDownloadService = ((Background.BoundPipe)service).getService();
			if ( mGallery != null ) {
				final ViewFlow mGal = mGallery;
				OnTouchListener otl = new OnTouchListener() {
					Runnable g = new Runnable() {
						@Override
						public void run() {
							Lg.e("running foo.");
							int pos = mGal.getSelectedItemPosition();
							insertClick(pos);
							mGal.reloadAll();
						}
			  		};
			  		long holdtime = 0;
			  		float x=(float) 0.0;
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						int   action = event.getAction();
						long current = event.getEventTime();
						float c_x = event.getX();
						switch(action) {
							case MotionEvent.ACTION_DOWN:
								holdtime = current;
								Lg.d("down: " + holdtime);
								v.postDelayed(g, 1500);
								Lg.d("x: " + c_x);
								x  =c_x;
								break;
							case MotionEvent.ACTION_MOVE:
								Lg.d("x: " + Math.abs(c_x-x));
								if ( Math.abs(c_x-x) > 50 ) {
									v.removeCallbacks(g);
								}
								break;
							case MotionEvent.ACTION_UP:
								Lg.d("up: " + (current - holdtime));
								Lg.d("x: " + Math.abs(c_x-x));
								if ( current-holdtime < 1500 ) {
									v.removeCallbacks(g);
								}
								holdtime = 0;
								break;
						}
						/* consume no events */ 
						/* continue to pass all events to gallery */
						return false;
					}
			  	};
			  	
			  	mImageFlowAdapter = new ImageFlowAdapter(getBaseContext(), mDownloadService, otl);
				mGallery.setAdapter(mImageFlowAdapter, mGalleryPosition);
				mGallery.setOnTouchListener(otl);
				mGallery.setFlowIndicator((CircleFlowIndicator)findViewById(R.id.viewflowindic));
			}
        }
        public void onServiceDisconnected(ComponentName className) {
            mDownloadService = null;
        }
    };

    void doBindService() {
        bindService(new Intent(this,
                Background.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    
    @Override
    public void onPause() {
    	super.onPause();
        //unregisterReceiver(receiver);
    	if ( mLam != null ) { mLam.dispatchPause(isFinishing());  }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Lg.e("onDestroy()");
        App.closeHelper();
        doUnbindService();
        stopService(new Intent(App.getContext(), Background.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Lg.e("req:" + requestCode + " res:" + resultCode + " " + data );
        Lg.d("ok:" + RESULT_OK + " cancel:" + RESULT_CANCELED);
        Lg.d("save:"   + App.RESULT_SAVE   + " res:" + resultCode);
        Lg.d("cancel:" + App.RESULT_CANCEL + " res:" + resultCode);
        Lg.d("delete:" + App.RESULT_DELETE + " res:" + resultCode);
        Lg.d("prefs:"  + App.RESULT_PREFS  + " res:" + resultCode);
        Lg.d("ok:"     + App.RESULT_OK     + " res:" + resultCode);
        ServiceListWidget slw = (ServiceListWidget)ListAppAdapter.viewMap.get("services");
        FillupListWidget flw = (FillupListWidget)ListAppAdapter.viewMap.get("fillups");
        if ( resultCode == App.RESULT_DELETE || 
        	 resultCode == App.RESULT_SAVE  ) {
            if ( slw != null ) { 
            	slw.updateAll(); 
            }
            if ( flw != null ) { 
            	flw.updateAll(); 
            }
        	
        } else if ( resultCode == App.RESULT_PREFS ) {
        	App.getApp().setupGlobals();
           	Lg.e("updateAll from Prefs");
           	App.reloadViews();
           	Lg.e("prefs changed: " + App.preferencesChanged);
            //if ( flw != null ) { 
            //	Lg.e("blue");
            //	flw.updateAll(); 
            //}
            //if ( slw != null ) { 
            //	Lg.e("blue");
            //	slw.updateAll(); 
            //}
        }
        if ( data != null ) { 
        	if ( data.hasExtra("return") ) {
        		int r = data.getIntExtra("return", -1);
        		Lg.d("extra:" + r);
        	}
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSettings:
                Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
                startActivityForResult(settingsActivity, 1);
                return true;
        }       
        return super.onOptionsItemSelected(item);
    }     
    
    public Dialog createWebViewDialog(String title, String filename, boolean cancelable, final Runnable r, boolean showButton) {
    	final Dialog mInfo;
    	mInfo = new Dialog(this,android.R.style.Theme_Black_NoTitleBar);
    	mInfo.setContentView(R.layout.webviewdialog);
    	mInfo.setCancelable(cancelable);
    	
    	final Button dismiss = (Button)mInfo.findViewById(R.id.buttonDialogDismiss);
    	WebView w = (WebView)mInfo.findViewById(R.id.webViewDialog);
    	
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
}
