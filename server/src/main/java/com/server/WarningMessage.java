package com.server;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class WarningMessage {

    private String nickname;
    private double latitude;
    private double longitude;
    private String dangertype;
    private LocalDateTime sent;

    public WarningMessage() {
        
    }

    public WarningMessage(String nickname, double latitude, double longitude, LocalDateTime sent, String dangertype) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sent = sent;
        this.dangertype = dangertype;
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

    public LocalDateTime getSent() {
        return sent;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setSent(LocalDateTime sent) {
        this.sent = sent;
    }
    
    public long dateAsInt() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
        }
    
    public void setSent(long epoch) {
        sent = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
        }

}