package com.server;

import java.util.ArrayList;

public class WarningMessage {

    private String nickname;
    private String latitude;
    private String longitude;
    private String dangertype;

    public WarningMessage() {
        
    }

    public WarningMessage(String nickname, String latitude, String longitude, String dangertype) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNick(String nick) {
        this.nickname = nickname;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDangertype() {
        return dangertype;
    }

    public void setDangertype(String dangertype) {
        this.dangertype = dangertype;
    }
    
}