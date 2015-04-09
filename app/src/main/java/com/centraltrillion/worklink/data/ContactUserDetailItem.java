package com.centraltrillion.worklink.data;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactUserDetailItem implements DataItem {

    private String groupId;
    private String groupName;
    private String email;
    private Bitmap photo;
    private String phone;
    private String id;
    private String emId;
    private String supervisorId;
    private String supervisorName;
    private String deputyId;
    private String deputyName;
    private String skill;
    private String interest;
    private String intro;
    private String photoUrl;
    private String photoMd5;
    private String photoFileKey;
    private String status;
    private HashMap<String, String> name = null;
    private HashMap<String, String> title = null;
    private HashMap<String, String> department = null;
    private HashMap<String, String> jobLocation = null;
    private List<TelephoneItem> telephone = null;

    public void setTelephone(String phone, String region){
        TelephoneItem item = new TelephoneItem();
        item.tel = phone;
        item.region = region;
        if(telephone == null) {
            telephone = new ArrayList<TelephoneItem>();
        }
            telephone.add(item);
    }

    public List<TelephoneItem> getTelephone(){
        return telephone;
    }

    public void setJobLocation(String key, String value) {
        if (jobLocation == null) {
            jobLocation = new HashMap<String, String>();
        }
        jobLocation.put(key, value);
    }

    public String getJobLocation(String key) {
        return jobLocation.get(key);
    }

    public void setDepartment(String key, String value) {
        if (department == null) {
            department = new HashMap<String, String>();
        }
        department.put(key, value);
    }

    public String getDepartment(String key) {
        return department.get(key);
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

    public String getDeputyName() {
        return deputyName;
    }

    public void setDeputyName(String deputyName) {
        this.deputyName = deputyName;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public String getEmId() {
        return emId;
    }

    public void setEmId(String emId) {
        this.emId = emId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getDeputyId() {
        return deputyId;
    }

    public void setDeputyId(String deputyId) {
        this.deputyId = deputyId;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public class TelephoneItem{
        public String tel;
        public String region;
    }
}
