package com.thinkseedo.gasgraph.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.EditRecord;
import com.thinkseedo.gasgraph.EditService;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.database.CostRecord;
import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.database.Record;
import com.thinkseedo.gasgraph.util.GenericListAdapter.Holder;

public class ServiceHolder implements Holder {
	
	private LayoutInflater mInflater;
	int 		mLayoutId;
	int 		mPosition;
	Context 	mContext;
	TextView	date;
	TextView	price;
	TextView	distance;
	TextView	type;
	TextView	center;
	ImageView   img;
	
	public ServiceHolder(Context context, int layoutId){
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mLayoutId = layoutId;
	}
	
	@Override
	public View getNewView() {
		View v = mInflater.inflate(mLayoutId, null);
		ServiceHolder holder = new ServiceHolder(mContext, mLayoutId);
		v.setTag(holder);
		v.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				ServiceHolder holder = (ServiceHolder)v.getTag();
				Record r = App.mServices.mList.get(holder.mPosition);
				if ( r instanceof CostRecord ) {
					CostRecord cr = (CostRecord)r;
					EditService.editServiceId(mContext, cr.id);
				} else {
					GasRecord gr = (GasRecord)r;
					EditRecord.editRecordId(mContext, gr.id);
				}
				return true;
			}
		});
		return v;
	}

	@Override
	public void updateView(View view, int pos) {
		Lg.d("service: " + pos);
		mPosition = pos;
		date  = (TextView) view.findViewById(R.id.textDate);
		price = (TextView) view.findViewById(R.id.textPrice);
		distance = (TextView) view.findViewById(R.id.textDistance);
		center = (TextView) view.findViewById(R.id.textCN_lv);
		type = (TextView) view.findViewById(R.id.textServiceType);
		img  = (ImageView)view.findViewById(R.id.imageView);
		//CostRecord cr = (CostRecord)get(pos);
		Record r = (Record)get(pos);
		if ( r instanceof CostRecord ) {
			CostRecord cr = (CostRecord)r;
			type.setText(App.formatDate(cr.mDate, App.DATE_DISPLAY)); 
			date.setText(""+cr.mDistance);
			center.setText(cr.mType);
			distance.setText("");
  			price.setText(App.getCurrencySymbol()+cr.mPrice);
			img.setImageResource(R.drawable.car_maint);
		} else {
			GasRecord gr = (GasRecord)r;
			type.setText(App.formatDate(gr.mDate, App.DATE_DISPLAY)); 
  			date.setText(App.formatList(App.units_bl, gr));
			center.setText(App.formatList(App.units_cn, gr));
			distance.setText(App.formatList(App.units_br, gr));
  			Lg.d("tr: " + App.units_tr);
  			Lg.d("tr: " + App.formatList(App.units_tr, gr));
  			price.setText(App.formatList(App.units_tr, gr));
			img.setImageResource(R.drawable.car_fuel);
		}
	}

	@Override
	public int size() {
		if ( App.mServices == null ) { return 0; }
		int ret = App.mServices.mList.size();
		Lg.d("service holder size: " + ret);
		return ret;
	}

	@Override
	public Object get(int pos) {
		if ( App.mServices == null ) { return null; }
		return App.mServices.mList.get(pos);
	}

}
