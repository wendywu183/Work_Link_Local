package com.centraltrillion.worklink.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageItem implements DataItem, Parcelable{

    private boolean mIsHead = false;
    private String mHeadTitle = "";

    private String mMsgId;
    private String mRoomId;
    private String mSenderId;
    private String mSenderName;
    private String mContent;
    /*
    *  Message type category
    *  (1.) message: Sent from user to delivery some messages, files and so on.
    *  (2.) event: Sent from server to notify some event happened.
    * */
    private String mMsgType;
    /*
     *  Type category
     *  (1.) text: Sent from user to delivery some messages in pure text format
     *  (2.) file: For file transmission.
     */
    private String mType;
    private String mUrl;
    private String mMsgTime;
    /*
    *  1: Message readed
    *  0: Unread
    * */
    private String mIsRead = "0";
    /* To indicate how many people have read the message. */
    private String mReadedCount = "0";
    /*
    *  1: Message touched
    *  0: Touched
    * */
    private String mIsTouched = "0";
    /*
     *
     *  0: Message sending
     *  1: Message sent successfully
     *  2: Message sent fail.
     * */
    private String mStatus = "0";
    private String mIndex = "0";

    /*
    *  Used to identify who has read the message.
    *  It store member ids in JsonArray string.
    * */
    private String mReadedMemberIds = "";


    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public String getIndex() {
        return mIndex;
    }

    public void setIndex(String mIndex) {
        this.mIndex = mIndex;
    }

    public boolean isHead() {
        return mIsHead;
    }

    public void setIsHead(boolean mIsHead) {
        this.mIsHead = mIsHead;
    }

    public String getHeadTitle() {
        return mHeadTitle;
    }

    public void setHeadTitle(String mHeadTitle) {
        this.mHeadTitle = mHeadTitle;
    }

    public String getMsgId() {
        return mMsgId;
    }

    public void setMsgId(String mMsgId) {
        this.mMsgId = mMsgId;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String mRoomId) {
        this.mRoomId = mRoomId;
    }

    public void setSenderId(String user_id) {
        this.mSenderId = user_id;
    }

    public String getSenderId() {
        return mSenderId;
    }

    public void setSenderName(String mSenderName) {
        this.mSenderName = mSenderName;
    }

    public String getSenderName() {
        return mSenderName;
    }

    public void setMessage(String message) {
        this.mContent = message;
    }

    public String getMessage() {
        return mContent;
    }

    public String getMsgTime() {
        return mMsgTime;
    }

    public void setMsgTime(String msgTime) {
        this.mMsgTime = msgTime;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getMsgType() {
        return mMsgType;
    }

    public void setMsgType(String mMsgType) {
        this.mMsgType = mMsgType;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String isRead() {
        return mIsRead;
    }

    public void setIsRead(String mIsRead) {
        this.mIsRead = mIsRead;
    }

    public String isTouched() {
        return mIsTouched;
    }

    public void setIsTouched(String mIsTouched) {
        this.mIsTouched = mIsTouched;
    }

    public String getReadedMemberIds() {
        return mReadedMemberIds;
    }

    public void setReadedMemberIds(String mReadedMemberIds) {
        this.mReadedMemberIds = mReadedMemberIds;
    }

    public String getReadedCount() {
        return mReadedCount;
    }

    public void setReadedCount(String mReadedCount) {
        this.mReadedCount = mReadedCount;
    }

    public MessageItem() {}

    public MessageItem(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MessageItem> CREATOR = new Parcelable.Creator<MessageItem>() {
        public MessageItem createFromParcel(Parcel in) {
            return new MessageItem(in);
        }

        public MessageItem[] newArray(int size) {
            return new MessageItem[size];
        }
    };

    private void readFromParcel(Parcel in) {
        mMsgId = in.readString();
        mRoomId = in.readString();
        mSenderId = in.readString();
        mSenderName = in.readString();
        mContent = in.readString();
        mMsgType = in.readString();
        mType = in.readString();
        mUrl = in.readString();
        mMsgTime = in.readString();
        mReadedMemberIds = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMsgId);
        dest.writeString(mRoomId);
        dest.writeString(mSenderId);
        dest.writeString(mSenderName);
        dest.writeString(mContent);
        dest.writeString(mMsgType);
        dest.writeString(mType);
        dest.writeString(mUrl);
        dest.writeString(mMsgTime);
        dest.writeString(mReadedMemberIds);
    }
}
