package com.centraltrillion.worklink.utils.parser;

import com.centraltrillion.worklink.data.DataItem;
import com.centraltrillion.worklink.data.RoomInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class RoomInfoParser implements IParser {
    @Override
    public <T extends DataItem> T getParsingData(String jsonStr, Class<T> type) {
        try {
            RoomInfo roomInfo = new RoomInfo();
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray msgRecordJsonAry = jsonObject.getJSONArray("member_message_record");
            ArrayList<RoomInfo.MemberMsgRecord> msgRecordsList = roomInfo.getMemberMsgRecordList();

            roomInfo.setRoomId(jsonObject.getString("room_id"));
            roomInfo.setType(jsonObject.getString("room_type"));
            roomInfo.setRoomName(jsonObject.getString("room_name"));
            for (int i = 0, len = msgRecordJsonAry.length(); i < len; i++) {
                JSONObject msgRecordJsonObj = msgRecordJsonAry.getJSONObject(i);
                RoomInfo.MemberMsgRecord memberMsgRecord = new RoomInfo.MemberMsgRecord();

                memberMsgRecord.setMemberId(msgRecordJsonObj.getString("user_id"));
                memberMsgRecord.setInitMsgId(msgRecordJsonObj.has("init_message_id") ? msgRecordJsonObj.getString("init_message_id") : "0");
                memberMsgRecord.setLastMsgId(msgRecordJsonObj.has("last_message_id") ? msgRecordJsonObj.getString("last_message_id") : "0");
                msgRecordsList.add(memberMsgRecord);
            }
            roomInfo.setMemberMsgRecordList(msgRecordsList);

            return type.cast(roomInfo);
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
