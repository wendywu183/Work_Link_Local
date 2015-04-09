package com.centraltrillion.worklink.utils.im;

public class ImMessageEvent  {
    /**
     *  EVENT_CONNECTED for receiving the notification that socket connect successfully.
     */
    public static final String EVENT_CONNECTED = "connected";

    /* TODO: Maybe we need to maintain the event mapping table in outer file format. It's hard-code here temporarily. */
    /**
     *  EVENT_JOIN_ROOM used for emit a event to server to let user(s) join a conversation room.
     */
    public static final String EVENT_JOIN_ROOM = "joinRoom";

    /**
     *  EVENT_JOIN_ROOM used for receiving the notification that join room successfully.
     */
    public static final String EVENT_JOIN_ROOM_SUCCESS = "joinRoomSuccess";
    /**
     *  EVENT_LEAVE_ROOM used for emit a event to server to let user(s) leave a conversation room.
     */
    public static final String EVENT_LEAVE_ROOM = "leaveRoom";
    /**
     *  EVENT_SEND used to let user send a message.
     */
    public static final String EVENT_SEND_MESSAGE = "sendMessage";

    /**
     *  EVENT_RECEIVE_MESSAGE for receiving the notification that sent message from self successfully or receive the message from member.
     */
    public static final String EVENT_RECEIVE_MESSAGE = "receiveMessage";

    /**
     *  EVENT_UPDATE_READMESSAGE used for emit server a event to send an read message notification to others.
     */
    public static final String EVENT_READ_MESSAGE = "readMessage";

    /**
     *  EVENT_UPDATE_READMESSAGE for receiving the notification that sent message from self is read by others.
     */
    public static final String EVENT_UPDATE_READMESSAGE = "updateReadMessage";
}
