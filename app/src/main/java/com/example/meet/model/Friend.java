package com.example.meet.model;

import com.example.meet.bmob.MeetUser;

public class Friend {

    private MeetUser meetUser;
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
