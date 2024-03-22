package com.example.resqapp;

public class Itemcoast {
    private String firstName;
    private String lastName;
    private String address;
    private double latitude;
    private double longitude;
    private String contactNum;
    private String useremail;
    private String timestamp;

    public Itemcoast(String userEmail, String firstName, String lastName, String address, double latitude, double longitude, String contactNum, String timestamp) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNum = contactNum;
        this.useremail = userEmail;
        this.timestamp = timestamp;


    }

    public String getUserEmail() {
        return useremail;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getContactNum() {
        return contactNum;
    }

    public String getTimestamp() {
        return timestamp;
    }



}