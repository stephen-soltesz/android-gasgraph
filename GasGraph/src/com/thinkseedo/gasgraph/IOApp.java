package com.thinkseedo.gasgraph;

import com.thinkseedo.gasgraph.widget.ImportWidget;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

public class IOApp extends TabActivity { // implements TabHost.TabContentFactory {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.io_one);
	    final TabHost tabHost = getTabHost();
	    tabHost.addTab(tabHost.newTabSpec("tab2")
	        .setIndicator("", getResources().getDrawable(R.drawable.export))
	        .setContent(new Intent(this, ExportCSV.class)));
	    tabHost.addTab(tabHost.newTabSpec("tab1")
	        .setIndicator("", getResources().getDrawable(R.drawable.ic_menu_upload))
	        .setContent(new Intent(this, ImportWidget.class)));
	}

	/** {@inheritDoc} */
	//public View createTabContent(String tag) {
	 //   final TextView tv = new TextView(this);
	  //  tv.setText("Content for tab with tag " + tag);
	   // return tv;
    //}
}
