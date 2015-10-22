package com.leanhippo.root.trackmylocation.data;

/**
 * Created by root on 7/1/15.
 */
public class UserInfo {

    private long userId;

    public UserInfo() {
        userId = 0;
    }
    public UserInfo(long userId) {
        this.userId = userId;
    }
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
