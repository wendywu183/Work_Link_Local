package com.centraltrillion.worklink.data;

import java.util.HashMap;

public class UpdateTimeItem implements DataItem {

    private HashMap<String, String> mMap = null;

    public void setHashMap(HashMap map) {
        this.mMap = map;
    }

    public String getUpdateTime(String tag) {
        if (null != mMap && mMap.containsKey(tag))
            return mMap.get(tag);
        else
            return "";
    }
}
