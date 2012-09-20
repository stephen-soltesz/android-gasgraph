package com.thinkseedo.gasgraph.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
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
//import com.thinkseedo.gasgraph.GasGraphActivity;
import com.thinkseedo.gasgraph.R;
import com.thinkseedo.gasgraph.database.DatabaseHelperSample;
import com.thinkseedo.gasgraph.database.GasRecord;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

public class GasRecordList { 
    public List<GasRecord> mList;
    static String DIRNAME="GasGraph";
	
	public GasRecordList(List<GasRecord> mlist2) {
		mList = mlist2;
   	}
	public List<Double> getValuesFor(String field) {
		return getValuesFor(field, null);
	}
	public List<Double> getValuesFor(String field, Date since, int atLeast) {
		// get values since.
		List<Double> set = getValuesFor(field, since);
		// then check the size.
		if ( set.size() > 0 && set.size() < atLeast ) {
			// if size is off, get all fields, and return a subset.
			set = getValuesFor(field, null);
			if ( set.size() >= atLeast ) {
			    return set.subList(0, atLeast);
			}
		}
		// return original set if size == 0  or >= atLeast
		return set;
	}
	
	public List<Double> getValuesFor(String field, Date since) {
		List<Double> ret = new ArrayList<Double>();
		for (int i=0; i < mList.size(); i ++ ) {
			GasRecord gr = mList.get(i);
			if ( since == null || gr.mDate.after(since) ) {
				if ( gr.mMPG != null ) {
					if ( gr.mDD != null && gr.mDD > 100 ) {
						ret.add(App.getVal(field, gr));
					}
				}
			}
		}
		return ret;
	}
	
	public List<Date> getDatesFor(String field) {
		return getDatesFor(field, null);
	}
	
	public List<Date> getDatesFor(String field, Date since) {
		List<Date> ret = new ArrayList<Date>();
		for (int i=0; i < mList.size(); i ++ ) {
			GasRecord gr = mList.get(i);
			if ( since == null || gr.mDate.after(since) ) {
			if ( gr.mMPG != null ) {
				if ( gr.mDD != null && gr.mDD > 100 ) {
					ret.add(gr.mDate);
				}
			}
			}
		}
		return ret;
	}
	public List<Date> getDatesSince(Date since, int atLeast) {
		List<Date> set = getDatesSince(since);
		if ( set.size() > 0 && set.size() < atLeast ) {
			// if size is off, get all fields, and return a subset.
			set = getDatesSince(null);
			if ( set.size() >= atLeast ) {
			    return set.subList(0, atLeast);
			}
		}
		
		return set;
	}
	
	public List<Date> getDatesSince(Date since) {
		List<Date> ret = new ArrayList<Date>();
		for (int i=0; i < mList.size(); i ++ ) {
			GasRecord gr = mList.get(i);
			if ( since == null || gr.mDate.after(since) ) {
				ret.add(gr.mDate);
			}
		}
		return ret;
		
	}
	
	public void remove(int id) {
   		if ( mList.size() == 0 ) {
   			return;
   		}
   		for ( int i=0; i < mList.size(); i++) {
   			GasRecord gr = mList.get(i);
   			if ( gr.id == id ) {
   				mList.remove(i);
   				return;
   			}
   		}
	}
	
	// position 0 is the largest date.
   	public boolean add(GasRecord gr){
   		// NOTE: insert and order by date 
   		if ( mList.size() == 0 ) {
   			mList.add(gr);
   		} else if ( mList.size() == 1 ) {
   			GasRecord next=null;
   			next = mList.get(0);
   			if ( gr.mDate.after(next.mDate) ) {
   				mList.add(0,gr);
   			} else {
   				mList.add(gr);
   			}
   		} else {
   			// 2 or more GRs
   			GasRecord pos=null;
	   		for ( int i=0; i < mList.size(); i++) {
	   			pos = mList.get(i);
	   			if ( gr.mDate.after(pos.mDate) ) {
	   				// found it.
	   				mList.add(i,gr);
	   				return true;
	   			} 
   			}
	   		mList.add(gr);
   		}
   		return true;
   	}
   	
   	public void updateCalculatedValues() {
   		if ( mList.size() == 0 ) {
   			return;
   		}
   		double volume_total = 0.0;
   		boolean filled = false;
   		GasRecord prev=null;
   		for (int i=mList.size()-2; i >= 0; i-- ) {
   			GasRecord gr = mList.get(i);
	   		if ( gr.mLocation != null && !gr.mLocation.equals("break") ) {
	   			gr.mDD = (double)(gr.mDistance - mList.get(i+1).mDistance);
	   		}
	   		volume_total=0;
	   		Lg.i("gr: " + i + " vol: " + gr.mVolume);
   			if ( gr.mFilled.equals(true) ) {
   				volume_total += gr.mVolume;
   				prev = gr;
   				for ( int j=i+1; j < mList.size(); j++) {
	   				prev = mList.get(j);
	   				if ( prev.mFilled.equals(true) ) {
	   					filled = true;
	   					break; 
	   				} else {
	   					Lg.i("pr: " + i + " vol: " + prev.mVolume);
	   					volume_total += prev.mVolume;
	   					Lg.d("vol: " + volume_total);
	   				}
	   			}
	   			if ( gr.mLocation.equals("break") || gr.mMissed ) {
	   				gr.mMPG = null;
	   				gr.mPPM = null;
	   				gr.mDD  = null;
	   			} else if ( gr.mDistance > prev.mDistance &&
	   						volume_total > 0 &&
	   						filled == true )
	   			{
	   				Lg.d("d: " + gr.mDistance + " - " + prev.mDistance + " = " + (gr.mDistance-prev.mDistance));
	   				Lg.d("v: " + volume_total);
	   				gr.mMPG = (gr.mDistance - prev.mDistance)/(volume_total);
	   				Lg.d("mpg: " + gr.mMPG);
	   				gr.mPPM = 100*(gr.mPrice * volume_total)/(gr.mDistance - prev.mDistance);
	   			}
   			}
   		}
   		return;
   	}
   	
    public static void writeFileLines(OutputStream outputStream, ArrayList<String> lines) throws IOException {
    	// new DataOutputStream(new FileOutputStream(file, false));
    	DataOutputStream buf = (DataOutputStream)outputStream; 
        Iterator<String> iter = lines.iterator();
        while (iter.hasNext()) { 
        	String line = iter.next();
        	buf.writeBytes(line + "\n"); 
        }
    	buf.close();
    	return;
    }
    
    public static ArrayList<String> readFileLines( InputStream inputStream ) throws IOException {
    	DataInputStream buf = (DataInputStream)inputStream;
    	ArrayList<String> ret = new ArrayList<String>();
    	int line_count = 0;
        while ( true ) {
        	String line = buf.readLine();
        	line_count += 1;
        	if ( line == null ) { break; }
        	ret.add(line);
        }
        Lg.d("lines: " + line_count);
        return ret;
    }
    
    public static void saveToFile(OutputStream outputStream, List<GasRecord> mList2) {
  		StringBuilder ret = new StringBuilder();
    	ret.append("## fillups\n")
    	   .append(GasRecord.HEADER);
        try {
        	ArrayList<String> lines = new ArrayList<String>();
        	Iterator<GasRecord> iter = mList2.iterator();
        	lines.add(ret.toString());
        	while (iter.hasNext()) {
        		GasRecord rec = iter.next();
        		lines.add(rec.toString());
        	}
        	Import.writeFileLines(outputStream, lines);
        } catch (IOException e){
			App.postDataError("IO", "save gas to file", e.toString());
        }
    	return;
    }
    public static void saveGasToFile(OutputStream outputStream, List<GasRecord> mList2) {
        // TODO: add 'car name' to the 'filename'
        //String filename = App.getContext().getResources().getString(R.string.filename);
  		StringBuilder ret = new StringBuilder();
  		
		ret.append("## vehicles\n")
    		.append("\"make\",\"model\",\"note\",\"distance\",\"volume\",\"consumption\"\n")
    		.append("\"" + App.getPrefs().getString("year", "1997") + "::" + App.getPrefs().getString("make", "Volvo") + 
    				"\",\"" + App.getPrefs().getString("model", "850") + 
    				"\",\"\",\"2\",\"2\",\"2\"\n")
    		.append("## fillups\n")
    		.append("\"make\",\"model\",\"date\",\"mileage\",\"fuel\",\"price\",\"partial\",\"note\"");
        try {
        	ArrayList<String> lines = new ArrayList<String>();
        	Iterator<GasRecord> iter = mList2.iterator();
        	lines.add(ret.toString());
        	while (iter.hasNext()) {
        		GasRecord rec = iter.next();
        		lines.add(rec.toString());
        	}
        	writeFileLines(outputStream, lines);
        } catch (IOException e){
			App.postDataError("IO", "save gas to file", e.toString());
        }
    	return;
    }
    
    static int checkExternalStorage() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            return 2;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
        	return 1;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
        	return 0;
        }
    }
    public static OutputStream getOutputStream(String filename, boolean internal) 
    	   throws FileNotFoundException {
    	return new DataOutputStream(new FileOutputStream(getImportExportFile(filename, internal)));
    }
    public static OutputStream getOutputStream(boolean internal) 
    	   throws FileNotFoundException {
    	return new DataOutputStream(new FileOutputStream(getImportExportFile(internal)));
    }
    public static InputStream getInputStream(boolean internal) 
    	   throws FileNotFoundException {
    	return new DataInputStream(new FileInputStream(getImportExportFile(internal)));
    }
    public static InputStream getInputStream(File file) 
    	   throws FileNotFoundException {
    	return new DataInputStream(new FileInputStream(file));
    }
    public static File getExternalDir(String dir)  {
    	return new File(Environment.getExternalStorageDirectory(), dir);
    }
    public static File getImportExportDir()  {
    	return getExternalDir(DIRNAME);
    }
    public static File getImportExportFile(boolean internal)  {
   		String filename = App.getContext().getResources().getString(R.string.filename);
   		File dir = null;
    	if ( internal ) {
    		dir = App.getContext().getFilesDir();
    	} else {
    		dir = getImportExportDir(); // Environment.getExternalStoragePublicDirectory(DIRNAME);
    	}
    	dir.mkdirs();
   		File file = new File(dir, filename);
   		Lg.d("IO file: " + file.toString());
   		return file;
    }
    public static File getImportExportFile(String filename, boolean internal)  {
   		File dir = null;
    	if ( internal ) {
    		dir = App.getContext().getFilesDir();
    	} else {
    		dir = getImportExportDir(); 
    	}
    	dir.mkdirs();
   		File file = new File(dir, filename);
   		Lg.d("IO file: " + file.toString());
   		return file;
    }
    public static String[] getExternalFileList(String filename)  {
   		final File dir = getImportExportDir(); 
    	return dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File d, String file) {
				if ( dir.equals(d) && file.matches(".*.csv$") ) {
					return true;
				}
				return false;
			}
    	});
    }

    
    public final static int IMPORT_FU=-1;
    public final static int IMPORT_START=0;
    public final static int IMPORT_OK=1;
    public final static int IMPORT_NO_FILE=2;
    public final static int IMPORT_EXTERNAL_STORAGE_ERROR=3;
    public final static int IMPORT_FORMAT_ERROR=4;

    public static void displayImportMessage(Integer id) {
        File extDir = getImportExportFile(false);
        if ( id == IMPORT_OK ) {
        	App.showToast("Successful Import!");
        } else if ( id == IMPORT_NO_FILE ) {
        	App.showToast("No file found in " + extDir);
        } else if ( id == IMPORT_EXTERNAL_STORAGE_ERROR ) {
        	App.showToast("ERROR: External storage is not Readable");
        } else if ( id == IMPORT_START) {
        	App.showToast("Importing from " + extDir);
        } else if ( id == IMPORT_FORMAT_ERROR ) {
        	//App.showToast("Format error while importing");
        }
    }
    
	public static int importRecords(OrmLiteSqliteOpenHelper helper, String filename, String car) {
        int ext_storage_state = checkExternalStorage();
        if ( ext_storage_state == 2 || ext_storage_state == 1) {
        	try {
        		Dao<GasRecord, Integer> grDao;
        		grDao = App.getDao();
        		File input = getExternalDir(filename);
        		App.mRecords = readGasFromFileSetCar(getInputStream(input), car);
        		Lg.d("Adding to db: ");
			    for ( int i=0; i < App.mRecords.mList.size(); i++ ) {
			    	GasRecord gr = App.mRecords.mList.get(i);
			    	if ( gr != null ) {
			    		grDao.create(gr);
			    	}
        		}
			    App.mRecords.updateCalculatedValues();
			    return IMPORT_OK;
        	} catch (FileNotFoundException e) {
        		//App.postDataError("FileNotFound", "importRecords", e.toString());
        		return IMPORT_NO_FILE;
        	} catch (SQLException e) {
        		App.postDataError("SQL", "importRecords", e.toString());
			}
        } else {
        	return IMPORT_EXTERNAL_STORAGE_ERROR;
        }
		return IMPORT_FU;
	}
	
    public static int importRecords(OrmLiteSqliteOpenHelper helper) {
        int ext_storage_state = checkExternalStorage();
        if ( ext_storage_state == 2 || ext_storage_state == 1) {
        	try {
        		Dao<GasRecord, Integer> grDao;
        		grDao = App.getDao();
        		App.mRecords = readGasFromFile(getInputStream(false));
        		Lg.d("Adding to db: ");
			    for ( int i=0; i < App.mRecords.mList.size(); i++ ) {
			    	GasRecord gr = App.mRecords.mList.get(i);
			    	if ( gr != null ) {
			    		grDao.create(gr);
			    	}
        		}
			    App.mRecords.updateCalculatedValues();
			    return IMPORT_OK;
        	} catch (FileNotFoundException e) {
        		//App.postDataError("FileNotFound", "importRecords", e.toString());
        		return IMPORT_NO_FILE;
        	} catch (SQLException e) {
        		App.postDataError("SQL", "importRecords", e.toString());
			}
        } else {
        	return IMPORT_EXTERNAL_STORAGE_ERROR;
        }
		return IMPORT_FU;
    }
    
    public static void exportRecords(String filename) {
        int ext_storage_state = checkExternalStorage();
        File extDir = getImportExportFile(filename, false);
        if ( ext_storage_state == 2 ) {
        	App.showToast("Exporting to " + extDir);
        	try {
        		OutputStream out = getOutputStream(filename, false);
        		GasRecordList.saveGasToFile(out, App.mRecords.mList);
        		App.showToast("Successful Export!");
        	} catch (FileNotFoundException e ) {
        		App.showToast("Cannot Write to SDcard");
        	}
        } else if ( ext_storage_state == 1 ) {
        	App.showToast("External storage is Read-Only");
        } else {
        	App.showToast("ERROR: External storage is not Writable");
        }
        return;
    }
    /*
    public static void copySampleDB() {
    	InputStream myInput;
		try {
			boolean copied = App.getPrefs().getBoolean("sample_copied", false);
			if ( ! copied ) {
				myInput = App.getContext().getAssets().open(DatabaseHelperSample.DATABASE_NAME);
				File output_name = App.getContext().getDatabasePath(".");
				Lg.d("mkdirs:" + output_name.mkdirs() );
				if ( output_name.exists() ) {
					OutputStream myOutput = new FileOutputStream(App.getContext().getDatabasePath(DatabaseHelperSample.DATABASE_NAME));
					copyIStoOS(myInput, myOutput);
					App.getPrefs().edit().putBoolean("sample_copied", true).commit();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    */
    public static GasRecord readPosition(int pos) {
    	GasRecord gr = null;
    	try {
       		Dao<GasRecord, Integer> grDao;
       		grDao = App.getDao();
			QueryBuilder<GasRecord, Integer> builder = grDao.queryBuilder();
			builder.orderBy(GasRecord.DATE_FIELD_NAME, false);
			builder.offset(pos).limit(1);
			List<GasRecord> mList = grDao.query(builder.prepare());
			//Lg.d("querysize: " + mList.size());
			if (mList.size() > 0 ) {
				gr = mList.get(0);
				//Lg.d("querysize: " + gr.toString());
			}
		} catch (SQLException e) {
			App.postDataError("SQL", "loading data from db", e.toString());
		}
    	
    	return gr;
    }
    
    public static GasRecordList simpleReadFromDB(int count) {
		GasRecordList grl = null;
        try {
			Lg.d("error: a");
       		Dao<GasRecord, Integer> grDao;
       		grDao = App.getDao();
			Lg.d("error: b");
			QueryBuilder<GasRecord, Integer> builder = grDao.queryBuilder();
			builder.orderBy(GasRecord.DATE_FIELD_NAME, false);
			if ( count > 0 ){
				builder.limit(count);
			}
			Lg.d("error: c");
			List<GasRecord> mList = grDao.query(builder.prepare());
			Lg.d("error: d");
			grl = new GasRecordList(mList);
		} catch (SQLException e) {
			Lg.d("error: " + e);
			App.postDataError("SQL", "loading data from db", e.toString());
		}
        return grl;
    }
    public static void copyIStoOS(InputStream is, OutputStream os) throws IOException {
    	// write the inputStream to an outputStream
    	int read = 0;
    	int total = 0;
    	byte[] bytes = new byte[4096];
     
		while ((read = is.read(bytes)) != -1) {
			total += read;
			os.write(bytes, 0, read);
		}
		Lg.d("total read: " + total);
    	is.close();
    	os.flush();
    	os.close();
    }
    
    public static void restartFirstActivity() {
    	Intent i = App.getApp().getBaseContext()
    				.getPackageManager()
    				.getLaunchIntentForPackage(
    					App.getApp().getBaseContext().getPackageName()
    				);
    	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
    	App.getApp().startActivity(i);
    }

    /*
    public static void loadSampleData(OrmLiteSqliteOpenHelper helper) {
    	try {
    		// try to delete everything.
        	Dao<GasRecord, Integer> grDao;
        	grDao = App.getDao();
    		DeleteBuilder<GasRecord, Integer> deleteBuilder = grDao.deleteBuilder();
    		grDao.delete(deleteBuilder.prepare());
    		
    		// get stock db from assets.
    		InputStream myInput = App.getContext().getAssets().open(DatabaseHelperSample.DATABASE_NAME);
            OutputStream myOutput = new FileOutputStream(App.getContext().getDatabasePath(DatabaseHelperSample.DATABASE_NAME));
            copyIStoOS(myInput, myOutput);
            restartFirstActivity();
		} catch (SQLException e) {
			App.postDataError("SQL", "reset db", e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			App.postDataError("IO", "loading sample db", e.toString());
			e.printStackTrace();
		}
    	return;
    }
    public static void simpleResetDB(OrmLiteSqliteOpenHelper helper) {
    	try {
    		// try to delete everything.
        	Dao<GasRecord, Integer> grDao;
        	grDao = App.getDao();
    		DeleteBuilder<GasRecord, Integer> deleteBuilder = grDao.deleteBuilder();
    		grDao.delete(deleteBuilder.prepare());
		} catch (SQLException e) {
			App.postDataError("SQL", "reset db", e.toString());
		}
    }
    */
    public static void addGasRecord(Dao<GasRecord, Integer> grDao, GasRecord gr) {
		int ret;
		try {
			ret = grDao.create(gr);
			assert ret == 1;
		} catch (SQLException e) {
			App.postDataError("SQL", "add data to db", e.toString());
		}
    }
    
    public static String[] readVehiclesFromFile(String file) {
    	
    	ArrayList<String> list = null;
		try {
			list = readFileLines(new DataInputStream(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			Lg.d("filenotfound: " + e.toString());
			return null;
		} catch (IOException e) {
			Lg.d("ioexception: " + e.toString());
			return null;
		}
       	Iterator<String> iter = list.iterator();
       	List<String> ret = new ArrayList<String>();
       	// find the vehicles marker and consume header
       	while ( iter.hasNext() )  {
       		String line = iter.next();
       		Lg.i("line: " + line);
       		if ( line.equals("## vehicles") ) { 
       			// just skip header
       			String header = iter.next();
       			break; 
       		} 
       	}

       	while ( iter.hasNext() )  {
       		String line = iter.next();
       		Lg.i("line: " + line);
       		if ( line.matches("^##.*") ) { 
       			break;
       		}
       		// read the first vehicle as make/model for all remaining lines.
       		String[] fields = line.split(",");
       		String make  = fields[0].replaceAll("^\"|\"$", "");
       		String model = fields[1].replaceAll("^\"|\"$", "");
       		ret.add(make + "::" + model);
       	}
       	if ( ret.size() > 0 ) {
       		String[] r = new String[ret.size()];
       		ret.toArray(r);
       		return r;
       	}
       	return null;
    }
    
    
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
    }
    
    static GasRecordList readFillupsFromList(ArrayList<String> list ) {
       	Iterator<String> iter = list.iterator();
       	ArrayList<GasRecord> mList = new ArrayList<GasRecord>();
    	
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
				GasRecord gr = new GasRecord(line);
				Lg.e("adding record: " + gr.mDate);
				mList.add(gr);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        return new GasRecordList(mList);
    }
    
    public static GasRecordList readGasFromFile(InputStream inputStream){
    	GasRecordList records=null;
        try {
        	
        	ArrayList<String> list = readFileLines(inputStream);
        	readVehicleFromList(list);
        	records = readFillupsFromList(list);
        	records.updateCalculatedValues();
        	
        } catch (IOException e){
			App.postDataError("IO", "read from File", e.toString());
        }
    	return records;
    }
    public static GasRecordList readGasFromFileSetCar(InputStream inputStream, String car){
    	GasRecordList records=null;
        try {
        	String[] fields = car.split("::");
        	if ( fields.length == 3 ) {
        		App.getPrefs().edit().putString("year", fields[0]).commit();
        		App.getPrefs().edit().putString("make", fields[1]).commit();
        		App.getPrefs().edit().putString("model", fields[2]).commit();
        	} else if ( fields.length == 2 )  {
        		App.getPrefs().edit().putString("make", fields[0]).commit();
        		App.getPrefs().edit().putString("model", fields[1]).commit();
        	} else { 
        		Lg.e("could not read 3 fields from : " + car); 
        	}
        	ArrayList<String> list = readFileLines(inputStream);
        	records = readFillupsFromList(list);
        	records.updateCalculatedValues();
        } catch (IOException e){
			App.postDataError("IO", "read from File", e.toString());
        }
    	return records;
    }

    public static int count(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n')
                        ++count;
                }
            }
            return count;
        } finally {
            is.close();
        }
    }
}
