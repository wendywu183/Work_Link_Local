package com.centraltrillion.worklink.data;

import org.json.JSONArray;

public class MultipleChatInfo implements DataItem {
    private String mRoomId;
    private String mRoomType;
    private String mCreateTime;
    private MessageRecord mMsgRecord = new MessageRecord();

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

    public String getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(String mCreateTime) {
        this.mCreateTime = mCreateTime;
    }

    public MessageRecord getMsgRecord() {
        return mMsgRecord;
    }

    public void setMsgRecord(MessageRecord mMsgRecord) {
        this.mMsgRecord = mMsgRecord;
    }

    public static class MessageRecord {
        private String mRoomId;
        private String mMsgId;
        private String mSenderId;
        private String mSenderName;
        private String mContent;
        private String mMsgType;
        private JSONArray mEventMembers;
        private String mType;
        private String mUrl;
        private String mCreateTime;

        public String getRoomId() {
            return mRoomId;
        }

        public void setRoomId(String mRoomId) {
            this.mRoomId = mRoomId;
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

        public String getContent() {
            return mContent;
        }

        public void setContent(String mContent) {
            this.mContent = mContent;
        }

        public String getMsgType() {
            return mMsgType;
        }

        public void setMsgType(String mMsgType) {
            this.mMsgType = mMsgType;
        }

        public JSONArray getEventMembers() {
            return mEventMembers;
        }

        public void setEventMembers(JSONArray mEventMembers) {
            this.mEventMembers = mEventMembers;
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

        public String getCreateTime() {
            return mCreateTime;
        }

        public void setCreateTime(String mCreateTime) {
            this.mCreateTime = mCreateTime;
        }
    }

}
