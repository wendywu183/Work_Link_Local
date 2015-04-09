package com.centraltrillion.worklink.data;

public class ContactUserPhotoItem {

    private String userId;
    private String photoUrl;
    private String photoMd5;
    private String photoFileKey;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
