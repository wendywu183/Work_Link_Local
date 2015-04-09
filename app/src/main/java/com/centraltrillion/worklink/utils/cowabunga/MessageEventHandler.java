package com.centraltrillion.worklink.utils.cowabunga;

import android.content.Context;
import android.content.Intent;
import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.MessageItem;
import com.centraltrillion.worklink.data.MessageNotificationItem;
import com.centraltrillion.worklink.data.RoomInfo;
import com.centraltrillion.worklink.utils.DBUtility;
import com.centraltrillion.worklink.utils.ParserUtility;
import com.centraltrillion.worklink.utils.PreLoaderUtility;
import com.centraltrillion.worklink.utils.Utility;
import com.centraltrillion.worklink.utils.gcm.GCMIntentService;
import com.centraltrillion.worklink.utils.gcm.GCMUtility;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Date;

/* TODO: Naming will be refined and refactor later. */
public class MessageEventHandler implements IEventIHandler {
    @Override
    public void doEvent(Context ctx, String eventJson) {
        try {
            MessageNotificationItem msgNotifItem = ParserUtility.getParsingResult(ParserUtility.PARSER_CA_MESSAGE_NOTIFICATION, eventJson, MessageNotificationItem.class);
            MessageItem item = new MessageItem();
            RoomInfo roomInfo = new RoomInfo();
            String eventMembers = msgNotifItem.getEventMembers().toString();
            JSONArray memberJsonAry;

            roomInfo.setRoomId(msgNotifItem.getRoomId());
            item.setRoomId(roomInfo.getRoomId());
            item.setSenderId(msgNotifItem.getSenderId());
            item.setSenderName(msgNotifItem.getSenderName());
            item.setMsgId(msgNotifItem.getMsgId());
            item.setMessage(msgNotifItem.getMsg());
            item.setType(msgNotifItem.getType());
            item.setMsgType(msgNotifItem.getMsgType());
            item.setMsgTime(Utility.transDateToString("yyyy-M-d HH:mm", new Date()));

            String senderName = PreLoaderUtility.getInstance(ctx).getNameById(item.getSenderId());
            if (roomInfo.getRoomId().startsWith("s")) {
                memberJsonAry = new JSONArray();

                memberJsonAry.put(senderName);
                roomInfo.setMember(memberJsonAry.toString());
            } else {
                memberJsonAry = new JSONArray(eventMembers);
                JSONArray temp = new JSONArray();
                int len = memberJsonAry.length();

                for (int i = 0; i < len; i++) {
                    String memberId = memberJsonAry.getJSONObject(i).getString("member_id");
                    String memberName = PreLoaderUtility.getInstance(ctx).getNameById(memberId);

                    temp.put(memberName);
                }
                temp.put(senderName);
                memberJsonAry = temp;
            }

                    /* Only single room or event type multiple room have event members. */
            if (roomInfo.getRoomId().startsWith("s") || (item.getMsgType().equals("event") && item.getType().isEmpty())) {
                roomInfo.setMember(memberJsonAry.toString());
            }
            DBUtility.delRoomInfo(ctx, "room_id=?", new String[]{roomInfo.getRoomId()});
            DBUtility.insertRoomInfo(ctx, roomInfo);
             /* TODO: It will be refined later. */
             /* 1. Insert new message item and room information if room information is not exist before. */
            ArrayList<MessageItem> msgItems = DBUtility.selectMessage(ctx, null, "room_id=?", new String[]{roomInfo.getRoomId()}, null);

            DBUtility.delMessage(ctx, "room_id=?", new String[]{roomInfo.getRoomId()});
            msgItems.add(item);
            DBUtility.insertMessage(ctx, msgItems.toArray(new MessageItem[0]));

             /* 2. Broadcast intent to notify others. */
            Intent broadcastIntent = null;
            broadcastIntent = new Intent(GCMIntentService.GCM_RECEIVE_MESSAGE_BROADCAST);

            ctx.sendBroadcast(broadcastIntent);
                    /* 3. Display the notification. */
            if (item.getMsgType().equals("event")) {
                GCMUtility.sendNotification(ctx, MainActivity.class, R.mipmap.ic_launcher, ctx.getString(R.string.app_name), item.getMessage());
            } else {
                GCMUtility.sendNotification(ctx, MainActivity.class, R.mipmap.ic_launcher, ctx.getString(R.string.app_name), item.getSenderName() + ":" + item.getMessage());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
