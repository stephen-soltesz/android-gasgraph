package com.thinkseedo.gasgraph.database;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.util.Import;
import com.thinkseedo.gasgraph.util.Import.HeaderSet;
import com.thinkseedo.gasgraph.util.Import.MissingColumnException;
import com.thinkseedo.gasgraph.util.Lg;

@DatabaseTable
public class GasRecord extends Record implements Serializable {
	private static final long serialVersionUID = -7538442522617311201L;
	public Integer mPosition=0; // position holder.

	public static final String DATE_FIELD_NAME = "mDate";
   	
	//@DatabaseField(generatedId = true)
   	//public Integer id; 
   	//@DatabaseField(index=true, columnName=DATE_FIELD_NAME, dataType=DataType.DATE_LONG)
   	//public Date	   mDate;
   	@DatabaseField
   	public Double  mVolume;
   	@DatabaseField
   	public Double  mPrice;
   	//@DatabaseField
   	//public Integer mDistance;
   	@DatabaseField
   	public Boolean mFilled;
   	@DatabaseField
   	public String  mType;
   	@DatabaseField
   	public String  mLocation;
   	@DatabaseField(defaultValue="false")
   	public Boolean mSaved;
   	
   	@DatabaseField
   	public Boolean mMissed;
   	@DatabaseField
   	public String  mNotes;
   	
   	// Calculated values
   	public Double  mMPG;
   	public Double  mPPM;
   	public Double  mDD;
   	
   	public GasRecord(Import.RecordRow record) throws NumberFormatException {
   		for (String s : record.keySet() ) {
   			Lg.d("row: " + s  + " " + record.get(s));
   		}
   		/* required fields */
		mDate     = App.parseDate(record.get("date"));
   		mDistance = (new Double(record.get("mileage", "distance"))).intValue();
   		mVolume   = new Double(record.get("volume", "fuel"));
   		mPrice    = new Double(record.get("price"));
   		
   		/* helpful fields */
   		mFilled   = new Boolean((record.get("partial",false).equals("1") ? false : true ) );
   		mMissed   = new Boolean((record.get("missed", false).equals("1") ? true : false ) );
   		mType     = new String(record.get("type", 87));
   		mNotes	  = new String(record.get("note", -1));	
   		mLocation = "";
   	}
   	
   	public GasRecord(String entry) throws Exception {
   		this.setDefaults();
   		String[] fields = entry.split(",");
   		String[] clean = new String[fields.length];
   		String make  = App.getPrefs().getString("make", "Volvo");
   		String model = App.getPrefs().getString("model", "850");
   		for ( int i=0; i < fields.length; i ++ ) {
   			clean[i] = fields[i].replaceAll("^\"|\"$",""); //substitute)substring(1,fields[i].length()-2);
   		}
   		if ( fields.length >= 7 ) {
   			if ( clean[0].equals(make) && clean[1].equals(model) ) {
   				mDate     = App.parseDate(clean[2]);
   				mDistance = (new Double(clean[3])).intValue();
   				mVolume   = new Double(clean[4]);
   				mPrice    = new Double(clean[5]);
   				Lg.i("partial: " + clean[6]);
   				if ( clean[6].equals("0") ) {
   					mFilled = true;
   				} else {
   					mFilled = false;
   				}
   				
   				String[] type_and_location = clean[7].split(" ");
   				mType   = new String(type_and_location[0]);
				mMissed = false;
   				if ( type_and_location.length > 1 ) { 
   					mLocation = type_and_location[1];
   					if ( mLocation.matches(".*break.*") ) {
   						mMissed = true;
   					}
   				} else {
   					mLocation = "default";
   				}
   				mMPG      = null; 
   				mPPM      = null; 
   				mDD       = null; 
   			} else {
   				throw new Exception("make model mismatch: "+ make + model + "  " + clean[0] + clean[1]);
   			}
   		} 
   	}
   	public GasRecord(Date date, Double price, 
   					 Double volume, Integer distance, 
   					 boolean filled, String type, String location, 
   					 boolean missed, String notes) {
   		this.update(date, price, volume, distance, filled, 
   					 type, location, missed, notes);
   	}
   	public GasRecord() {
   	}
   	public void update(GasRecord gr) {
   		if ( gr != null ) {
   			mDate     = gr.mDate; 
   			mVolume   = gr.mVolume;
   			mPrice    = gr.mPrice;
   			mDistance = gr.mDistance;
   			mFilled   = gr.mFilled;
   			mType     = gr.mType;
   			mLocation = gr.mLocation;
   			mMissed   = gr.mMissed;
   			mNotes    = gr.mNotes;
   		}
   	}
   	public void update(Date date, Double price, 
   					   Double volume, Integer distance, 
   					   boolean filled, String type, String location,
   					   boolean missed, String notes) {
   		mDate     = date; 
   		mVolume   = volume;
   		mPrice    = price;
   		mDistance = distance;
   		mFilled   = filled;
   		mType     = type;
   		mLocation = location;
   		mMissed   = missed;
   		mNotes    = notes;
   	}
   	private void setDefaults() {
		mDate	  = App.parseDate(null);
   		mVolume   = new Double(0.0);
   		mPrice    = new Double(0.0);
   		mDistance = new Integer(0);
   		mFilled   = new Boolean(true);
   		String list[] = App.getContext().getResources().getStringArray(R.array.fuel_type);
   		mType     = new String(list[0]);
   		mLocation = new String("default");
   		mMissed   = new Boolean(false);
   		mNotes    = "";
   		mMPG      = null; 
   		mPPM      = null; 
   		mDD       = null; 
   	}
   	public String toString(String sep) {
  		StringBuilder ret = new StringBuilder();
        String date = App.formatDate(mDate, false);
        String partial = null;
        String missed = null;
        if ( mFilled ) {
        	partial = "0";
        } else {
        	partial = "1";
        }
        if ( mMissed ) {
        	missed = "1";
        } else {
        	missed = "0";
        }
        
        ret.append("\"" + App.getPrefs().getString("make", "Volvo") + "\"").append(sep)
           .append("\"" + App.getPrefs().getString("model", "850") + "\"").append(sep)
           .append("\"" + App.getPrefs().getString("year", "1997") + "\"").append(sep)
           .append("\"" + date 		+ "\"").append(sep)
           .append("\"" + mDistance + "\"").append(sep)
           .append("\"" + mVolume 	+ "\"").append(sep)
           .append("\"" + mPrice 	+ "\"").append(sep)
           .append("\"" + partial 	+ "\"").append(sep)
           .append("\"" + missed 	+ "\"").append(sep)
           .append("\"" + mType 	+ "\"").append(sep)
           .append("\"" + mNotes 	+ "\"");
    	return ret.toString();
   	}
	public static String[] HEADER_LIST = { "make", "model", "year","date",
								  		   "mileage","volume","price",
								  		   "partial","missed","type",
								  		   "note" };
	static String q(String s) { return "\"" + s + "\""; }
	
	public static boolean hasMinimumHeaderSet(Set<String> set) {
		boolean hasDate=false, hasDistance=false, 
				hasVolume=false, hasPrice=false;
		for ( String header : set ) {
			if ( header.equals("date") ) { 
				hasDate = true;
			}
			if ( header.equals("price") ) {
				hasPrice = true;
			}
			if ( header.equals("mileage") || header.equals("distance") ) {
				hasDistance = true;
			}
			if ( header.equals("volume") || header.equals("fuel") ) {
				hasVolume=true;
			}
		}
		return (hasDate && hasPrice && hasDistance && hasVolume);
	}
	public static String getHeader() {
  		StringBuilder ret = new StringBuilder();
  		String sep = ",";
		for (int i=0; i < HEADER_LIST.length; i++ ) {
			ret.append(q(HEADER_LIST[i])).append(sep);
		}
		return ret.toString();
	}
	
	public static String HEADER = "\"make\",\"model\",\"year\",\"date\"," + 
								  "\"mileage\",\"volume\",\"price\"," + 
								  "\"partial\",\"missed\",\"type\"," + 
								  "\"note\"";
   	public String toString() {
   		return toString(",");
    }
	@Override
	public int getType() {
		return Record.TYPE_GAS;
	}
}
