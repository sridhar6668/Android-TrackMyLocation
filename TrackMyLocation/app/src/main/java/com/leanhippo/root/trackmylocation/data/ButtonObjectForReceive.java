package com.leanhippo.root.trackmylocation.data;

import android.widget.Button;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by root on 7/21/15.
 */
public class ButtonObjectForReceive {

    double latitude = 0;
    double longitude = 0;



    double previousLatitude = 0;
    double previousLongitude = 0;

    String code;
    Button button;
    int status;
    long unixStartTime;
    long duration;
    Marker marker;
    long focusCount;
    String name;


    int lineColor;

    long idleCount;
    boolean idleToActiveFlag;

    public int getStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(int stopFlag) {
        this.stopFlag = stopFlag;
    }

    int stopFlag;

    public ButtonObjectForReceive( String name, String code, Button button) {
        this.code = code;
        this.button = button;
        this.name = name;
    }

    public ButtonObjectForReceive(String code, Button button) {
        this.code = code;
        this.button = button;
    }

    public boolean isIdleToActive() {
        return idleToActiveFlag;
    }

    public void setIdleToActiveFlag(boolean idleToActiveFlag) {
        this.idleToActiveFlag = idleToActiveFlag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public long getIdleCount() {
        return idleCount;
    }

    public void setIdleCount(long idleCount) {
        this.idleCount = idleCount;
    }


    public double getPreviousLatitude() {
        return previousLatitude;
    }

    public void setPreviousLatitude(double previousLatitude) {
        this.previousLatitude = previousLatitude;
    }

    public double getPreviousLongitude() {
        return previousLongitude;
    }

    public void setPreviousLongitude(double previousLongitude) {
        this.previousLongitude = previousLongitude;
    }

    public long getFocusCount() {
        return focusCount;
    }

    public void setFocusCount(long focusCount) {
        this.focusCount = focusCount;
    }
    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getUnixStartTime() {
        return unixStartTime;
    }

    public void setUnixStartTime(long unixStartTime) {
        this.unixStartTime = unixStartTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }




    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
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


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }



}
