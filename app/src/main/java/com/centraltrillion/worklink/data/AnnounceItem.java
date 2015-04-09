package com.centraltrillion.worklink.data;

import java.util.ArrayList;
import java.util.List;

public class AnnounceItem implements DataItem {

    private String announceId;
    private String companyId;
    private String title;
    private String author;
    private String type;
    private String content;
    private String read;
    private String updateTime;
    private String createTime;
    private String top;
    private List<AttachmentItem> attachmentItemList = null;

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public List<AttachmentItem> getAttachmentItemList() {
        if(attachmentItemList == null){
            attachmentItemList = new ArrayList<AttachmentItem>();
        }
        return attachmentItemList;
    }

    public void setAttachmentItemList(List<AttachmentItem> attachmentItemList) {
        this.attachmentItemList = attachmentItemList;
    }

    public String getAnnounceId() {
        return announceId;
    }

    public void setAnnounceId(String announceId) {
        this.announceId = announceId;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public AnnounceItem(AnnounceItem item){
        this.announceId = "" + item.getAnnounceId();
        this.companyId = "" + item.getCompanyId();
        this.title = "" + item.getTitle();
        this.author = "" + item.getAuthor();
        this.type = "" + item.getType();
        this.content = "" + item.getContent();
        this.companyId = "" + item.getCompanyId();
        this.read = "" + item.getRead();
        this.updateTime = "" + item.getUpdateTime();
        this.createTime = "" + item.getCreateTime();
        this.attachmentItemList = new ArrayList<AttachmentItem>(item.getAttachmentItemList());
    }

    public AnnounceItem(){

    }
}
