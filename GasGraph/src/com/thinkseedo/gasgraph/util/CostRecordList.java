package com.thinkseedo.gasgraph.util;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.database.CostRecord;
import com.thinkseedo.gasgraph.database.GasRecord;
import com.thinkseedo.gasgraph.database.Record;


public class CostRecordList { 
    //public List<CostRecord> mList;
    public RecordList mList;
    static String DIRNAME="GasGraph";
	
	//public CostRecordList(List<CostRecord> mlist2) {
	//	mList = mlist2;
   	//}
	public CostRecordList(RecordList mlist2) {
		mList = mlist2;
   	}
	
	/* public class RecordList extends List<Record> {
	 *    insert(Record r);
	 *    get(int pos);
	 *    
	 */
	
	public List<Double> getValuesFor(String field) {
		List<Double> ret = new ArrayList<Double>();
		for (int i=0; i < mList.size(); i ++ ) {
			CostRecord cr = (CostRecord)mList.get(i);
			ret.add(App.getVal(field, cr));
		}
		return ret;
	}
	
	public List<Date> getDatesFor(String field) {
		List<Date> ret = new ArrayList<Date>();
		for (int i=0; i < mList.size(); i ++ ) {
			CostRecord cr = (CostRecord)mList.get(i);
			ret.add(cr.mDate);
		}
		return ret;
	}
	
   	public void updateCalculatedValues() {
   		if ( mList.size() == 0 ) {
   			return;
   		}
   		for (int i=mList.size()-2; i >= 0; i-- ) {
   			Record r = (Record)mList.get(i);
   			if ( r instanceof CostRecord ) {
   				CostRecord cr = (CostRecord)mList.get(i);
	   			cr.mDistD = (double)(cr.mDistance - mList.get(i+1).mDistance);
	   			Lg.d("i: " + i + " " + cr.mDate + " " + cr.mDistD);
   			}
   		}
   		return;
   	}
   	
    public static CostRecordList simpleReadFromDB() {
		CostRecordList crl = null;
        try {
			RecordList rl = new RecordList();
			
			QueryBuilder<CostRecord, Integer> builder = App.getCostDao().queryBuilder();
			builder.orderBy(CostRecord.DATE_FIELD_NAME, false);
			List<CostRecord> mList = App.getCostDao().query(builder.prepare());
			for ( CostRecord c : mList ) { rl.add(c); }
			
			//QueryBuilder<GasRecord, Integer> grbuilder = App.getDao().queryBuilder();
			//builder.orderBy(CostRecord.DATE_FIELD_NAME, false);
			//List<GasRecord> mList2 = App.getDao().query(grbuilder.prepare());
			//for ( GasRecord g : mList2 ) { 
			//	try { 
			//	   rl.add(g); 
			//	} catch ( ClassCastException e ) {
			//		Lg.e("could not add: " + e);
			//	}
			//}
			
			crl = new CostRecordList(rl);
		} catch (SQLException e) {
			Lg.d("error: " + e);
			App.postDataError("SQL", "loading data from db", e.toString());
		}
        return crl;
    }
 
    public static CostRecordList simpleReadFromDB_old() {
		CostRecordList crl = null;
        try {
			Lg.d("error: a");
       		Dao<CostRecord, Integer> crDao;
       		crDao = App.getCostDao();
			Lg.d("error: b");
			QueryBuilder<CostRecord, Integer> builder = crDao.queryBuilder();
			builder.orderBy(CostRecord.DATE_FIELD_NAME, false);
			Lg.d("error: c");
			List<CostRecord> mList = crDao.query(builder.prepare());
			RecordList rl = new RecordList();
			for ( CostRecord c : mList ) {
				Lg.d(""+c);
				rl.add(c);
			}
			Lg.d("error: d");
			//crl = new CostRecordList(mList);
			crl = new CostRecordList(rl);
		} catch (SQLException e) {
			Lg.d("error: " + e);
			App.postDataError("SQL", "loading data from db", e.toString());
		}
        return crl;
    }

    public static void simpleResetDB(OrmLiteSqliteOpenHelper helper) {
    	try {
    		// try to delete everything.
        	Dao<CostRecord, Integer> grDao;
        	grDao = App.getCostDao();
    		DeleteBuilder<CostRecord, Integer> deleteBuilder = grDao.deleteBuilder();
    		grDao.delete(deleteBuilder.prepare());
		} catch (SQLException e) {
			App.postDataError("SQL", "reset db", e.toString());
		}
    }
    
    public static void saveToFile(OutputStream outputStream, RecordList mList2) {
  		StringBuilder ret = new StringBuilder();
    	ret.append("## costs\n")
    	   .append(CostRecord.HEADER);
        try {
        	ArrayList<String> lines = new ArrayList<String>();
        	Iterator<Record> iter = mList2.iterator();
        	lines.add(ret.toString());
        	while (iter.hasNext()) {
        		Record r = (Record)iter.next();
        		if ( r instanceof CostRecord ) {
        			CostRecord rec = (CostRecord)r;
        			lines.add(rec.toString());
        		}
        	}
        	Import.writeFileLines(outputStream, lines);
        } catch (IOException e){
			App.postDataError("IO", "save gas to file", e.toString());
        }
    	return;
    }
    
    /*
    static void readVehicleFromList(ArrayList<String> list) {
       	Iterator<String> iter = list.iterator();
       	// find the vehicles marker and consume header
       	while ( iter.hasNext() )  {
       		String line = iter.next();
       		if ( line.equals("## vehicles") ) { 
       			String header = iter.next();
       			break; 
       		} 
       	} 

       	// read the first vehicle as make/model for all remaining lines.
       	try {
       		String[] fields = iter.next().split(",");
       		String make  = fields[0].replaceAll("^\"|\"$", "");
       		String model = fields[1].replaceAll("^\"|\"$", "");
       		App.getPrefs().edit().putString("make", make).commit();
       		App.getPrefs().edit().putString("model", model).commit();
       	} catch (Exception e) {
       		Lg.e("error parsing make/model from CSV");
       	}
       	return;
    } */
    
    /*
    static CostRecordList readFillupsFromList(ArrayList<String> list ) {
       	Iterator<String> iter = list.iterator();
       	ArrayList<CostRecord> mList = new ArrayList<CostRecord>();
    	
       	// find the fillups marker and consume header.
       	while ( iter.hasNext() )  {
       		String line = iter.next();
       		if ( line.equals("## fillups") ) { 
       			String header = iter.next();
       			break; 
       		} 
       	}
        	
        // read all fill-up lines until the 'costs' marker and stop.
        while (iter.hasNext()) {
        	String line = iter.next();
        	if ( line.equals("## costs") ) { break; } 
        	Lg.d("converting: " + line);
        	try {
				CostRecord gr = new CostRecord(line);
				Lg.e("adding record: " + gr.mDate);
				mList.add(gr);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        return new CostRecordList(mList);
    } */
    

}
