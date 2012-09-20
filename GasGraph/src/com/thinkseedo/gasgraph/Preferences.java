package com.thinkseedo.gasgraph;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.util.Utilities;
import com.thinkseedo.gasgraph.util.Utilities.Entry;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class Preferences extends PreferenceActivity {
	
	void updateSummary(Preference p, String summary, String pattern, String value) {
		Lg.d("summary: " + summary);
		Lg.d("pattern: " + pattern);
		Lg.d("value: " + value);
		summary = summary.replace(pattern, value);
		Lg.d("summary2: " + summary);
		p.setSummary(summary);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        App.preferencesChanged = true;
        ListPreference pl = (ListPreference)findPreference("currency"); 
        Preference p; 
        OnPreferenceChangeListener ocl = new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Lg.d("preference change: " + preference);
				String key = preference.getKey();
				int id = Preferences.this.getResources().getIdentifier("pref_summary_" + key, "string", getPackageName());
				String summary = Preferences.this.getString(id);
				updateSummary(preference, summary, key, newValue.toString());
				return true;
			}
        };
        OnPreferenceChangeListener ocl_units = new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Lg.d("preference change: " + preference);
				String key = preference.getKey();
				int id = Preferences.this.getResources().getIdentifier("pref_summary_" + key, "string", getPackageName());
				String summary = Preferences.this.getString(id);
				String[] l = App.mUnitAbbv.get(newValue.toString());
				updateSummary(preference, summary, key, l[1].replace("CUR", App.getCurrencySymbol()));
				return true;
			}
        };
        
        Map<String, ?> map = getPreferenceManager().getSharedPreferences().getAll();
        String[] PREFS = {"year", "make", "model", "currency", "volume", "distance",}; 
        String[] UNITS = { "units_cn", "units_tr", "units_br", "units_bl"};
        //String key = "year";
        for ( String key : PREFS ) {
        	p = findPreference(key);
        	if ( p != null ) { 
				int id = Preferences.this.getResources().getIdentifier("pref_summary_" + key, 
							"string", getPackageName());
				if ( id != 0 ) {
					String summary = Preferences.this.getString(id);
					updateSummary(p, summary, key, p.getSharedPreferences().getString(key, ""));
        			p.setOnPreferenceChangeListener(ocl);
				}
        	}
        }
        for ( String key : UNITS ) {
        	p = findPreference(key);
        	if ( p != null ) { 
				int id = Preferences.this.getResources().getIdentifier("pref_summary_" + key, 
							"string", getPackageName());
				if ( id != 0 ) {
					String summary = Preferences.this.getString(id);
					String val = p.getSharedPreferences().getString(key, "blue");
					String[] l = App.mUnitAbbv.get(val);
					updateSummary(p, summary, key, l[1].replace("CUR", App.getCurrencySymbol()));
        			p.setOnPreferenceChangeListener(ocl_units);
				}
        	}
        }
        
    	Map<CharSequence,CharSequence> s = new TreeMap<CharSequence,CharSequence>();
    	//Entry<CharSequence,CharSequence> entry; 
    	//List<Entry<CharSequence, CharSequence>> valList = 
    	//		new ArrayList<Entry<CharSequence, CharSequence>>();
    	List<CharSequence> valList = new ArrayList<CharSequence>();
    	List<CharSequence> dispList = new ArrayList<CharSequence>();
    	
        Locale[] locals = Locale.getAvailableLocales();
        for ( Locale l : locals ) {
        	//Lg.d("l: " +l);
        	try {
        		Currency c = Currency.getInstance(l);
        		CharSequence val = c.getSymbol(l);
        		CharSequence disp = c.getCurrencyCode() + " : " + c.getSymbol(l);
        		s.put(val, val);
        		//Lg.d("\tdisp: " + disp);
    		    //valList.add(val);
    		    //dispList.add(disp);
        	} catch ( IllegalArgumentException e ) {
        		Lg.d("\terror: " +e);
        	}
        }
        
        //Set<CharSequence> v = s.keySet();
        //Collection<CharSequence> d = s.values();
        
        //Lg.d("size: " + s.size());
        //Lg.d("size: " + dispList.size());
        
        CharSequence[] entriesV = new CharSequence[s.size()];
        //CharSequence[] entriesD = new CharSequence[dispList.size()];
        
        //valList.toArray(entriesD);
        s.keySet().toArray(entriesV);
        //dispList.toArray(entriesD);
        
        pl.setEntries(entriesV);
        pl.setEntryValues(entriesV);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
         switch (keyCode) {
         	case KeyEvent.KEYCODE_BACK:
         	   /* Sample for handling the Menu button globally */
        		setResult(App.RESULT_PREFS);
        		finish();
        	    return true;
         }
         return false;
    } 
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Lg.d("onDestroy in Preferences");
    }

}
