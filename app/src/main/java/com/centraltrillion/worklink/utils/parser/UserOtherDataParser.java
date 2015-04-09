package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.ContactUserDetailItem;
import com.centraltrillion.worklink.data.DataItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class UserOtherDataParser implements IParser {
    private final static String DEBUG = "UserOtherDataParser";

    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            if (jsonStr != null) {
                ContactUserDetailItem item = new ContactUserDetailItem();
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONObject jsonObject1 = jsonObject.getJSONObject("photo");
                item.setPhotoUrl(jsonObject1.getString("url"));
                item.setPhotoMd5(jsonObject1.getString("md5"));
                item.setPhotoFileKey(jsonObject1.getString("filekey"));

                jsonObject1 = jsonObject.getJSONObject("contact_info");
                item.setEmail(jsonObject1.getString("email"));
                item.setPhone(jsonObject1.getString("phone"));
                
                JSONArray jsonArray = jsonObject1.getJSONArray("telephone");
                for(int i = 0; i < jsonArray.length(); i++){
                    jsonObject1 = jsonArray.getJSONObject(i);
                    item.setTelephone(jsonObject1.getString("tel"), jsonObject1.getString("region"));
                }
                jsonObject1 = jsonObject.getJSONObject("job_info");
                item.setEmId(jsonObject1.getString("employee_id"));
                item.setSupervisorId(jsonObject1.getString("supervisor_id"));
                item.setSupervisorName(jsonObject1.getString("supervisor_name"));
                item.setDeputyId(jsonObject1.getString("deputy_id"));
                item.setDeputyName(jsonObject1.getString("deputy_name"));
                item.setSkill(jsonObject1.getString("skill"));

                jsonObject1 = jsonObject.getJSONObject("about_info");
                item.setInterest(jsonObject1.getString("interest"));
                item.setIntro(jsonObject1.getString("introduction"));

                jsonObject1 = jsonObject.getJSONObject("name");
                Iterator<String> iterator = jsonObject1.keys();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    item.setName(next, jsonObject1.getString(next));
                }
                jsonObject1 = jsonObject.getJSONObject("job_title");
                iterator = jsonObject1.keys();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    item.setTitle(next, jsonObject1.getString(next));
                }
                jsonObject1 = jsonObject.getJSONObject("dept");
                iterator = jsonObject1.keys();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    item.setDepartment(next, jsonObject1.getString(next));
                }
                jsonObject1 = jsonObject.getJSONObject("job_location");
                iterator = jsonObject1.keys();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    item.setJobLocation(next, jsonObject1.getString(next));
                }
                return type.cast(item);
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type) {
        return null;
    }
}
