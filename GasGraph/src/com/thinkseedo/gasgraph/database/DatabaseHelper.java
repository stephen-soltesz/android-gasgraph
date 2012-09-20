package com.thinkseedo.gasgraph.database;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.thinkseedo.gasgraph.App;
import com.thinkseedo.gasgraph.util.Lg;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	/************************************************
	 * Suggested Copy/Paste code. Everything from here to the done block.
	 ************************************************/

	public static final String DATABASE_NAME = "gasgraph.db";
	private static final int DATABASE_VERSION_ONE = 1;
	private static final int DATABASE_VERSION = 2;

	private Dao<GasRecord, Integer> rGasDao;
	private Dao<CostRecord, Integer> rCostDao;

	public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
    private static DatabaseHelper helper = null;
    private static final AtomicInteger usageCounter = new AtomicInteger(0);
    public static synchronized DatabaseHelper getHelper(Context context) {
        if (helper == null) {
            helper = new DatabaseHelper(context);
        }
        usageCounter.incrementAndGet();
        return helper;
    }
    @Override
    public void close() {
        if (usageCounter.decrementAndGet() == 0) {
            super.close();
            rGasDao = null;
            helper = null;
        }
    }


	/************************************************
	 * Suggested Copy/Paste Done
	 ************************************************/

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, GasRecord.class);
			TableUtils.createTable(connectionSource, CostRecord.class);
		} catch (SQLException e) {
			Lg.e("Unable to create datbases"+ e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, 
						ConnectionSource connectionSource,
						int oldVer, int newVer) {
		try {
			if ( oldVer == DATABASE_VERSION_ONE ) {
				Dao<GasRecord, Integer> dao = getGasRecordDao();
				Lg.d("UPDATE adding new columns:");
				dao.executeRaw("ALTER TABLE `gasrecord` ADD COLUMN mMissed SMALLINT;");
				dao.executeRaw("ALTER TABLE `gasrecord` ADD COLUMN mNotes VARCHAR;");
				// select all, then look for any with the old 'missed' values.
				QueryBuilder<GasRecord, Integer> builder = dao.queryBuilder();
				builder.orderBy(GasRecord.DATE_FIELD_NAME, false);
				List<GasRecord> mList = dao.query(builder.prepare());
				for ( GasRecord gr : mList ) {
					UpdateBuilder<GasRecord, Integer> ub = dao.updateBuilder();
					if ( gr.mLocation.matches(".*break.*") ) {
						Lg.d("updating Missed true");
						ub.updateColumnValue("mMissed", true);
					} else {
						Lg.d("updating Missed false");
						ub.updateColumnValue("mMissed", false);
					}
					ub.where().eq("id", gr.id);
		   			dao.update(ub.prepare());
				}
				TableUtils.createTable(connectionSource, CostRecord.class);
			} else {
				TableUtils.dropTable(connectionSource, GasRecord.class, true);
				onCreate(sqliteDatabase, connectionSource);
			}
		} catch (SQLException e) {
			Lg.e("Unable to upgrade database from version " + oldVer + " to new "
					+ newVer + e);
		}
	}

	public Dao<CostRecord, Integer> getCostRecordDao() throws SQLException {
		if (rCostDao == null) {
			rCostDao = getDao(CostRecord.class);
		}
		return rCostDao;
	}
	public Dao<GasRecord, Integer> getGasRecordDao() throws SQLException {
		if (rGasDao == null) {
			rGasDao = getDao(GasRecord.class);
		}
		return rGasDao;
	}
	
    public void resetTables() {
    	try {
    		// try to delete everything.
    		if ( rGasDao == null ) {
    			getGasRecordDao();
    		}
    		if ( rCostDao == null ) {
    			getCostRecordDao();
    		}
    		rGasDao.delete(rGasDao.deleteBuilder().prepare());
    		rCostDao.delete(rCostDao.deleteBuilder().prepare());
		} catch (SQLException e) {
			Lg.e("SQL error reset db: " + e.toString());
		}
    }
    
}
