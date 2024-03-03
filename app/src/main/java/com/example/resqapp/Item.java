package com.example.resqapp;

public class Item {

    private String firstName;
    private String lastName;
    private String address;
    private double latitude;
    private double longitude;
    private String addressadmin;
    private double latitudeadmin;
    private double longitudeadmin;
    private String contactNum;

    public Item(String firstName, String lastName, String address, double latitude, double longitude, String addressadmin, double latitudeadmin, double longitudeadmin, String contactNum) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.addressadmin = addressadmin;
        this.latitudeadmin = latitudeadmin;
        this.longitudeadmin = longitudeadmin;
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

    public String getAddressAdmin() {
        return addressadmin;
    }

    public double getLatitudeAdmin() {
        return latitudeadmin;
    }

    public double getLongitudeAdmin() {
        return longitudeadmin;
    }

    public String getContactNum() {
        return contactNum;
    }


}

