package com.centraltrillion.worklink.data;


public class AttachmentItem {

    private String attachmentId;
    private String attachmentTitle;
    private String attachmentUrl;
    private String attachmentFileKey;
    private String attachmentMd5;

    public String getAttachmentFilesize() {
        return attachmentFilesize;
    }

    public void setAttachmentFilesize(String attachmentFilesize) {
        this.attachmentFilesize = attachmentFilesize;
    }

    private String attachmentFilesize;

    public String getAttachmentTitle() {
        return attachmentTitle;
    }

    public void setAttachmentTitle(String attachmentTitle) {
        this.attachmentTitle = attachmentTitle;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentFileKey() {
        return attachmentFileKey;
    }

    public void setAttachmentFileKey(String attachmentFileKey) {
        this.attachmentFileKey = attachmentFileKey;
    }

    public String getAttachmentMd5() {
        return attachmentMd5;
    }

    public void setAttachmentMd5(String attachmentMd5) {
        this.attachmentMd5 = attachmentMd5;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }
}
