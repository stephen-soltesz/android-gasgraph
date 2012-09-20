package com.thinkseedo.gasgraph.database;

import java.io.Serializable;
import java.util.Date;
import java.util.Comparator;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.thinkseedo.gasgraph.App;

@DatabaseTable
public abstract class Record {
	public static final int TYPE_GAS  = 1;
	public static final int TYPE_COST = 2;
	public static final String DATE_FIELD_NAME = "mDate";
   	
	@DatabaseField(generatedId = true)
   	public Integer id; 
   	@DatabaseField(index=true, columnName=DATE_FIELD_NAME, dataType=DataType.DATE_LONG)
   	public Date	   mDate;
   	@DatabaseField
   	public Integer mDistance;

	public String toString() {
		return App.formatDate(mDate, App.DATE_STORAGE) + ":" + mDistance;
	}
	
	public abstract int getType();
}
