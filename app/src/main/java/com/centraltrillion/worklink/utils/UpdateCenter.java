package com.centraltrillion.worklink.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.centraltrillion.worklink.LoginActivity;
import com.centraltrillion.worklink.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateCenter {
    public static final String DEBUG = "UpdateCenter";
    public static final String IMAGEDIR = "image";
    public static final String INQUEUE = "inqueue";
    public static final String REFRESH_TOKEN = "refreshtoken";
    public static final String NO_NETWORK = "nonetwork";
    public static final String TIME_OUT = "timeout";
    public static final String TOKEN_FAILD = "token_faild";
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int SOCKET_TIMEOUT = 10000;
    private static HashMap<String, String> imageQueue = new HashMap<String, String>();

    public static void init(Activity context) {
        File imageDir = new File(context.getApplicationContext().getFilesDir(), IMAGEDIR);
        if (!imageDir.exists())
            imageDir.mkdir();
    }

    private static HttpParams getHttpParams() {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);
        return httpParameters;
    }

    public static HttpResponse doHttpGet(String url) throws IOException {
        HttpClient httpClient = new DefaultHttpClient(getHttpParams());
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", getAccessToken());
        HttpResponse response = httpClient.execute(httpGet);
        return response;
    }

    public static void getJsonFromServerDeprecate(final String url,
                                                  final JsonDownloadListener callback, final Context context, final String tag) {
        //use below later
        if (Utility.isOnline(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpClient httpClient = new DefaultHttpClient(getHttpParams());
                        HttpGet httpGet = new HttpGet(url);
                        httpGet.setHeader("Authorization", "Bearer 00000000");
                        HttpResponse response = httpClient.execute(httpGet);
                        HttpEntity entity = null;
                        String result = null;
                        entity = response.getEntity();
                        result = EntityUtils.toString(entity);

                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            callback.gotJsonFromServer(tag, result);
                        } else {
                            callback.gotJsonFromServer(tag, null);
                            Log.e(DEBUG, url + result);
                        }

                    } catch (ConnectTimeoutException e) {
                        callback.gotJsonFromServer(tag, null);
                        Log.e(DEBUG, "ConnectTimeoutException,getJsonStringFromServer");

                    } catch (Exception e) {
                        Log.e(DEBUG, "getJsonStringFromServer has exception " + e.toString());
                        callback.gotJsonFromServer(tag, null);
                    }
                }
            }).start();
        } else {
            callback.gotJsonFromServer(tag, null);
        }
    }

    public static void getJsonFromServer(final String url,
                                         final JsonDownloadListener callback, final Context context, final String tag) {
        //use below later
        if (Utility.isOnline(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpResponse response = doHttpGet(url);
                        HttpEntity entity = response.getEntity();
                        String result = EntityUtils.toString(entity);

                        int status = response.getStatusLine().getStatusCode();

                        if (status == HttpStatus.SC_NOT_FOUND) {
                            Log.e(DEBUG, url + result + " tag:" + tag);
                            callback.gotJsonFromServer(tag, null);
                            return;
                        }

                        //if AccessToken faild
                        if (status == HttpStatus.SC_UNAUTHORIZED) {
                            String getTokenCallBack = getNewTokenByRefreshToken(context);
                            if (getTokenCallBack.equals(TOKEN_FAILD)) {
                                //clear account
                                //abao test
                                Log.e(DEBUG, "refresh_token faild: " + getRefreshToken(context));
                                Log.e(DEBUG, "tag:" + tag + " url:" + url);
                                clearRefreshToken(context);
                                Utility.clearAccount(context);
                                //show error message
                                Utility.showToastMessage(context, context.getString(R.string.login_token_faild));
                                //logout
                                Intent intent = new Intent(context, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                                return;
                            } else if (getTokenCallBack.equals(TIME_OUT)) {
                                callback.gotJsonFromServer(tag, null);
                                return;
                            } else if (getTokenCallBack.equals(NO_NETWORK)) {
                                callback.gotJsonFromServer(tag, null);
                                return;
                            } else {
                                response = doHttpGet(url);
                                entity = response.getEntity();
                                result = EntityUtils.toString(entity);
                            }
                        }

                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            callback.gotJsonFromServer(tag, result);
                        } else {
                            Log.e(DEBUG, url + result);
                            callback.gotJsonFromServer(tag, null);
                        }

                    } catch (ConnectTimeoutException e) {
                        Log.e(DEBUG, "getJsonStringFromServer has exception :" + e.toString() + " tag: " + tag + " url:" + url);
                        callback.gotJsonFromServer(tag, null);

                    } catch (Exception e) {
                        Log.e(DEBUG, "getJsonStringFromServer has exception :" + e.toString() + " tag: " + tag + " url:" + url);
                        callback.gotJsonFromServer(tag, null);
                    }
                }
            }).start();
        } else {
            callback.gotJsonFromServer(tag, null);
        }
    }

    private static HttpResponse doHttpPostJson(String url, String json) {
        try {
            HttpParams httpParameters = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);

            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            HttpPost httppost = new HttpPost(url);
            StringEntity se = new StringEntity(json);

            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httppost.setHeader("Content-Type", "application/json");
            httppost.setHeader("Authorization", UpdateCenter.getAccessToken());
            httppost.setEntity(se);

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void postJsonToServer(final String url, final String json,
                                        final JsonDownloadListener callback, final Context context, final String tag) {
        if (Utility.isOnline(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        HttpParams httpParameters = new BasicHttpParams();
//
//                        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
//                        HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);
//
//                        HttpClient httpclient = new DefaultHttpClient(httpParameters);
//                        HttpPost httppost = new HttpPost(url);
//                        StringEntity se = new StringEntity(json);
//
//                        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//                        httppost.setHeader("Content-Type", "application/json");
//                        httppost.setHeader("Authorization", UpdateCenter.getAccessToken());
//                        httppost.setEntity(se);
//
//                        // Execute HTTP Post Request
//                        HttpResponse response = httpclient.execute(httppost);
                        HttpResponse response= doHttpPostJson(url, json);
                        int statusCode = response.getStatusLine().getStatusCode();
                        HttpEntity entity = null;
                        String strResult;

                        if(statusCode == HttpStatus.SC_NOT_FOUND){
                            callback.gotJsonFromServer(tag, null);

                            return;
                        }

                        if(statusCode == HttpStatus.SC_UNAUTHORIZED){
                            String getTokenCallBack = getNewTokenByRefreshToken(context);
                            if (getTokenCallBack.equals(TOKEN_FAILD)){
                                //clear account
                                //abao test
                                Log.e(DEBUG, "refresh_token faild: "+getRefreshToken(context));
                                Log.e(DEBUG, "tag:" + tag + " url:" + url);
                                clearRefreshToken(context);
                                Utility.clearAccount(context);
                                //show error message
                                Utility.showToastMessage(context,context.getString(R.string.login_token_faild));
                                //logout
                                Intent intent = new Intent(context,LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                                return;
                            }else if (getTokenCallBack.equals(TIME_OUT)){
                                callback.gotJsonFromServer(tag, null);
                                return;
                            }else if (getTokenCallBack.equals(NO_NETWORK)){
                                callback.gotJsonFromServer(tag, null);
                                return;
                            }else{
                                response= doHttpPostJson(url, json);
                            }
                        }

                        entity = response.getEntity();
                        strResult = EntityUtils.toString(entity);

                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK || response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                            callback.gotJsonFromServer(tag, strResult);
                        } else {
                            callback.gotJsonFromServer(tag, null);
                            Log.e(DEBUG, url + strResult);
                        }
                    } catch (ConnectTimeoutException e) {
                        callback.gotJsonFromServer(tag, null);
                        Log.e(DEBUG, "ConnectTimeoutException,getJsonStringFromServer");

                    } catch (Exception e) {
                        Log.e(DEBUG, "postJsonToServer has exception " + e.toString());
                        callback.gotJsonFromServer(tag, null);
                    }
                }
            }).start();
        } else {
            callback.gotJsonFromServer(tag, null);
        }
    }

    public static String getNewTokenByRefreshToken(Context context){
        String oldRefreshToken = getRefreshToken(context);
        if (!oldRefreshToken.equals("")) {
            try {
                String url = String.format(context.getString(R.string.WORK_LINK_SERVER),
                        context.getString(R.string.api_post_token));
                HttpClient httpClient = new DefaultHttpClient(getHttpParams());
                HttpPost httppost = new HttpPost(url);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("grant_type", "refresh_token"));
                nameValuePairs.add(new BasicNameValuePair("client_secret", "b;k33VDphn3B6gNC7#PX"));
                nameValuePairs.add(new BasicNameValuePair("client_id", "worklink_client_android"));
                nameValuePairs.add(new BasicNameValuePair("refresh_token", oldRefreshToken));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = null;
                response = httpClient.execute(httppost);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                int httpStatus = response.getStatusLine().getStatusCode();
                if (httpStatus == HttpStatus.SC_OK) {
                    //Parse Access Token
                    JSONObject jsonObject = new JSONObject(result);
                    String access_token = jsonObject.getString("access_token");
                    String refresh_token = jsonObject.getString("refresh_token");
                    setAccessToken(access_token);
                    setRefreshToken(context, refresh_token);
                    //abao test
                    Log.d(DEBUG, "renew access_token: " + getAccessToken());
                    Log.d(DEBUG, "renew refresh_token: " + getRefreshToken(context));
                    return getAccessToken();
                } else if (httpStatus == HttpStatus.SC_FORBIDDEN || httpStatus == HttpStatus.SC_UNAUTHORIZED) {
                    Log.e(DEBUG, "refresh_token faild , httpStatus ="+ httpStatus +" msg :" + result);
                    return TOKEN_FAILD;
                } else {
                    Log.e(DEBUG, "refresh_token faild , httpStatus ="+ httpStatus +" msg :" + result);
                    return TOKEN_FAILD;
                }
            } catch (ConnectTimeoutException e) {
                Log.e(DEBUG, "getNewTokenByRefreshToken has exception " + e.toString());
                return TIME_OUT;
            } catch (Exception e) {
                Log.e(DEBUG, "getNewTokenByRefreshToken has exception " + e.toString());
                return NO_NETWORK;
            }
        } else {
            Log.e(DEBUG, "refresh_token == null");
            return TOKEN_FAILD;
        }
    }

    private static String accessToken;

    public static void setAccessToken(String token) {
        accessToken = token;
    }

    public static String getAccessToken() {
        return "Bearer " + accessToken;
    }

    public static void setRefreshToken(Context context, String refreshToken) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putString(REFRESH_TOKEN, refreshToken);
        PE.commit();
    }

    public static void clearRefreshToken(Context context) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor PE = settings.edit();
        PE.putString(REFRESH_TOKEN, "");
        PE.commit();
    }

    public static String getRefreshToken(Context context) {
        SharedPreferences settings = context.getSharedPreferences(LoginActivity.SETTING, Context.MODE_PRIVATE);
        return settings.getString(REFRESH_TOKEN, "");
    }

    public static void putJsonToServer(final String url, final String json,
                                       final JsonDownloadListener callback, Context context, final String tag) {
        if (Utility.isOnline(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpParams httpParameters = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
                        HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);

                        HttpClient httpclient = new DefaultHttpClient(httpParameters);
                        HttpPut httpput = new HttpPut(url);

                        //by Jenny
//                        StringEntity se = new StringEntity(json);
//                        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//                        httpput.setHeader("Content-Type", "application/json");
//                        httpput.setHeader("Authorization", "Bearer 00000000");
//                        httpput.setEntity(se);

                        //by aBao
                        httpput.setHeader("Authorization", UpdateCenter.getAccessToken());
                        ByteArrayEntity baEntity = new ByteArrayEntity(json.toString().getBytes("UTF8"));
                        baEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        httpput.setEntity(baEntity);

                        // Execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httpput);
                        HttpEntity entity = null;
                        entity = response.getEntity();
                        String strResult = EntityUtils.toString(entity);

                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            callback.gotJsonFromServer(tag, strResult);
                        } else {
                            callback.gotJsonFromServer(tag, null);
                            Log.e(DEBUG, url + strResult);
                        }
                    } catch (ConnectTimeoutException e) {
                        callback.gotJsonFromServer(tag, null);
                        Log.e(DEBUG, "ConnectTimeoutException,getJsonStringFromServer");

                    } catch (Exception e) {
                        Log.e(DEBUG, "putJsonToServer has exception " + e.toString());
                        callback.gotJsonFromServer(tag, null);
                    }
                }
            }).start();
        } else {
            callback.gotJsonFromServer(tag, null);
        }
    }

    public static void deleteDataOnServer(final String url, final String json,
                                          final JsonDownloadListener callback, Context context, final String tag) {
        if (Utility.isOnline(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpParams httpParameters = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
                        HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);

                        HttpClient httpclient = new DefaultHttpClient(httpParameters);
                        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url);

                        //by aBao
                        httpDelete.setHeader("Authorization", UpdateCenter.getAccessToken());
                        ByteArrayEntity baEntity = new ByteArrayEntity(json.toString().getBytes("UTF8"));
                        baEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        httpDelete.setEntity(baEntity);

                        // Execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httpDelete);
                        HttpEntity entity = null;
                        entity = response.getEntity();
                        String strResult = EntityUtils.toString(entity);

                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            callback.gotJsonFromServer(tag, strResult);
                        } else {
                            callback.gotJsonFromServer(tag, null);
                            Log.e(DEBUG, url + strResult);
                        }
                    } catch (ConnectTimeoutException e) {
                        callback.gotJsonFromServer(tag, null);
                        Log.e(DEBUG, "ConnectTimeoutException,getJsonStringFromServer");

                    } catch (Exception e) {
                        Log.e(DEBUG, "putJsonToServer has exception " + e.toString());
                        callback.gotJsonFromServer(tag, null);
                    }
                }
            }).start();
        } else {
            callback.gotJsonFromServer(tag, null);
        }

    }

    public static byte[] downloadImage(final String url, Context context) {
        byte[] result = null;

        if (Utility.isOnline(context)) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = null;
                entity = response.getEntity();
                result = EntityUtils.toByteArray(entity);
//            {
//                file.getParentFile().mkdirs();
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.e("jenny", "performDownloadIcon--> happen exception");
//                }
//            }
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(result);
//            fos.close();
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            return null;
        }
        return null;
    }


    public static String getLocalFilePath(Context context, final String imageUrl, final String imageMd5) {
        final String subUrl = imageUrl.replaceAll("[^\\w]", "");
        if (imageUrl.trim().equals(""))
            return null;

        if (Utility.sImageQueue.containsKey(imageUrl)) {
            return INQUEUE;

        } else {
            String path = null;
            String fileDir = context.getCacheDir().getPath() + File.separator + UpdateCenter.IMAGEDIR;

            File dir = new File(fileDir);
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(fileDir + File.separator + subUrl);

            Utility.sImageQueue.put(imageUrl, file.getPath());

            if (!file.exists()) {
                if (UpdateCenter.isImageDownloading(file.getPath())) {
                    //downloading
                    while (true) {
                        try {
                            Thread.sleep(500);
                            if (file.exists())
                                break;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    path = file.getPath();
                } else {
                    UpdateCenter.performDownloadImage(imageUrl, file);
                    if (file.exists())
                        path = file.getPath();
                }
            } else if (!imageMd5.equals("") && !imageMd5.equals(UpdateCenter.getImageMd5(file))) {
                UpdateCenter.performDownloadImage(imageUrl, file);
                path = file.getPath();
            } else {
                path = file.getPath();
            }

            Utility.sImageQueue.remove(imageUrl);
            return path;
        }
    }

    // This function is used to download image.
    public static void needToDownloadImage(final File file, final String imageUrl) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(imageUrl);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = null;
            byte[] result;
            entity = response.getEntity();
            result = EntityUtils.toByteArray(entity);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(result);
            fos.close();
            if (imageQueue.containsKey(file.getPath()))
                imageQueue.remove(file.getPath());
        } catch (Exception e) {
            e.getStackTrace();
            Log.e(DEBUG, "file = " + file.toString() + " error = " + e.getMessage());
            Log.e(DEBUG, "needToDownloadImage happen Exception");
        }
    }

    public static String readJsonFromCache(final String path, Context context) {

        StringBuilder sb = new StringBuilder();
        File file = new File(context.getApplicationContext().getFilesDir(), path);
        if (file.exists()) {
            try {
                InputStream in = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                in.close();
            } catch (Exception e) {
                e.getStackTrace();
            }
        } else {
            Log.d(DEBUG, "readJsonFromCache, file not exist");
        }
        return sb.toString();
    }

    public static String getImageMd5(final File file) {
        StringBuilder sb = new StringBuilder();
        FileInputStream in = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            FileChannel ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            md5.update(byteBuffer);
            byte[] array = md5.digest();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
        } catch (NoSuchAlgorithmException e) {
            e.getStackTrace();
            Log.e(DEBUG, "getImageMd5 happen NoSuchAlgorithmException");
        } catch (Exception e) {
            e.getStackTrace();
            Log.e(DEBUG, "getImageMd5 happen Exception");
        } finally {
            if (null != in)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return sb.toString();
    }

    public static boolean isImageDownloading(String key) {
        return imageQueue.containsKey(key);
    }

    public static void performDownloadImage(String imageUrl, File file) {
        imageQueue.put(file.getPath(), imageUrl);
        needToDownloadImage(file, imageUrl);
    }
}
