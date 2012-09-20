package com.thinkseedo.gasgraph.util;

import com.thinkseedo.gasgraph.App;
//import com.thinkseedo.gasgraph.GasGraphActivity;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.R.id;
import com.thinkseedo.gasgraph.database.GasRecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class GenericListAdapter extends BaseAdapter {

  	Holder mHolder;
  	public boolean mRedraw = true;
	
  	public interface Holder {
  		public View getNewView();
  		public void updateView(View view, int pos);
  		public int size();
  		public Object get(int pos);
  	}
  		
  	public GenericListAdapter(Holder holder) {
  		mHolder   = holder;
    }
  	
  	public void reDraw() {
		Lg.d("redraw: ");
	    this.mRedraw = true;
        this.notifyDataSetChanged();
	    this.mRedraw = false;
  	}

  	public View getView(final int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid
  		// unneccessary calls to findViewById() on each row.
  		Holder holder;

  		// When convertView is not null, we can reuse it directly, there is
  		// no need to reinflate it. We only inflate a new View when the convertView
 		// supplied by ListView is null.
 		if (convertView == null || mRedraw == true) {
 			convertView = mHolder.getNewView();
  		}
		holder = (Holder)convertView.getTag();
  		holder.updateView(convertView, position);
  		return convertView;
  	}

  	@Override
  	public long getItemId(int position) {
  		return 0;
  	}

  	@Override
  	public int getCount() {
  		return mHolder.size();
	}

 	@Override
  	public Object getItem(int position) {
  		return mHolder.get(position);
  	}
}
