package com.thinkseedo.gasgraph.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.EditRecord;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.util.GenericListAdapter.Holder;

public class FillupHolder implements Holder {
	
	private LayoutInflater mInflater;
	int mLayoutId;
	Context mContext;
	int mPosition;
	TextView	date;
	TextView	bl;
	TextView	cn;
	TextView	br;
	TextView	tr;
	
	public FillupHolder(Context context, int layoutId){
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mLayoutId = layoutId;
	}
	
	@Override
	public View getNewView() {
		View v = mInflater.inflate(mLayoutId, null);
		FillupHolder holder = new FillupHolder(mContext, mLayoutId);
		v.setTag(holder);
		v.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				FillupHolder holder = (FillupHolder)v.getTag();
				GasRecord gr = App.mRecords.mList.get(holder.mPosition);
				EditRecord.editRecordId(mContext, gr.id);
				return true;
			}
		});
		return v;
	}

	@Override
	public void updateView(View view, int pos) {
		mPosition = pos;
		Lg.d("update: " + pos);
		date = (TextView) view.findViewById(R.id.textDate_lv);
		bl   = (TextView) view.findViewById(R.id.textBL_lv);
		cn   = (TextView) view.findViewById(R.id.textCN_lv);
		br   = (TextView) view.findViewById(R.id.textBR_lv);
		tr   = (TextView) view.findViewById(R.id.textTR_lv);
		GasRecord gr = (GasRecord)get(pos);
		date.setText(App.formatDate(gr.mDate, App.DATE_DISPLAY)); 
  		bl.setText(App.formatList(App.units_bl, gr));
		cn.setText(App.formatList(App.units_cn, gr));
		br.setText(App.formatList(App.units_br, gr));
  		tr.setText(App.formatList(App.units_tr, gr));
	}

	@Override
	public int size() {
		if ( App.mRecords == null ) { return 0; };
		Lg.d("fillup holder size: " + App.mRecords.mList.size());
		return App.mRecords.mList.size();
	}

	@Override
	public Object get(int pos) {
		if ( App.mRecords == null ) { return null; }
		return App.mRecords.mList.get(pos);
	}

}
