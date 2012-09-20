package com.thinkseedo.gasgraph.widget;


import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.util.Lg;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import kankan.wheel.widget.adapters.WheelViewAdapter;

public class NumberDial extends LinearLayout {
	
	int mDialCount=1;
	int mDecimalPlaces=0;
	boolean mFraction = false;
	int mValue=0;
	List<Runnable> mPending = new LinkedList<Runnable>();
	AttributeSet mAttrs=null;
	LayoutInflater mInflater = null;
	
	public NumberDial(Context context) {
		super(context);
		Log.d("default", "default numberdial");
	}
	public NumberDial(Context context, final AttributeSet attrs) {
		super(context, attrs);
		mDialCount = Integer.parseInt((String) this.getTag());
		//mInflater.inflate(R.layout.wheel, this);
		//Log.d("width", "w: " + mDialCount );
		mAttrs = attrs;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		for ( Runnable r : mPending ) {
			this.postDelayed(r, 1000);
		}
		//mInflater = LayoutInflater.from(getContext());
		mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setupInitial(mAttrs);
	}
	
	private void setupInitial(AttributeSet attrs) {
		OnKeyListener priceListener = new SpinKeyListener();
		for (int i=0; i < mDialCount ; i++) {
			//WheelView w = new WheelView(getContext(), attrs);
			
			WheelView w = (WheelView)View.inflate(getContext(), R.layout.wheel, null);
			Log.d("setup", "i: " + i);
			initWheel(w, i, priceListener, (i==0 && mFraction ? true : false));
			
			LayoutParams l = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			l.gravity = Gravity.RIGHT;
			w.setLayoutParams(l);
			this.addView(w, 0, l);
		}
	}
   	Handler mHandler = new Handler();
   	boolean mRunning = false;
   	int mCount = 0;
   	
   	
   	public void setText(String s) {
   		try {
   			setCount(Integer.parseInt(s.replace(".", "").replace(",", "")));
   		} catch (Exception e){
   			Lg.d("parse error: " + s);
   			setCount(0);
   		}
   	}
   	public String getText() {
   		int ret = getCount();
   		int scale = (int)Math.pow(10, mDecimalPlaces);
   		String fmt = "%." + mDecimalPlaces + "f";
   		Lg.d("format: " + fmt);
    	Lg.d("ND getText() == " + String.format(fmt, ((double)ret/(double)scale)));
    	return String.format(fmt, ((double)ret/(double)scale));
   	}
   	public void setFraction(boolean fraction) {
   		mFraction = fraction;
   	}
   	
   	
   	public void setDialCount(int width) {
   		this.removeAllViews();
   		assert(width > 0 && width <= 10);
   		mDialCount = width;
		setupInitial(mAttrs);
   	}
   	public void setCount(int count) {
   		mCount = count;
   		Lg.d("count: " + mCount);
		Runnable r = new Runnable() {
			@Override
			public void run() {
				updateWheels(mCount);
			}
		};
		mHandler.postDelayed(r, 500);
   	}
   	public int getCount() {
   		int ret=0;
    	for (int i=0; i < mDialCount ; i++ ) {
    		WheelView wheel = getWheel(i);
    		int c = (int)Math.pow(10, i);
    		ret += c*wheel.getCurrentPosition();
    	}
   		return ret;
   	}
   	
	public void addDecimal(int places) {
		mDecimalPlaces = places;
		TextView t = new TextView(this.getContext());
		t.setText(".");
		t.setTextSize(30);
		LayoutParams l = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		l.gravity = Gravity.CENTER;
		t.setLayoutParams(l);
		t.setGravity(Gravity.CENTER);
		if ( mDecimalPlaces > 0 ) {
		    this.addView(t, mDialCount - places);
		}
	}
	
	@Override
	protected void onFocusChanged (boolean gainFocus, 
									int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		Log.d("log", "FOCUS");
		if ( gainFocus ) {
			this.setBackgroundColor(Color.RED);
		} else {
			this.setBackgroundColor(Color.BLACK);
		}
		return;
	}
	
    class SpinKeyListener implements OnKeyListener {
    	String mString = "0";
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			String s = String.valueOf(event.getDisplayLabel());
			Lg.d("keycode: " + keyCode);
			Lg.d("back: " + (KeyEvent.KEYCODE_BACK != keyCode));
			if ( KeyEvent.ACTION_UP == event.getAction() ) {
				if ( KeyEvent.KEYCODE_BACK != keyCode && (
					 (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) || 
					  keyCode == KeyEvent.KEYCODE_DEL ) 
				   )
				{
					if ( KeyEvent.KEYCODE_DEL == keyCode ) {
						mString = mString.substring(0,mString.length()-1);
					} else {
						mString += s;
						Log.d("s", "s: " + s);
					}
					mCount = Integer.parseInt(mString);
					updateWheels(mCount);
					return true;
				} else {
					return false;
				}
			}
			return true;
		}
    }
    
    private void updateWheels(int count) {
    	for (int i=0; i < mDialCount ; i++ ) {
    		int c = (int)Math.pow(10, i);
    		Lg.d("p: " + i + " v: " + (count/c)%10);
    		setWheel(i, (count/c)%10);
    	}
    }
    
    private void setWheel(int id, int i) {
        WheelView wheel = getWheel(id);
        wheel.setCurrentPosition(i, true);
    }
    private WheelView getWheel(int id) {
    	return (WheelView) findViewById(id);
    }
    
    public void setAdapter(int i, WheelViewAdapter adap) {
    	WheelView w = getWheel(i);
    	if ( i < mDialCount ) {
    		w.setViewAdapter(adap);
    	}
    }
    
    void initWheel(WheelView wheel, int id, OnKeyListener l, boolean fraction) {
    	WheelViewAdapter adap = null;
    	int initialPosition = 0;
    	if ( fraction ) {
    		adap = new AbstractWheelAdapter() {
    			@Override
    			public int getItemsCount() {
    				return 10;
    			}
    			@Override
    			public View getItem(int index, View convertView, ViewGroup parent) {
    				if ( convertView == null ) {
    					convertView = mInflater.inflate(R.layout.fraction_text, null);
    					TextView bar = (TextView)convertView.findViewById(R.id.textBar);
    					bar.setText("\u2015");
    				}
    				TextView top = (TextView)convertView.findViewById(R.id.textTop);
    				top.setText(String.valueOf((10-index)%10));
    				return convertView;
    			}
    		};
    		initialPosition = 9;
    	} else {
    		adap = new NumericWheelAdapter(this.getContext(), 0, 9);
    		initialPosition = 0;
    	}
        wheel.setViewAdapter(adap);
        /* wheel.setOnKeyListener(l); */
        wheel.setId(id);
        wheel.setCurrentPosition(initialPosition, true);
        wheel.setFocusable(true);
        wheel.setFocusableInTouchMode(true);
        wheel.requestFocus();
        wheel.addChangingListener(changedListener);
        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setInterpolator(new AnticipateOvershootInterpolator());
        wheel.setVisibleItems(2);
        wheel.setFadingEdgeLength(0);
        wheel.setVerticalFadingEdgeEnabled(false);
        wheel.setHorizontalFadingEdgeEnabled(false);
    }
    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            wheelScrolled = true;
        }
        public void onScrollingFinished(WheelView wheel) {
            wheelScrolled = false;
            //updateStatus();
        }
    };
    private boolean wheelScrolled = false;
 
    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            //if (!wheelScrolled) {
                //updateStatus();
            //}
        }
    };
}
