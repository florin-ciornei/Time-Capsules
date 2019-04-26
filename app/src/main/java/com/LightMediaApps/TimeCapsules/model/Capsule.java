package com.LightMediaApps.TimeCapsules.model;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class Capsule implements Serializable {

    private String id,userId;
    private String description;
    private long createdTime;//time in milliseconds when the capsule was created
    private long openTime;//time in milliseconds when the capsule should open
    private long inverseCreatedTime;//inverse created time to be able to sort capsules

    //data that can be put inside the capsule and is hidden until the capsule opens
    //by default they are empty strings. If they are empty strings they'll not be displayed
    private String imageName = "";
    private String gifURL = "";
    private String text = "";

    public Capsule() {

    }

    public Capsule(String description, long createdTime, long openTime) {
        this.description = description;
        this.createdTime = createdTime;
        this.openTime = openTime;
    }

    public String getDescription() {
        return description;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getImageName() {
        return imageName;
    }

    public String getGifURL() {
        return gifURL;
    }

    public String getText() {
        return text;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setGifURL(String gifURL) {
        this.gifURL = gifURL;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    public long getOpenTime() {
        return openTime;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOpenDateFormatted() {
        GregorianCalendar openDate = new GregorianCalendar();
        openDate.setTimeInMillis(openTime);
        return openDate.get(GregorianCalendar.DAY_OF_MONTH) + "." + openDate.get(GregorianCalendar.MONTH) + "." + openDate.get(GregorianCalendar.YEAR);
    }

    public void setInverseCreatedTime(long inverseCreatedTime) {
        this.inverseCreatedTime = inverseCreatedTime;
    }

    public long getInverseCreatedTime() {
        return inverseCreatedTime;
    }

    public boolean isOpened() {
        return openTime < (new GregorianCalendar()).getTimeInMillis();
    }
}
