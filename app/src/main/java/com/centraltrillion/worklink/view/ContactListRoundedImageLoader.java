package com.centraltrillion.worklink.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.centraltrillion.worklink.utils.BitmapUtility;
import com.centraltrillion.worklink.utils.UpdateCenter;
import com.centraltrillion.worklink.utils.Utility;

public class ContactListRoundedImageLoader extends AsyncTask<String, Void, Bitmap> {

    private ImageView image;
    private LruCache<String, Bitmap> lruCache;
    private Context context;
    private DisplayMetrics metrics;
    private String photoUrl;
    private String photoMd5;


    public ContactListRoundedImageLoader(String photoUrl, String photoMd5, Context context, DisplayMetrics metrics, ImageView image, LruCache<String, Bitmap> lruCache) {
        super();
        this.image = image;
        this.lruCache = lruCache;
        this.context = context;
        this.metrics = metrics;
        this.photoUrl = photoUrl;
        this.photoMd5 = photoMd5;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap sampleBitmap = null;
        byte[] downloadedImage = UpdateCenter.downloadImage(photoUrl, context);
        if (downloadedImage != null) {
            Bitmap data = BitmapUtility.decodeSampledBitmapFromArrays(downloadedImage, 120, 120, metrics);
            sampleBitmap = Utility.toRoundBitmap(context, data);

            if (sampleBitmap != null)
                addBitmapToMemoryCache(params[0], sampleBitmap);
        }
        return sampleBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        image.setImageBitmap(bitmap);
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            lruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        return lruCache.get(key);
    }
}