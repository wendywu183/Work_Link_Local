package com.centraltrillion.worklink.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.centraltrillion.worklink.data.MessageItem;
import com.centraltrillion.worklink.data.RoomInfo;
import com.centraltrillion.worklink.utils.im.ImProvider;
import java.util.ArrayList;

public class DBUtility {

    /* ===== Message table operations ===== */

    public static synchronized ArrayList<MessageItem> selectMessage(Context ctx, String[] projections, String selections, String[] selectionArgs, String sortOrder) {
        ContentResolver sResolver = ctx.getContentResolver();
        Cursor c = null;
        ArrayList<MessageItem> msgItems = new ArrayList<MessageItem>();

        try {
            c = sResolver.query(ImProvider.URI_IM_MESSAGE, projections, selections, selectionArgs, sortOrder);

            if (c != null) {
                while (c.moveToNext()) {
                    MessageItem msgItem = new MessageItem();

                    msgItem.setRoomId(c.getString(1));
                    msgItem.setMsgTime(c.getString(2));
                    msgItem.setSenderId(c.getString(3));
                    msgItem.setSenderName(c.getString(4));
                    msgItem.setMessage(c.getString(5));
                    msgItem.setMsgId(c.getString(6));
                    msgItem.setMsgType(c.getString(7));
                    msgItem.setType(c.getString(8));
                    msgItem.setIsTouched(c.getString(10));
                    msgItem.setReadedMemberIds(c.getString(11));
                    msgItem.setIsRead(c.getString(12));
                    msgItem.setReadedCount(c.getString(13));
                    msgItem.setStatus(c.getString(14));
                    msgItem.setIndex(c.getString(15));

                    msgItems.add(msgItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        return msgItems;
    }

    public static synchronized void insertMessage(Context ctx, MessageItem... msgItems) {
        ContentResolver sResolver = ctx.getContentResolver();

        ContentValues[] values = new ContentValues[msgItems.length];
        int len = msgItems.length;

        for (int i = 0; i < len; i++) {
            ContentValues value = new ContentValues();
            MessageItem msgItem = msgItems[i];

            value.put("room_id", msgItem.getRoomId());
            value.put("time", msgItem.getMsgTime());
            value.put("sender_id", msgItem.getSenderId());
            value.put("sender_name", msgItem.getSenderName());
            value.put("message", msgItem.getMessage());
            value.put("message_id", msgItem.getMsgId());
            value.put("message_type", msgItem.getMsgType());
            value.put("type", msgItem.getType());
            value.put("touched", msgItem.isTouched());
            value.put("readed_member_ids", msgItem.getReadedMemberIds());
            value.put("readed", msgItem.isRead());
            value.put("readed_count", msgItem.getReadedCount());
            value.put("status", msgItem.getStatus());
            value.put("msg_index", msgItem.getIndex());

            values[i] = value;
        }

        sResolver.bulkInsert(ImProvider.URI_IM_MESSAGE, values);
    }

    public static synchronized void delMessage(Context ctx, String selections, String[] selectionArgs) {
        ContentResolver sResolver = ctx.getContentResolver();

        sResolver.delete(ImProvider.URI_IM_MESSAGE, selections, selectionArgs);
    }

    public static synchronized void updateMessage(Context ctx, String selection, String[] selectionArgs, MessageItem... msgItems) {
        ContentResolver sResolver = ctx.getContentResolver();
        int len = msgItems.length;

        for (int i = 0; i < len; i++) {
            MessageItem msgItem = msgItems[i];
            ContentValues value = new ContentValues();

            value.put("room_id", msgItem.getRoomId());
            value.put("time", msgItem.getMsgTime());
            value.put("sender_id", msgItem.getSenderId());
            value.put("sender_name", msgItem.getSenderName());
            value.put("message", msgItem.getMessage());
            value.put("message_id", msgItem.getMsgId());
            value.put("message_type", msgItem.getMsgType());
            value.put("type", msgItem.getType());
            value.put("touched", msgItem.isTouched());
            value.put("readed_member_ids", msgItem.getReadedMemberIds());
            value.put("readed", msgItem.isRead());
            value.put("readed_count", msgItem.getReadedCount());
            value.put("status", msgItem.getStatus());
            value.put("msg_index", msgItem.getIndex());

            sResolver.update(ImProvider.URI_IM_MESSAGE, value, selection, selectionArgs);
        }
    }

    /* ===== Room table operations ===== */

    public static synchronized ArrayList<RoomInfo> selectRoomInfo(Context ctx, String[] projections, String selection, String[] selectionArgs, String sortOrder) {
        ContentResolver sResolver = ctx.getContentResolver();
        Cursor c = null;
        RoomInfo roomInfo = null;
        ArrayList<RoomInfo> roomInfoList = null;

        try {
            c = sResolver.query(ImProvider.URI_IM_ROOM, projections, selection, selectionArgs, sortOrder);

            if (c != null) {
                roomInfoList = new ArrayList<RoomInfo>();

                while (c.moveToNext()) {
                    roomInfo = new RoomInfo();

                    roomInfo.setRoomId(c.getString(1));
                    roomInfo.setMember(c.getString(2));
                    roomInfo.setType(c.getString(3));
                    roomInfo.setIconUrl(c.getString(4));
                    roomInfo.setRoomName(c.getString(5));

                    roomInfoList.add(roomInfo);
                }
            } else {
                return null;
            }

        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        return roomInfoList;
    }

    public static synchronized void insertRoomInfo(Context ctx, RoomInfo... roomInfos) {
        ContentResolver sResolver = ctx.getContentResolver();
        ContentValues[] values = new ContentValues[roomInfos.length];
        int len = roomInfos.length;

        for (int i = 0; i < len; i++) {
            RoomInfo roomInfo = roomInfos[i];
            ContentValues value = new ContentValues();

            value.put("room_id", roomInfo.getRoomId());
            value.put("member", roomInfo.getMember());
            value.put("type", roomInfo.getType());
            value.put("icon", roomInfo.getIconUrl());
            value.put("name", roomInfo.getRoomName());

            values[i] = value;
        }
        sResolver.bulkInsert(ImProvider.URI_IM_ROOM, values);
    }

    public static synchronized void delRoomInfo(Context ctx, String selections, String[] selectionArgs) {
        ContentResolver sResolver = ctx.getContentResolver();

        sResolver.delete(ImProvider.URI_IM_ROOM, selections, selectionArgs);

    }

    public static synchronized void updateRoomInfo(Context ctx, String selection, String[] selectionArgs, RoomInfo... roomInfos) {
        ContentResolver sResolver = ctx.getContentResolver();
        int len = roomInfos.length;

        for (int i = 0; i < len; i++) {
            RoomInfo roomInfo = roomInfos[i];
            ContentValues value = new ContentValues();

            value.put("room_id", roomInfo.getRoomId());
            value.put("member", roomInfo.getMember());
            value.put("type", roomInfo.getType());
            value.put("icon", roomInfo.getIconUrl());
            value.put("name", roomInfo.getRoomName());

            sResolver.update(ImProvider.URI_IM_ROOM, value, selection, selectionArgs);
        }
    }

}
