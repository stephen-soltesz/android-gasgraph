package com.thinkseedo.gasgraph;

import taptwo.widget.TitleFlowIndicator;
import taptwo.widget.ViewFlow;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;

public class WebApp extends Activity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.webviewdialog);
        
       	String file;
       	if ( App.MARKET == App.MARKET_AMAZON ) {
   	   		file= "web_usage_amz.html";
       	} else {
   	   		file= "web_usage.html";
       	}
        Button b = (Button)findViewById(R.id.buttonDialogDismiss);
        b.setVisibility(View.GONE);
        WebView w = (WebView)findViewById(R.id.webViewDialog);
        w.setVisibility(View.VISIBLE);
        w.loadUrl("file:///android_asset/" + file);
        w.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        w.setScrollContainer(true);
		
    }
}