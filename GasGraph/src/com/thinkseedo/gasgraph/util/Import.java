package com.thinkseedo.gasgraph.util;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.database.CostRecord;
import com.thinkseedo.gasgraph.database.GasRecord;

import java.sql.SQLException;
import android.os.Environment;

public class Import {
    static String DIRNAME="GasGraph";
    static String[] SAMPLES= {"samples-prius.csv", "samples-volvo.csv", "samples-empty.csv"};
    
    public static final String SECTION_VEHICLES="vehicles";
    public static final String SECTION_FILLUPS="fillups";
    public static final String SECTION_COSTS="costs";
    public static final String SECTION_DEFAULT="default";
    
	static String[] splitLine(String line) throws IOException {
		if ( line == null ) { 
			throw new IOException("early eof");
		}
		String exp = "\\s*,+\\s*";
		String[] fields = line.split(exp,-1); // ",|\"\\s++,\\s++\"");
		String[] ret = new String[fields.length];
		int i=0;
		Lg.d("fieldlen: " + fields.length);
		for ( String f : fields ) {
			/* trim " and spaces */
			ret[i] = f.replaceAll("^\\s*\"|\"\\s*$", "");
			i++;
		}
		return ret;
	}
	
	static public class MissingColumnException extends Exception {
		private static final long serialVersionUID = 1L;};
		
	static public class HeaderSet extends ArrayList<String[]> {
		private static final long serialVersionUID = 1L; }
	
	static public class RecordBlocks extends TreeMap<String,ArrayList<TreeMap<String,String>>> {
		private static final long serialVersionUID = 1L; }
	static public class RecordLine extends ArrayList<TreeMap<String,String>> {
		private static final long serialVersionUID = 1L; }
	static public class RecordRow extends TreeMap<String,String> {
		private static final long serialVersionUID = 1L; 
			public boolean and (boolean... b) {
				boolean contains=true;
				for (int j=0; j < b.length; j++ ) {
					contains = contains && super.containsKey(b[j]);
				}
				return contains;
			}
			public boolean v(String... keys) {
				boolean ret=false;
				for (int i=0; i < keys.length; i++ ) {
					ret = ret || super.containsKey(keys[i]);
				}
				return ret;
			}
			public String get (String key, Integer def) {
				String ret = super.get(key);
				if ( ret != null ) {
					return ret;
				} else {
					if ( def.equals(-1) ) {
						return "";
					} else {
						return def.toString();
					}
				}
			}
			public String get (String key, Boolean def) {
				String ret = super.get(key);
				if ( ret != null ) {
					return ret;
				} else {
					return def.toString();
				}
			}
			public String get (String... keys) {
				String ret=null;
				for (int i=0; i < keys.length; i++ ) {
					ret = super.get(keys[i]);
					if ( ret != null ) {
						return ret;
					}
				}
				return null;
			}
		}
	
    public static InputStream getInputStream(String filename) 
    	   throws FileNotFoundException {
    	return new DataInputStream(new FileInputStream(getExternalFile(filename)));
    }
    public static OutputStream getOutputStream(String filename) 
    	   throws FileNotFoundException {
    	return new DataOutputStream(new FileOutputStream(getExternalFile(filename)));
    }
    public static File getExternalFile(String filename)  {
    	File dir = new File(Environment.getExternalStorageDirectory(), DIRNAME);
    	if ( !dir.exists() ) {
    		dir.mkdirs();
    	}
   		return new File(dir, filename);
    }
    public static File getExternalDir(String dir)  {
    	return new File(Environment.getExternalStorageDirectory(), dir);
    }
    
    public static String[] getExternalFileList()  {
   		final File dir = getExternalDir(DIRNAME); 
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
	
	static public RecordBlocks cutIntoBlocks(InputStream fis, String section) throws IOException, MissingColumnException {
		RecordBlocks   blocks = new RecordBlocks();
		RecordLine   dataList = null;
		RecordRow            row = null;
		Map<Integer,String> headerMap=null;
		boolean firstLine=true;
		String blockname=SECTION_DEFAULT;
		boolean stopAtNextSection=false;
		DataInputStream is = new DataInputStream(fis); 
		while ( true ) {
			String line = is.readLine();
			if ( line == null ) {
				break;
			}
			if ( line.matches("^## .*") || firstLine ) {
				Lg.e("firstline: ");
				if ( stopAtNextSection ) {
					break;
				}
				headerMap = new TreeMap<Integer,String>();
				if ( dataList != null ) {
					blocks.put(blockname, dataList);
				}
				dataList  = new RecordLine();
				firstLine = false;
				
				if ( line.matches("^## .*") ) {
					blockname = line.substring(3);
					line = is.readLine();
					if ( section != null && blockname.equals(section) ) {
						stopAtNextSection=true;
					}
				}
				String[] fields = splitLine(line);
				for ( int i=0; i < fields.length; i++ ) {
					headerMap.put(i, fields[i]);
				}
				Lg.d("header: " + headerMap);
				//line = is.readLine();
				continue;
			}
			
			String[] fields = splitLine(line);
			row = new RecordRow();
			for ( int i=0; i < fields.length; i++ ) {
				Lg.d("i: " + i + " " + "hmap: " + headerMap.get(i) + " field: " + fields[i]);
				String h = headerMap.get(i);
				if ( h != null ) {
					row.put(headerMap.get(i), fields[i]);
				}
			}
			Lg.d("row: " + row);
			dataList.add(row);
		}
		blocks.put(blockname, dataList);
		return blocks;
	}
    public final static int IMPORT_FU=-1;
    public final static int IMPORT_START=0;
    public final static int IMPORT_OK=1;
    public final static int IMPORT_NO_FILE=2;
    public final static int IMPORT_EXTERNAL_STORAGE_ERROR=3;
	
	public static int importRecords(OrmLiteSqliteOpenHelper helper, String filename) throws FileNotFoundException, IOException, MissingColumnException {
        int ext_storage_state = checkExternalStorage();
        if ( ext_storage_state == 2 || ext_storage_state == 1) {
        	try {
        		File rootdir = Environment.getExternalStorageDirectory();
        		FileInputStream fis = new FileInputStream(new File(rootdir, filename));
        		RecordBlocks blocks = Import.cutIntoBlocks(fis, null);
        		String make  = App.getPrefs().getString("make", "Volvo");
   		   		String model = App.getPrefs().getString("model", "850");
        		String year  = App.getPrefs().getString("year", "1997");
   				Lg.d("mmy1: " + make + " " + model + " " + year);
        		boolean containsVehicles = true;
        		for ( String key : blocks.keySet() ) {
        			Lg.e("keys: " + key);
        		}
        		if ( blocks.containsKey(SECTION_VEHICLES) ) {
        			RecordLine rline = (RecordLine)blocks.get(SECTION_VEHICLES);
        			for (int i=0; i < rline.size(); i++){
        				RecordRow row = (RecordRow)rline.get(i);
        				String m = row.get("make");
        				String[] fields = m.split("::");
        				if ( fields.length == 2 ) {
        					year = fields[0];
        					make = fields[1];
        				} else if ( fields.length == 1 ) {
        					make = m;
        					year = "1997";
        				}
        				App.getPrefs().edit().putString("make", make).commit();
        				App.getPrefs().edit().putString("model", row.get("model")).commit();
        				App.getPrefs().edit().putString("year", year).commit();
        				make  = App.getPrefs().getString("make", "Volvo");
   		   				model = App.getPrefs().getString("model", "850");
        				year  = App.getPrefs().getString("year", "1997");
        				Lg.d("mmy: " + make + " " + model + " " + year);
        			}
        		} else { 
        			containsVehicles = false;
        		}
        		if ( blocks.containsKey(SECTION_FILLUPS) || blocks.containsKey(SECTION_DEFAULT) ) {
        			RecordLine rline=null; 
        			if ( blocks.containsKey(SECTION_FILLUPS) ) { 
        				rline = (RecordLine)blocks.get(SECTION_FILLUPS);
        			}
        			if ( blocks.containsKey(SECTION_DEFAULT) ) {
        				rline = (RecordLine)blocks.get(SECTION_DEFAULT);
        			}
        			if ( rline == null ) { Lg.e("missing rline"); throw new MissingColumnException(); }
        			if ( rline.size() > 0 ) {
        				if ( !GasRecord.hasMinimumHeaderSet(rline.get(0).keySet()) ) {
        					throw new MissingColumnException();
        				}
        			}
        			for (int i=0; i < rline.size(); i++){
        				Lg.d("line: " + i);
        				RecordRow row = (RecordRow)rline.get(i);
        				if ( (make.equals(row.get("make")) && 
        					  model.equals(row.get("model"))) || 
        					!containsVehicles ) {
        					Lg.d("\tnew gr: " + row);
        					GasRecord gr = new GasRecord(row);
        					if ( gr != null ) {
        						App.getDao().create(gr);
        					} 
        				} 
        			} 
        		} 
        		boolean t = false;
        		if ( t && blocks.containsKey(SECTION_COSTS) ) {
        			RecordLine rline = (RecordLine)blocks.get(SECTION_COSTS);
        			for (int i=0; i < rline.size(); i++){
        				RecordRow row = (RecordRow)rline.get(i);
        				if ( make.equals(row.get("make")) && 
        					 model.equals(row.get("model")) &&
        					 year.equals(row.get("year")) ) {
        					CostRecord cr = new CostRecord(row);
        					if ( cr != null ) {
								App.getCostDao().create(cr);
        					}
        				}
        			}
        		}
			    //App.mServices.updateCalculatedValues();
			    return IMPORT_OK;
        	} catch (SQLException e) {
        		App.postDataError("SQL", "importRecords", e.toString());
			} finally {
				Lg.e("");
			}
        } else {
        	return IMPORT_EXTERNAL_STORAGE_ERROR;
        }
		return IMPORT_FU;
	}
	
    public static void exportSamples() {
    	InputStream myInput;
    	getExternalFile("null");
		try {
			Lg.d("calling exportSamples");
			boolean copied = App.getPrefs().getBoolean(App.SAMPLE_COPIED, false);
			Lg.d("copied: " + copied);
			if ( ! copied ) {
				int count = 0;
				for ( String filename : SAMPLES ) {
					myInput = App.getContext().getAssets().open(filename);
					File output_name = getExternalFile(filename);
					Lg.d("outname: " + output_name );
					if ( ! output_name.exists() ) {
						OutputStream myOutput = getOutputStream(filename);
						Utilities.copyIStoOS(myInput, myOutput);
						count += 1;
					}
				}
				if ( count == SAMPLES.length ) {
					App.getPrefs().edit().putBoolean(App.SAMPLE_COPIED, true).commit();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
    public static void exportRecords(String filename) {
        int ext_storage_state = checkExternalStorage();
        File extDir = getExternalFile(filename);
        if ( ext_storage_state == 2 ) {
        	App.showToast("Exporting to " + extDir);
        	try {
        		OutputStream out = getOutputStream(filename);
        		CarRecordList.saveToFile(out, null);
        		GasRecordList.saveToFile(out, App.mRecords.mList);
        		//CostRecordList.saveToFile(out, App.mServices.mList);
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
    
    public static void displayImportMessage(Integer id) {
        File extDir = getExternalDir(DIRNAME);
        if ( id == IMPORT_OK ) {
        	App.showToast("Successful Import!");
        } else if ( id == IMPORT_NO_FILE ) {
        	App.showToast("No file found in " + extDir);
        } else if ( id == IMPORT_EXTERNAL_STORAGE_ERROR ) {
        	App.showToast("ERROR: External storage is not Readable");
        } else if ( id == IMPORT_START) {
        	App.showToast("Importing from " + extDir);
        }
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
    
    public static void writeFileLines(OutputStream outputStream, ArrayList<String> lines) throws IOException {
    	// new DataOutputStream(new FileOutputStream(file, false));
    	DataOutputStream buf = (DataOutputStream)outputStream; 
        Iterator<String> iter = lines.iterator();
        while (iter.hasNext()) { 
        	String line = iter.next();
        	buf.writeBytes(line + "\n"); 
        }
    	buf.flush();
    	return;
    }
    
}
