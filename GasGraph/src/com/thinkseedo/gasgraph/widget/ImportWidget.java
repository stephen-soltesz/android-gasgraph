package com.thinkseedo.gasgraph.widget;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.Background;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.R.id;
import com.thinkseedo.gasgraph.R.layout;
import com.thinkseedo.gasgraph.database.DatabaseHelper;
import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.util.GasRecordList;
import com.thinkseedo.gasgraph.util.Import;
import com.thinkseedo.gasgraph.util.Import.MissingColumnException;
import com.thinkseedo.gasgraph.util.Import.RecordBlocks;
import com.thinkseedo.gasgraph.util.Import.RecordLine;
import com.thinkseedo.gasgraph.util.Import.RecordRow;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.util.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class ImportWidget extends LinearLayout {
	static {
		Lg.d("starting ImportWidget");
	}
	
	static Context mContext = null;
	
	public ImportWidget(Context context) {
		super(context);
		mContext = context;
	}
	public ImportWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	public static Context getStaticContext() {
		return mContext;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		//final Activity act = (Activity)getContext();
		//((Activity)getContext()).getLayoutInflater().inflate(R.layout.notes_widget, this);
		LayoutInflater mInflater = LayoutInflater.from(getContext());
		mInflater.inflate(R.layout.widget_import, this);
		
        App.setListener(this, R.id.buttonImport, mImportListener);
        Button b = (Button)findViewById(R.id.buttonImport);
        //Drawable d = b.getBackground();
        //StateListDrawable sd = (StateListDrawable)d;
        //sd.setDither(true);
        //d.setDither(true);
        //Lg.d("button class: " + d.getClass());
        //setupSpinnerFiles();
        
    	setOnItemSelectedListener(R.id.spinnerFilenames, new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String filename = (String)parent.getItemAtPosition(position);
				Lg.d("filenames selected: " + filename);
				setupCarList(filename);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
    	});
    	
    	setOnClickListener(R.id.checkBoxExtra, new OnClickListener() {
			@Override
			public void onClick(View v) {
				Lg.d("checkbox: click");
				openWarning();
				setupSpinnerFiles();
			}
    	});
	}
    
    void openWarning() {
        if ( App.getPrefs().getBoolean("showcsvwarning", true) ) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        	builder.setMessage("GasGraph will scan for *.csv files out-side of GasGraph's storage.\n\n" +
        				   "Only a few CSV formats are supported.\n\n" +
        				   "If you have a CSV file with fill up history that GasGraph " +
        				   "does not support, please send me an example by e-mail to " + 
        				   "see if I can help.")
           	       .setCancelable(false)
           	       .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
           	    	   public void onClick(DialogInterface dialog, int id) {
           	        	   App.getPrefs().edit().putBoolean("showcsvwarning", false).commit(); 
           	    	   }
           	       });
        	builder.create().show();
        }
    	return;
    }
    public void updateAll() {
    	setupSpinnerFiles();
    }
    
    void setupSpinnerFiles() {
    	CheckBox b = (CheckBox)findViewById(R.id.checkBoxExtra);
    	boolean scanExtra = b.isChecked();
    	String[] fileList=null;
    	if ( scanExtra ) {
    		Lg.d("extra files");
    		fileList = getFileList(".*.csv$", "GasGraph", "FuelLog");
    	} else {
    		Lg.d("just GG files");
    		fileList = getFileList(".*.csv$", "GasGraph");
    	}
    	setSpinnerList(R.id.spinnerFilenames, fileList);
    	
    	String filename = getSelectedItem(R.id.spinnerFilenames);
    	setupCarList(filename);
    }
    
    void setOnClickListener(int id, OnClickListener l) {
    	View v = (View)findViewById(id);
    	v.setOnClickListener(l);
    }
    
    void setupCarList(String filename) {
    	final File rootdir = Environment.getExternalStorageDirectory();
    	Lg.d("getting carnames from file: " + filename);
    	if ( filename != null ) {
    		String[] carList = readVehiclesFromFile((new File(rootdir, filename)).toString());
    		if ( carList != null ) {
    			for ( String car : carList ) {
    				Lg.d("car: " + car);
    			}
    		}
    		setSpinnerList(R.id.spinnerCarnames, carList);
    	} else {
    		setSpinnerList(R.id.spinnerCarnames, null);
    	}
    }
    
    void setOnItemSelectedListener(int id, OnItemSelectedListener l) {
        Spinner spinner = (Spinner)findViewById(id);
        spinner.setOnItemSelectedListener(l);
    }
    
    String getSelectedItem(int id) {
        Spinner spinner = (Spinner)findViewById(id);
        int pos = spinner.getLastVisiblePosition();
        Lg.d("pos: " + pos);
        if ( pos == -1 ) {
        	spinner.setSelection(0);
        	pos = 0;
        }
        SpinnerAdapter adapt = spinner.getAdapter();
       	String item = null;
        if ( adapt != null ) {
        	int count = spinner.getAdapter().getCount();
        	if ( pos < count ) { 
        		item = (String)spinner.getAdapter().getItem(pos);
        	} else {
        		if ( count == 0 ) {
        			item = null;
        		} else {
        			item = (String)spinner.getAdapter().getItem(0);
        		}
        	}
        }
    	return item;
    }
    
    void setSpinnerList(int id, String[] list) {
        Spinner spinner = (Spinner)findViewById(id);
        if ( list == null ) {
        	spinner.setEnabled(false);
        	return;
        } else {
        	spinner.setEnabled(true);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getContext(), 
        						android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(spinnerAdapter);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
    }
    
    public static String[] getFileList(final String pattern, String... dirs)  {
    	FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File d, String file) {
				if ( file.matches(pattern) ) {
					return true;
				}
				return false;
			}
    	};
    	final File rootdir = Environment.getExternalStorageDirectory();
    	List<String> filelist = new ArrayList<String>();
    	for ( String dir : dirs ) {
    		File d = new File(rootdir, dir);
    		String[] list = d.list(filter);
    		if ( list == null )  continue;
    		for ( int i=0; i < list.length ; i++ ){
    			filelist.add(dir + "/" + list[i]);
    		}
    	}
    	if ( filelist.size() > 0 ) {
    		String[] ret = new String[filelist.size()];
    		filelist.toArray(ret);
    		for (String s : ret ) {
    			Lg.d("filename: " + s);
    		}
    		return ret;
    	}
    	return null;
    }
    
    OnClickListener mImportListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get filename
			String filename = getSelectedItem(R.id.spinnerFilenames);
			// get car
			String car = getSelectedItem(R.id.spinnerCarnames);
			// get replace or add
			RadioGroup rg = (RadioGroup)findViewById(R.id.radioGroup2);
			int id = rg.getCheckedRadioButtonId();
			boolean addSelected=false;
			if ( id == R.id.radioAdd ){
				addSelected=true;
			}
			// make available for progress dialog
			//App.ctx = ImportWidget.this.getContext();
			App.importFile(filename, car, addSelected);
		}
    };
    
    public static String[] readVehiclesFromFile(String filename) {
    	
   		RecordBlocks blocks;
   		FileInputStream fis; 
		try {
			fis = new FileInputStream(filename);
			blocks = Import.cutIntoBlocks(fis, Import.SECTION_VEHICLES);
		} catch (IOException e) {
			Lg.d("readVehiclesFromFile: error io");
			return null;
		} catch (MissingColumnException e) {
			Lg.d("readVehiclesFromFile: missingcolumn");
			return null;
		}
   		
       	SortedSet<String> ret = new TreeSet<String>();
       	
       	Lg.d("blocks: " + blocks);

       	if ( !blocks.containsKey(Import.SECTION_DEFAULT) && 
       		 !blocks.containsKey(Import.SECTION_VEHICLES) && 
       		 !blocks.containsKey(Import.SECTION_FILLUPS) ) {
       		return null;
       	}
		String make;
		String model;
		String year;
		
		RecordLine rline = null; 
       	if ( blocks.containsKey(Import.SECTION_VEHICLES) ) {
   			rline = (RecordLine)blocks.get(Import.SECTION_VEHICLES);
       	} else if ( blocks.containsKey(Import.SECTION_FILLUPS) ) {
   			rline = (RecordLine)blocks.get(Import.SECTION_FILLUPS);
       	} else if ( blocks.containsKey(Import.SECTION_DEFAULT) ) {
   			rline = (RecordLine)blocks.get(Import.SECTION_DEFAULT);
       	}
       	if ( rline == null ) { return null; }
       	
   		if ( rline.size() == 0 ) {
			ret.add("default");
   		} else {
   			for (int i=0; i < rline.size(); i++){
   				RecordRow row = (RecordRow)rline.get(i);
   				Lg.e("line: " + row);
   				make = row.get("make");
   				model = row.get("model");
   				year = row.get("year");
   				Lg.d("m: " + make + " mo: " + model + " y: " + year);
   				if ( year != null && make != null && model != null ) { 
   					ret.add(year + "::" + make + "::" + model);
   				} else if ( make != null && model != null ) { 
   					ret.add(make + "::" + model);
   				} else {
   					ret.add("default");
   				}
       		}
   		}
       	
       	Lg.e("carlist: " + ret);
       	if ( ret.size() > 0 ) {
       		String[] r = new String[ret.size()];
       		ret.toArray(r);
       		return r;
       	}
       	return null;
    }
    
    
    //@Override
    //protected void onDestroy() {
     //   super.onDestroy();
    //}
  
}
