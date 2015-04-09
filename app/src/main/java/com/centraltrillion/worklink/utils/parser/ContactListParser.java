package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.ContactUserItem;
import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ContactListParser implements IParser {
    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        return null;
    }

    @Override
    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type) {
        try {
            if (jsonStr != null && !jsonStr.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                String groupType = jsonObject.getString("group_type");

                if (groupType.equals("message")) {
                    return parseMessageGroupItem(jsonObject, type);
                } else {
                    return parseContactItem(jsonObject, type);
                }
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    private <T extends DataItem> ArrayList<T> parseMessageGroupItem(JSONObject jsonObject, Class<T> type) {
        try {
            if (!jsonObject.has("error")) {
                ArrayList<T> list = new ArrayList<T>();
                JSONArray jsonArray = jsonObject.getJSONArray("message_group");

                if (jsonArray == null) {
                    return null;
                }
                String groupId = jsonObject.getString("group_id");
                int len = jsonArray.length();

                for (int i = 0; i < len; i++) {
                    ContactUserItem item = new ContactUserItem();
                    JSONObject jsonMsgGroupObj = jsonArray.getJSONObject(i);

                    item.setGroupId(groupId);
                    item.setId(jsonMsgGroupObj.getString("_id"));
                    item.setRoomId(jsonMsgGroupObj.getString("room_id"));
                    item.setGroupUpdateTime(jsonMsgGroupObj.getString("update_time"));
                    item.setPhotoUrl(jsonMsgGroupObj.getJSONObject("photo").getString("url"));
                    item.setPhotoMd5(jsonMsgGroupObj.getJSONObject("photo").getString("md5"));
                    /* TODO: It's language is default assignment testing.*/
                    item.setName(Utility.TEST_DEFAULT_LANGUAGE, jsonMsgGroupObj.getString("group_name"));
                    item.setMemberCount(jsonMsgGroupObj.getString("count"));

                    list.add(type.cast(item));
                }
                return list;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T extends DataItem> ArrayList<T> parseContactItem(JSONObject jsonObject, Class<T> type) {
        try {
//            JSONObject jsonObject = new JSONObject(jsonStr);
            if (!jsonObject.has("error")) {
                ArrayList<T> list = new ArrayList<T>();
                JSONArray jsonArray = jsonObject.getJSONArray("contact_users");
                if (jsonArray == null) {
                    return null;
                }
                String groupId = jsonObject.getString("group_id");
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    ContactUserItem item = new ContactUserItem();
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    item.setId(jsonObject1.getString("user_id"));
                    item.setGroupId(groupId);
                    item.setPhone(jsonObject1.getString("phone_number"));
                    item.setPhotoUrl(jsonObject1.getJSONObject("photo").getString("url"));
                    item.setPhotoMd5(jsonObject1.getJSONObject("photo").getString("md5"));
                    item.setStatus(jsonObject1.getString("state"));
                    item.setJobMail(jsonObject1.getString("job_mail"));
                    JSONObject jsonObject2 = jsonObject1.getJSONObject("name");

                    Iterator<String> iterator = jsonObject2.keys();
                    while (iterator.hasNext()) {
                        String next = iterator.next();
                        item.setName(next, jsonObject2.getString(next));
                    }
                    jsonObject2 = jsonObject1.getJSONObject("job_title");
                    iterator = jsonObject2.keys();
                    while (iterator.hasNext()) {
                        String next = iterator.next();
                        item.setTitle(next, jsonObject2.getString(next));
                    }
                    jsonObject2 = jsonObject1.getJSONObject("dept");
                    iterator = jsonObject2.keys();
                    while (iterator.hasNext()) {
                        String next = iterator.next();
                        item.setDept(next, jsonObject2.getString(next));
                    }
                    list.add(type.cast(item));
                }
                return list;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}