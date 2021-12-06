package com.example.meet.bmob;

import cn.bmob.v3.BmobObject;

/**
 * 隐私私有库
 */
public class PrivateSet extends BmobObject {

    private String objectId;
    private String phone;

    @Override
    public String getObjectId() {
        return objectId;
    }


    @Override
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
