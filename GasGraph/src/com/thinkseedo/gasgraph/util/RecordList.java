package com.thinkseedo.gasgraph.util;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.thinkseedo.gasgraph.database.Record;

public class RecordList extends TreeSet<Record> {
	private static final long serialVersionUID = -44824612837354359L;
	
	Record[] mRecs = null;

	static Comparator<Record> mRC;
	static {
		mRC = new Comparator<Record>() {
			@Override
			public int compare(Record lhs, Record rhs) {
				if ( lhs.mDate.equals(rhs.mDate) && 
						 lhs.mDistance.equals(rhs.mDistance) ) {
						return 0;
				} 
				if ( lhs.mDate.equals(rhs.mDate) ) {
					return rhs.mDistance.compareTo(lhs.mDistance);
				} 
				if ( lhs.mDistance.equals(rhs.mDistance) ) {
					return rhs.mDate.compareTo(lhs.mDate);
				} 
				if ( lhs.mDistance.compareTo(rhs.mDistance) > 0 && 
					 lhs.mDate.compareTo(rhs.mDate) > 0  ) {
					return -1;
				} 
				if ( lhs.mDistance.compareTo(rhs.mDistance) < 0 && 
					 lhs.mDate.compareTo(rhs.mDate) < 0  ) {
					return 1;
				}
				throw new ClassCastException("Cannot order these values: " + lhs + " " + rhs);
				//return 0;
			}
		};
	}
	
	public RecordList() {
		super(mRC);
	}
	
	public Record get(int index) {
		int s = size();
		if ( mRecs == null || s != mRecs.length ) {
		    mRecs = new Record[s]; 
		}
		toArray(mRecs);
		return mRecs[index];
	}
}
