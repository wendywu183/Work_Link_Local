package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.data.MessageItem;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class MessageParser implements IParser {
    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            MessageItem messageItem = new MessageItem();
            JSONObject jsonObject = new JSONObject(jsonStr);
            String msgTime = jsonObject.getString("create_time");
            TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
            TimeZone localTimeZone = TimeZone.getDefault();
            msgTime = Utility.transDateToString("yyyy-M-d HH:mm", Utility.parseStringToDate("yyyy-M-d HH:mm:ss.SSS'Z'", msgTime, Locale.US, gmtTimeZone), Locale.getDefault(), localTimeZone);

            /* Event message has no "index", so we set it as the empty string. */
            messageItem.setIndex(jsonObject.has("index") ? jsonObject.getString("index") : "");
            messageItem.setRoomId(jsonObject.getString("room_id"));
            messageItem.setMsgId(jsonObject.getString("message_id"));
            messageItem.setSenderId(jsonObject.getString("sender_id"));
            messageItem.setSenderName(jsonObject.getString("sender_name"));
            messageItem.setMessage(jsonObject.getString("content"));
            messageItem.setMsgType(jsonObject.getString("message_type"));
            messageItem.setType(jsonObject.getString("type"));
            messageItem.setUrl(jsonObject.getString("url"));
            messageItem.setMsgTime(msgTime);

            return type.cast(messageItem);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type) {
        return null;
    }
}
