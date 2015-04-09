package com.centraltrillion.worklink.data;

import java.util.HashMap;

public class LoginUserDataItem implements DataItem {
    private String id;
    private String companyId;
    private HashMap<String, String> companyName = null;
    private HashMap<String, String> name = null;
    private HashMap<String, String> title = null;

    private String motto;
    private String intro;
    private String status;

    private String photoUrl;
    private String photoMd5;
    private String photoFileKey;

    private String bannerUrl;
    private String bannerMd5;
    private String bannerFileKey;

    public void setBannerUrl(String url) {
        this.bannerUrl = url;
    }

    public void setBannerMd5(String md5) {
        this.bannerMd5 = md5;
    }

    public void setBannerFileKey(String filekey) {
        this.bannerFileKey = filekey;
    }

    public String getBannerUrl() {
        return this.bannerUrl;
    }

    public String getBannerMd5() {
        return this.bannerMd5;
    }

    public String getBannerFileKey() {
        return this.bannerFileKey;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyId() {
        return this.companyId;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }

    public String getMotto() {
        return this.motto;
    }

    public void setCompanyName(String key, String value) {
        if (companyName == null) {
            companyName = new HashMap<String, String>();
        }
        companyName.put(key, value);
    }

    public String getCompanyName(String key) {
        return companyName.get(key);
    }

    public void setName(String key, String value) {
        if (name == null) {
            name = new HashMap<String, String>();
        }
        name.put(key, value);
    }

    public String getName(String key) {
        return name.get(key);
    }

    public void setTitle(String key, String value) {
        if (title == null) {
            title = new HashMap<String, String>();
        }
        title.put(key, value);
    }

    public String getTitle(String key) {
        return title.get(key);
    }


    public String getPhotoFileKey() {
        return photoFileKey;
    }

    public void setPhotoFileKey(String photoFileKey) {
        this.photoFileKey = photoFileKey;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

}
