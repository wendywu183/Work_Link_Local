package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.data.LoginUserDataItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class LoginUserDataParser implements IParser {
    private final static String DEBUG = "LoginUserDataParser";

    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            if (jsonStr != null) {
                LoginUserDataItem item = new LoginUserDataItem();
                JSONObject jsonObject = new JSONObject(jsonStr);
                item.setId(jsonObject.getString("_id"));
                item.setCompanyId(jsonObject.getString("company_id"));
                item.setMotto(jsonObject.getString("motto"));
                item.setIntro(jsonObject.getString("introduction"));
                item.setStatus(jsonObject.getString("state"));

                JSONObject photoJson = jsonObject.getJSONObject("photo");
                item.setPhotoUrl(photoJson.getString("url"));
                item.setPhotoFileKey(photoJson.getString("filekey"));
                item.setPhotoMd5(photoJson.getString("md5"));

                JSONObject bannerJson =jsonObject.getJSONObject("corporate_image");
                item.setBannerFileKey(bannerJson.getString("filekey"));
                item.setBannerMd5(bannerJson.getString("md5"));
                item.setBannerUrl(bannerJson.getString("url"));

                JSONObject nameJson = jsonObject.getJSONObject("name");
                Iterator<String> iterator = nameJson.keys();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    item.setName(next, nameJson.getString(next));
                }

                JSONObject titleJson = jsonObject.getJSONObject("job_title");
                iterator = titleJson.keys();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    item.setTitle(next, titleJson.getString(next));
                }

                JSONObject companyJson = jsonObject.getJSONObject("company_name");
                iterator = companyJson.keys();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    item.setCompanyName(next, companyJson.getString(next));
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
