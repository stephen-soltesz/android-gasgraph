package com.thinkseedo.gasgraph;

import java.util.Calendar;

import com.thinkseedo.gasgraph.util.GasRecordList;
import com.thinkseedo.gasgraph.util.Lg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ExportCSV extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_view);
        String s = "gasgraph-" + App.formatDate(null, App.DATE_FILENAME);
        App.setText(this, R.id.editExportFilename, s);
        App.setListener(this, R.id.buttonExport, mExportListener);
    }
    
    OnClickListener mExportListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String s = App.getText(ExportCSV.this, R.id.editExportFilename);
			String fn = s.replaceAll("[^a-zA-Z0-9\\-_]", "");
			if ( !s.equals(fn) ) {
				Toast.makeText(ExportCSV.this, "Filename changed to remove illegal characters.", Toast.LENGTH_SHORT).show();
				App.setText(ExportCSV.this, R.id.editExportFilename, fn);
			}
			Lg.d("filename: " + fn);
			GasRecordList.exportRecords(fn+".csv");
		}
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //closeHelper();
        //doUnbindService();
        //stopService(new Intent(App.getContext(), Background.class));
    }
  

}
