package com.centraltrillion.worklink.data;

import java.util.ArrayList;

public class ChatListSearchResultItem {
    public RoomInfo getRoomInfo() {
        return mRoomInfo;
    }

    public void setRoomInfo(RoomInfo mRoomInfo) {
        this.mRoomInfo = mRoomInfo;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String mPhotoUrl) {
        this.mPhotoUrl = mPhotoUrl;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mSearchChatContents) {
        this.mContent = mSearchChatContents;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public ArrayList<MessageItem> getMsgItemList() {
        return mMsgItemList;
    }

    public void setMsgItemList(ArrayList<MessageItem> msgItemList) {
        this.mMsgItemList = msgItemList;
    }

    private RoomInfo mRoomInfo;
    private String mName;
    private String mPhotoUrl;
    private String mContent;
    /*
    * 0:Contact (include: Single, Multiple. Group type chat)
    * 1:Chat content
    * */
    /* TODO: It's will be refined for more readable constant. */
    private String mType;
    private ArrayList<MessageItem> mMsgItemList;
}
