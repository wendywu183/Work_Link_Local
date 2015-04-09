package com.centraltrillion.worklink.data;

import java.util.HashMap;

public class NoticeToItem {
    private String userId = null;
    private HashMap<String, String> name = null;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setName(String key, String value) {
        if (name == null) {
            name = new HashMap<String, String>();
        }
        name.put(key, value);
    }

    public HashMap<String, String> getName() {
        return name;
    }
}
