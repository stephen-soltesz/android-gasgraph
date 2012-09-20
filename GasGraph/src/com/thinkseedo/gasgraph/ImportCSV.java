package com.thinkseedo.gasgraph;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import com.thinkseedo.gasgraph.util.Lg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

//public class ImportCSV extends Activity {
public class ImportCSV extends Activity {
	static {
		Lg.d("starting ImportCSV");
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_view);
		Lg.d("onCreate ImportCSV");
        App.setListener(this, R.id.buttonImport, mImportListener);
        setupSpinnerFiles();
        
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
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("GasGraph will scan for *.csv files out-side of GasGraph's storage.\n\n" +
        				   "Only a few CSV formats are supported.\n\n" +
        				   "If you have a CSV file with fill-up history that GasGraph " +
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
    		String[] carList = GasRecordList.readVehiclesFromFile((new File(rootdir, filename)).toString());
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
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, 
        						android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(spinnerAdapter);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
			//App.act = ImportCSV.this;
			
		    Intent intent = new Intent(App.getContext(), Background.class);
		    intent.putExtra("action", App.BG_TYPE_IMPORT);
		    intent.putExtra("addSelected", addSelected);
		    intent.putExtra("filename", filename);
		    intent.putExtra("car", car);
		    startService(intent);
			App.deleteImages();
		}
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
  
}
