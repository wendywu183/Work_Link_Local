package com.centraltrillion.worklink.utils;


import android.content.Context;
import android.util.Log;

import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.ContactGroupItem;
import com.centraltrillion.worklink.data.ContactUserDetailItem;
import com.centraltrillion.worklink.data.ContactUserItem;
import com.centraltrillion.worklink.data.ContactUserPhotoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContactUtility {
    private static final String DEBUG = "ContactUtility";

    public static ContactUserDetailItem getUserDetailDataById(Context context, String userId) {
        ContactUserDetailItem item = new ContactUserDetailItem();
        String userDataStr = Utility.getJsonFromDB(context, Utility.FUNCTION_CONTACT, userId);
        if (userDataStr != null && !userDataStr.equals("")) {
            item = ParserUtility.getParsingResult(ParserUtility.PARSER_USER_OTHER_DATA, userDataStr, ContactUserDetailItem.class);
        }
        if (item != null) {
            return item;
        }
        return null;
    }

    public static ContactUserPhotoItem getUserPhotoDataById(Context context, String userId) {
        ContactUserDetailItem item = new ContactUserDetailItem();
        ContactUserPhotoItem photoItem = new ContactUserPhotoItem();
        String userDataStr = Utility.getJsonFromDB(context, Utility.FUNCTION_CONTACT, userId);
        if (userDataStr != null && !userDataStr.equals("")) {
            item = ParserUtility.getParsingResult(ParserUtility.PARSER_USER_OTHER_DATA, userDataStr, ContactUserDetailItem.class);
        }
        if (item != null) {
            photoItem.setUserId(item.getId());
            photoItem.setPhotoUrl(item.getPhotoUrl());
            photoItem.setPhotoMd5(item.getPhotoMd5());
            photoItem.setPhotoFileKey(item.getPhotoFileKey());
            return photoItem;
        }
        return null;
    }

    public static List<String> getLocalFavoriteUserIdList(Context context) {
        List<String> favoriteIdList = new ArrayList<String>();
        String jsonStr = Utility.getJsonFromDB(context, Utility.FUNCTION_CONTACT);
        List<ContactGroupItem> groupList = null;
        if (jsonStr != null && !jsonStr.equals("")) {
            groupList = ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_GROUP, jsonStr, ContactGroupItem.class);
            String groupId = groupList.get(0).getGroupId();
            String jsonStr2 = Utility.getJsonFromDB(context, Utility.FUNCTION_CONTACT + "_" + groupId);
            if (jsonStr2 != null && !jsonStr2.equals("")) {
                List<ContactUserItem> subList = ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_LIST, jsonStr2, ContactUserItem.class);
                int len = subList.size();
                for (int i = 0; i < len; i++) {
                    favoriteIdList.add(subList.get(i).getId());
                }
                return favoriteIdList;
            }
        }
        return null;
    }

    public static void addUserToFavorite(Context context, JsonDownloadListener listener, String userId) {
        String url = context.getString(R.string.WORK_LINK_SERVER, context.getString(R.string.api_post_add_favorite));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userId);
            UpdateCenter.postJsonToServer(url, jsonObject.toString(), listener, context, Utility.POST_FAVORITE_ADD_TAG);

        } catch (JSONException e) {
            Log.e(DEBUG, "JsonException e = " + e.toString());
            e.printStackTrace();
        }
    }

    public static void deleteUserFromFavorite(Context context, JsonDownloadListener listener, String userId) {
        String url = context.getString(R.string.WORK_LINK_SERVER, context.getString(R.string.api_post_delete_favorite));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userId);
            UpdateCenter.deleteDataOnServer(url, jsonObject.toString(), listener, context, Utility.POST_FAVORITE_DELETE_TAG);

        } catch (JSONException e) {
            Log.e(DEBUG, "JsonException e = " + e.toString());
            e.printStackTrace();
        }
    }
}
