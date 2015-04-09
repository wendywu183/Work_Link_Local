package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.ContactGroupItem;
import com.centraltrillion.worklink.data.DataItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class GroupListParser implements IParser {
    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        return null;
    }

    @Override
    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type) {
        try {
            if (jsonStr != null) {
                ArrayList<T> list = new ArrayList<T>();
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONArray jsonArray = jsonObject.getJSONArray("group");
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    ContactGroupItem item = new ContactGroupItem();
                    item.setGroupId(jsonArray.getJSONObject(i).getString("group_id"));
                    item.setGroupType(jsonArray.getJSONObject(i).getString("group_type"));
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i).getJSONObject("group_name");
                    Iterator<String> iterator = jsonObject1.keys();
                    while (iterator.hasNext()) {
                        String next = iterator.next();
                        item.setGroupName(next, jsonObject1.getString(next));
                    }
                    list.add(type.cast(item));
                }
                return list;
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
