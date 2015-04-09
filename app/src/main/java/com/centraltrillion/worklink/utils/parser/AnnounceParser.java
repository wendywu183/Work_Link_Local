package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.AttachmentItem;
import com.centraltrillion.worklink.data.AnnounceItem;
import com.centraltrillion.worklink.data.DataItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AnnounceParser implements IParser {
    private static final String DEBUG = "AnnounceParser";
    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            if (jsonStr != null) {
                JSONArray jsonArray = new JSONArray(jsonStr);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                AnnounceItem item = new AnnounceItem();
                AttachmentItem attachItem;
                item.setAnnounceId(jsonObject.getString("_id"));
                item.setCompanyId(jsonObject.getString("company_id"));
                item.setTitle(jsonObject.getString("title"));
                item.setAuthor(jsonObject.getString("author"));
                item.setType(jsonObject.getString("type"));
                item.setContent(jsonObject.getString("content"));
                JSONArray jsonArray1 = jsonObject.getJSONArray("attachment_urls");
                ArrayList<AttachmentItem> attachList = new ArrayList<AttachmentItem>();
                for (int j = 0; j < jsonArray1.length(); j++) {
                    attachItem = new AttachmentItem();
                    attachItem.setAttachmentTitle(jsonArray1.getJSONObject(j).getString("title"));
                    attachItem.setAttachmentUrl(jsonArray1.getJSONObject(j).getString("urls"));
                    attachItem.setAttachmentFileKey(jsonArray1.getJSONObject(j).getString("filekey"));
                    attachItem.setAttachmentMd5(jsonArray1.getJSONObject(j).getString("md5"));
                    attachList.add(attachItem);
                }
                item.setAttachmentItemList(attachList);
                item.setUpdateTime(jsonObject.getString("update_time"));
                item.setCreateTime(jsonObject.getString("create_time"));

                return type.cast(item);

            } else {
                return null;
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type) {
        try {
            if (jsonStr != null) {
                ArrayList<T> list = new ArrayList<T>();
                JSONArray jsonArray = new JSONArray(jsonStr);
                JSONObject jsObject;
                AnnounceItem item;
                AttachmentItem attachItem;

                for (int i = 0; i < jsonArray.length(); i++) {
                    item = new AnnounceItem();
                    jsObject = jsonArray.getJSONObject(i);
                    item.setAnnounceId(jsObject.getString("_id"));
                    item.setCompanyId(jsObject.getString("company_id"));
                    item.setTitle(jsObject.getString("title"));
                    item.setAuthor(jsObject.getString("author"));
                    item.setType(jsObject.getString("type"));
                    item.setContent(jsObject.getString("content"));
                    item.setRead(jsObject.getString("read"));
                    item.setTop(jsObject.getString("top"));
                    JSONArray jsonArray1 = jsObject.getJSONArray("attachment_url");
                    JSONObject jsonObject1;
                    ArrayList<AttachmentItem> attachList = new ArrayList<AttachmentItem>();
                    int length = jsonArray1.length();
                    for (int j = 0; j < length; j++) {
                        jsonObject1 = jsonArray1.getJSONObject(j);
                        attachItem = new AttachmentItem();
                        attachItem.setAttachmentTitle(jsonObject1.getString("title"));
                        attachItem.setAttachmentUrl(jsonObject1.getString("url"));
                        attachItem.setAttachmentFileKey(jsonObject1.getString("filekey"));
                        attachItem.setAttachmentMd5(jsonObject1.getString("md5"));
                        attachItem.setAttachmentFilesize(jsonObject1.getString("filesize"));
                        attachList.add(attachItem);
                    }
                    item.setAttachmentItemList(attachList);
                    item.setUpdateTime(jsObject.getString("update_time"));
                    item.setCreateTime(jsObject.getString("create_time"));
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
