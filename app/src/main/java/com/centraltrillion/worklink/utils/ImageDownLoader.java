package com.centraltrillion.worklink.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.Log;

import com.centraltrillion.worklink.MainActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageDownLoader {

    private String DEBUG = "ImageDownLoader";
    private LruCache<String, Bitmap> mMemoryCache;
    private FileUtils fileUtils;
    private ExecutorService mImageThreadPool = null;
    private Context mContext;
    DisplayMetrics metrics;
    private int difaultImageSize = 120;

    public ImageDownLoader(Context context) {
        // every app is support 32MB * 3
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 8;
        this.mContext = context;
        //for LruCache is 1/8 (4MB)
        mMemoryCache = new LruCache<String, Bitmap>(mCacheSize) {


            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }

        };

        fileUtils = new FileUtils(context);
    }

    //synchronized is most important for develop.
    public ExecutorService getThreadPool() {
        if (mImageThreadPool == null) {
            synchronized (ExecutorService.class) {
                if (mImageThreadPool == null) {
                    //for speed , open two thread to download.
                    mImageThreadPool = Executors.newFixedThreadPool(2);
                }
            }
        }

        return mImageThreadPool;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void removeBitmapFromMemCache(String key){
        String subUrl = key.replaceAll("[^\\w]", "");
        mMemoryCache.remove(subUrl);
    }

    public void removeAllMemCache(){
        mMemoryCache.evictAll();
    }

    public Bitmap downlaodImage(final String url, String md5, final int width, final int height, final onImageLoaderListener listener) {
        if (url.equals("")){
            return null;
        }

        //replace special symbol , because there's like folder's url.
        final String subUrl = url.replaceAll("[^\\w]", "");
        Bitmap bitmap = showCacheBitmap(subUrl, md5);
        if (bitmap != null) {
            return bitmap;
        } else {

            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    listener.onImageLoader((Bitmap) msg.obj, url);
                }
            };

            getThreadPool().execute(new Runnable() {

                @Override
                public void run() {

                    if(url.equals("")){
                        Message msg = handler.obtainMessage();
                        msg.obj = null;
                        handler.sendMessage(msg);

                    }else {

                        String path = fileUtils.needToDownloadImage(subUrl, url);
                        Bitmap bitmap = null;

                        if (!path.equals("")) {
                            bitmap = BitmapUtility.decodeSampledBitmapFromFiles(path, width, height,
                                    MainActivity.metrics);
                            //Bitmap bitmap = getBitmapFormUrl(url);
//                    try {
//                        //save to SDcard or local system folder
//                        fileUtils.savaBitmap(subUrl, bitmap);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                            //add bitmap to cache
                            addBitmapToMemoryCache(subUrl, bitmap);
                        } else {
                            Log.d(DEBUG, "download faild :" + url);
                        }

                        Message msg = handler.obtainMessage();
                        msg.obj = bitmap;
                        handler.sendMessage(msg);
                    }
                }
            });
        }

        return null;
    }

    public Bitmap downloadImage(final String url, final onImageLoaderListener listener) {
        return downlaodImage(url, "", difaultImageSize, difaultImageSize, listener);
    }

    //getBitmap from cache or file
    public Bitmap showCacheBitmap(String url, String md5) {
        if (getBitmapFromMemCache(url) != null) {
            return getBitmapFromMemCache(url);
        } else if (fileUtils.isFileExists(url) && fileUtils.getFileSize(url) != 0) {

            //if md5 is different,then return null
            if (md5.equals("") || fileUtils.getMd5(url).equals(md5)) {
                Bitmap bitmap = fileUtils.getBitmap(url);
                addBitmapToMemoryCache(url, bitmap);
                return bitmap;
            } else {
                return null;
            }
        }

        return null;
    }

    private Bitmap getBitmapFormUrl(String url) {
        Bitmap bitmap = null;
        HttpURLConnection con = null;
        try {
            //HttpGet request = new HttpGet(url);
            HttpUriRequest request = new HttpGet(url.toString());
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                byte[] bytes = EntityUtils.toByteArray(entity);
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                return bitmap;
            } else {
                throw new IOException("Download failed, HTTP response code "
                        + statusCode + " - " + statusLine.getReasonPhrase());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return bitmap;
    }

    //Cancel download
    public synchronized void cancelTask() {
        if (mImageThreadPool != null) {
            mImageThreadPool.shutdownNow();
            mImageThreadPool = null;
        }
    }

    public interface onImageLoaderListener {
        void onImageLoader(Bitmap bitmap, String url);
    }

}
