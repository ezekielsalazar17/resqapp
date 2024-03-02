package com.example.resqapp;

public class Item {
    private String firstName;
    private String lastName;
    private String address;
    private double latitude;
    private double longitude;
    private String contactNum;

    public Item(String firstName, String lastName, String address, double latitude, double longitude, String contactNum) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNum = contactNum;

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

}
