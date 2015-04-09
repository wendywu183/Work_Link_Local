package com.centraltrillion.worklink.utils;

import java.util.HashMap;

public class FileTypeUtility {
    public static final String FILE_TYPE_JPG = "jpg";
    public static final String FILE_TYPE_PNG = "png";

    public static final HashMap<String, String> mimeTypes = new HashMap<String, String>();
    static {
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("doc", "application/msword");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("txt", "text/plain");
    }

    public static String getMiMeType(String type){
        return mimeTypes.get(type);
    }
}

