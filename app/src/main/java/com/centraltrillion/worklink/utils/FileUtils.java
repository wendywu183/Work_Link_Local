package com.centraltrillion.worklink.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {
    private static final String DEBUG = "FileUtils";
    //sdcard dir
	private static String mSdRootPath = Environment.getExternalStorageDirectory().getPath();

    //local system dir
	private static String mDataRootPath = null;
	private final static String FOLDER_NAME = "/image";

	public FileUtils(Context context){
        if(context.getCacheDir()!=null)
		mDataRootPath = context.getCacheDir().getPath();
	}

	private String getStorageDirectory(){
//		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ?
//				mSdRootPath + FOLDER_NAME : mDataRootPath + FOLDER_NAME;
        return mDataRootPath + FOLDER_NAME;
	}
	
	public void savaBitmap(String fileName, Bitmap bitmap) throws IOException {
		if(bitmap == null){
			return;
		}
		String path = getStorageDirectory();
		File folderFile = new File(path);
		if(!folderFile.exists()){
			folderFile.mkdir();
		}
		File file = new File(path + File.separator + fileName);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.JPEG, 100, fos);
		fos.flush();
		fos.close();
	}

    // This function is used to download image.
    public String needToDownloadImage(String fileName, final String imageUrl) {
        String path = getStorageDirectory();
        File folderFile = new File(path);
        if(!folderFile.exists()){
            folderFile.mkdir();
        }

        File file = new File(path + File.separator + fileName);
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
        } catch (Exception e) {
            e.getStackTrace();
            return "";
        }

        return file.getPath();
    }

    public String getMd5(String fileName){
        File file = new File(getStorageDirectory() + File.separator + fileName);
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

	public Bitmap getBitmap(String fileName){
		return BitmapFactory.decodeFile(getStorageDirectory() + File.separator + fileName);
	}
	
	public boolean isFileExists(String fileName){
		return new File(getStorageDirectory() + File.separator + fileName).exists();
	}
	
	public long getFileSize(String fileName) {
		return new File(getStorageDirectory() + File.separator + fileName).length();
	}
	
	public void deleteFile() {
		File dirFile = new File(getStorageDirectory());
		if(! dirFile.exists()){
			return;
		}
		if (dirFile.isDirectory()) {
			String[] children = dirFile.list();
			for (int i = 0; i < children.length; i++) {
				new File(dirFile, children[i]).delete();
			}
		}
		
		dirFile.delete();
	}
}
