package org.ofbiz.smartphone.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
        static final String TAG = "DatabaseHelper";
        static final String DATABASE_NAME = "ofbiz.db";
        static final String TABLE_PROFILE="profile";
        static final int DATABASE_VERSION = 1;

        private SQLiteDatabase db=null;

        public DatabaseHelper(Context context) {
                super(context, DATABASE_NAME, null, DATABASE_VERSION);
                db = getWritableDatabase();
               // db.openDatabase(path, factory, flags)
                Log.d(TAG, "DatabaseHelper constructor");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
                Log.d(TAG, "DatabaseHelper onCreate called");
                db.execSQL("CREATE TABLE IF NOT EXISTS "+ TABLE_PROFILE
                                + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                + "profilename TEXT,"
                                + "serveraddress TEXT,"
                                + "port INTEGER NULL,"
                                + "username TEXT,"
                                + "password TEXT,"
                                + "isdefault INTEGER"
                                + ");");
        }
        


        @Override
        public synchronized void close() {
                db.close();
                super.close();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                Log.w(TAG, "database upgrade requested, ignored.");

        }
        
        public void insertProfile(ContentValues values) {
                Log.d(TAG, "insertProfile: " + values);
                Long rowid = db.insert(TABLE_PROFILE, "", values);
                if (rowid < 0) {
                        Log.e(TAG, "database insert failed: " + rowid);
                } else {
                        Log.d(TAG, "database insert success, rowid=" + rowid);
                }
        }

        public void updateProfile(int pid, ContentValues values) {
            Log.d(TAG, "insertProfile: " + values);
            if(((Integer)(values.get("isdefault"))).intValue()==1)
            {
            	ContentValues tmpValues=new ContentValues();
            	tmpValues.put("isdefault", 0);
            	db.update(TABLE_PROFILE, tmpValues, null, null);
            }
            db.update(TABLE_PROFILE, values, "id="+pid, null);
        }
        
        public void deleteProfile(int pid)
        {
        	Log.d(TAG, "DeleteProfile: " + pid);
        	db.delete(TABLE_PROFILE, "id="+pid, null);
        }
        
        public int getCount(Cursor c) {
                Log.d(TAG, "getCount()");

                try {
                        Log.d(TAG, "count=" + c.getCount());
                        return c.getCount();
                } catch (Exception e) {
                        Log.e(TAG, "database exception: " + e);
                        return -1;
                }
        }

        public Cursor queryAll() {
                Log.d(TAG, "queryAll()");
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                qb.setTables(DatabaseHelper.TABLE_PROFILE);
                return qb.query(db, null, null, null, null, null, null);
        }
        
//        public Cursor getMountById(int mount_id) {
//                Log.d(TAG, "getMountById()");
//                
//                SQLiteQueryBuilder mqb = new SQLiteQueryBuilder();
//                mqb.setTables(DatabaseHelper.MOUNT_TABLE);
//                
//                String proj = String.format("id=%d", mount_id);
//                return mqb.query(db, null, proj, null, null, null, null);
//        }
}
