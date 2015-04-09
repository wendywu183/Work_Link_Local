package com.centraltrillion.worklink.utils;

import android.content.Context;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.ContactGroupItem;
import com.centraltrillion.worklink.data.ContactUserItem;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* TODO : For preloading contact, it will be removed. */
public class PreLoaderUtility implements JsonDownloadListener {
    private static final String REQ_GET_GROUP = "req_get_group";
    private static final String REQ_GET_GROUP_CONTACTS = "req_get_group_contacts";

    private static Context sCtx;
    private static PreLoaderUtility sInstance = null;

    private Object syncObjKey = new Object();
    private HashMap mGroupContactMap = new HashMap();
    private HashMap<String, String> mIdNameMap = new HashMap<String, String>();
    private HashMap<String, String> mIdImgUrlMap = new HashMap<String, String>();


    public static PreLoaderUtility getInstance(Context ctx) {
        sCtx = ctx;

        if(sInstance == null) {
            sInstance = new PreLoaderUtility();
        }
        return sInstance;
    }

    private void checkUpdateTime(Context ctx) {
        String rawUrl = String.format(ctx.getString(R.string.WORK_LINK_SERVER), ctx.getString(R.string.api_get_update_time));
        String url = String.format(rawUrl, Utility.getAccount(ctx).getCompanyId());

        UpdateCenter.getJsonFromServerDeprecate(url, this, ctx, Utility.GET_JSON_TAG_UPDATETIME);
    }

    public String getNameById(String id) {
        return mIdNameMap.get(id);
    }

    public String getImgUrlById(String id) {
        return mIdImgUrlMap.get(id);
    }

    public void applyGroupJson() {
        String oldGroupJson = Utility.getJsonFromDB(sCtx, Utility.FUNCTION_CONTACT);

            /* 1.First, check the cached data whether or not the json data cached before. */
        if (oldGroupJson != null && !oldGroupJson.isEmpty()) {
            /* 2. If the data is cached before. Then we use it to display. */
            applyDataFromJson(oldGroupJson, REQ_GET_GROUP);
            /* 3. Check the data is up-to-date or not */
            checkUpdateTime(sCtx);
        } else {
            //get group data
            String rawUrl = sCtx.getString(R.string.WORK_LINK_SERVER, sCtx.getString(R.string.api_get_all_contact_group));
                /* Testing data from server temporarily. */
            String url = String.format(rawUrl, 0, 20, Utility.getAccount(sCtx).getCompanyId());

            UpdateCenter.getJsonFromServer(url, this, sCtx, REQ_GET_GROUP);
        }
    }

    public void applyDataFromJson(final String jsonStr, String tag) {
        String companyId = Utility.getAccount(sCtx).getCompanyId();
        String userId = Utility.getAccount(sCtx).getId();

        if (tag.equals(REQ_GET_GROUP)) {
            try {
                List<ContactGroupItem> contactGroup = ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_GROUP, jsonStr, ContactGroupItem.class);
                String rawUrl = sCtx.getString(R.string.WORK_LINK_SERVER, sCtx.getString(R.string.api_contact_group_detail));
                String url = null;

                for (ContactGroupItem groupItem : contactGroup) {
                    String groupId = groupItem.getGroupId();

                    if (!mGroupContactMap.containsKey(groupItem)) {
                        mGroupContactMap.put(groupItem, new ArrayList<ContactUserItem>());
                    }

                    String oldContackJson = Utility.getJsonFromDB(sCtx, Utility.FUNCTION_CONTACT + "_" + groupId);

                    /* If the contact data cached before then we reuse it. */
                    if (oldContackJson != null && !oldContackJson.isEmpty()) {
                        applyDataFromJson(oldContackJson, REQ_GET_GROUP_CONTACTS);
                    } else {

                        url = String.format(rawUrl, groupId, 0, 20, companyId);

                        UpdateCenter.getJsonFromServer(url, this, sCtx, REQ_GET_GROUP_CONTACTS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (tag.equals(REQ_GET_GROUP_CONTACTS)) {
            /*
            /* For multi-thread downcount. If mGroupCount == 0 then it's mean all threads
            /* that send the request of "REQ_GET_GROUP_CONTACTS"
            /*
             */
            synchronized (syncObjKey) {
                try {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        JSONObject jsonResultObj = new JSONObject(jsonStr);

                        if (jsonResultObj != null && !jsonResultObj.has("error")) {
                            List<ContactUserItem> contactItems = ParserUtility.getParsingList(ParserUtility.PARSER_CONTACT_LIST, jsonStr, ContactUserItem.class);

                            if (contactItems.size() > 0) {
                                String contactGroupId = contactItems.get(0).getGroupId();

                                Utility.setJsonToDB(sCtx, jsonStr, Utility.FUNCTION_CONTACT + "_" + contactGroupId);
                                for(ContactUserItem userItem : contactItems) {
                                    mIdNameMap.put(userItem.getId(), userItem.getName().get(Utility.TEST_DEFAULT_LANGUAGE));
                                    mIdImgUrlMap.put(userItem.getId(), userItem.getPhotoUrl());
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (tag.equals(Utility.GET_JSON_TAG_UPDATETIME)) {
            if (jsonStr != null && !jsonStr.isEmpty()) {
                Utility.setUpdateTimeTable(jsonStr);
                String newUpdateTime = Utility.getUpdateTimeByFunction(Utility.FUNCTION_CONTACT);
                String oldUpdateTime = Utility.getLocalTimeByFunction(sCtx, Utility.FUNCTION_CONTACT);

                //if have new update time -> get new json
                if (newUpdateTime != null && !newUpdateTime.equals(oldUpdateTime)) {
                    /* Clear the old data and retrieve again. */
                    Utility.setJsonToDB(sCtx, "", Utility.FUNCTION_CONTACT);
                    /* Also, clear the old data. */
                    mGroupContactMap.clear();
                    applyGroupJson();
                }
            }
        }
    }

    @Override
    public void gotJsonFromServer(String tag, String jsonStr) {
        if (tag.equals(REQ_GET_GROUP)) {
            applyDataFromJson(jsonStr, REQ_GET_GROUP);
        } else if (tag.equals(REQ_GET_GROUP_CONTACTS)) {
            applyDataFromJson(jsonStr, REQ_GET_GROUP_CONTACTS);
        }
    }
}
