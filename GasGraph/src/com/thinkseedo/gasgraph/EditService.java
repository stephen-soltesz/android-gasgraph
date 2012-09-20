package com.thinkseedo.gasgraph;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
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
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.thinkseedo.gasgraph.database.CostRecord;
import com.thinkseedo.gasgraph.database.DatabaseHelper;
//import com.thinkseedo.gasgraph.util.DrawableManager;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.widget.RowWidget;
import com.thinkseedo.gasgraph.widget.NotesWidget;

public class EditService extends Activity { // OrmLiteBaseActivity<DatabaseHelper> {
	public  static final int 	ADD_ID    = 1;
	public  static final int    EDIT_ID   = 2;
	public  static final String ADD_KEY   = "add";
	public  static final String EDIT_KEY  = "edit";
	public  static final String RETURN_KEY  = "return";
	public  static final String DELETE_KEY  = "delete";
	
    static final int TIME_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;
	
	private static final int    ADD_NEW_ID   = -1;
	
	Handler mHandler = new Handler();
	
	public static void addServiceView(Context context) {
		Intent intent = new Intent(context, EditService.class);
		intent.putExtra(ADD_KEY,  0);
		((Activity)context).startActivityForResult(intent, ADD_ID);
	}

	public static void editServiceId(Context context, int gasRecordId) {
		Intent intent = new Intent(context, EditService.class);
		intent.putExtra(EDIT_KEY, gasRecordId);
		((Activity)context).startActivityForResult(intent, EDIT_ID);
	}
	static int pix = 10;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_add_view_linear);
        RowWidget t = (RowWidget)findViewById(R.id.rowDistance);
        t.appendToLabel(" (" + App.distance + ")");
        t = (RowWidget)findViewById(R.id.rowPrice);
        t.appendToLabel(" (" + App.getCurrencySymbol() + ")");
        
        Intent i = this.getIntent();
        if ( i.hasExtra(ADD_KEY) ) {
        	setupDisplay(ADD_NEW_ID);
        } else if ( i.hasExtra(EDIT_KEY) ) {
        	int id = i.getIntExtra(EDIT_KEY,  -1);
        	if ( id == -1 ) {
        		EditService.this.setResult(App.RESULT_CANCEL);
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
	
	public CostRecord mCostRecord;
    public Calendar mCal = Calendar.getInstance(Locale.getDefault());
    Button mButtonSave;
    Button mButtonCancel;
    int position;
    boolean mDeleteOnCancel=false;
    
    public void setDeleteOnCancel() {
    	mDeleteOnCancel=true;
    }

	/**
	 * @param context
	 * @throws SQLException 
	 */
	public void setupDisplay(final int id) {
		final Dao<CostRecord, Integer> crDao;
		
		try {
			crDao = App.getCostDao();
			if ( id == ADD_NEW_ID ) { 
				mCostRecord = new CostRecord();
				
				((TextView)findViewById(R.id.textTitle)).setText(R.string.service_title_add);
				mCal = Calendar.getInstance(Locale.getDefault());
				((RowWidget)findViewById(R.id.rowPrice)).setText("0.00");
        		((RowWidget)findViewById(R.id.rowDistance)).setText("0");
        		String s = App.getPref("last_serviceType", "Oil Change");
				((RowWidget)findViewById(R.id.rowType)).setText(s);
				((RowWidget)findViewById(R.id.rowMark)).setChecked(false);
				((NotesWidget)findViewById(R.id.notesWidget)).setText("");
				
			} else {
				((TextView)findViewById(R.id.textTitle)).setText(R.string.service_title_edit);
				mCostRecord = crDao.queryForId(id);
				if (mCostRecord != null ) {
					mCal.setTime(mCostRecord.mDate);
					this.update(mCostRecord);
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
        		//Intent i = new Intent(EditService.this, GasGraphActivity.class);
        		Intent i = new Intent(EditService.this, ListExample.class);
        		if ( id == ADD_NEW_ID ) {
        			// we were adding a new record, 
        			// so, cancel does nothing
        			i.putExtra(RETURN_KEY, id);
        			EditService.this.setResult(App.RESULT_CANCEL, i);
        		} else {
        			// we were editing a record, 
        			// so, cancel deletes the record.
        			i.putExtra(DELETE_KEY, id);
        			EditService.this.setResult(App.RESULT_DELETE, i);
        			try {
						crDao.deleteById(id);
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
                String price 	= ((RowWidget)findViewById(R.id.rowPrice)).getText();
        		String distance = ((RowWidget)findViewById(R.id.rowDistance)).getText();
                boolean isMarked = ((RowWidget)findViewById(R.id.rowMark)).isChecked();
                String serviceType = ((RowWidget)findViewById(R.id.rowType)).getText();
                String notes = ((NotesWidget)findViewById(R.id.notesWidget)).getText();
                
                App.setPref("last_serviceType", serviceType);
                Number p = null;
				Number d = null;
				CostRecord cr = null;
				try {
					p = NumberFormat.getInstance(Locale.getDefault()).parse(price);
					d = NumberFormat.getInstance(Locale.getDefault()).parse(distance);
					//public CostRecord(Date date, Integer distance, 
   					//	  Double price, String type, 
   					//	  Boolean mark, String note) {
					cr = new CostRecord(mCal.getTime(), 
                						d.intValue(),
                						p.doubleValue(), 
                						serviceType,
                						isMarked, notes);
					//cr.mPosition = mCostRecord.mPosition;
					try {
						if ( id == ADD_NEW_ID )  {
							// TODO: check for add.
							//App.mServices.mList.add(cr);
							//Collections.sort(App.mServices.mList, cr);
							crDao.create(cr);
						} else {
							cr.id = id;
							// TODO: check for update.
							crDao.update(cr);
						}
						if ( isMarked ) {
							App.deleteImages();
						}
						EditService.this.setResult(App.RESULT_SAVE);
						finish();
					} catch ( ClassCastException e ) {
						App.showToast("Cannot insert this Cost Record. Check Date & Odometer.");
					}
				} catch (ParseException e) {
					App.postDataError("Parse", "add button parse", e.toString());
				} catch (SQLException e) {
					App.postDataError("SQL", "add button", e.toString());
					throw new RuntimeException(e);
				}
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
	
	public void update(CostRecord cr){
		if ( cr != null ) {
			mCostRecord = cr;
			mCal.setTime(cr.mDate);
	    	RowWidget date = (RowWidget)findViewById(R.id.rowDate);
	    	date.setText(App.formatDate(cr.mDate, App.DATE_DISPLAY));
	    		
	    	RowWidget distance = (RowWidget)findViewById(R.id.rowDistance);
	    	distance.setText(App.formatInteger(cr.mDistance.doubleValue()));
	    		
	        RowWidget price = (RowWidget)findViewById(R.id.rowPrice);
	    	price.setText(App.formatDouble(cr.mPrice, true));
	    	
	        RowWidget type = (RowWidget)findViewById(R.id.rowType);
	        type.setText(cr.mType);
	        
	        RowWidget isMarked = (RowWidget)findViewById(R.id.rowMark);
	        isMarked.setChecked(cr.mMark);
	    	
	        NotesWidget noteWidget = (NotesWidget)findViewById(R.id.notesWidget);
	    	noteWidget.setText(cr.mNote);
		}
    	return;
	}

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                	mCal.set(year, monthOfYear, dayOfMonth);
                }
            };

    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                	mCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                	mCal.set(Calendar.MINUTE, minute);
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
