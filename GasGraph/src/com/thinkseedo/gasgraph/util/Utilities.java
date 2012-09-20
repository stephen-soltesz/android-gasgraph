package com.thinkseedo.gasgraph.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class Utilities {

	public static class Entry<K, V> implements Map.Entry<K, V> {
	    private final K key;
	    private V value;

	    public Entry(K key, V value) {
	        this.key = key;
	        this.value = value;
	    }

	    @Override
	    public K getKey() {
	        return key;
	    }

	    @Override
	    public V getValue() {
	        return value;
	    }

	    @Override
	    public V setValue(V value) {
	        V old = this.value;
	        this.value = value;
	        return old;
	    }
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
    
    public static int countLinesInFile(String filename) throws IOException {
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
}
