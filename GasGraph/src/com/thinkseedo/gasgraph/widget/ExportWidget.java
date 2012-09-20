package com.thinkseedo.gasgraph.widget;

import java.util.Calendar;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.util.GasRecordList;
import com.thinkseedo.gasgraph.util.Import;
import com.thinkseedo.gasgraph.util.Lg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ExportWidget extends LinearLayout {
	
	public ExportWidget(Context context) {
		super(context);
	}
	public ExportWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater mInflater = LayoutInflater.from(getContext());
		mInflater.inflate(R.layout.widget_export, this);
        String s = "gasgraph-" + App.formatDate(null, App.DATE_FILENAME);
        App.setText(this, R.id.editExportFilename, s);
        App.setListener(this, R.id.buttonExport, mExportListener);
    }
	public void clearKeyboard() {
        EditText et = (EditText)findViewById(R.id.editExportFilename);
		InputMethodManager imm = (InputMethodManager)App.getApp().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
	}
    
    OnClickListener mExportListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String s = App.getText(ExportWidget.this, R.id.editExportFilename);
			String fn = s.replaceAll("[^a-zA-Z0-9\\-_]", "");
			if ( !s.equals(fn) ) {
				Toast.makeText(getContext(), "Filename changed to remove illegal characters.", Toast.LENGTH_SHORT).show();
				App.setText(ExportWidget.this, R.id.editExportFilename, fn);
			}
			Lg.d("filename: " + fn);
			Import.exportRecords(fn+".csv");
		}
    };
}
