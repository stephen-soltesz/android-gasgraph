package com.thinkseedo.gasgraph.util;

import java.util.SortedSet;

import taptwo.widget.TitleProvider;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.Background;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.widget.StatsWidget;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ImageFlowAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    //private Context mContext;
  	private LayoutInflater mInflater;
    private String[] graphTypes;
    Background mDownloadService=null;
    OnTouchListener mOTL;
    int mSize;
    
    public ImageFlowAdapter(Context c, Background dls, OnTouchListener otl) {
    	//mContext = c;
    	//mInflater = LayoutInflater.from(mContext);
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	mDownloadService = dls;
		graphTypes = App.getGraphTypes();
		mOTL = otl;
		mSize = App.mGraphSets.get(App.distance + App.volume).size(); 
    }
    
    @Override 
    public int getCount() {
        return mSize;
    }
    
    @Override 
    public Object getItem(int position) {
        return position;
    }
    
    @Override 
    public long getItemId(int position) {
        return position;
    }
    
    @Override 
  	public View getView(final int position, View convertView, ViewGroup parent) {
    	if ( convertView == null ) {
	 		Lg.w("creating new imageholder");
		  	convertView = mInflater.inflate(R.layout.image_item, null);
		  	ImageHolder ih = new ImageHolder();
		  	ih.v = (ImageView)convertView.findViewById(R.id.imageGraph);
		  	ih.a = (ImageView)convertView.findViewById(R.id.imageAlert);
		  	ih.p = (ProgressBar)convertView.findViewById(R.id.progressSpin);
		  	ih.r = (RelativeLayout)convertView.findViewById(R.id.spinnerLayout);
		  	ih.t = (TextView)convertView.findViewById(R.id.textGraphTitle);
		  	//ih.v.setOnTouchListener(mOTL);
		  	convertView.setTag(ih);
    	}
    	ImageHolder holder = (ImageHolder)convertView.getTag();
 	
		assert (mDownloadService != null );
		String graphtag = graphTypes[position];
		Lg.d("downloading: " + graphtag);
		holder.t.setBackgroundColor(Color.parseColor("#"+App.color));
		holder.v.setTag(Integer.valueOf(position));
		mDownloadService.setupImageWithTag(graphtag, holder);
  		return convertView;
  	}
}
