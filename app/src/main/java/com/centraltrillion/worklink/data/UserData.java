package com.centraltrillion.worklink.data;

import java.util.ArrayList;

public class UserData implements DataItem{

    private String id;
    private String name;
    private String mail;
    private ArrayList<ContactUserDetailItem> contactList;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMail() {
        return mail;
    }

    public ArrayList<ContactUserDetailItem> getContactList() {
        if(contactList == null)
            contactList = new ArrayList<ContactUserDetailItem>();
        return contactList;
    }
}
