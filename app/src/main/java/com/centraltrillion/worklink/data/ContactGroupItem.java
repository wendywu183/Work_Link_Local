package com.centraltrillion.worklink.data;

import java.util.HashMap;

public class ContactGroupItem implements DataItem {
    private String groupId;
    private String groupType;
    private String groupCount;
    private String groupUpdateTime;

    public HashMap<String, String> groupName = null;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public void setGroupName(String key, String name) {
        if (groupName == null) {
            groupName = new HashMap<String, String>();
        }
        groupName.put(key, name);
    }

    public String getGroupName(String key) {
        return groupName.get(key);
    }

    public String getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(String groupCount) {
        this.groupCount = groupCount;
    }

    public String getGroupUpdateTime() {
        return groupUpdateTime;
    }

    public void setGroupUpdateTime(String groupUpdateTime) {
        this.groupUpdateTime = groupUpdateTime;
    }

    //contact: have used this constructor to copy the ContactGroup item
    public ContactGroupItem(ContactGroupItem item) {
        this.groupId = "" + item.getGroupId();
        this.groupType = "" + item.getGroupType();
        this.groupName = new HashMap<>(item.groupName);
        this.groupCount = "" + item.getGroupCount();
        this.groupUpdateTime = "" + item.getGroupUpdateTime();
    }

    public ContactGroupItem() {
    }

}
