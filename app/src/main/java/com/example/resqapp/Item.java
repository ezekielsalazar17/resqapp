package com.example.resqapp;

public class Item {
    private String firstName;
    private String lastName;

    public Item(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}