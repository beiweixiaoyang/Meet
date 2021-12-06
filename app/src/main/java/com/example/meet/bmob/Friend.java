package com.example.meet.bmob;

import cn.bmob.v3.BmobObject;

/**
 * 好友类
 */
public class Friend extends BmobObject {
    //本身
    private MeetUser meetUser;
    //好友
    private MeetUser FriendUser;

    public MeetUser getMeetUser() {
        return meetUser;
    }

    public void setMeetUser(MeetUser meetUser) {
        this.meetUser = meetUser;
    }

    public MeetUser getFriendUser() {
        return FriendUser;
    }

    public void setFriendUser(MeetUser friendUser) {
        FriendUser = friendUser;
    }
}
