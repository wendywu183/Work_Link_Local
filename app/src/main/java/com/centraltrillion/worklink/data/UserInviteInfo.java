package com.centraltrillion.worklink.data;


import java.util.ArrayList;

public class UserInviteInfo implements DataItem {
    private String mRoomId;
    private String mRoomType;
    private String mCreateTime;
    private String mMsgId;
    private String mSenderId;
    private String mSenderName;
    private String mInviteContent;
    private String mMsgType;
    private String mType;
    private String mUrl;
    private String mMsgCreateTime;
    private ArrayList<String> memberIds = new ArrayList<String>();

    public String getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(String mCreateTime) {
        this.mCreateTime = mCreateTime;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String mRoomId) {
        this.mRoomId = mRoomId;
    }

    public String getRoomType() {
        return mRoomType;
    }

    public void setRoomType(String mRoomType) {
        this.mRoomType = mRoomType;
    }

    public String getMsgId() {
        return mMsgId;
    }

    public void setMsgId(String mMsgId) {
        this.mMsgId = mMsgId;
    }

    public String getSenderId() {
        return mSenderId;
    }

    public void setSenderId(String mSenderId) {
        this.mSenderId = mSenderId;
    }

    public String getSenderName() {
        return mSenderName;
    }

    public void setSenderName(String mSenderName) {
        this.mSenderName = mSenderName;
    }

    public String getInviteContent() {
        return mInviteContent;
    }

    public void setInviteContent(String mInviteContent) {
        this.mInviteContent = mInviteContent;
    }

    public String getMsgType() {
        return mMsgType;
    }

    public void setMsgType(String mMsgType) {
        this.mMsgType = mMsgType;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getMsgCreateTime() {
        return mMsgCreateTime;
    }

    public void setMsgCreateTime(String msgCreateTime) {
        this.mMsgCreateTime = msgCreateTime;
    }

    public ArrayList<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(ArrayList<String> memberIds) {
        this.memberIds = memberIds;
    }
}
