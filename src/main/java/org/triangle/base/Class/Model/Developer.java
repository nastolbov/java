package org.triangle.base.Class.Model;

public class Developer {
    private int developerID;
    private String name;
    private String contactName;
    private String phoneNumber;
    private String email;
    private String address;

    public Developer(int developerID, String name, String contactName, String phoneNumber, String email, String address) {
        this.developerID = developerID;
        this.name = name;
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    public int getDeveloperID() {
        return developerID;
    }

    public void setDeveloperID(int developerID) {
        this.developerID = developerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
