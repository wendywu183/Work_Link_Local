package com.centraltrillion.worklink.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class RoomInfo implements Parcelable, DataItem {
    private String mRoomId = "";
    private String mMember = "";
    private String mType = "";
    private String mIconUrl = "";
    private String mRoomName = "";
    /* Used to mark the last read message id */
    private ArrayList<MemberMsgRecord> mMemberMsgRecordList = new ArrayList<MemberMsgRecord>();


    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String mRoomId) {
        this.mRoomId = mRoomId;
    }

    public String getMember() {
        return mMember;
    }

    public void setMember(String mMember) {
        this.mMember = mMember;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String mIconUrl) {
        this.mIconUrl = mIconUrl;
    }

    public String getRoomName() {
        return mRoomName;
    }

    public void setRoomName(String mRoomName) {
        this.mRoomName = mRoomName;
    }

    public ArrayList<MemberMsgRecord> getMemberMsgRecordList() {
        return mMemberMsgRecordList;
    }

    public void setMemberMsgRecordList(ArrayList<MemberMsgRecord> mMemberMsgRecordList) {
        this.mMemberMsgRecordList = mMemberMsgRecordList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RoomInfo> CREATOR = new Creator<RoomInfo>() {
        public RoomInfo createFromParcel(Parcel in) {
            return new RoomInfo(in);
        }

        public RoomInfo[] newArray(int size) {
            return new RoomInfo[size];
        }
    };

    public RoomInfo(Parcel in) {
        readFromParcel(in);
    }

    public RoomInfo() {
    }

    private void readFromParcel(Parcel in) {
        mRoomId = in.readString();
        mMember = in.readString();
        mType = in.readString();
        mIconUrl = in.readString();
        mRoomName = in.readString();
        mMemberMsgRecordList = in.readArrayList(MemberMsgRecord.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mRoomId);
        dest.writeString(mMember);
        dest.writeString(mType);
        dest.writeString(mIconUrl);
        dest.writeString(mRoomName);
        dest.writeList(mMemberMsgRecordList);
    }


    public static class MemberMsgRecord implements Parcelable {
        private String mMemberId = null;
        private String mInitMsgId = null;
        private String mLastMsgId = null;


        public String getMemberId() {
            return mMemberId;
        }

        public void setMemberId(String mMemberId) {
            this.mMemberId = mMemberId;
        }

        public String getInitMsgId() {
            return mInitMsgId;
        }

        public void setInitMsgId(String mInitMsgId) {
            this.mInitMsgId = mInitMsgId;
        }

        public String getLastMsgId() {
            return mLastMsgId;
        }

        public void setLastMsgId(String mLastMsgId) {
            this.mLastMsgId = mLastMsgId;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<MemberMsgRecord> CREATOR = new Creator<MemberMsgRecord>() {
            public MemberMsgRecord createFromParcel(Parcel in) {
                return new MemberMsgRecord(in);
            }

            public MemberMsgRecord[] newArray(int size) {
                return new MemberMsgRecord[size];
            }
        };

        public MemberMsgRecord(Parcel in) {
            readFromParcel(in);
        }

        public MemberMsgRecord() {
        }

        private void readFromParcel(Parcel in) {
            mMemberId = in.readString();
            mInitMsgId = in.readString();
            mLastMsgId = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mMemberId);
            dest.writeString(mInitMsgId);
            dest.writeString(mLastMsgId);
        }
    }
}
