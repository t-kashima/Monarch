package com.unuuu.monarchsample;

import io.realm.RealmObject;

/**
 * Created by kashima on 15/07/01.
 */
public class User extends RealmObject {
    private int userId;
    private int age;
    private int device;

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

    public int getDevice() {
        return device;
    }

    public void setDevice(int device) {
        this.device = device;
    }
}
