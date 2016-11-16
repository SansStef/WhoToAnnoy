package com.stefano.whotoannoy;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by Stefano on 11/15/2016.
 */
public class Annoyance  extends RealmObject {

    //DB columns
    private Date callDate;
    private String contactName;
    private String contactPhone;

    //Constructors
    public Annoyance(){}

    //Getters and Setters
    public void setCallDate(Date d){
        callDate = d;
    }
    public Date getCallDate(){
        return callDate;
    }

    public void setContactName(String n){
        contactName = n;
    }
    public String getContactName(){
        return contactName;
    }

    public void setContactPhone(String p){
        contactPhone = p;
    }
    public String getContactPhone(){
        return contactPhone;
    }
}