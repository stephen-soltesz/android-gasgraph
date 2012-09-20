package com.thinkseedo.gasgraph;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

public class TabApp extends TabActivity { // implements TabHost.TabContentFactory {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.main_new);
	    final TabHost tabHost = getTabHost();
	    //tabHost.addTab(tabHost.newTabSpec("tab2")
	    //		
	     //   .setIndicator("", getResources().getDrawable(R.drawable.mile2))
	      //  .setContent(new Intent(this, GasGraphActivity.class)));
	    tabHost.addTab(tabHost.newTabSpec("tab1")
	        .setIndicator("", getResources().getDrawable(R.drawable.ic_maint))
	        //.setContent(new Intent(this, ImportCSV.class)));
	        .setContent(new Intent(this, ListExample.class)));
	    
        View view = (View) findViewById(R.id.imageViewAdd);
        if ( view != null ) {
        	view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String tag = TabApp.this.getTabHost().getCurrentTabTag();
					if ( tag.equals("tab2") ) {
						EditRecord.addRecordView(TabApp.this);
					} else {
						EditService.addServiceView(TabApp.this);
					}
				}
        	});
        }
	}

	/** {@inheritDoc} */
	//public View createTabContent(String tag) {
	 //   final TextView tv = new TextView(this);
	  //  tv.setText("Content for tab with tag " + tag);
	   // return tv;
    //}
}
