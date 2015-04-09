package com.centraltrillion.worklink.data;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;

public class ContactUserItem implements DataItem, Parcelable {
    private String id;
    private String groupId;
    private String phone;
    private String photoUrl;
    private String photoMd5;
    private String photoFileKey;
    private String status;
    private String jobMail;
    private String groupUpdateTime;
    private String roomId;
    private String memberCount;
    public HashMap<String, String> name = null;
    public HashMap<String, String> title = null;
    public HashMap<String, String> dept = null;

    public void setDept(String key, String value) {
        if (dept == null) {
            dept = new HashMap<String, String>();
        }
        dept.put(key, value);
    }

    public HashMap<String, String> getDept() {
        return dept;
    }

    public String getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(String memberCount) {
        this.memberCount = memberCount;
    }

    public void setTitle(String key, String value) {
        if (title == null) {
            title = new HashMap<String, String>();
        }
        title.put(key, value);
    }

    public HashMap<String, String> getTitle() {
        return title;
    }

    public void setName(String key, String value) {
        if (name == null) {
            name = new HashMap<String, String>();
        }
        name.put(key, value);
    }

    public HashMap<String, String> getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupUpdateTime() {
        return groupUpdateTime;
    }

    public void setGroupUpdateTime(String groupUpdateTime) {
        this.groupUpdateTime = groupUpdateTime;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoMd5() {
        return photoMd5;
    }

    public void setPhotoMd5(String photoMd5) {
        this.photoMd5 = photoMd5;
    }

    public String getPhotoFileKey() {
        return photoFileKey;
    }

    public void setPhotoFileKey(String photoFileKey) {
        this.photoFileKey = photoFileKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getJobMail() {
        return jobMail;
    }

    public void setJobMail(String jobMail) {
        this.jobMail = jobMail;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public ContactUserItem(ContactUserItem item) {

        this.phone = "" + item.getPhone();
        this.id = "" + item.getId();
        this.photoUrl = "" + item.getPhotoUrl();
        this.photoMd5 = "" + item.getPhotoMd5();
        this.photoFileKey = "" + item.getPhotoFileKey();
        this.status = "" + item.getStatus();
        this.jobMail = "" + item.getJobMail();
        this.name = new HashMap<>(item.name);
        this.title = new HashMap<>(item.title);
    }

    public static final Creator<ContactUserItem> CREATOR = new Creator<ContactUserItem>() {
        public ContactUserItem createFromParcel(Parcel in) {
            return new ContactUserItem(in);
        }

        public ContactUserItem[] newArray(int size) {
            return new ContactUserItem[size];
        }
    };

    public ContactUserItem(Parcel in) {
        readFromParcel(in);
    }

    public ContactUserItem() {}

    private void readFromParcel(Parcel in) {
        id = in.readString();
        roomId = in.readString();
        groupId = in.readString();
        phone = in.readString();
        photoUrl = in.readString();
        photoMd5 = in.readString();
        photoFileKey = in.readString();
        status = in.readString();
        jobMail = in.readString();
        groupUpdateTime = in.readString();
        name = (HashMap<String, String>) in.readSerializable();
        title = (HashMap<String, String>) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(roomId);
        dest.writeString(groupId);
        dest.writeString(phone);
        dest.writeString(photoUrl);
        dest.writeString(photoMd5);
        dest.writeString(photoFileKey);
        dest.writeString(status);
        dest.writeString(jobMail);
        dest.writeString(groupUpdateTime);
        dest.writeSerializable(name);
        dest.writeSerializable(title);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
