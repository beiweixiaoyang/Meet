package com.example.meet.bmob;

import cn.bmob.v3.BmobObject;

/**
 * 隐私私有库
 */
public class PrivateSet extends BmobObject {

    String id;
    private String phone;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
