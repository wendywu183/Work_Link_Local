package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.data.UserInviteInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class UserInviteInfoParser implements IParser {
    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            UserInviteInfo inviteInfo = new UserInviteInfo();
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONObject msgRecordJsonObj = jsonObj.getJSONObject("message_record");

            inviteInfo.setRoomId(jsonObj.getString("room_id"));
            inviteInfo.setRoomType(jsonObj.getString("room_type"));
            inviteInfo.setCreateTime(jsonObj.getString("create_time"));
            inviteInfo.setMsgId(msgRecordJsonObj.getString("message_id"));
            inviteInfo.setSenderId(msgRecordJsonObj.getString("sender_id"));
            inviteInfo.setSenderName(msgRecordJsonObj.getString("sender_name"));
            inviteInfo.setInviteContent(msgRecordJsonObj.getString("content"));
            inviteInfo.setMsgType(msgRecordJsonObj.getString("message_type"));

            JSONArray idsJsonAry = msgRecordJsonObj.getJSONArray("event_members");
            int len = idsJsonAry.length();
            ArrayList<String> memberIds = new ArrayList<String>();

            for(int i = 0 ; i < len ; i++) {
                JSONObject memberJsonObj = idsJsonAry.getJSONObject(i);

                if (memberJsonObj.has("member_id")) {
                    memberIds.add(memberJsonObj.getString("member_id"));
                }
            }
            inviteInfo.setMemberIds(memberIds);
            inviteInfo.setType(msgRecordJsonObj.getString("type"));
            inviteInfo.setUrl(msgRecordJsonObj.getString("url"));
            inviteInfo.setMsgCreateTime(msgRecordJsonObj.getString("create_time"));

            return type.cast(inviteInfo);
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
