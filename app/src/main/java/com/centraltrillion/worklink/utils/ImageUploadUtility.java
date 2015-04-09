package com.centraltrillion.worklink.utils;

import android.os.Handler;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class ImageUploadUtility implements Runnable {
    private static final String DEBUG = "ImageUploadUtility";
    private static final int DEFAULT_TIME_OUT = 30000;
    private URL connectURL;
    private String fileName;
    private String companyId;
    private FileInputStream fileInputStream = null;
    private Handler handler = null;
    private ImageUploadListener listener = null;

    public ImageUploadUtility(ImageUploadListener listener, String urlString, String fileName, String companyId) {
        try {
            connectURL = new URL(urlString);
            this.fileName = fileName;
            this.companyId = companyId;
            this.listener = listener;
            handler = new Handler();
        } catch (Exception ex) {
            Log.i("HttpFileUpload", "URL Malformatted");
        }
    }

    public void sendNow(FileInputStream fStream) {
        fileInputStream = fStream;
        Sending();
    }

    void Sending() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String iFileName = "test.jpg";
               iFileName = fileName;//fileName.replaceAll("[^\\w]", "");
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = UUID.randomUUID().toString();
                try {
                    Log.e(DEBUG, "Starting Http File Sending to URL");

                    // Open a HTTP connection to the URL
                    HttpURLConnection conn = (HttpURLConnection) connectURL.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setConnectTimeout(DEFAULT_TIME_OUT);
                    conn.setReadTimeout(DEFAULT_TIME_OUT);

                    // Use a post method.
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", UpdateCenter.getAccessToken());
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"filename\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(iFileName);
                    dos.writeBytes(lineEnd);

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"company_id\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(companyId);
                    dos.writeBytes(lineEnd);

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"file\"; " + "filename=\"" + iFileName + "\"" + lineEnd);
                    dos.writeBytes("Content-Type: image/png" + lineEnd);
                    dos.writeBytes(lineEnd);

                    Log.e(DEBUG, "Headers are written");

                    // create a buffer of maximum size
                    int bytesAvailable = fileInputStream.available();
                    int maxBufferSize = 1024;
                    int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    byte[] buffer = new byte[bufferSize];

                    // read file and write it into form...
                    int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    /** End header*/
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // close streams
                    fileInputStream.close();

                    dos.flush();
                    Log.e(DEBUG, "File Sent, Response: " + String.valueOf(conn.getResponseCode()));
                    InputStream is = conn.getInputStream();

                    // retrieve the response from server
                    int ch;

                    StringBuffer b = new StringBuffer();
                    while ((ch = is.read()) != -1) {
                        b.append((char) ch);
                    }
                    final String s = b.toString();
                    Log.i("Response", s);
                    dos.close();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.imageUploadCallback(s);
                        }
                    });
                } catch (MalformedURLException ex) {
                    Log.e(DEBUG, "URL error: " + ex.getMessage(), ex);
                } catch (IOException ioe) {
                    Log.e(DEBUG, "IO error: " + ioe.getMessage(), ioe);
                }
            }
        }).start();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
    }

}
