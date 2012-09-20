package com.thinkseedo.gasgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
//import com.thinkseedo.gasgraph.GasGraphActivity.ImageAdapter.ImageHolder;
import com.thinkseedo.gasgraph.database.DatabaseHelper;
import com.thinkseedo.gasgraph.database.DatabaseHelperSample;
import com.thinkseedo.gasgraph.database.GasRecord;
//import com.thinkseedo.gasgraph.util.DrawableManager;
import com.thinkseedo.gasgraph.util.CostRecordList;
import com.thinkseedo.gasgraph.util.GasRecordList;
import com.thinkseedo.gasgraph.util.GraphGenerator;
import com.thinkseedo.gasgraph.util.ImageHolder;
import com.thinkseedo.gasgraph.util.Import;
import com.thinkseedo.gasgraph.util.Import.MissingColumnException;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.widget.FillupListWidget;
import com.thinkseedo.gasgraph.widget.ImportWidget;
import com.thinkseedo.gasgraph.widget.ServiceListWidget;
import com.thinkseedo.gasgraph.widget.StatsWidget;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

public class Background extends IntentService {
	
    private static Map<String, ImageHolder> pendingMap;
    private static Map<String, ImageHolder> errorMap;
    private static Map<String, String> urlMap;
    private static Map<String, Drawable> drawableMap;
    static {
        drawableMap = new HashMap<String, Drawable>();
        pendingMap = new HashMap<String, ImageHolder>();
        errorMap = new HashMap<String, ImageHolder>();
        urlMap = new HashMap<String, String>();
    }
    //private static ReentrantLock lock = new ReentrantLock();
    private static int mW=0;
    private static int mH=0;
    static int count=0;
    private static boolean mDeleting=false;
	
    //private OrmLiteSqliteOpenHelper mDBHelper = null;
	public Background() {
		super("Background");
	}

	public Background(String name) {
		super(name);
	    Lg.d("Constructor: " + name);
	}
	
    private final IBinder mBinder = new BoundPipe();
    public class BoundPipe extends Binder {
        public Background getService() {
            return Background.this;
        }
        //public void setupImageWithTag(String tag, ImageHolder holder) { 
        //	getDownloadService().setupImageWithTag(tag, holder);
        //}
        //public void setupImageWithTag(String tag, ImageHolder holder) { 
        //	Background.this.setupImageWithTag(tag, holder);
        //}
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    private Handler handler;
    
    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
    	handler = new Handler();
    	return super.onStartCommand(intent, flags, startId);
    }

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getStringExtra("action");
	    Lg.d("Background: action: " + action);
		if ( action.equals(App.BG_TYPE_POST_MODEL) ) { 
    		App.postToDB(App.urlModel,
    				intent.getStringExtra("uuid"), 
    				intent.getStringExtra("model"), 
			 		intent.getStringExtra("width"), 
			 		intent.getStringExtra("height"),
			 		intent.getStringExtra("android"),
			 		intent.getStringExtra("ggversion"));
		} else if ( action.equals(App.BG_TYPE_POST_ERROR) ) { 
		    App.postToDB(App.urlError,
    		  		 intent.getStringExtra("uuid"), 
    		  		 intent.getStringExtra("model"), 
    		  		 intent.getStringExtra("note"),
    		  		 intent.getStringExtra("error"),
    		  		 intent.getStringExtra("error_str"));
		} else if ( action.equals(App.BG_TYPE_POST_USER) ) { 
			Lg.d("post user data: " + action);
			boolean share = App.getPrefs().getBoolean("network", true);
			boolean samplesClear = App.getPrefs().getBoolean(App.PREF_SAMPLES_CLEAR, false);
			Lg.d("post user share : " + share);
			Lg.d("post user sample: " + samplesClear);
			if ( share && samplesClear ) {
		    	boolean b = App.postToDB(App.urlData,
    		  		 intent.getStringExtra("uuid"), 
    		  		 intent.getStringExtra("year"), 
    		  		 intent.getStringExtra("make"), 
    		  		 intent.getStringExtra("model"),
    		  		 intent.getStringExtra("date"),
    		  		 intent.getStringExtra("distance"),
    		  		 intent.getStringExtra("volume"),
    		  		 intent.getStringExtra("price"),
    		  		 intent.getStringExtra("type"));
		    	// record the fact that we've reported this data.
		    	Lg.i("user post result: " + b);
		    	if ( b ) { 
		    		updateIdAsSaved(intent.getIntExtra("id", -1), 0);
		    	}
			}
		} else if ( action.equals(App.BG_TYPE_DELETE_IMAGES ) ) { 
			Intent broadcast = new Intent();
			broadcast.setAction(App.BROADCAST_RELOAD);
			sendBroadcast(broadcast);
			mDeleting = true;
			try {
				for ( String i : App.mUnitAbbv.keySet() ) {
					String[] ls = App.mUnitAbbv.get(i);
					File f = getFileFor(ls[0]);
					if ( f.exists() ) { 
						Lg.i("deleting: " + i);
						f.delete(); 
					}
				}
			} finally {
				mDeleting = false;
			}
		} else if ( action.equals(App.BG_TYPE_COPY_SAMPLES) ) { 
			Lg.d("copy samples");
			//GasRecordList.copySampleDB();
			Import.exportSamples();
		} else if ( action.equals(App.BG_TYPE_FLUSH_CACHE) ) { 
			Lg.e("flush");
			flushCache();
		} else if ( action.equals(App.BG_TYPE_TOAST) ) { 
			String message = intent.getStringExtra("message");
			sendToast(message, true);
		} else if ( action.equals(App.BG_TYPE_EXPORT) ) { 
			Lg.e("no export yet");
		} else if ( action.equals(App.BG_TYPE_IMPORT) ) { 
		    String  filename 	= intent.getStringExtra("filename");
		    String  car 		= intent.getStringExtra("car");
		    boolean addSelected = intent.getBooleanExtra("addSelected", false);
		    startProgress();
		    Lg.d("fn: " + filename + " car: " + car );
		    if ( filename != null ) {
            	if ( !App.getPrefs().getBoolean(App.PREF_SAMPLES_CLEAR, true) ) {
            		Lg.d("loading host data before import");
            		App.getPrefs().edit().putBoolean(App.PREF_SAMPLES_CLEAR, true).commit(); 
            		setupDatabase();
            	}
				if ( ! addSelected ) {
					// then replace by removing the old data.
					Lg.e("resetting db");
            		//GasRecordList.simpleResetDB(App.getHelper());
            		((DatabaseHelper)App.getHelper()).resetTables();
				}
				if ( car == null ) {
					car = "default";
				}
				if ( car != null ) {
					String[] fields = car.split("::");
   		   			String year  = App.getPrefs().getString("year", "1997");
					String make  = App.getPrefs().getString("make", "Volvo");
   		   			String model = App.getPrefs().getString("model", "850");
   					// "default" will leave unchanged.
       				if ( fields.length == 3 ) {
       					year = fields[0];
       					make = fields[1];
       					model = fields[2];
       				} else if ( fields.length == 2 ) {
       					year = "1997";
       					make = fields[0];
       					model = fields[1];
       				}
       				App.getPrefs().edit().putString("make", make).commit();
       				App.getPrefs().edit().putString("model", model).commit();
        			App.getPrefs().edit().putString("year", year).commit();
				}
				int result=GasRecordList.IMPORT_FORMAT_ERROR;
				try { 
					result = Import.importRecords(App.getHelper(), filename);
				} catch ( FileNotFoundException e ) {
					Lg.d("file: " + e);
				} catch ( IOException e ) {
					Lg.d("io: " + e);
				} catch (MissingColumnException e) {
					sendToast("Missing headers. At a minimum you need: " + 
							  "date, distance, volume, & price", true);
				}
				App.mRecords = GasRecordList.simpleReadFromDB(0);
				App.mRecords.updateCalculatedValues();
				//App.mServices = CostRecordList.simpleReadFromDB();
				//App.mServices.updateCalculatedValues();
				GasRecordList.displayImportMessage(result);
				App.reloadViews();
				App.deleteImages();
		    }
            stopProgress();
            //Intent broadcast = new Intent();
            //broadcast.setAction(GasGraphActivity.BROADCAST_RELOAD);
            //sendBroadcast(broadcast);
		} else if ( action.equals(App.BG_TYPE_RELOAD) ) { 
			handler.post(new Runnable() {
				@Override
				public void run() {
					Lg.e("onRELOAD");
					FillupListWidget flw = (FillupListWidget)ListAppAdapter.viewMap.get("fillups");
					if ( flw != null ) {
						flw.refresh(true);
					} else {
						Lg.e("refresh fillup list");
					}
					StatsWidget sw = (StatsWidget)ListAppAdapter.viewMap.get("stats");
					if ( sw != null ) {
						sw.updateStats();
					} else {
						Lg.e("refresh stats list");
					}
				}
			});
		} else if ( action.equals(App.BG_TYPE_DOWNLOAD) ) { 
			String tag = intent.getStringExtra("tag");
			long d = intent.getLongExtra("date", -1); 
			Date since; 
			if ( d == -1 ) { 
				since = null; 
			} else { 
				since = new Date(d);
			}
			if ( tag.equals("distance") ) {
				tag = "mi";
			}
			downloadTag(tag, since);
		} else if ( action.equals(App.BG_TYPE_LOAD_SERVICES ) ) { 
			if ( App.mServices == null ) {
				App.mServices = CostRecordList.simpleReadFromDB();
				App.mServices.updateCalculatedValues();
				handler.post(new Runnable() {
					@Override
					public void run() {
						ServiceListWidget slw = (ServiceListWidget)ListAppAdapter.viewMap.get("services");
						//slw.refresh();
					}
				});
			}
		} else if ( action.equals(App.BG_TYPE_LOAD_FILLUPS ) ) { 
			if ( App.mRecords == null ) {
				App.mRecords= GasRecordList.simpleReadFromDB(0);
				App.mRecords.updateCalculatedValues();
			}
			if ( App.runAfterLoad != null ) {
				handler.post(App.runAfterLoad); 
			}
		}
	}
    void setupDatabase() {
        App.closeHelper();
        App.getHelper();
    }
    void startProgress() {
   		handler.post(new Runnable() {
			@Override
			public void run() {
				if ( App.mDialog != null ) {
					App.mDialog.dismiss();
					App.mDialog = null;
				}
				if ( ImportWidget.getStaticContext() != null ) {
					//App.mDialog = ProgressDialog.show(App.ctx, "", "Loading... Please wait...", true);
					App.mDialog = ProgressDialog.show(ImportWidget.getStaticContext(), 
							"", "Loading... Please wait...", true);
				}
			}
   		});
    }
    void stopProgress(){
   		handler.post(new Runnable() {
			@Override
			public void run() {
				if ( App.mDialog != null ) {
					App.mDialog.dismiss();
					App.mDialog = null;
				}
				App.ctx=null;
			}
   		});
    }
	
	@Override
	public void onDestroy() {
        super.onDestroy();
        App.closeHelper();
	}
	
    private void updateIdAsSaved(int id, int sleepms) {
		try {
        	Dao<GasRecord, Integer> grDao;
        	grDao = App.getDao();
			UpdateBuilder<GasRecord, Integer> ub = grDao.updateBuilder();
			ub.updateColumnValue("mSaved", true);
			if ( id > 0 ) {
				ub.where().eq("id", id);
			   	grDao.update(ub.prepare());
			}
			if ( sleepms > 0 ) {
				Thread.sleep(sleepms);
			}
		} catch (SQLException e) {
			App.postDataError("SQL", "update Id for saved", e.toString());
		} catch (InterruptedException e) {
			App.postDataError("Interrupted", "update Id for saved", e.toString());
		}
    }
    public static void downloadAllImages(Date since) {
    	if ( mW != 0 && mH != 0 ) {
    		String[] graphTypes = App.getGraphTypes();
    		downloadAllImages(graphTypes, mW, mH, since);
    	}
    }
    
    public static void downloadAllImages(String[] graph_list, int width, int height, Date since) {
        //mW = width;
        //mH = height;
    	
		for ( int i=0; i < graph_list.length; i++) {
			Lg.i("posting: " + graph_list[i]);
			Intent intent = new Intent(App.getContext(), Background.class);
			intent.putExtra("action", App.BG_TYPE_DOWNLOAD);
			intent.putExtra("tag", graph_list[i]);
			if ( since == null ) {
				intent.putExtra("date", (long)-1);
			} else {
				intent.putExtra("date", since.getTime());
			}
			App.getApp().startService(intent);
		}
    }
    
    public static void setupSizes(int width, int height) {
    	Lg.d("setup sizes");
        mH = height;
        mW = width;
    }
    
    public void flushCache() {
    	//lock.lock();
    	Lg.d("locking flushCache()");
    	try {
    		drawableMap.clear();
    		//for ( String key : drawableMap.keySet() ) {
    		//	drawableMap.remove(key);
    		//}
    	} finally {
    		//lock.unlock();
    		//Lg.d("unlocking flushCache()");
    	}
    }
    public void setupImageInWebview(String tag, ImageHolder holder) {
		Lg.d("checking for : " + tag);
    	if ( urlMap.containsKey(tag) ) {
    		Lg.d("found : " + tag);
    		holder.r.setMinimumHeight(mH);
   			holder.r.setMinimumWidth(mW);
   			holder.r.setVisibility(View.VISIBLE);
   			holder.v.setVisibility(View.GONE);
			holder.p.setVisibility(View.GONE);
   			holder.t.setVisibility(View.GONE);
			holder.a.setVisibility(View.GONE);
    		//holder.w.setVisibility(View.VISIBLE);
    		Lg.d(urlMap.get(tag));
			//String html = "<html><body><img src=\"" + urlMap.get(tag) + "\" width=\"100%\" /></body></html>";
    		DisplayMetrics dm = new DisplayMetrics();
    		//int nw = (int) Math.ceil(dm.widthPixels * (dm.densityDpi / 160.0));
    		//int nh = (int) Math.ceil(dm.heightPixels * (dm.densityDpi / 160.0));
    		//App.act.getWindowManager().getDefaultDisplay().getMetrics(dm);
			String html = "<html><head>" +
					"<meta name='viewport' content='initial-scale='" + dm.density + "' />" +
					"<style>* {margin:0;padding:0;}</style>"+
					"<script type='text/javascript'>" +
					"<!--" +
					"	function errorLoad(){" +
					"		/* window.location = 'file:///android_asset/web_error.html'; */" +
					"		window.location = 'http://www.google.com';" +
					"   }" +
					"-->" +
					"</script>" +
					"</head>" +
					"<body onLoad='errorLoad()'>" +
					"<img onerror='errorLoad()' width='"+ mW + "' height='" + mH + 
					//"<img width='"+ mW/dm.density + "' height='" + mH/dm.density + 
					"' src='" + urlMap.get(tag).replace("c", "x") + "' />" +
					"</body></html>";
    		//holder.w.loadData(html, "text/html", null);
    		//holder.w.loadUrl(urlMap.get(tag));
    		//holder.w.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    		//holder.w.getSettings().setLoadWithOverviewMode(false);
    	    //holder.w.getSettings().setSupportZoom(true);
    	    //holder.w.getSettings().setUseWideViewPort(false);
    		//holder.w.loadUrl("http://www.google.com");
    	} else {
    		Lg.d("did not find : " + tag);
    		holder.r.setMinimumHeight(mH);
   			holder.r.setMinimumWidth(mW);
   			holder.r.setVisibility(View.VISIBLE);
   			holder.v.setVisibility(View.VISIBLE);
			holder.p.setVisibility(View.VISIBLE);
   			holder.t.setVisibility(View.VISIBLE);
   			holder.t.setText("Loading " + tag);
			holder.a.setVisibility(View.GONE);
			//holder.w.setVisibility(View.GONE);
    	}
    }
    
    public void setupImageWithTag(String tag, ImageHolder holder) {
    	Drawable img = null;
    	holder.name = tag;
    	Lg.e("setupImageWithTag: " + tag);
    	//lock.lock();
    	//Lg.d("locking setupImageWithTag()");
    	try {
        	pendingMap.put(tag,  holder);
        	if (drawableMap.containsKey(tag)) {
        		Lg.d("drawable contains " + tag );
        	    img = drawableMap.get(tag);
        	} else {
        		Lg.d("drawableFromFile: " + tag );
        		img = drawableFromFile(tag);
        		if ( img == null ) { 
        			Lg.e("pending: " + tag);
        			Intent intent = new Intent(App.getContext(), Background.class);
			      	intent.putExtra("action", App.BG_TYPE_DOWNLOAD);
			      	intent.putExtra("tag", tag);
			      	App.getApp().startService(intent);
        			if ( errorMap.containsKey(tag) ) {
        				// previously confirmed error.
        				setupImageHolder(tag, holder, img, true);
        			} else {
        				setupImageHolder(tag, holder, img, false);
        			}
        			// ERROR so return
        			return;
        		}
        		// since it wasn't loaded already, and the file exists.
        		drawableMap.put(tag,  img);
    		}
        } finally {
        	//lock.unlock();
        	//Lg.d("unlocking setupImageWithTag()");
        }
    	setupImageHolder(tag, holder, img, false);
		return;
    }
    
    /* 
     * 1. waiting for data
     * 2. not enough data
     * 3. error loading data
     * 4. show image
     */
    
    void setupHolderWithWaiting(String tag, ImageHolder holder) {
   		holder.r.setMinimumHeight(mH);
   		holder.r.setMinimumWidth(mW);
   		holder.r.setVisibility(View.VISIBLE);
   		holder.v.setVisibility(View.GONE);
   		holder.a.setVisibility(View.GONE);
		holder.p.setVisibility(View.VISIBLE);
   		holder.t.setVisibility(View.VISIBLE);
   		
		String[] graphTitle = App.mUnitAbbv.get(tag);
		if ( graphTitle == null ) { return; }
		holder.t.setText(graphTitle[3]);
    }
    void setupHolderWithNoData(String tag, ImageHolder holder) {
   		holder.r.setMinimumHeight(mH);
   		holder.r.setMinimumWidth(mW);
   		holder.r.setVisibility(View.VISIBLE);
   		holder.v.setVisibility(View.GONE);
		holder.p.setVisibility(View.GONE);
		
   		holder.t.setVisibility(View.VISIBLE);
		holder.a.setVisibility(View.VISIBLE);
		holder.t.setText(R.string.error_no_data);
		
		String[] graphTitle = App.mUnitAbbv.get(tag);
		if ( graphTitle == null ) { return; }
		holder.t.setText(holder.t.getText() + graphTitle[3]);
    }
    void setupHolderWithError(String tag, ImageHolder holder) {
   		holder.r.setMinimumHeight(mH);
   		holder.r.setMinimumWidth(mW);
   		holder.r.setVisibility(View.VISIBLE);
   		holder.v.setVisibility(View.GONE);
		holder.p.setVisibility(View.GONE);
		
   		holder.t.setVisibility(View.VISIBLE);
		holder.a.setVisibility(View.VISIBLE);
		holder.t.setText(R.string.error_loading);
		
		String[] graphTitle = App.mUnitAbbv.get(tag);
		if ( graphTitle == null ) { return; }
		holder.t.setText(holder.t.getText() + graphTitle[3]);
    }
    
    void setupHolderWithImage(String tag, ImageHolder holder, Drawable img) {
   		holder.r.setMinimumHeight(mH);
   		holder.r.setMinimumWidth(mW);
   		holder.r.setVisibility(View.VISIBLE);
    	holder.v.setVisibility(View.VISIBLE);
        holder.p.setVisibility(View.GONE);
        holder.t.setVisibility(View.GONE);
   		holder.a.setVisibility(View.GONE);
        holder.v.setImageDrawable(img);
    }
 
    public void setupImageHolder(String tag, ImageHolder holder, Drawable img, boolean error) {
   		holder.r.setMinimumHeight(mH);
   		holder.r.setMinimumWidth(mW);
   		holder.r.setVisibility(View.VISIBLE);
   		
		int size = 0; 
		if ( App.mRecords != null ) {
			size = App.mRecords.getValuesFor(tag).size(); 
		} else {
			Lg.e("mRecords == null");
		}
   		if ( size == 0 ) {
   			setupHolderWithNoData(tag, holder);
   		} else if ( error ) {
   			setupHolderWithError(tag, holder);
   		} else if ( img != null ) {
   			setupHolderWithImage(tag, holder, img);
   		} else {
   			setupHolderWithWaiting(tag, holder);
   		}
    }
    
    public Drawable setupGraphView(String tag, ImageHolder holder) {
    	GraphGenerator g = new GraphGenerator();
		File f = getFileFor(tag);
		String[] graphFields = App.mUnitAbbv.get(tag);
		String url=g.getGraphURL(mW,mH,graphFields[3],tag,
						App.mRecords.getValuesFor(tag), 
						App.mRecords.getDatesFor(tag));
		Drawable img = null;
		if ( url != null ) {
			img = fetch(url, tag);
		}
		return img;
    	
    }
    
    public void downloadTag(final String tag, Date since) {
    	GraphGenerator g = new GraphGenerator();
		File f = getFileFor(tag);
		Lg.d("download: " + tag + " since: " + since);
		//Lg.d("download for " + f.toString());
		if ( ! f.exists() ) {
			Lg.d("get: " + tag);
			Lg.d("file does not exist");
			String[] graphFields = App.mUnitAbbv.get(tag);
			Lg.d("gf.length: " + graphFields.length);
			Lg.d("gf0: " + graphFields[0]);
			Lg.d("gf3: " + graphFields[3]);
			String url = null;
			if ( App.mRecords != null ) {
				Lg.e("len: " + App.mRecords.mList.size() );
				url=g.getGraphURL(mW,mH,graphFields[3],tag,
		   					App.mRecords.getValuesFor(tag, since), 
		   					App.mRecords.getDatesFor(tag, since));
			} else {
				Lg.e("mRecords == null");
			}
		   	if ( url != null ) {
		   		final Drawable img;
				img = fetch(url, tag);
				Lg.d("putting: " + tag + " with value : " + url);
				urlMap.put(tag, url);
				//lock.lock();
				//Lg.d("locking downloadTag()");
				try {
					manageMapsFor(tag, img);
				} finally {
					//lock.unlock();
					//Lg.d("unlocking downloadTag()");
				}
		   	}
        }
    }
    
    void manageMapsFor(String tag, Drawable img) {
		ImageHolder holder;
    	boolean error;
    	if ( img == null ) {
			errorMap.put(tag, null);
			error = true;
    	} else {
    		drawableMap.put(tag, img);
    		error = false;
    	}
		Lg.d("managingMapsFor: " + tag);
		if ( pendingMap.containsKey(tag) ) {
			holder = pendingMap.remove(tag);
			if ( holder.name.equals(tag) ) {
				postForHolder(tag, holder, img, error);
			}
		} else {
			Lg.e("pending does not have: " + tag);
		}
    }
    void postForHolder(final String tag, final ImageHolder holder, 
    				   final Drawable img, final boolean error) {
		holder.v.post(new Runnable() { 
			@Override
			public void run() {
				setupImageHolder(tag, holder, img, error);
			} 
		});
    }			
    public static Drawable drawableFromFile(final String tag) {
    	if ( mDeleting ) {
    		return null;
    	}
        InputStream is=null;
		try {
			is = new FileInputStream(getFileFor(tag));
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			return null; 
		}
		return Drawable.createFromStream(is, tag);
    }
    
    private InputStream inputStreamToFile(InputStream is, File output) throws FileNotFoundException {
        try {
        	// write the inputStream to a FileOutputStream
        	OutputStream out = App.getContext().openFileOutput(output.getName(), Context.MODE_WORLD_READABLE);
        	int read = 0;
        	byte[] bytes = new byte[4096];
         
        	while ((read = is.read(bytes)) != -1) {
        		out.write(bytes, 0, read);
        	}
        	is.close();
        	out.flush();
        	out.close();
         
        } catch (IOException e) {
        	Lg.d("Exception: " + e);
        }	
		//Lg.d("saved to file: " + output);
        return new FileInputStream(output);
    }
    
    public static File getFileFor(String tag) {
        return new File(App.getContext().getFilesDir(), tag + "x.png");
    }
    
    void sendToast(final String msg, boolean force) {
    	if ( count % 4 == 0 || force ) {
   			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(App.getContext(), msg, Toast.LENGTH_LONG).show();
				}
    		});
   		}
    	count += 1;
    }

    private Drawable fetch(String urlString, String tag) {
    	Drawable ret = null;
    	String msg = null;
        if ( ! App.getPrefs().getBoolean("graphbackup", false) ) {
        	ret = tryPrimary(urlString, tag);
    	   	if ( ret != null ) { return ret; }
    	   	msg = "Cannot contact chart.apis.google.com\nTrying to load from a backup source...";
    	   	sendToast(msg, false);
        }
    	
    	ret = tryBackup(urlString, tag);
    	if ( ret != null ) { return ret; }
    	
        if ( ! App.getPrefs().getBoolean("graphbackup", false) ) {
        	App.postDataError(tag, "Backup failed 1 times", "");
        }
    	msg = "Unable to load chart from any source...\n"+
    		  "Please try again later, or report this to support@thinkseedo.com";
    	sendToast(msg, true);
    	return null;
    }
    
    Drawable tryPrimary(String urlString, String tag ) {
    	URL myFileUrl;
    	String err="";
    	try {
    		myFileUrl= new URL(urlString);
		} catch (MalformedURLException e) {
			App.postDataError("MalformedURL", "failed url: " + urlString, e.toString());
			return null;
		}
        File outFile = getFileFor(tag);
        HttpURLConnection conn=null; 
        int retry=0;
        // try three times and then give up.
        while ( retry < 3 ) {
        	try {
        		conn = (HttpURLConnection)myFileUrl.openConnection();
        		conn.setDoInput(true); 
				conn.connect();
        		InputStream is = conn.getInputStream();
        		is = inputStreamToFile(is, outFile);
        		Drawable drawable = Drawable.createFromStream(is, "src");
        		try {
        			is.close();
        			conn.disconnect();
        		} catch ( Exception e){
        			Lg.d("exception on close");
        		}
        		return drawable;
			} catch (IOException e) {
				try {
					Lg.e("sleeping 500ms retry: " + retry);
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					err = e1.toString();
				}
			}
        	retry += 1;
        }
        App.postDataError("Loading image failed 3 times", tag, err);
    	return null;
    }
    Drawable tryBackup(String urlString, String tag ) {
    	Lg.e("using backup server for " + tag + " : " + App.urlBackup);
    	return App.getImageFromPostToURL(App.urlBackup, "url", urlString);
    }
}
