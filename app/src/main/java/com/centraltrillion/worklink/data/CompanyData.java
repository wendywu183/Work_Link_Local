package com.centraltrillion.worklink.data;

import android.graphics.Bitmap;

import java.io.Serializable;

public class CompanyData implements Serializable {

	private String mTitle;
	private String mAddress;
	private String mPhone="";
	private String mFax;
	private String mImageUrl;
	private Bitmap mBitmap;
	private String mLatitude;
	private String mLongitude;
	private String id;
	private String md5;
    private String mPhoneExt="";
    private String mPhoneReg="";

	public CompanyData() {
	}

    public void setPhoneExt(String ext){
        this.mPhoneExt=ext;
    }

    public void setPhoneReg(String reg){
        this.mPhoneReg = reg;
    }

    public String getPhoneFull(){
        StringBuilder sb = new StringBuilder();
        sb.append(mPhoneReg).append(" ").append(mPhone);
        if(!mPhoneExt.equals(""))
            sb.append("#").append(mPhoneExt);

        return sb.toString();
    }


	public void setMd5(String md5){
		this.md5 = md5;
	}
	
	public String getMd5(){
		return this.md5;
	}
	
	public void setTitle(String name) {
		this.mTitle = name;
	}

	public String getTitle() {
		return this.mTitle;
	}
	
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
	public void setPosition(String lat,String lon){
		this.mLatitude=lat;
		this.mLongitude=lon;
	}
	
	public String getLatitude(){
		return this.mLatitude;
	}
	
	public String getLongitude(){
		return this.mLongitude;
	}
	
	public void setPhone(String phone) {
		this.mPhone = phone;
	}



	public String getPhone() {
		return this.mPhone;
	}

	public void setAddress(String address) {
		this.mAddress = address;
	}

	public String getAddress() {
		return this.mAddress;
	}

	public void setFax(String fax) {
		this.mFax = fax;
	}

	public String getFax() {
		return this.mFax;
	}

	public void setImageUrl(String image_url) {
		this.mImageUrl = image_url;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setBitmap(Bitmap bitmap) {
		this.mBitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}
}
