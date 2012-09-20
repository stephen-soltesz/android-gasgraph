package com.thinkseedo.gasgraph.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GalleryFling extends CustomGallery {
	public GalleryFling(Context context) {
		super(context);
	}

	public GalleryFling(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GalleryFling(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return super.onScroll(e1, e2, (float) (distanceX*1.6), distanceY);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	                       float velocityY) {        
		// TODO: find a way to make a shorter swipes invoke a fling.
		return false;
	}
}
