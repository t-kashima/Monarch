package com.unuuu.monarchsample;

import io.realm.RealmObject;

/**
 * Created by kashima on 15/07/01.
 */
public class User extends RealmObject {
    private int userId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
