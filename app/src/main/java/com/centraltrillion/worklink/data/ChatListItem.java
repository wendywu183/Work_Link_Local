package com.centraltrillion.worklink.data;


/* Chat List Item */
public class ChatListItem {
    private String mUserId;
    private String mUserName;
    private String mIconUrl;
    private String mLastMessage;
    private String mLastMessageTime;
    private int mUserStatus;
    private int mUnReadMessageCount;
    private RoomInfo mRoomInfo;

//    public String getUserId() {
//        return mUserId;
//    }
//
//    public void setUserId(String mUserId) {
//        this.mUserId = mUserId;
//    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

//    public String getIconUrl() {
//        return mIconUrl;
//    }

//    public void setIconUrl(String mIconUrl) {
//        this.mIconUrl = mIconUrl;
//    }

//    public int getUserStatus() {
//        return mUserStatus;
//    }
//
//    public void setUserStatus(int mUserStatus) {
//        this.mUserStatus = mUserStatus;
//    }

    public String getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(String mLastMessage) {
        this.mLastMessage = mLastMessage;
    }

    public String getLastMessageTime() {
        return mLastMessageTime;
    }

    public void setLastMessageTime(String mLastMessageTime) {
        this.mLastMessageTime = mLastMessageTime;
    }

    public int getUnReadMessageCount() {
        return mUnReadMessageCount;
    }

    public void setUnReadMessageCount(int mUnReadMessageCount) {
        this.mUnReadMessageCount = mUnReadMessageCount;
    }

    public RoomInfo getRoomInfo() {
        return mRoomInfo;
    }

    public void setRoomInfo(RoomInfo mRoomInfo) {
        this.mRoomInfo = mRoomInfo;
    }
}
