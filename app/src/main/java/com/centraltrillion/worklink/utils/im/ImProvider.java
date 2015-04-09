package com.centraltrillion.worklink.utils.im;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import com.centraltrillion.worklink.utils.AbstractDbProvider;

public class ImProvider extends AbstractDbProvider {
    /* For remote initialize the db name for different user name. */
    public static final String CALL_METHOD_DB_NAME_SETUP = "db_name_setup";

    public static final String AUTHORITY = "com.centraltrillion.worklink.im.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    /* Table URI */
    public static final Uri URI_IM_MESSAGE = Uri.withAppendedPath(CONTENT_URI,
            UtilSQLiteOpenHelper.TB_IM_MESSAGE);
    /* Table URI */
    public static final Uri URI_IM_ROOM = Uri.withAppendedPath(CONTENT_URI,
            UtilSQLiteOpenHelper.TB_IM_ROOM);

    public static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.im.status";
    public static final String MULTIPLE_RECORD_MIME_TYPE = "v.android.cursor.dir/vnd.im.mstatus";
    private IDbHelper mDbHelper = null;
    private String mDBName = "Im.db";

    @Override
    public String getType(Uri uri) {
        return this.getId(uri) < 0 ? MULTIPLE_RECORD_MIME_TYPE : SINGLE_RECORD_MIME_TYPE;
    }

    private long getId(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();

        if (lastPathSegment != null) {
            try {
                return Long.parseLong(lastPathSegment);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    @Override
    protected IDbHelper getDbHelper() {
        checkDbByName();

        return mDbHelper;
    }

    private void checkDbByName() {
        /* For multiple use have multiple DB */
        if (mDbHelper == null || !mDBName.equals(((UtilSQLiteOpenHelper) mDbHelper).getDatabaseName())) {
            if (mDbHelper != null) {
                /* Close the previous database*/
                ((UtilSQLiteOpenHelper) mDbHelper).close();
            }
            mDbHelper = new UtilSQLiteOpenHelper(mDBName, getContext());
        }
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (method.equals(CALL_METHOD_DB_NAME_SETUP)) {
            mDBName = extras.getString("db_name");
            /* Default DB's name is IM.db*/
            mDBName = (mDBName == null || mDBName.isEmpty()) ? "IM.db" : mDBName;
        }

        return super.call(method, arg, extras);
    }

    @Override
    public boolean onCreate() {
        super.onCreate();

        return true;
    }

    public static class UtilSQLiteOpenHelper extends SQLiteOpenHelper implements IDbHelper {
        private static final int VERSION = 1;
        /* TB_IM_HISTORY_MESSAGE is used for test currently */
        public static final String TB_IM_MESSAGE = "im_message";
        public static final String TB_IM_ROOM = "im_room";

        private String mDbName;

        public UtilSQLiteOpenHelper(String name, Context context) {
            this(name, context, null, VERSION);

            mDbName = name;
//          SQLiteDatabase.loadLibs(Utility.getContext());
        }

        public UtilSQLiteOpenHelper(String name, Context context, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public String getDatabaseName() {
            return mDbName;
        }

        @Override
        public IDatabase getWritableDb() {
//            return new EncryptDatabase(super.getWritableDb("myPassword"));
            return new UnEncryptDatabase(super.getWritableDatabase());
        }

        @Override
        public IDatabase getReadableDb() {
//            return new EncryptDatabase(super.getWritableDb("myPassword"));
            return new UnEncryptDatabase(super.getWritableDatabase());
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
           /*
            * Room id maybe maybe integer and autoincrement
            * */
            final String TB_IM_MESSAGE_SQL_STMT = "create table " + TB_IM_MESSAGE
                    + "( id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " room_id TEXT,"
                    + " time TEXT,"
                    + " sender_id TEXT,"
                    + " sender_name TEXT,"
                    + " message TEXT,"
                    + " message_id TEXT,"
                    + " message_type TEXT,"
                    + " type TEXT,"
                    + " deleted TEXT,"
                    + " touched TEXT,"
                    + " readed_member_ids TEXT,"
                    + " readed TEXT,"
                    + " readed_count TEXT,"
                    + " status TEXT,"
                    + " msg_index TEXT)";

           /*
            * Room id maybe maybe integer and autoincrement. And member not the pure text.
            * */
            final String TB_IM_ROOM_SQL_STMT = "create table " + TB_IM_ROOM
                    + "( id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " room_id TEXT,"
                    + " member TEXT,"
                    + " type TEXT,"
                    + " icon TEXT,"
                    + " name TEXT)";

            db.execSQL(TB_IM_MESSAGE_SQL_STMT);
            db.execSQL(TB_IM_ROOM_SQL_STMT);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /* TODO: Currently, it's a simple implementation. But, it must be not like this in real case. */
            db.execSQL("DROP TABLE IF EXISTS " + TB_IM_MESSAGE);
            db.execSQL("DROP TABLE IF EXISTS " + TB_IM_ROOM);
            onCreate(db);
        }

//        /* TODO: For testing to clear data after removing process. */
//        private static SQLiteDatabase mDb = null;

        class UnEncryptDatabase implements IDatabase {
            private SQLiteDatabase mDb = null;

            public UnEncryptDatabase(SQLiteDatabase db) {
                mDb = db;
            }

            /* TODO: For testing to clear data after removing process. */
//            public UnEncryptDatabase(SQLiteDatabase db) {
//                if(mDb == null) {
//                    mDb = db;
//                    db.execSQL("DROP TABLE IF EXISTS " + TB_IM_MESSAGE);
//                    db.execSQL("DROP TABLE IF EXISTS " + TB_IM_ROOM);
//                    UtilSQLiteOpenHelper.this.onCreate(db);
//                }
//            }

            @Override
            public void beginTransaction() {
                mDb.beginTransaction();
            }

            @Override
            public void endTransaction() {
                mDb.endTransaction();
            }

            @Override
            public void setTransactionSuccessful() {
                mDb.setTransactionSuccessful();
            }

            @Override
            public long insertOrThrow(String table, String nullColumnHack, ContentValues values) {
                return mDb.insertOrThrow(table, nullColumnHack, values);
            }

            @Override
            public long insert(String table, String nullColumnHack, ContentValues values) {
                return mDb.insert(table, nullColumnHack, values);
            }

            @Override
            public int delete(String table, String whereClause, String[] whereArgs) {
                return mDb.delete(table, whereClause, whereArgs);
            }

            @Override
            public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
                return mDb.update(table, values, whereClause, whereArgs);
            }

            @Override
            public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
                return mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            }
        }
    }
}
