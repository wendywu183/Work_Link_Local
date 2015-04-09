package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.data.MultipleChatInfo;
import com.centraltrillion.worklink.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class MultipleChatInfoParser implements IParser {
    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            MultipleChatInfo multipleChatInfo = new MultipleChatInfo();
            MultipleChatInfo.MessageRecord msgRecord = multipleChatInfo.getMsgRecord();
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONObject msgRecordJsonObj = jsonObj.getJSONObject("message_record");
            JSONArray eventMembersJsonAry = new JSONArray();
            TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
            TimeZone localTimeZone = TimeZone.getDefault();
            String chatCreateTime = jsonObj.getString("create_time");
            String msgCreateTime = msgRecordJsonObj.getString("create_time");
            chatCreateTime = Utility.transDateToString("yyyy-M-d HH:mm", Utility.parseStringToDate("yyyy-M-d'T'HH:mm:ss.SSS'Z'", chatCreateTime, Locale.US, gmtTimeZone), Locale.US, localTimeZone);
            msgCreateTime = Utility.transDateToString("yyyy-M-d HH:mm", Utility.parseStringToDate("yyyy-M-d HH:mm:ss.SSS'Z'", msgCreateTime, Locale.US, gmtTimeZone), Locale.US, localTimeZone);

            multipleChatInfo.setRoomId(jsonObj.getString("room_id"));
            multipleChatInfo.setRoomType(jsonObj.getString("room_type"));
            multipleChatInfo.setCreateTime(chatCreateTime);
            multipleChatInfo.setMsgRecord(msgRecord);

            msgRecord.setRoomId(msgRecordJsonObj.getString("room_id"));
            msgRecord.setMsgId(msgRecordJsonObj.getString("message_id"));
            msgRecord.setSenderId(msgRecordJsonObj.getString("sender_id"));
            msgRecord.setSenderName(msgRecordJsonObj.getString("sender_name"));
            msgRecord.setContent(msgRecordJsonObj.getString("content"));
            msgRecord.setMsgType(msgRecordJsonObj.getString("message_type"));
            msgRecord.setType(msgRecordJsonObj.getString("type"));
            msgRecord.setUrl(msgRecordJsonObj.getString("url"));
            msgRecord.setEventMembers(eventMembersJsonAry);
            msgRecord.setCreateTime(msgCreateTime);

            return type.cast(multipleChatInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public <T extends DataItem> ArrayList<T> getParsingList(String jsonStr, Class<T> type) {
        return null;
    }
}
