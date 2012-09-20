package com.thinkseedo.gasgraph.database;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.util.Import;
import com.thinkseedo.gasgraph.util.Lg;

@DatabaseTable
public class CostRecord extends Record implements Serializable {
	private static final long serialVersionUID = -7538442522617311301L;
	public static final String DATE_FIELD_NAME = "mDate";
   	
	//@DatabaseField(generatedId = true)
   	//public Integer id; 
   	//@DatabaseField(index=true, columnName=DATE_FIELD_NAME, dataType=DataType.DATE_LONG)
   	//public Date	   mDate;
   	//@DatabaseField
   	//public Integer mDistance;
   	@DatabaseField
   	public Double  mPrice;
   	@DatabaseField
   	public String  mType;
   	@DatabaseField(defaultValue="false")
   	public Boolean mMark;
   	@DatabaseField
   	public String  mNote;
   	@DatabaseField(defaultValue="false")
   	public Boolean mSaved;
   	
   	
   	public Double  mDistD;
   	public Double  mDateD;
   	
   	public CostRecord(Import.RecordRow record) {
		mDate     = App.parseDate(record.get("date"));
   		mDistance = (new Double(record.get("distance"))).intValue();
   		mPrice    = new Double(record.get("price"));
   		mType     = new String(record.get("type"));
   		mMark	  = new Boolean(record.get("mark"));
   		mNote	  = new String(record.get("note"));
   	}
   	
   	public CostRecord(Date date, Integer distance, 
   				  Double price, String type, 
   				  Boolean mark, String note) {
   		this.update(date, distance, price, type, mark, note);
   	}
   	
   	public CostRecord() {
   	}
   	
   	public void update(CostRecord cr) {
   		if ( cr != null ) {
   			mDate     = cr.mDate; 
   			mDistance = cr.mDistance;
   			mPrice    = cr.mPrice;
   			mType     = cr.mType;
   			mMark 	  = cr.mMark;
   			mNote 	  = cr.mNote;
   		}
   	}
   	
   	public void update(Date date, Integer distance, Double price, 
   						String type, Boolean mark, String note) {
   		mDate     = date; 
   		mDistance = distance;
   		mPrice    = price;
   		mType     = type;
   		mMark     = mark;
   		mNote     = note;
   	}
   	
   	private void setDefaults() {
		mDate	  = App.parseDate(null);
   		mDistance = new Integer(0);
   		mPrice    = new Double(0.0);
   		mType     = "";
   		mNote     = "";
   		mMark     = false;
   	}
   	
   	public String toString() {
   		return toString(",");
    }
   	
   	public String toString(String sep) {
  		StringBuilder ret = new StringBuilder();
        String date = App.formatDate(mDate, false);
        
        ret.append("\"" + App.getPrefs().getString("make", "Volvo") + "\"").append(sep)
           .append("\"" + App.getPrefs().getString("model", "850") + "\"").append(sep)
           .append("\"" + App.getPrefs().getString("year", "1997") + "\"").append(sep)
           .append("\"" + date + "\"").append(sep)
           .append("\"" + mDistance + "\"").append(sep)
           .append("\"" + mPrice + "\"").append(sep)
           .append("\"" + mMark + "\"").append(sep)
           .append("\"" + mType + "\"").append(sep)
           .append("\"" + mNote + "\"").append(sep);
    	return ret.toString();
   	}
   	public static String HEADER = "\"make\",\"model\",\"year\"," + 
   								  "\"date\",\"distance\",\"price\"," + 
   								  "\"mark\",\"type\",\"note\"";
   	
   	
    public static void addCostRecord(Dao<CostRecord, Integer> crDao, CostRecord cr) {
		int ret;
		try {
			ret = crDao.create(cr);
			assert ret == 1;
		} catch (SQLException e) {
			Lg.e("SQL: add data to db: " + e.toString());
			App.postDataError("SQL", "add data to db", e.toString());
		}
    }

	@Override
	public int getType() {
		return Record.TYPE_COST;
	}
}
