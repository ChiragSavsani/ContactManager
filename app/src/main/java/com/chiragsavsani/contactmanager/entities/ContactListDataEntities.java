package com.chiragsavsani.contactmanager.entities;

import android.graphics.Bitmap;

/**
 * Created by chirag.savsani on 9/25/2015.
 */
public class ContactListDataEntities {
    Bitmap contactImage;
    String contactName, contactNumber, contactEmail;

    public ContactListDataEntities() {

    }

    public ContactListDataEntities(Bitmap contactImage, String contactName, String contactNumber, String contactEmail) {
        this.contactImage = contactImage;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.contactEmail = contactEmail;
    }

    public Bitmap getContactImage() {
        return contactImage;
    }

    public void setContactImage(Bitmap contactImage) {
        this.contactImage = contactImage;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactEmail() { return contactEmail; }

    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
}
