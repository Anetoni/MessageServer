package com.server;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;

public class WarningMessage {

    private String nickname;
    private double latitude;
    private double longitude;
    private String dangertype;
    private ZonedDateTime sent;
    private String phonenumber;
    private String areacode;

    public WarningMessage() {
        
    }

    //Phonenumber and areacode
    public WarningMessage(String nickname, double latitude, double longitude, ZonedDateTime sent, String dangertype, String phonenumber, String areacode) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
        this.sent = sent;
        this.phonenumber = phonenumber;
        this.areacode = areacode;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNick(String nickname) {
        this.nickname = nickname;
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

    public String getDangertype() {
        return dangertype;
    }

    public void setDangertype(String dangertype) {
        this.dangertype = dangertype;
    }

    public ZonedDateTime getSent() {
        return sent;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setSent(ZonedDateTime sent) {
        this.sent = sent;
    }
    
    public long dateAsInt() {
        return sent.toInstant().toEpochMilli();
        }
    
    public void setSent(long epoch) {
        sent = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
        }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getAreacode() {
        return areacode;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

}