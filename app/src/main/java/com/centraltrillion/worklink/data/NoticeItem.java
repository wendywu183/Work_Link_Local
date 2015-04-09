package com.centraltrillion.worklink.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoticeItem implements DataItem {

    private String noticeId = null;
    private String companyId = null;
    private String referenceId = null;
    private String title = null;
    private String content = null;
    private String read = null;
    private String updateTime = null;
    private String createTime = null;
    private String photoUrl = null;
    private String photoMd5 = null;
    private String photoFileKey = null;
    private String employId = null;
    private String userId = null;
    private String starred = null;
    private List<AttachmentItem> attachmentItemList = null;
    private List<NoticeToItem> noticeToItemList = null;

    public HashMap<String, String> name = null;
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

    public void setName(String key, String value) {
        if (name == null) {
            name = new HashMap<String, String>();
        }
        name.put(key, value);
    }

    public HashMap<String, String> getName() {
        return name;
    }

    public List<AttachmentItem> getAttachmentItemList() {
        if (attachmentItemList == null) {
            attachmentItemList = new ArrayList<AttachmentItem>();
        }
        return attachmentItemList;
    }

    public void setAttachmentItemList(List<AttachmentItem> attachmentItemList) {
        this.attachmentItemList = attachmentItemList;
    }

    public List<NoticeToItem> getNoticeToItemList() {
        if (noticeToItemList == null) {
            noticeToItemList = new ArrayList<NoticeToItem>();
        }
        return noticeToItemList;
    }

    public void setNoticeToItemList(List<NoticeToItem> noticeToItemList) {
        this.noticeToItemList = noticeToItemList;
    }

    public String getStarred() {
        return starred;
    }

    public void setStarred(String starred) {
        this.starred = starred;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

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

    public String getEmployId() {
        return employId;
    }

    public void setEmployId(String employId) {
        this.employId = employId;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public NoticeItem(NoticeItem item) {
        this.userId = "" + item.getUserId();
        this.noticeId = "" + item.getNoticeId();
        this.referenceId = "" + item.getReferenceId();
        this.companyId = "" + item.getCompanyId();
        this.title = "" + item.getTitle();
        this.content = "" + item.getContent();
        this.read = "" + item.getRead();
        this.updateTime = "" + item.getUpdateTime();
        this.createTime = "" + item.getCreateTime();
        this.photoUrl = "" + item.getPhotoUrl();
        this.photoMd5 = "" + item.getPhotoMd5();
        this.photoFileKey = "" + item.getPhotoFileKey();
        this.employId = "" + item.getEmployId();
        this.starred = "" + item.getStarred();
        this.name = new HashMap<>(item.name);
        this.dept = new HashMap<>(item.dept);
        this.attachmentItemList = new ArrayList<AttachmentItem>(item.getAttachmentItemList());
        this.noticeToItemList = new ArrayList<NoticeToItem>(item.getNoticeToItemList());
    }

    public NoticeItem() {

    }
}


