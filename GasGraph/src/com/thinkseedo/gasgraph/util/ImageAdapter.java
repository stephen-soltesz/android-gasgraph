package com.thinkseedo.gasgraph.util;

import java.util.SortedSet;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.Background;
import com.thinkseedo.gasgraph.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;
  	private LayoutInflater mInflater;
    private String[] graphTypes;
    Background mDownloadService=null;
    
    public ImageAdapter(Context c, Background dls) {
    	mContext = c;
    	mInflater = LayoutInflater.from(mContext);
    	mDownloadService = dls;
		graphTypes = App.getGraphTypes();
    }
    public int getCount() {
    	SortedSet<String> s;
        s = App.mGraphSets.get(App.distance + App.volume); 
        return s.size(); 
    }
    public Object getItem(int position) {
        return position;
    }
    public long getItemId(int position) {
        return position;
    }

  	public View getView(final int position, View convertView, ViewGroup parent) {
			if ( convertView == null ) {
				Lg.w("creating new imageholder");
		  		convertView = mInflater.inflate(R.layout.imagecell, null);
  			ImageHolder ih = new ImageHolder();
  			ih.v = (ImageView)convertView.findViewById(R.id.imageGraph);
  			ih.a = (ImageView)convertView.findViewById(R.id.imageAlert);
  			ih.p = (ProgressBar)convertView.findViewById(R.id.progressSpin);
  			ih.r = (RelativeLayout)convertView.findViewById(R.id.spinnerLayout);
  			ih.t = (TextView)convertView.findViewById(R.id.textGraphTitle);
  			convertView.setTag(ih);
  		} 
			ImageHolder holder = (ImageHolder)convertView.getTag();
		String graphtag = graphTypes[position];
			holder.t.setBackgroundColor(Color.parseColor("#"+App.color));
  		
			assert (mDownloadService != null );
			Lg.d("downloading: " + graphtag);
			mDownloadService.setupImageWithTag(graphtag, holder);
  		return convertView;
  	}

}
