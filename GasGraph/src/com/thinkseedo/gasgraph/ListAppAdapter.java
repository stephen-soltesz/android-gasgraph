package com.thinkseedo.gasgraph;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.thinkseedo.gasgraph.util.GasRecordList;
import com.thinkseedo.gasgraph.util.Lg;
import com.thinkseedo.gasgraph.widget.ExportWidget;
import com.thinkseedo.gasgraph.widget.ImportWidget;
import com.thinkseedo.gasgraph.widget.ServiceListWidget;
import com.thinkseedo.gasgraph.widget.FillupListWidget;
import com.thinkseedo.gasgraph.widget.StatsWidget;
import com.thinkseedo.gasgraph.widget.TimeBarWidget;
import com.thinkseedo.gasgraph.widget.TimeBarWidget.OnChangedListener;

import taptwo.widget.TitleProvider;

import android.app.Activity;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;

public class ListAppAdapter extends BaseAdapter implements TitleProvider {

    private static final int STATS_VIEW = 0;
    private static final int FILL_VIEW = 1;
    private static final int SERVICE_VIEW = 2;
    private static final int IO_VIEW = 3;
    private static final int HOWTO_VIEW = 4;
    private static final int EXPORT_VIEW = 30;
    private static final int IMPORT_VIEW = 40;
    //private static final int PREF_VIEW = 4;
    private static final int CARS_VIEW = 5;
    private static final int VIEW_MAX_COUNT = IO_VIEW + 1;
    
    private static enum ViewType { HOWTO, FILL, STATS, IO, NONE};
    private static ViewGroup[] mViewList = new ViewGroup[4];
    private static View[] mViews = new View[4];
    private final String[] names = {"How to. . .", "History", "Statistics", "Import/Export"};
    private final String[] shortnames = {"howto", "fillups", "stats", "io"};
    public static Map<String,View> viewMap = new TreeMap<String,View>();
    Handler mHandler = new Handler();
    private static int delay   = 500;
    //private static int delayFG = 500;
    private static int delayFG = 1;
    private static int posFG   = 0;

    private LayoutInflater mInflater;
    Context ctx;

    public ListAppAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ctx = context;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    
    public void setPosFG(int pos) {
    	posFG = pos;
    }

    @Override
    public int getViewTypeCount() {
        //return ViewType.values().length; // VIEW_MAX_COUNT;
        return 4;
    }

    @Override
    public int getCount() {
        //return names.length;
    	return 4;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    void switchToExport(ViewSwitcher vs, boolean showExport) {
    	View v = vs.getCurrentView();
    	ImportWidget w = (ImportWidget)v.findViewById(R.id.importWidget);
    	if ( (w == null) && showExport == false ) {
    		// then this is the export view.
    		vs.showNext();
    	} else if ( (w != null) && showExport == true ) {
    		// this is the import view.
    		vs.showNext();
    		v = vs.getCurrentView();
    		ExportWidget ex = (ExportWidget)v.findViewById(R.id.exportWidget);
    		EditText et = (EditText)ex.findViewById(R.id.editExportFilename);
    		et.setFocusable(true);
    	}
    }
    
    void setupLayout(final ViewGroup vg, 
    				 final int position, 
    				 final int layoutId,
    				 int delayLocal) {
    	mViewList[position] = vg;
        vg.postDelayed(new Runnable() {
        	@Override
        	public void run() {
        		mViews[position] = mInflater.inflate(layoutId, null);
        		setupView(position, mViews[position]);
				mViewList[position].addView(mViews[position], 0);
			}
        }, delayLocal);
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	/* if ( convertView != null ) {
    	   remove child view (and save it)
    	   then call setupLayout()
    	} */
    	if ( convertView == null ) {
    		convertView = mInflater.inflate(R.layout.frame, null);
    		switch (ViewType.values()[position]) {
       			case HOWTO:
       				setupLayout((ViewGroup)convertView, position, R.layout.webviewdialog, delay*2);
      				break;
       			case FILL:
       				setupLayout((ViewGroup)convertView, position, R.layout.fillup_list, 0);
       				break;
       			case STATS:
       				setupLayout((ViewGroup)convertView, position, R.layout.stats_view, 1);
       				break;
       			case IO:
       				setupLayout((ViewGroup)convertView, position, R.layout.io_one, delay*3);
       				break;
       		}
    	} 
    	return convertView;
    }

//    @Override
//    public View getView(final int position, View convertView, ViewGroup parent) {
    View setupView(int position, View convertView) {
        Lg.e("listappadapter: " + position + "  & convertView==null");
        viewMap.put(shortnames[position], convertView);
        switch (ViewType.values()[position]) {
        	case STATS:
            	//convertView = mInflater.inflate(R.layout.stats_view, null);
            	final StatsWidget sw = (StatsWidget)convertView; //findViewById(R.id.statsWidget);
            	//TimeBarWidget tbw = (TimeBarWidget)sw.findViewById(R.id.timeBar);
            	//tbw.setOnChangedListener(new OnChangedListener() {
				//	@Override
				//	public void onChanged(int pos, final Date since) {
				//		Lg.e("onChange() pos: " + pos + " date: " + since);
				//		sw.postDelayed(new Runnable() {
				//			@Override
				//			public void run() {
				//				sw.update(since);
				//				App.deleteImages();
				//			}
				//		}, 100);
				//	}
               	//});
               	//sw.post(new Runnable() {
				//	@Override
				//	public void run() {
				//		sw.update(null);
				//	}
               	//});
          		break;
        	case FILL:
               	//convertView = mInflater.inflate(R.layout.fillup_list, null);
               	//App.loadFillupsFromDB();
           		break;
           		//case SERVICE:
                //	convertView = mInflater.inflate(R.layout.service_list, null);
                //	App.loadServicesFromDB();
           		//	break;
                	//convertView = mInflater.inflate(R.layout.text, null);
           	//case NONE:
            //   	convertView = mInflater.inflate(R.layout.frame, null);
           	//	setupLayout((ViewGroup)convertView, position, R.layout.text, delay);
            //   	break;
           	case HOWTO:
              	//convertView = mInflater.inflate(R.layout.frame, null);
           		//setupLayout((ViewGroup)convertView, position, R.layout.webviewdialog, delay);
               	final String file;
               	if ( App.MARKET == App.MARKET_AMAZON ) {
               		file= "web_usage_amz.html";
               	} else {
               		file= "web_usage.html";
               	}
                Button b = (Button)convertView.findViewById(R.id.buttonDialogDismiss);
                b.setVisibility(View.GONE);
                final WebView w = (WebView)convertView.findViewById(R.id.webViewDialog);
                mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						w.setVisibility(View.VISIBLE);
						w.loadUrl("file:///android_asset/" + file);
						w.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
						w.setScrollContainer(true);
					}
                }, (position == posFG ? delayFG : delay ));
                break;
           	case IO:
               	//convertView = mInflater.inflate(R.layout.io_one, null);
               	final ViewSwitcher vs = (ViewSwitcher)convertView.findViewById(R.id.switcher);
               	OnClickListener exp = new OnClickListener() {
					@Override
					public void onClick(View v) {
						switchToExport(vs, true);
					}
               	};
               	OnClickListener imp = new OnClickListener() {
					@Override
					public void onClick(View v) {
						switchToExport(vs, false);
					}
               	};
               	final ImportWidget iw = (ImportWidget)convertView.findViewById(R.id.importWidget);
               	mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						iw.updateAll();
					}
               	}, (position == posFG ? delayFG : delay ));
               	final ExportWidget ex = (ExportWidget)convertView.findViewById(R.id.exportWidget);
               	final EditText et = (EditText)ex.findViewById(R.id.editExportFilename);
               	if ( et != null ) {
               		et.setFocusable(false);
					et.setFocusableInTouchMode(false);
               		et.postDelayed(new Runnable() {
						@Override
						public void run() {
							Lg.e("set focusable true");
							et.setFocusable(true);
							et.setFocusableInTouchMode(true);
						}
               		}, delayFG);
               	}
               	
               	ImageView impImage = (ImageView)convertView.findViewById(R.id.imageImport);
               	TextView impView = (TextView)convertView.findViewById(R.id.textImport);
               	impImage.setOnClickListener(imp);
               	impView.setOnClickListener(imp);
               	
               	ImageView expImage = (ImageView)convertView.findViewById(R.id.imageExport);
               	TextView expView = (TextView)convertView.findViewById(R.id.textExport);
               	expImage.setOnClickListener(exp);
               	expView.setOnClickListener(exp);
               	break;
           	default:
           		convertView = mInflater.inflate(R.layout.service_header, null);
           		break;
        }
        return convertView;
    }
    
    public void createWebViewDialog(View view, String filename) {
    	final Button dismiss = (Button)view.findViewById(R.id.buttonDialogDismiss);
    	WebView w = (WebView)view.findViewById(R.id.webViewDialog);
    	dismiss.setVisibility(View.GONE);
    	w.setBackgroundColor(0xFF000000);
        w.loadUrl("file:///android_asset/" + filename);
        w.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }


    /* (non-Javadoc)
	 * @see org.taptwo.android.widget.TitleProvider#getTitle(int)
	 */
	public String getTitle(int position) {
		return names[position];
	}

}
