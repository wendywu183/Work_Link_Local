package com.centraltrillion.worklink.utils;

import android.content.UriMatcher;
import android.net.Uri;

import com.centraltrillion.worklink.utils.im.ImProvider;

public class TableUriChecker {

    private static final int TB_IM_ROOM_OPERATION = 1;
    private static final int TB_IM_ROOM_OPERATION_ID = 2;
    private static final int TB_IM_MESSAGE_OPERATION = 3;
    private static final int TB_IM_MESSAGE_OPERATION_ID = 4;

    private static TableUriChecker sTableUriChecker = null;

    private UriMatcher mUriMatcher = null;

    private TableUriChecker() {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        mUriMatcher.addURI(ImProvider.AUTHORITY, ImProvider.UtilSQLiteOpenHelper.TB_IM_ROOM, TB_IM_ROOM_OPERATION);
        mUriMatcher.addURI(ImProvider.AUTHORITY, ImProvider.UtilSQLiteOpenHelper.TB_IM_ROOM + "/#", TB_IM_ROOM_OPERATION_ID);
        mUriMatcher.addURI(ImProvider.AUTHORITY, ImProvider.UtilSQLiteOpenHelper.TB_IM_MESSAGE, TB_IM_MESSAGE_OPERATION);
        mUriMatcher.addURI(ImProvider.AUTHORITY, ImProvider.UtilSQLiteOpenHelper.TB_IM_MESSAGE + "/#", TB_IM_MESSAGE_OPERATION_ID);
    }

    public static TableUriChecker getInstance() {
        if (sTableUriChecker == null)
            sTableUriChecker = new TableUriChecker();

        return sTableUriChecker;
    }

    public String retriveTableByUri(Uri uri) {
        int table_id = mUriMatcher.match(uri);
        String table = null;

        switch (table_id) {
            case TB_IM_ROOM_OPERATION:
            case TB_IM_ROOM_OPERATION_ID:
                table = ImProvider.UtilSQLiteOpenHelper.TB_IM_ROOM;
                break;

            case TB_IM_MESSAGE_OPERATION:
            case TB_IM_MESSAGE_OPERATION_ID:
                table = ImProvider.UtilSQLiteOpenHelper.TB_IM_MESSAGE;
                break;
        }
        return table;
    }
}
