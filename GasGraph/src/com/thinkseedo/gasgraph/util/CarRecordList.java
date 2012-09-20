package com.thinkseedo.gasgraph.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.thinkseedo.gasgraph.App;

public class CarRecordList {
	public static String HEADER = "\"make\"," + 
							      "\"model\","+
							      "\"year\","+
							      "\"distance\"," + 
							      "\"volume\"," + 
							      "\"note\"\n"
							      ;
	static String q(String s) { return "\"" + s + "\""; }
	static public void saveToFile(OutputStream out, List<String> list) {
  		StringBuilder ret = new StringBuilder();
  		String sep = ",";
		ret.append("## vehicles\n")
			.append(HEADER)
			.append(q(App.getPrefs().getString("make", "Volvo"))).append(sep)
			.append(q(App.getPrefs().getString("model", "850"))).append(sep) 
			.append(q(App.getPrefs().getString("year", "1997"))).append(sep)
			.append(q(App.getPrefs().getString("distance", "mi"))).append(sep)
			.append(q(App.getPrefs().getString("volume", "gal"))).append(sep)
			.append(q(App.getPrefs().getString("note", "")));
		
        try {
        	ArrayList<String> lines = new ArrayList<String>();
        	lines.add(ret.toString());
        	Import.writeFileLines(out, lines);
        } catch (IOException e){
			App.postDataError("IO", "save gas to file", e.toString());
        }

	}
}
