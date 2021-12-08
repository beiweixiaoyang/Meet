package com.example.meet.bmob;

import cn.bmob.v3.BmobObject;

/**
 * 缘分匹配 库
 */
public class FateSet extends BmobObject {
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
