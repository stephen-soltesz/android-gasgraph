package com.thinkseedo.gasgraph.widget;

import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.util.Lg;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotesWidget extends RelativeLayout {
	static EditText notes;
	static TextView check;
	static TextView preview;
	static Handler mHandler = new Handler();

	public NotesWidget(Context context) {
		super(context);
	}
	public NotesWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public NotesWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		final Activity act = (Activity)getContext();
		//((Activity)getContext()).getLayoutInflater().inflate(R.layout.notes_widget, this);
		LayoutInflater mInflater = LayoutInflater.from(getContext());
		mInflater.inflate(R.layout.widget_notes, this);
	//}
	//public static void setupNotes(final Activity act) {
		//getContext().getLayoutInflater().inflate(R.layout.notes_widget, this);
		setupViewItems();
		notes.setVisibility(View.GONE);
		check.setText("\u25B6" + " Notes");
		if ( true ) {
			
        notes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	            mHandler.postDelayed(new Runnable() {
	            	@Override
	            	public void run() {
						Rect r = new Rect();
						notes.getDrawingRect(r);
						notes.requestRectangleOnScreen(r, false);
	            	} 
	            }, 400);
			} 
        });
		notes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		    	Lg.d("SOFT WINDOW CHANGED: " + hasFocus);
		    	if ( hasFocus ) {
				mHandler.postDelayed(new Runnable() {
					public void run() {
	                   	notes.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
	                   	notes.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));                       
					}
				}, 200);
		    	}
		    }
		});
		}
        OnClickListener ocl = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int vis = notes.getVisibility();
				if ( vis == View.GONE ) {
					notes.setVisibility(View.VISIBLE);
					preview.setVisibility(View.GONE);
					check.setText("\u25BC" + " Notes");
					notes.requestFocus();
				} else {
					InputMethodManager imm = (InputMethodManager)act.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
					preview.setText(notes.getText());
					notes.setVisibility(View.GONE);
					preview.setVisibility(View.VISIBLE);
					check.setText("\u25B6" + " Notes");
				}
			}
        };
        check.setOnClickListener(ocl);
        preview.setOnClickListener(ocl);
	}
 
	public void setText(String txt) {
		notes.setText(txt);
		preview.setText(txt);
	}
	public String getText() {
		return notes.getText().toString();
	}
	private void setupViewItems() {
		notes = (EditText) findViewById(R.id.editNotes);
		check = (TextView) findViewById(R.id.checkedNotes);
		preview = (TextView) findViewById(R.id.textPreview);
	}

}
