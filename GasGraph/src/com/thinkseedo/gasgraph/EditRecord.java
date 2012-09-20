package com.thinkseedo.gasgraph;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.widget.NotesWidget;
import com.thinkseedo.gasgraph.widget.NumberDial;
import com.thinkseedo.gasgraph.widget.RowWidget;

public class EditRecord extends Activity { // OrmLiteBaseActivity<DatabaseHelper> {
	public  static final int 	ADD_ID    = 1;
	public  static final int    EDIT_ID   = 2;
	public  static final String ADD_KEY   = "add";
	public  static final String EDIT_KEY  = "edit";
	public  static final String RETURN_KEY  = "return";
	public  static final String DELETE_KEY  = "delete";
	
    static final int TIME_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;
	
	private static final int    ADD_NEW_ID   = -1;
	
	public static void addRecordView(Context context) {
		Intent intent = new Intent(context, EditRecord.class);
		intent.putExtra(ADD_KEY,  0);
		((Activity)context).startActivityForResult(intent, ADD_ID);
	}

	public static void editRecordId(Context context, int gasRecordId) {
		Intent intent = new Intent(context, EditRecord.class);
		intent.putExtra(EDIT_KEY, gasRecordId);
		((Activity)context).startActivityForResult(intent, EDIT_ID);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fillup_add_view_linear);
        RowWidget t = (RowWidget)findViewById(R.id.rowDistance);
        t.appendToLabel("\n (" + App.distance + ")");
        
        t = (RowWidget)findViewById(R.id.rowPrice);
        t.appendToLabel("\n (" + App.getCurrencySymbol() + ")");
        t = (RowWidget)findViewById(R.id.rowVolume);
        t.appendToLabel("\n (" + App.volume + ")");
        
        App.actCount += 1;
        Intent i = this.getIntent();
        if ( i.hasExtra(ADD_KEY) ) {
        	//Log.d(this.getClass().toString(), "Intent has: " + ADD_KEY);
        	setupDisplay(ADD_NEW_ID);
        } else if ( i.hasExtra(EDIT_KEY) ) {
        	int id = i.getIntExtra(EDIT_KEY,  -1);
        	//Log.d(this.getClass().toString(), "Intent has: " + EDIT_KEY + " id:" + id);
        	if ( id == -1 ){
        		EditRecord.this.setResult(RESULT_CANCELED);
        		finish();
        	}
        	setupDisplay(id);
        }
    }
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.closeHelper();
    }
    
	public void showHelp(View v) {
		
		Lg.d("fmt: " + v.getTag());
		String resName = (String) v.getTag();
		int id = v.getResources().getIdentifier(resName, 
						"string", getPackageName());
		if ( id != 0 ) {
			dialogMessage(id);
		}
	}
	public void dialogMessage(int id) {
		String s = this.getResources().getString(id);
		final String dismiss_label = this.getResources().getString(R.string.dialog_dismiss);
       	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(s)
        	.setCancelable(false)
            .setNegativeButton(dismiss_label, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int id) {
            	    dialog.dismiss();
            	}
        });
       	Dialog d = builder.create();
       	d.show();
	}
	
	public GasRecord mGasRecord;
    public interface OnSaveListener {
       void onAddRecord(GasRecord gr); 
    }	
    public interface OnCanceledListener {
       void onCanceledRecord(GasRecord gr); 
    }	
    public Calendar mCal = Calendar.getInstance(Locale.getDefault());
    protected OnSaveListener mSaveCallback=null;
    protected OnCanceledListener mCanceledCallback=null;
    Button mButtonSave;
    Button mButtonCancel;
    int position;
    boolean mDeleteOnCancel=false;
    
    public void setDeleteOnCancel() {
    	mDeleteOnCancel=true;
    }
    public void setOnCanceledListener(OnCanceledListener l) {
    	mCanceledCallback = l;
    }
    public void setOnSaveListener(OnSaveListener l) {
    	mSaveCallback = l;
    }

	/**
	 * @param context
	 * @throws SQLException 
	 */
	public void setupDisplay(final int id) {
		
        //setupFixedPointListener((EditText)findViewById(R.id.editDistance), false);
        //setupFixedPointListener((EditText)findViewById(R.id.editPrice), true);
        //setupFixedPointListener((EditText)findViewById(R.id.editVolume), true);
        
		try {
			if ( id == ADD_NEW_ID ) { 
				//Log.d("add", "");
				((TextView)findViewById(R.id.textTitle)).setText(R.string.add_title_add);
				mGasRecord = new GasRecord();
				mCal = Calendar.getInstance(Locale.getDefault());
				((RowWidget)findViewById(R.id.rowPrice)).setText(restorePref("last_price", "0.009"));
        		restorePref("last_fuelType", (Spinner)findViewById(R.id.spinnerFuelType), "87");
        		((RowWidget)findViewById(R.id.rowDistance)).setText(restorePref("last_distance", "0"));
        		((RowWidget)findViewById(R.id.rowVolume)).setText("0.000");
			} else {
				((TextView)findViewById(R.id.textTitle)).setText(R.string.add_title_edit);
				mGasRecord = App.getDao().queryForId(id);
				if (mGasRecord != null ) {
					mCal.setTime(mGasRecord.mDate);
					this.update(mGasRecord);
				} else {
					Lg.e("gas record null for id: " + id);
				}
			}
		} catch (SQLException e) {
			App.postDataError("SQL", "add button", e.toString());
			throw new RuntimeException(e);
		}
		updateDateDisplay();
		
		View mDateDisplay = ((RowWidget)findViewById(R.id.rowDate)).getView();
        mDateDisplay.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		showDialog(TIME_DIALOG_ID);
        		showDialog(DATE_DIALOG_ID);
          	}
        });
        
        mButtonCancel = (Button) findViewById(R.id.buttonCancel);
        if ( id == ADD_NEW_ID ) {
        	mButtonCancel.setText(R.string.add_cancel);
        } else {
        	mButtonCancel.setText(R.string.add_delete);
        }
        mButtonCancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
        		if ( mCanceledCallback != null ) {
        			mCanceledCallback.onCanceledRecord(mGasRecord);
        		}
        		Intent i = new Intent(EditRecord.this, ListExample.class);
        		if ( id == ADD_NEW_ID ) {
        			// we were adding a new record, 
        			// so, cancel does nothing
        			i.putExtra(RETURN_KEY, id);
        			EditRecord.this.setResult(App.RESULT_CANCEL, i);
        		} else {
        			// we were editing a record, 
        			// so, cancel deletes the record.
        			i.putExtra(DELETE_KEY, id);
        			EditRecord.this.setResult(App.RESULT_DELETE, i);
					//App.mRecords.remove(index);
					//App.mRecords.updateCalculatedValues();
					//mListAdapter.notifyDataSetChanged();
        			try {
						App.getDao().deleteById(id);
						Lg.e("Deleting images..");
						if ( App.mRecords != null ) {
							App.mRecords.remove(id);
							App.mRecords.updateCalculatedValues();
						}
						App.reloadViews();
						App.deleteImages();
					} catch (SQLException e) {
						App.postDataError("SQL", "add button", e.toString());
						throw new RuntimeException(e);
					}
        		}
        		finish();
			}
        });
        
        mButtonSave = (Button) findViewById(R.id.buttonAdd);
        mButtonSave.setOnClickListener(new Button.OnClickListener() {
        	@Override
            public void onClick(View v) {
        		Lg.d("SAVE onClick()");
                String price 	= ((RowWidget)findViewById(R.id.rowPrice)).getText();
        		String volume 	= ((RowWidget)findViewById(R.id.rowVolume)).getText();
        		String distance = ((RowWidget)findViewById(R.id.rowDistance)).getText();
                boolean isBreak = ((RowWidget)findViewById(R.id.rowBreak)).isChecked();
                
                boolean isFull 	= ((ToggleButton)findViewById(R.id.checkBoxFull)).isChecked();
                String fuelType = savePref("last_fuelType", (Spinner)findViewById(R.id.spinnerFuelType));
                String notes = ((NotesWidget)findViewById(R.id.notesWidget)).getText();
                App.setPref("last_price",     price);
                App.setPref("last_distance",  distance);
                
                Number p = null;
				Number v1 = null;
				Number d = null;
				GasRecord gr = null;
        		Lg.d("SAVE into Try");
				try {
					Lg.d("SAVE before parse price: " + price);
					p = NumberFormat.getInstance(Locale.getDefault()).parse(price);
					Lg.d("SAVE before parse volume: " + volume);
					v1 = NumberFormat.getInstance(Locale.getDefault()).parse(volume);
					Lg.d("SAVE before parse distance");
					d = NumberFormat.getInstance(Locale.getDefault()).parse(distance);
					//Lg.d("dist: " + odoCount) ;
					//d = odoCount;
					String loc = "default";
					if ( isBreak ) {
						loc = "break";
					}
					Lg.d("SAVE before create");
					gr = new GasRecord(mCal.getTime(), 
                						p.doubleValue(), 
                						v1.doubleValue(), 
                						d.intValue(),
                						isFull, fuelType, 
                						loc,isBreak,notes);
					gr.mPosition = mGasRecord.mPosition;
					if ( id == ADD_NEW_ID )  {
						Lg.d("gr add: " + gr); 
						App.getDao().create(gr);
					} else {
						Lg.d("gr update: " + gr); 
						gr.id = id;
						App.getDao().update(gr);
					}
					if ( App.mRecords != null ) {
						App.mRecords.remove(id);
						App.mRecords.add(gr);
						App.mRecords.updateCalculatedValues();
					}
					Lg.d("SAVE before reload");
					App.reloadViews();
					App.deleteImages();
					
				} catch (ParseException e) {
					Lg.d("ERROR: add button parse" + e.toString());
					App.postDataError("Parse", "add button parse", e.toString());
				} catch (SQLException e) {
					Lg.d("ERROR: SQL add button " + e.toString());
					App.postDataError("SQL", "add button", e.toString());
					throw new RuntimeException(e);
				} catch (Exception e) {
					Lg.d("ERROR: Exception: " + e.toString());
				}
        		if ( mSaveCallback != null ) {
        			mSaveCallback.onAddRecord(gr);
        		}
       			EditRecord.this.setResult(App.RESULT_SAVE);
        		finish();
            }
        });
	}
	
    public void updateDateDisplay() {
    	if ( mCal == null) {
    		return;
    	}
    	String s= App.formatDate(mCal.getTime(), App.DATE_DISPLAY_LONG);
        ((RowWidget)findViewById(R.id.rowDate)).setText(s);
    }
	
	public void update(GasRecord gr){
		if ( gr != null ) {
			mGasRecord = gr;
			mCal.setTime(gr.mDate);
	    	RowWidget date = (RowWidget)findViewById(R.id.rowDate);
	    	date.setText(App.formatDate(gr.mDate, true));
	    		
	        Spinner  spinFuelType = (Spinner)findViewById(R.id.spinnerFuelType);
	        String[] types = (String[])App.getContext()
	          							  .getResources()
	           							  .getStringArray(R.array.fuel_type);
	        for(int i=0; i < types.length ; i++ ){
	        	if ( types[i].equals(gr.mType) ) {
	        		spinFuelType.setSelection(i);
	        	}
	        }
	            
	        ToggleButton isFull = (ToggleButton)findViewById(R.id.checkBoxFull);
	        isFull.setChecked(gr.mFilled);
	        
	        RowWidget isBreak = (RowWidget)findViewById(R.id.rowBreak);
	        //isBreak.setChecked(gr.mLocation.equals("break"));
	        isBreak.setChecked(gr.mMissed);
	        
	    	//NumberDial odo = (NumberDial)findViewById(R.id.odometer);
	    	//odo.setCount(gr.mDistance);
	            
	    	RowWidget distance = (RowWidget)findViewById(R.id.rowDistance);
	    	distance.setText(App.formatInteger(gr.mDistance.doubleValue()));
	    		
	    	RowWidget volume = (RowWidget)findViewById(R.id.rowVolume);
	    	volume.setText(App.formatDouble(gr.mVolume, false));
	    		
	        RowWidget price = (RowWidget)findViewById(R.id.rowPrice);
	    	price.setText(App.formatDouble(gr.mPrice, false));
	    	
	        NotesWidget noteWidget = (NotesWidget)findViewById(R.id.notesWidget);
	    	noteWidget.setText(gr.mNotes);
	    	
		}
    	return;
	}

    
	private String savePref(String name, Object o){
		SharedPreferences pref = App.getPrefs();
		SharedPreferences.Editor editor = pref.edit();
		String returnText = null;
    	if (o.getClass().equals(EditText.class)) {
    	    EditText edit = ((EditText)o);
    		returnText = edit.getText().toString();
    	} else if ( o.getClass().equals(Spinner.class)) {
    		Spinner spin = ((Spinner)o);
	        returnText = (String)spin.getSelectedItem();
    	}
    	editor.putString(name, returnText);
    	editor.commit();
    	return returnText;
    }
	
    private String restorePref(String name, String alt) {
		SharedPreferences pref = App.getPrefs(); 
    	String restoredText = pref.getString(name, null);
	    if ( restoredText == null || restoredText.equals("") ) {
	    	restoredText = alt;
	    }
	    return restoredText;
    }
	
    private void restorePref(String name, Object o, String alt) {
		SharedPreferences pref = App.getPrefs(); 
    	String restoredText = pref.getString(name, null);
	    if ( restoredText == null || restoredText.equals("") ) {
	    	restoredText = alt;
	    }
    	if (o.getClass().equals(EditText.class)) {
    	    EditText edit = ((EditText)o);
	    	// move cursor to the right.
	    	edit.setText(restoredText);
	        edit.setSelection(restoredText.length());
    	} else if ( o.getClass().equals(Spinner.class)) {
    		Spinner spin = ((Spinner)o);
	        String[] types = (String[])App.getContext()
	          							  .getResources()
	           							  .getStringArray(R.array.fuel_type);
	        for(int i=0; i < types.length ; i++ ) {
	        	if ( types[i].equals(restoredText) ) {
	        		spin.setSelection(i);
	        	}
	        }
    	}
        return;
    }
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                	mCal.set(year, monthOfYear, dayOfMonth);
                	//mAddDiag.updateDate(mCal.getTime());
                	//mAddDiag.updateDateDisplay();
                }
            };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                	mCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                	mCal.set(Calendar.MINUTE, minute);
                	//mAddDiag.updateDate(mCal.getTime());
                	//mAddDiag.updateDateDisplay();
                    updateDateDisplay();
                }
            };
        
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, 
                        mCal.get(Calendar.HOUR_OF_DAY), 
                        mCal.get(Calendar.MINUTE), false);
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                            mDateSetListener,
                            mCal.get(Calendar.YEAR), 
                            mCal.get(Calendar.MONTH), 
                            mCal.get(Calendar.DAY_OF_MONTH));
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(
                        mCal.get(Calendar.HOUR_OF_DAY), 
                        mCal.get(Calendar.MINUTE));
                break;
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(
                            mCal.get(Calendar.YEAR), 
                            mCal.get(Calendar.MONTH), 
                            mCal.get(Calendar.DAY_OF_MONTH));
                break;
        }
    }    

}
