package com.thinkseedo.gasgraph.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.EditRecord;
import com.thinkseedo.gasgraph.EditService;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.database.CostRecord;
import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.util.GenericListAdapter.Holder;

public class GraphHolder implements Holder {
	
	LayoutInflater mInflater;
	int 		mLayoutId;
	int 		mPosition;
	Context 	mContext;
	
   	String			name;
	ImageView 		v;
	ImageView 		a;
	ProgressBar		p;
	RelativeLayout	r;
	TextView 		t;
	WebView  		w;
	
	public GraphHolder(Context context, int layoutId){
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mLayoutId = layoutId;
	}
	
	@Override
	public View getNewView() {
		View v = mInflater.inflate(mLayoutId, null);
		GraphHolder holder = new GraphHolder(mContext, mLayoutId);
		v.setTag(holder);
		v.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				GraphHolder holder = (GraphHolder)v.getTag();
				// share image
				//CostRecord cr = App.mServices.mList.get(holder.mPosition);
				//EditService.editServiceId(mContext, cr.id);
				return true;
			}
		});
		return v;
	}

	@Override
	public void updateView(View view, int pos) {
		mPosition = pos;
		//date  = (TextView) view.findViewById(R.id.textDate);
		//price = (TextView) view.findViewById(R.id.textPrice);
		//distance = (TextView) view.findViewById(R.id.textDistance);
		//type = (TextView) view.findViewById(R.id.textServiceType);
		//img  = (ImageView)view.findViewById(R.id.imageView);
		CostRecord cr = (CostRecord)get(pos);
		//date.setText(App.formatDate(cr.mDate, App.DATE_DISPLAY)); 
  		//price.setText(App.getCurrencySymbol()+cr.mPrice);
		//distance.setText(""+cr.mDistance);
		//type.setText(cr.mType);
		if ( cr.mType.equals("Oil Change") ) {
		//	img.setImageResource(R.drawable.car_oil);
		} else if ( cr.mType.equals("Maintenance") ) {
		//	img.setImageResource(R.drawable.car_maint);
		} else if ( cr.mType.equals("Parking") ) {
		//	img.setImageResource(R.drawable.car_parking);
		} else if ( cr.mType.equals("Repair") ) {
		//	img.setImageResource(R.drawable.car_repair);
		} else {
		//	img.setImageResource(R.drawable.car);
		}
	}

	@Override
	public int size() {
		//int ret = App.mServices.mList.size();
		//Lg.d("size: " + ret);
		return 0;
	}

	@Override
	public Object get(int pos) {
		//return App.mServices.mList.get(pos);
		return null;
	}

}
