package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.data.MessageNotificationItem;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class MessageNotificationParser implements IParser {
    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            MessageNotificationItem msgNotificationItem = new MessageNotificationItem();
            JSONObject jsonObject = new JSONObject(jsonStr);

            msgNotificationItem.setMsg(jsonObject.getString("content"));
            msgNotificationItem.setSenderId(jsonObject.getString("sender_id"));
            msgNotificationItem.setSenderName(jsonObject.getString("sender_name"));
            msgNotificationItem.setMsgTime(jsonObject.getString("create_time"));
            msgNotificationItem.setMsgId(jsonObject.getString("message_id"));
            msgNotificationItem.setRoomId(jsonObject.getString("room_id"));
            msgNotificationItem.setEventMembers(jsonObject.getJSONArray("event_members"));
            msgNotificationItem.setType(jsonObject.getString("type"));
            msgNotificationItem.setMsgType(jsonObject.getString("message_type"));
            msgNotificationItem.setUrl(jsonObject.getString("url"));

            return type.cast(msgNotificationItem);
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
