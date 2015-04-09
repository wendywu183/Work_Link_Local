package com.centraltrillion.worklink.utils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

public abstract class AbstractDbProvider extends ContentProvider {

    protected TableUriChecker mTableUriChecker = null;

    @Override
    public boolean onCreate() {
        mTableUriChecker = TableUriChecker.getInstance();

        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        IDbHelper dbHelper = getDbHelper();
        IDatabase db = dbHelper.getWritableDb();
        int updatedColumns = 0;

        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                if (insertInternal(uri, value, db) != null)
                    updatedColumns++;
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        if (updatedColumns > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return values.length;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newUri = insertInternal(uri, values, null);

        if (newUri != null)
            getContext().getContentResolver().notifyChange(newUri, null);

        return newUri;
    }

    /* the last segment of uri must be a specific table */
    private Uri insertInternal(Uri uri, ContentValues values, IDatabase db) {
        long rowId;
        Uri newUri = null;
        String table = mTableUriChecker.retriveTableByUri(uri);
        IDbHelper dbHelper = getDbHelper();

        if (db == null)
            db = dbHelper.getWritableDb();

        try {
            rowId = db.insertOrThrow(table, null, values);

            if (rowId > 0) {
                newUri = ContentUris.withAppendedId(uri, rowId);

                return newUri;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        IDbHelper dbHelper = getDbHelper();
        IDatabase db = dbHelper.getWritableDb();
        String table = mTableUriChecker.retriveTableByUri(uri);
        int rowAffected = db.delete(table, selection, selectionArgs);

        if (rowAffected > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowAffected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        IDbHelper dbHelper = getDbHelper();
        IDatabase db = dbHelper.getReadableDb();
        String table = mTableUriChecker.retriveTableByUri(uri);
        Cursor c = null;

        c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        IDbHelper dbHelper = getDbHelper();
        IDatabase db = dbHelper.getWritableDb();
        String table = mTableUriChecker.retriveTableByUri(uri);
        int rowAffected = db.update(table, values, selection, selectionArgs);

        return rowAffected;
    }

    /** User must implement the function to provide a IDbHelper instance */
    protected abstract IDbHelper getDbHelper();

    public interface IDbHelper {
        public IDatabase getReadableDb();

        public IDatabase getWritableDb();
    }

    public interface IDatabase {
        public void beginTransaction();

        public void endTransaction();

        public void setTransactionSuccessful();

        public long insertOrThrow(java.lang.String table, java.lang.String nullColumnHack, android.content.ContentValues values);

        public long insert(java.lang.String table, java.lang.String nullColumnHack, android.content.ContentValues values);

        public int delete(java.lang.String table, java.lang.String whereClause, java.lang.String[] whereArgs);

        public int update(java.lang.String table, android.content.ContentValues values, java.lang.String whereClause, java.lang.String[] whereArgs);

        public Cursor query(java.lang.String table, java.lang.String[] columns, java.lang.String selection, java.lang.String[] selectionArgs, java.lang.String groupBy, java.lang.String having, java.lang.String orderBy);
    }
}
