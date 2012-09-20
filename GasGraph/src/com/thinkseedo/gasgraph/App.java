package com.thinkseedo.gasgraph;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.thinkseedo.gasgraph.database.CostRecord;
import com.thinkseedo.gasgraph.database.DatabaseHelper;
import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.util.CostRecordList;
import com.thinkseedo.gasgraph.util.GasRecordList;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.util.RecordList;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class App extends Application {
	private static Context mContext;
	public static Integer VERSION_MAX = 7;
	public static String urlError = null;
	public static String urlModel = null;
	public static String urlData  = null; 
    public static String urlBackup = null; 
	public static Context ctx=null;
	public static Runnable runAfterLoad = null;
	public static ProgressDialog mDialog=null;
	
	public static String currency;
	public static String distance;
	public static String volume;
	public static String units_cn;
	public static String units_bl;
	public static String units_br;
	public static String units_tr;
	public static String color;
	
	public static String PREF_PLEASE_REVIEW = "reviewShown";
	public static String PREF_WELCOME_SHOWN = "welcomeShown13";
	public static String PREF_SAMPLE_DATA   = "sampledata";
	public static String PREF_SAMPLES_CLEAR = "clearsampledata";
	public static String PREF_START_ON_ADD  = "startOnAdd";
	public static String PREF_START_ON_LIST = "startOnList";
	public static String PREF_FIRST_RUN 	= "firstrun";
	public static String PREF_FIRST_DATE 	= "firstdate";
	public static String PREF_MARKET_REVIEW = "marketReview";
	
	public static String SAMPLE_COPIED = "sample_copied13";
	
	public static String MARKET_AMAZON 	= "amazonmarket";
	public static String MARKET_ANDROID = "androidmarket";
	public static String MARKET;
	
	public static String BG_TYPE_POST_USER = "user";
	public static String BG_TYPE_TOAST = "toast";
	public static String BG_TYPE_IMPORT = "import";
	public static String BG_TYPE_EXPORT= "export";
	public static String BG_TYPE_DELETE_IMAGES = "delete";
	public static String BG_TYPE_POST_MODEL = "model";
	public static String BG_TYPE_POST_ERROR = "error";
	public static String BG_TYPE_UPDATE    = "update";
	public static String BG_TYPE_DOWNLOAD  = "download";
	public static String BG_TYPE_FLUSH_CACHE = "flush";
	public static String BG_TYPE_COPY_SAMPLES = "copysamples";
	public static String BG_TYPE_LOAD_FILLUPS = "loadfillups";
	public static String BG_TYPE_LOAD_SERVICES = "loadservices";
	public static String BG_TYPE_RELOAD = "reload";
	
    public static String BROADCAST_RELOAD = "com.thinkseedo.gasgraph.RELOAD";
    
    public static int RESULT_OK 	= Activity.RESULT_FIRST_USER + 1;
    public static int RESULT_DELETE = Activity.RESULT_FIRST_USER + 2;
    public static int RESULT_SAVE 	= Activity.RESULT_FIRST_USER + 3;
    public static int RESULT_CANCEL = Activity.RESULT_FIRST_USER + 4;
    public static int RESULT_PREFS  = Activity.RESULT_FIRST_USER + 5;
    
	public static Map<String,  String[]> mUnitAbbv;
	public static Map<Integer, String[]> iUnitAbbv;
	public static boolean mWaitDialog = false;
	public static int actCount = 0;
    public static GasRecordList 	mRecords;
    public static CostRecordList	mServices;
    //public static List<GasRecord>	mList;
    public static String mUUID;
    public static boolean preferencesChanged=false;
    
    public static final Map<String, SortedSet<String>> mGraphSets = new HashMap<String, SortedSet<String>>();
    private static void setupSetWith(SortedSet<String> s, String[] list) {
    	for ( int i=0; i < list.length; i++) {
    		if ( list[i].equals("mi") || 
    			 list[i].equals("distance") || 
    			 list[i].equals("km") ) {
    			// NOTE: don't graph odometer.
    			continue;
    		}
    		s.add(list[i]);
    	}
    	return;
    }
    public static void setupGraphSets() {
    	String[] default_units = { 
    			App.getPrefs().getString("units_cn", "mile_gallon"),
    			App.getPrefs().getString("units_br", "currency_mile"),
    			App.getPrefs().getString("units_bl", "mi"),
    			App.getPrefs().getString("units_tr", "totalprice") 
    	};
        //String[] migal = { "totalprice", "mile_gallon", "price", "mi_diff", "gal", "gallon_100mile", "currency_mile" };
        String[] migal = { "totalprice", "mile_gallon", "price", "mi_diff", "gal", "gallon_100mile", "currency_mile",
        				//	"liter_100mile", "mile_liter", 
        				//	"liter_100km", "L", "km_diff", "km_liter", "currency_km", "liter_km", "liter_10km",
        					};
        String[] miL   = { "totalprice", "liter_100mile", "price", "mi_diff", "L", "mile_liter", "currency_mile" }; 
        String[] kmgal = { "totalprice", "price", "gal", "currency_km", "km_diff"  };
        String[] kmL   = { "totalprice", "liter_100km", "price", "L", "km_diff", "km_liter", "currency_km", "liter_km", "liter_10km" };
    	SortedSet<String> s = null;
        
    	s = new TreeSet<String>();
    	setupSetWith(s, migal);
    	setupSetWith(s, default_units);
        mGraphSets.put("migal",s);
        
    	s = new TreeSet<String>();
    	setupSetWith(s, miL);
    	setupSetWith(s, default_units);
        mGraphSets.put("miL",  s); 
        
    	s = new TreeSet<String>();
    	setupSetWith(s, kmgal);
    	setupSetWith(s, default_units);
        mGraphSets.put("kmgal", s); 
        
    	s = new TreeSet<String>();
    	setupSetWith(s, kmL);
    	setupSetWith(s, default_units);
        mGraphSets.put("kmL",  s); 
    }

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		setupGraphSets();
		
		// read the unit mappings from resources
		String[] unit_map = getResources().getStringArray(R.array.unit_map);
		mUnitAbbv = new TreeMap<String,String[]>();
		iUnitAbbv = new TreeMap<Integer,String[]>();
		for (int i=0; i < unit_map.length ; i++ ) {
			String[] l = unit_map[i].split("\\|",-1);
			mUnitAbbv.put(l[0], l);
			iUnitAbbv.put(i, l);
		}
		// setup global values 
		mUUID = App.getPrefWithDefault("uuid", "new-uuid");
		setupMarket();
		setupGlobals();
		setupGraphSets();
	}
	void setupGlobals() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        App.currency = prefs.getString("currency", "dollar");
        App.distance = prefs.getString("distance", "mi");
        App.volume   = prefs.getString("volume",   "gal");
        App.units_cn = prefs.getString("units_cn", "mile_gallon");
        App.units_br = prefs.getString("units_br", "currency_mile");
        App.units_bl = prefs.getString("units_bl", "mi");
        App.units_tr = prefs.getString("units_tr", "totalprice");
        App.color    = prefs.getString("color", "3DCA05");
	}
	void setupMarket() {
		
        String versionName = "1";
        try {
        	versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
        	e.printStackTrace();
        }
        char c = versionName.charAt(versionName.length()-1);
        if ( c == 'a' ) {
        	MARKET = MARKET_AMAZON;
        } else {
        	MARKET = MARKET_ANDROID;
        }
	}
	public static String readFileAsString(String filename) {
		FileInputStream is;
		try {
			is = new FileInputStream(filename);
			return readStreamAsString(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	public static String readFileAsString(int id) {
		InputStream is = App.getApp().getResources().openRawResource(id);
		return readStreamAsString(is);
	}
	public static String readStreamAsString(InputStream is) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			int read = 0;
			int total = 0;
			char[] chars = new char[4096];
			while ((read = reader.read(chars)) != -1) {
				total += read;
				//sb.append(chars);
				sb.append(chars, 0, read);
			}
			Lg.d("total read: " + total);
			reader.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return sb.toString();
	}
    public static boolean setSampleDataFlag(boolean flag ){
        SharedPreferences prefs = App.getPrefs();
        prefs.edit().putBoolean(App.PREF_SAMPLES_CLEAR, !flag).commit();
        return flag;
    }
	static double g2l(double g) { return g*3.7854117; }
	static double l2g(double l) { return l/3.7854117; }
	static double m2k(double m) { return m*1.609344; }
	static double k2m(double k) { return k/1.609344; }
	static Double adjustVD(Double d, String toV, String toD) {
		if ( d == null ) return null;
		Double ret=null;
		ret = adjustV(d, toV);
		
		if ( distance.equals("mi") && toD.equals("km") ) {
			ret = k2m(ret);	
	 	} else if ( distance.equals("km") && toD.equals("mi") ) {
			ret = m2k(ret);	
	 	}
		return ret;
	}
	static Double adjustDV(Double d, String toD, String toV) {
		if ( d == null ) return null;
		Double ret=null;
		if ( distance.equals("mi") ) {
			ret = adjustD(d, toD);
	 	} else if ( distance.equals("km") ) {
			ret = adjustD(d, toD);
	 	}
		// reversed b/c v is in denominator
		if ( volume.equals("gal") && toV.equals("L") ) {
			ret = l2g(ret);	
	 	} else if ( volume.equals("L") && toV.equals("gal") ) {
			ret = g2l(ret);	
	 	}
		return ret;
	}
	static Double adjustD(Integer i, String to) {
		return adjustD(i.doubleValue(), to);
	}
	static Double adjustD(Double d, String to) {
		if ( d == null ) return null;
		double ret=d;
		if ( distance.equals("km") && to.equals("mi") ) {
			return k2m(d);
		} else if ( distance.equals("mi") && to.equals("km") ) {
			return m2k(d);
		}
		return ret;
	}
	static Double adjustV(Double d, String to) {
		if ( d == null ) return null;
		double ret=0;
		if ( volume.equals("gal") && to.equals("gal") ) {
			return d;
		} else if ( volume.equals("L") && to.equals("L") ) {
			return d;
		} else if ( volume.equals("gal") && to.equals("L") ) {
			return g2l(d);
		} else if ( volume.equals("L") && to.equals("gal") ) {
			return l2g(d);
		}
		return ret;
	}
	
	public static String formatList(String s, GasRecord gr){
		String[] l=null;
		if ( mUnitAbbv.containsKey(s) ) {
			l = mUnitAbbv.get(s);
		} else {
			if ( s.equals("volume") ) {
				l = new String[] { App.volume , "", "0"};
			}
			if ( s.equals("distance") ) {
				l = new String[] { App.distance, "", "0" };
			}
		}
		//if ( s.equals("mi") || s.equals("km") )  {
		//	String od = App.formatDouble(getVal(s, gr), l[2]);
		//	String trip = App.formatDouble(getVal(s+"_diff", gr), l[2]); 
		//	return od + " (" + trip + ")";
		//} else {
		//return App.formatDouble(getVal(s, gr), l[2]);
		return App.formatDouble(getVal(s, gr), l[2], s);
		//}
	}
	public static Double getVal(String s, CostRecord gr) {
		return 0.0;
	}
	
    public static void restartFirstActivity() {
    	Intent i = App.getApp().getBaseContext()
    				.getPackageManager()
    				.getLaunchIntentForPackage(
    					App.getApp().getBaseContext().getPackageName()
    				);
    	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
    	App.getApp().startActivity(i);
    }
	
	public static Double getVal(String s, GasRecord gr) {
		String[] l=null;
		if ( mUnitAbbv.containsKey(s) ) {
			l = mUnitAbbv.get(s);
		} else {
			if ( s.equals("volume") ) {
				l = new String[] { App.volume };
			}
			if ( s.equals("distance") ) {
				l = new String[] { App.distance };
			}
		}
		Double ret=null;
		if ( l[0].equals("totalprice") ) {
			ret = (gr.mPrice*gr.mVolume);
		} else if ( l[0].equals("price") ) {
			ret = (gr.mPrice);
		} else if ( l[0].equals("mi") ) {
			ret = App.adjustD(gr.mDistance, "mi");
		} else if ( l[0].equals("mi_diff") ) {
			ret = App.adjustD(gr.mDD, "mi");
		} else if ( l[0].equals("mile_gallon") ) {
			ret = adjustDV(gr.mMPG, "mi", "gal");
		} else if ( l[0].equals("gallon_100mile") ) {
			if ( gr.mMPG != null ) 
				ret = adjustVD(100.0/gr.mMPG, "gal", "mi");
		} else if ( l[0].equals("liter_100mile") ) {
			if ( gr.mMPG != null ) 
				ret = adjustVD(100.0/gr.mMPG, "L", "mi");
		} else if ( l[0].equals("currency_mile") ) {
			if ( gr.mPPM != null ) {
				if ( distance.equals("mi") ) { 
					ret = gr.mPPM;
				} else if ( distance.equals("km") ) { 
					ret = k2m(gr.mPPM);
			} }
		} else if ( l[0].equals("currency_km") ) {
			if ( gr.mPPM != null ) {
				if ( distance.equals("km") ) { 
					ret = gr.mPPM;
				} else if ( distance.equals("mi") ) { 
					ret = m2k(gr.mPPM);
			} }
		} else if ( l[0].equals("L") ) {
			ret = adjustV(gr.mVolume, "L");
		} else if ( l[0].equals("gal") ) {
			ret = adjustV(gr.mVolume, "gal");
		} else if ( l[0].equals("km") ) {
			ret = adjustD(gr.mDistance, "km");
		} else if ( l[0].equals("km_diff") ) {
			ret = App.adjustD(gr.mDD, "km");
		} else if ( l[0].equals("mile_liter") ) {
			ret = adjustDV(gr.mMPG, "mi", "L");
		} else if ( l[0].equals("liter_km") ) {
			if ( gr.mMPG != null ) 
				ret = adjustVD(1.0/gr.mMPG, "L", "km");
		} else if ( l[0].equals("liter_10km") ) {
			if ( gr.mMPG != null ) 
				ret = adjustVD(10.0/gr.mMPG, "L", "km");
		} else if ( l[0].equals("liter_100km") ) {
			if ( gr.mMPG != null ) 
				ret = adjustVD(100.0/gr.mMPG, "L", "km");
		} else if ( l[0].equals("km_liter") ) {
			ret = adjustDV(gr.mMPG, "km", "L");
		}
		return ret;
	}
	
	//public static void setupRecordList(List<GasRecord> mlist) {
	//	mRecords = new GasRecordList(mlist);
	//}
	
	public static App getApp() {
		return (App)mContext;
	}

	public static Context getContext() {
		return mContext;
	}
	
    public static String formatInteger(Double double1) {
    	if ( double1 == null ) { return ""; } 
		return String.format(Locale.getDefault(), "%.0f", double1);
    }
    public static String formatMPG(Double d) {
    	if ( d == null || d.equals(0.0) ) return "";
    	return String.format(Locale.getDefault(), "%03.2f", d);
    }
    public static String getCurrencySymbol() {
    	String symbol_pre=null;
    	if ( currency.equals("dollar") ) {
    		return "$";
    		//symbol_pre = "$";
    	} else if ( currency.equals("pound") ) {
    		symbol_pre = "\u00A3";
    	} else if ( currency.equals("euro") ) {
    		symbol_pre = "\u20AC";
    	} else if ( currency.equals("rupee") ) {
    		symbol_pre = "\u20B9";
    	} else if ( currency.equals("yen") ) {
    		symbol_pre = "\u00A5";
    	} else {
    		symbol_pre = "$";
    	}
    	if ( App.currency != null ) {
    		if ( App.currency.length() > 6 ) {
    			return App.currency.substring(6);
    		} else {
    			return App.currency;
    		}
    	} else {
    		return "$";
    	}
    	//return symbol_pre;
    }
    
    public static String formatPrice(Double d){
    	if ( d == null || d.equals(0.0) ) return "";
    	String symbol_pre = null;
    	symbol_pre = getCurrencySymbol();
    	return String.format(Locale.getDefault(), symbol_pre + "%04.2f", d);
    }
    public static String formatDouble(Double d, String precision, String s) {
    	if ( s.equals("mi_diff") || s.equals("km_diff") ) {
    		String x = formatDouble(d, precision);
    		if ( !x.equals("") ) {
    			return " (+" + formatDouble(d, precision) + ")";
    		}
    		return "";
    	} else if ( s.equals("price") || s.equals("totalprice") ||
    			    s.equals("currency_mile") || s.equals("currency_km") ) {
    		return formatPrice(d);
    	} else {
    		return formatDouble(d, precision);
    	}
    }
    
    public static String formatDouble(Double d, String precision) {
    	if ( d == null ) return "";
    	String fmt = "%." + precision + "f";
    	return String.format(Locale.getDefault(), fmt, d);
    }
    
    public static String formatDouble(Double d, boolean isPrice) {
    	if ( d == null ) return "";
    	if ( isPrice ){
    		return String.format(Locale.getDefault(), "%04.2f", d);
    	} else {
    		return String.format(Locale.getDefault(), "%05.3f", d);
    	}
    }
    
    public static Date parseDate(String sDate) {
    	if ( sDate == null ) {
    		return Calendar.getInstance().getTime();
    	}
    	String format = App.getContext().getResources().getString(R.string.time_format_export);
    	SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		return sdf.parse(sDate, new ParsePosition(0));
    	//DateFormat df = DateFormat.getDateTimeInstance(); // Locale.getDefault());
    	
		//return new Date(Long.parseLong(sDate)); // sdf.parse(sDate, new ParsePosition(0));
    }
    public final static int DATE_DISPLAY = 1;
    public final static int DATE_STORAGE = 2;
    public final static int DATE_FILENAME = 3;
    public final static int DATE_DISPLAY_LONG = 4;
    
    public static String formatDate(Date d, int type){
    	Date x=null;
    	if ( d == null ) {
    		x = Calendar.getInstance().getTime();
    	} else {
    		x = d;
    	}
    	switch (type ) {
    		case DATE_DISPLAY_LONG:
    			DateFormat df2 = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
    			return df2.format(x);
    		case DATE_DISPLAY:
    			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
    			return df.format(x);
    		case DATE_STORAGE:
    			String format = App.getContext().getResources().getString(R.string.time_format_export);
    			SimpleDateFormat sdf;
    			sdf = new SimpleDateFormat(format, Locale.US);
    			return sdf.format(x);
    		case DATE_FILENAME:
    			sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
    			return sdf.format(x);
    	}
    	return null;
    }
    public static String formatDate(Date d, boolean forDisplay){
    	if ( forDisplay ) { 
    		//String format=null;
    		//format = App.getContext().getResources().getString(R.string.time_format_display);
    		//SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
    		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
    		return df.format(d);
    	} else {
    		String format = App.getContext().getResources().getString(R.string.time_format_export);
    		SimpleDateFormat sdf;
    		sdf = new SimpleDateFormat(format, Locale.US);
    		return sdf.format(d);
    		// return (new Long(d.getTime())).toString();
    	}
    }
    public static void deleteCarData(String carname) {
    	String filename = App.getContext().getResources().getString(R.string.filename);
        App.getContext().deleteFile(filename);
    }
    
    public static void appendFileLines( String file, ArrayList<String> lines) throws IOException {
    	DataOutputStream buf = new DataOutputStream(App.getContext().openFileOutput(file, Context.MODE_APPEND));
        Iterator<String> iter = lines.iterator();
        while (iter.hasNext()) { 
        	String line = iter.next();
        	buf.writeBytes(line + "\n"); 
        }
    	buf.close();
    	return;
    }
    public static void setListener(Activity a, int id, OnClickListener l) {
    	View v = (View)a.findViewById(id);
    	v.setOnClickListener(l);
    }
    public static void setListener(View v, int id, OnClickListener l) {
    	View i = (View)v.findViewById(id);
    	i.setOnClickListener(l);
    }
    public static String getText(Activity a, int id) {
    	TextView t = (TextView)(a.findViewById(id));
    	return t.getText().toString();
    }
    public static String getText(View v, int id) {
    	TextView t = (TextView)(v.findViewById(id));
    	return t.getText().toString();
    }
    public static void setText(View v, int id, String s) {
    	TextView t = (TextView)(v.findViewById(id));
    	t.setText(s);
    }
    public static void setText(Activity a, int id, String s) {
    	TextView t = (TextView)(a.findViewById(id));
    	t.setText(s);
    }

    public static HttpClient createHttpClient()
    {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);

        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

        return new DefaultHttpClient(conMgr, params);
    }
    	
    public static Drawable getImageFromPostToURL(String url, String... key_vals) { // GasRecord gr) {
    	HttpClient httpclient = createHttpClient();
    	// the first argument should be the URL.
	    HttpPost httppost = new HttpPost(url);
	    Drawable ret=null;
	
	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(key_vals.length/2);
	        for ( int i=0; i < key_vals.length; i+=2 ) {
	        	nameValuePairs.add(new BasicNameValuePair(key_vals[i], key_vals[i+1])); 
	        }
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        byte[] content = EntityUtils.toByteArray(response.getEntity());
        	ret = Drawable.createFromStream(new ByteArrayInputStream(content), "src");
	        return ret;
	        
	    } catch (ClientProtocolException e) {
	        Lg.d("exception: CPE " + e);
	    } catch (IOException e) {
	        Lg.d("exception: " + e);
	    }
	    return ret;
    }
 
    public static boolean postToDB(String... args) { 
        if ( args[0] == null ) {
            return false;
        }
    	HttpClient httpclient = createHttpClient();
    	// the first argument should be the URL.
	    HttpPost httppost = new HttpPost(args[0]);
	
	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(args.length+1);
	        for ( int i=1; i < args.length; i++ ) {
	        	nameValuePairs.add(new BasicNameValuePair("entry." + (i-1) + ".single", args[i])); 
	        }
	        nameValuePairs.add(new BasicNameValuePair("pageNumber", "0")); // type
	        nameValuePairs.add(new BasicNameValuePair("backupCache", "")); // type
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	
	        String ok_string = "Your response has been recorded";
	        
	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        String body = EntityUtils.toString(response.getEntity());
	        boolean b = body.contains(ok_string);
	        return b;
	        
	    } catch (ClientProtocolException e) {
	        Lg.d("exception: CPE " + e);
	    } catch (IOException e) {
	        Lg.d("exception: " + e);
	    }
	    return false;
    }
    
    public static int[] checkWinSize(Activity a) { 
        // get the screen size
        DisplayMetrics metrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(metrics);   
        int screenHeight = metrics.heightPixels;
        int screenWidth = metrics.widthPixels;
        return new int[] { screenWidth, screenHeight };
    } 
    
    public static void flushCache() {
    	Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_FLUSH_CACHE);
		getApp().startService(intent);
    }
    public static void loadServicesFromDB() {
    	Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_LOAD_SERVICES);
		getApp().startService(intent);
    }
    public static void reloadViews() {
    	Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_RELOAD);
		getApp().startService(intent);
    }
    public static void loadFillupsFromDB() {
    	Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_LOAD_FILLUPS);
		getApp().startService(intent);
    }
    public static void showToast(String message) {
    	Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_TOAST);
		intent.putExtra("message", message);
		getApp().startService(intent);
    }
    
    public static void deleteImages() {
    	Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_DELETE_IMAGES);
		getApp().startService(intent);
    }
    
    public static void copySamples() {
    	Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_COPY_SAMPLES);
		getApp().startService(intent);
    }
    
    public static void downloadAllImages(String[] graph_list, int width, int height) {
		for ( int i=0; i < graph_list.length; i++) {
			Lg.i("posting: " + graph_list[i]);
			Intent intent = new Intent(App.getContext(), Background.class);
			intent.putExtra("tag", graph_list[i]);
			intent.putExtra("action", App.BG_TYPE_DOWNLOAD);
			App.getApp().startService(intent);
		}
    }
    public static void importFile(String filename, String car, boolean addSelected) {
		// make available for progress dialog
		Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_IMPORT);
		intent.putExtra("addSelected", addSelected);
		intent.putExtra("filename", filename);
		intent.putExtra("car", car);
		getApp().startService(intent);
    }
    public static void postDataError(String e, String note, String trace) {
    	Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_POST_ERROR);
		intent.putExtra("uuid", App.mUUID);
		intent.putExtra("model", App.getModel());
		intent.putExtra("note", note);
		intent.putExtra("error", e);
		intent.putExtra("error_str", trace);
		getApp().startService(intent);
    }
    public static String getModel() {
    	return Build.BRAND + " " + Build.MODEL + " " + Build.DEVICE + " " + Build.CPU_ABI;
    }
    
    @SuppressWarnings("unused")
	public static void postDataFirst(Activity a) {
    	if ( Lg.DEBUG == true ) {
    		return;
    	}
    	String model   = getModel();
    	String android = Build.VERSION.RELEASE;
    	int[]  display_list = App.checkWinSize(a);
    	String width  = Integer.toString(display_list[0]);
    	String height = Integer.toString(display_list[1]);
    	String ggversion = "";
		try {
			ggversion = a.getPackageManager().getPackageInfo(a.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			ggversion = "0";
		}
		Intent intent = new Intent(App.getContext(), Background.class);
		intent.putExtra("action", App.BG_TYPE_POST_MODEL);
		intent.putExtra("model", model); 
		intent.putExtra("uuid", App.mUUID);
		intent.putExtra("model", model);
		intent.putExtra("width", width);
		intent.putExtra("height", height);
		intent.putExtra("android", android);
		intent.putExtra("ggversion", ggversion);
		getApp().startService(intent);
    }
    
    public static String[] getGraphTypes(){
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
    
    static int count = 0;
    static OrmLiteSqliteOpenHelper mDBHelper;
    //public static OrmLiteSqliteOpenHelper getHelper() {
    public static OrmLiteSqliteOpenHelper getHelper() {
    	if ( count == 0 ) {
    		boolean samplesClear = App.getPrefs().getBoolean(App.PREF_SAMPLES_CLEAR, true);
    		//if ( !samplesClear ) { 
    		//	Lg.e("getHelper for Samples");
    			//mDBHelper = OpenHelperManager.getHelper(App.getContext(), DatabaseHelperSample.class);
    		//	mDBHelper = DatabaseHelperSample.getHelper(App.getContext());
    		//} else {
    			Lg.e("getHelper for Default");
    			//mDBHelper = OpenHelperManager.getHelper(App.getContext(), DatabaseHelper.class);
    			mDBHelper = DatabaseHelper.getHelper(App.getContext());
    		//}
    		count += 1;
    	}
        return mDBHelper ;
    }
    
    public static Dao<GasRecord, Integer> getDao() {
   		boolean samplesClear = App.getPrefs().getBoolean(App.PREF_SAMPLES_CLEAR, true);
   		Dao<GasRecord, Integer> grDao=null;
		try {
			//if ( !samplesClear ) { 
			//	grDao = ((DatabaseHelperSample)getHelper()).getGasRecordDao();
			//} else {
				grDao = ((DatabaseHelper)getHelper()).getGasRecordDao();
			//}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return grDao;
    }
    
    public static Dao<CostRecord, Integer> getCostDao() {
   		boolean samplesClear = App.getPrefs().getBoolean(App.PREF_SAMPLES_CLEAR, true);
   		Dao<CostRecord, Integer> grDao=null;
		try {
			grDao = ((DatabaseHelper)getHelper()).getCostRecordDao();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return grDao;
    }
    
    public static void closeHelper() {
        // release the helper before exiting.
        if (mDBHelper != null) {
            //OpenHelperManager.releaseHelper();
        	mDBHelper.close();
            mDBHelper = null;
            count = 0;
        }
    }

    public static void postDataError_old(String error, String note, String trace) {
    	postToDB(App.urlError,
    			 App.mUUID,
    			 error,
    			 note,
    			 trace);
    }
    
    public static String getVehicleSpec() {
        String y  = App.getPrefs().getString("year", "1997");
        String mk = App.getPrefs().getString("make", "Volvo");
        String mo = App.getPrefs().getString("model","");
        String ret;
        if ( !mo.equals("") ) {
        	ret = y + " " + mk + " " + mo;
        } else {
        	ret = y + " " + mk;
        }
    	return ret;
    }
    
    
    public static Date getCurrentDate(){
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        return new Date(mYear, mMonth, mDay, mHour, mMinute);
    }
    
    static String setPref(String key, String value) { 
    	Editor e = App.editPrefs();
    	e.putString(key,  value);
    	e.commit();
    	return value;
    }
    static String getPref(String key, String value) { 
    	return getPrefWithDefault(key,value);
    }
    
    static String getPrefWithDefault(String key, String value) { 
		SharedPreferences p = App.getPrefs();
		String val = p.getString(key, null); 
		if ( val == null && value.equals("new-uuid") ) { 
			val = UUID.randomUUID().toString();
			p.edit().putString(key, val).commit();
			return val;
		} else if ( val == null ){
			p.edit().putString(key, value).commit();
			return value;
		}
    	return val;
    }
    
    public static SharedPreferences getPrefs() { 
		return PreferenceManager.getDefaultSharedPreferences(App.getContext()); 
    }
	static Editor editPrefs() {
		return getPrefs().edit();
	}
}
