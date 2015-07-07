package com.unuuu.monarch.sample;

import io.realm.RealmObject;

/**
 * Created by kashima on 15/07/01.
 */
public class User extends RealmObject {
    private int userId;
    private int age;
    private int deviceCount;

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }
}
