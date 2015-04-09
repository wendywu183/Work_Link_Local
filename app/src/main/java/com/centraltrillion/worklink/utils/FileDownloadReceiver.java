package com.centraltrillion.worklink.utils;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

public class FileDownloadReceiver extends BroadcastReceiver {
    private static final String DEBUG = "FileDownloadReceiver";

    @SuppressLint("NewApi")
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sPreferences = context.getSharedPreferences(Utility.ANNOUNCE_ATTACH_TABLE, 0);
        long enqueue = sPreferences.getLong("reference", 0);
        SharedPreferences.Editor editor = sPreferences.edit();
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            Cursor c = dm.query(query);
            if (c.moveToFirst()) {
                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);

                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                    String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    Uri uri = Uri.parse(uriString);
                    editor.putString("" + enqueue, uri.toString());
                    editor.commit();

                    intent = new Intent();
//                    intent.setAction(Intent.ACTION_VIEW);

//                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
//                    String extension = Utility.getFileExtension(new File(uri.toString()).toString().substring(1));
//                    String mimeType = "";
//
//                    mimeType = myMime.getMimeTypeFromExtension(extension);
//
//                    Log.e(DEBUG, mimeType);
                    Log.e(DEBUG, uri.toString());
//                    intent.setData(Uri.parse("content://com.centraltrillion.worklink.utils.AttachFileOpenProvider/" + uri));
////                    intent.setDataAndType(uri, "application/pdf");
//                    intent.setType("application/pdf");
                    intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                Log.e(DEBUG, "" + c.getInt(columnIndex));
            }
        }
    }


}
