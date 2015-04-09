package com.centraltrillion.worklink.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageDecodeAsyncTask extends AsyncTask<Void, Void, Void> {

    private static final String debug = "LBImageDecodeAsyncTask";

    private Context mContext;
    private String mUrl;
    private String md5;
    private int mDstWidth;
    private int mDstHeight;
    private onImageLoaderListener mCallback = null;
    private int mPosition;
    private String mPath = null;
    private Bitmap mBitmap = null;
    private DisplayMetrics mMetrics;

    public ImageDecodeAsyncTask(Activity context, String moduleName, String fileName, String url, String md5,
                                int dstWidth, int dstHeight, int position, onImageLoaderListener listener) {
        this.mContext = context;
        this.mUrl = url;
        this.mDstWidth = dstWidth;
        this.mDstHeight = dstHeight;
        this.mCallback = listener;
        this.md5 = md5;
        this.mPosition = position;

        mMetrics = mContext.getResources().getDisplayMetrics();
    }

    @Override
    protected Void doInBackground(Void... params) {
        mPath = UpdateCenter.getLocalFilePath(mContext, mUrl, md5);

        try {
            if (null != mPath && !mPath.equals(UpdateCenter.INQUEUE)) {
                // decodeFile use BitmapUtility
                mBitmap = BitmapUtility.decodeSampledBitmapFromFiles(mPath, mDstWidth, mDstHeight,
                        mMetrics);
            }

        } catch (OutOfMemoryError error) {
            Log.e(debug, "Catch Out Of Memory error from func : " + mUrl);
            System.gc();

        } catch (Exception e) {
            Log.e(debug, "catch exception : " + e.toString() + "from " + mUrl);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void a) {
        mCallback.onImageLoader(mBitmap, mPosition);
    }

    public interface onImageLoaderListener {

        public void onImageLoader(Bitmap bitmap, int position);

        public void putHolder(Object obj);
    }
}
