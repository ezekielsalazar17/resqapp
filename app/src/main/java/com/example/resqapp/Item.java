package com.example.resqapp;

import android.widget.ImageButton;

public class Item {
    String name;
    String address;
    double latitude;
    double longitude;
    long contactNum;
    ImageButton checkbox;



    public Item(String name, String address, double latitude, double longitude, long contactNum) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNum = contactNum;

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public ImageButton getCheckbox() {
        return checkbox;
    }

    public void setCheckbox(ImageButton checkbox) {
        this.checkbox = checkbox;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getContactNum() {
        return contactNum;
    }

    public void setContactNum(int contactNum) {
        this.contactNum = contactNum;
    }




}
