package com.thinkseedo.gasgraph.widget;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.util.Lg;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class RowWidget extends RelativeLayout {
	TextView label;
	EditText edit;
	Spinner  spinner;
	ToggleButton toggle;
	Button   button;
	NumberDial wheel;
	
	final int TYPE_TOGGLE = 1;
	final int TYPE_BUTTON = 2;
	final int TYPE_SPINNER = 3;
	final int TYPE_NUMBER = 4;
	final int TYPE_WHEEL  = 5;
	
	Handler mHandler = new Handler();
	int decimalPlaces = 0;
	int valueType = 0;
	int valueWidth = 6;
	boolean numberPrice = false;
	boolean wheelFraction = false;
	String helpName = "";
	String labelText = "";
	String valueText = "";
	CharSequence[] valueArray = null;

	public RowWidget(Context context) {
		super(context);
	}
	public RowWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		getAttributes(attrs);
	}
	public RowWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getAttributes(attrs);
	}
	void getAttributes(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RowWidget);
		final int N = a.getIndexCount();
		for (int i = 0; i < N; ++i)
		{
		    int attr = a.getIndex(i);
		    switch (attr)
		    {
		        case R.styleable.RowWidget_decimalPlaces:
		            decimalPlaces = a.getInteger(attr, 0);
		            Lg.d("d: " + decimalPlaces);
		            break;
		        case R.styleable.RowWidget_valueWidth:
		            valueWidth = a.getInteger(attr, 1);
		            Lg.d("w: " + valueWidth );
		            break;
		        case R.styleable.RowWidget_helpName:
		            helpName  = a.getString(attr);
		            Lg.d("h: " + helpName);
		            break;
		        case R.styleable.RowWidget_labelText:
		            labelText = a.getString(attr);
		            Lg.d("l: " + labelText);
		            break;
		        case R.styleable.RowWidget_numberPrice:
		        	numberPrice = a.getBoolean(attr, false);
		        	Lg.d("b: " + numberPrice);
		        	break;
		        case R.styleable.RowWidget_wheelFraction:
		        	wheelFraction = a.getBoolean(attr, false);
		        	Lg.d("wfb: " + wheelFraction);
		        	break;
		        case R.styleable.RowWidget_valueArray:
		        	valueArray = a.getTextArray(attr);
		            Lg.d("va: " + valueArray[0]);
		        	break;
		        case R.styleable.RowWidget_valueType:
		            valueType = a.getInteger(attr, 1);
		            Lg.d("v: " + valueType);
		            break;
		        case R.styleable.RowWidget_valueText:
		            valueText = a.getString(attr);
		            Lg.d("t: " + valueText);
		            break;
		    }
		}
		a.recycle();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		final Activity act = (Activity)getContext();
		//((Activity)getContext()).getLayoutInflater().inflate(R.layout.notes_widget, this);
		LayoutInflater mInflater = LayoutInflater.from(getContext());
		mInflater.inflate(R.layout.widget_row, this);
		
		label 	= (TextView) findViewById(R.id.textLabel);
		toggle  = (ToggleButton) findViewById(R.id.checkValue);
		button  = (Button) findViewById(R.id.buttonValue);
		spinner = (Spinner) findViewById(R.id.spinnerValue);
		edit 	= (EditText) findViewById(R.id.editValue);
		wheel   = (NumberDial) findViewById(R.id.wheelValue);
		ImageView image = (ImageView) findViewById(R.id.imageHelp);
		image.setTag(helpName);
		label.setText(labelText);
		
		makeGone(toggle, button, spinner, edit, wheel);
		switch(valueType) {
			case TYPE_TOGGLE:
				toggle.setVisibility(View.VISIBLE);
				toggle.setText(valueText);
				toggle.setTextOn(valueText);
				toggle.setTextOff(valueText);
				break;
			case TYPE_BUTTON:
				button.setVisibility(View.VISIBLE);
				button.setText(valueText);
				break;
			case TYPE_SPINNER:
				spinner.setVisibility(View.VISIBLE);
		        ArrayAdapter<CharSequence> adapter = 
		        	new ArrayAdapter<CharSequence>(getContext(), 
						android.R.layout.simple_spinner_item, valueArray);
		        spinner.setAdapter(adapter);
		        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				break;
			case TYPE_NUMBER:
				edit.setVisibility(View.VISIBLE);
				setupFixedPointListener(edit);
				edit.setText(valueText);
				break;
			case TYPE_WHEEL:
				wheel.setVisibility(View.VISIBLE);
				wheel.setFraction(wheelFraction);
				wheel.setDialCount(valueWidth);
				Lg.d("FRACTION: " + wheelFraction);
				wheel.addDecimal(decimalPlaces);
				wheel.setCount(Integer.parseInt(valueText));
				break;
		}
	}
	
	public void showHelp(View v) {
		Lg.d("fmt: " + v.getTag());
	}
	
    private void setupFixedPointListener(EditText p) {
    	class NewTextWatcher implements TextWatcher {
        	private boolean mSelfChange=false;
        	public NewTextWatcher(){
        		super();
        	}
            @Override
            public void afterTextChanged(Editable s) {
                // Ignore the change caused by s.replace().
                if (mSelfChange) { return; }
                
            	int m = Math.min(Selection.getSelectionStart(s),Selection.getSelectionEnd(s));
            	if ( m < s.length() ) {
            		mSelfChange = true;
            		// then pull out the character added.
            		if ( m > 0 ) {
            			char c = s.charAt(m-1);
            			s.replace(m-1, m, "");
            			s.append(c);
            		}
            		mSelfChange = false;
            	}
                
                // Format string with or without fraction.
                String conv = s.toString().replaceAll("[^0-9]", ""); 
               	if ( conv == "" ) { conv = "0"; }
               	
                String formatted = null;
                double div = Math.pow(10, decimalPlaces);
               	String fmt;
                if ( decimalPlaces == 0 ) {
                	fmt = "%.0f";
                } else {
                	fmt = "%." + decimalPlaces + "f";
                }
               	if ( conv.length() > 0 ){
               		formatted = String.format(fmt, Double.parseDouble(conv)/div);
               	} else {
               		formatted = "0";
               	}
	            
                mSelfChange = true;
                s.replace(0, s.length(), formatted, 0, formatted.length());
                if (formatted.equals(s.toString())) {
                	// The text could be changed by other TextWatcher after we changed it. If we found the
                	// text is not the one we were expecting, just give up calling setSelection().
                    Selection.setSelection(s, s.length());
                }
                mSelfChange = false;
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
    		
    	}
        p.addTextChangedListener(new NewTextWatcher()); 
    }
	public void appendToLabel(String s) {
		label.setText(label.getText() + s);
	}
	public void setLabel(String s) {
		label.setText(s);
	}
	public View getView() {
		if ( valueType == TYPE_BUTTON ) {
			return button;
		}
		return null;
	}
	public void setText(String s) {
		if ( valueType == TYPE_NUMBER ) {
			edit.setText(s);
	        edit.setSelection(s.length());
		} else if ( valueType == TYPE_SPINNER ) {
			@SuppressWarnings("unchecked")
			ArrayAdapter<CharSequence> myAdap = (ArrayAdapter<CharSequence>) spinner.getAdapter();
			int spinnerPosition = myAdap.getPosition(s);
			spinner.setSelection(spinnerPosition);
		} else if ( valueType == TYPE_BUTTON ) {
			button.setText(s);
		} else if ( valueType == TYPE_WHEEL ) {
			wheel.setText(s);
		}
		return;
	}
	public String getText() {
		if ( valueType == TYPE_NUMBER ) {
			return edit.getText().toString();
		} else if ( valueType == TYPE_SPINNER ) {
			return spinner.getSelectedItem().toString();
		} else if ( valueType == TYPE_WHEEL ) {
			return wheel.getText();
		}
		return null;
	}
	public void setChecked(boolean v) {
		if ( valueType == TYPE_TOGGLE ) {
			toggle.setChecked(v);
		}
	}
	public boolean isChecked() {
		if ( valueType == TYPE_TOGGLE ) {
			return toggle.isChecked();
		}
		return false;
	}
	void makeGone(View... views) {
		for (int i =0; i < views.length; i++ ) {
			views[i].setVisibility(View.GONE);
		}
	}
}
