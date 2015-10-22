package com.leanhippo.root.trackmylocation.data;

/**
 * Created by root on 7/19/15.
 */
public class locationUpdate {
    private String passCode;
    private double latitude;
    private double longitude;
    private double active;

    public String getPassCode() {
        return passCode;
    }

    public void setPassCode(String passCode) {
        this.passCode = passCode;
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

    public double getActive() {
        return active;
    }

    public void setActive(double active) {
        this.active = active;
    }


}
