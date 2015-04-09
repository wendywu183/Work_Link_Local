package com.centraltrillion.worklink.utils.parser;

import android.util.Log;

import com.centraltrillion.worklink.data.AttachmentItem;
import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.data.NoticeItem;
import com.centraltrillion.worklink.data.NoticeToItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NoticeListParser implements IParser {
    private static final String DEBUG = "NoticeListParser";

    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        return null;
    }

    @Override
    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type) {
        try {
            if (jsonStr != null) {
                ArrayList<T> list = new ArrayList<T>();
                JSONArray jsonArray = new JSONArray(jsonStr);
                JSONObject jsonObject = null;
                NoticeItem item = null;
                AttachmentItem attachmentItem = null;

                int len = jsonArray.length();
                for(int i = 0; i < len; i++){
                    item = new NoticeItem();
                    jsonObject = jsonArray.getJSONObject(i);
                    item.setNoticeId(jsonObject.getString("_id"));
                    JSONObject jsonObject1 = jsonObject.getJSONObject("notice_from");
                    JSONObject jsonObject2 = jsonObject1.getJSONObject("name");
                    Iterator<String> iterator = jsonObject2.keys();
                    while (iterator.hasNext()) {
                        String next = iterator.next();
                        item.setName(next, jsonObject2.getString(next));
                    }
                    jsonObject2 = jsonObject1.getJSONObject("dept");
                    iterator = jsonObject2.keys();
                    while (iterator.hasNext()) {
                        String next = iterator.next();
                        item.setDept(next, jsonObject2.getString(next));
                    }
                    item.setEmployId(jsonObject1.getString("employee_id"));
                    item.setUserId(jsonObject1.getString("_id"));
                    item.setContent(jsonObject.getString("content"));
                    item.setTitle(jsonObject.getString("title"));
                    item.setCompanyId(jsonObject.getString("company_id"));
                    item.setReferenceId(jsonObject.getString("references_id"));
                    item.setUpdateTime(jsonObject.getString("update_time"));
                    item.setCreateTime(jsonObject.getString("create_time"));
                    item.setStarred(jsonObject.getString("starred"));
                    item.setRead(jsonObject.getString("read"));
                    JSONArray jsonArray1 = jsonObject.getJSONArray("attachment_url");
                    int length = jsonArray1.length();
                    AttachmentItem attachItem = null;
                    List<AttachmentItem> attachList = new ArrayList<AttachmentItem>();
                    for (int j = 0; j < length; j++) {
                        jsonObject1 = jsonArray1.getJSONObject(j);
                        attachItem = new AttachmentItem();
                        attachItem.setAttachmentId(jsonObject1.getString("_id"));
                        attachItem.setAttachmentTitle(jsonObject1.getString("title"));
                        attachItem.setAttachmentUrl(jsonObject1.getString("url"));
                        attachItem.setAttachmentFileKey(jsonObject1.getString("filekey"));
                        attachItem.setAttachmentMd5(jsonObject1.getString("md5"));
                        attachItem.setAttachmentFilesize(jsonObject1.getString("filesize"));
                        attachList.add(attachItem);
                    }
                    item.setAttachmentItemList(attachList);
                    NoticeToItem noticeToItem = null;
                    List<NoticeToItem> noticeToList = new ArrayList<NoticeToItem>();
                    jsonArray1 = jsonObject.getJSONArray("notice_to");
                    length = jsonArray1.length();
                    for(int j = 0; j < length; j++){
                        jsonObject1 = jsonArray1.getJSONObject(j);
                        noticeToItem = new NoticeToItem();
                        noticeToItem.setUserId(jsonObject1.getString("_id"));
                        jsonObject2 =jsonObject1.getJSONObject("name");
                        iterator = jsonObject2.keys();
                        while (iterator.hasNext()) {
                            String next = iterator.next();
                            noticeToItem.setName(next, jsonObject2.getString(next));
                        }
                        noticeToList.add(noticeToItem);
                    }
                    item.setNoticeToItemList(noticeToList);
                    list.add(type.cast(item));
                }
                return list;

            } else {
                return null;
            }
        } catch (JSONException e) {
            Log.e(DEBUG, "json exception e = " + e.toString());
            e.printStackTrace();
        }
        return null;
    }
}
