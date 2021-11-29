package com.example.meet.event;

import org.greenrobot.eventbus.EventBus;

/**
 * 封装EventBus的一些方法
 */
public class EventManager {


    //更新好友列表
    public static final int FLAG_UPDATE_FRIEND_LIST = 1000;
    /**
     * 注册EvetBus
     * @param subscriber
     */
    public static void register(Object subscriber){
        EventBus.getDefault().register(subscriber);
    }
    /**
     * unRegister
     */

    public static void unregister(Object subscriber){
        EventBus.getDefault().unregister(subscriber);
    }

    public static void post(int flagUpdateFriendList){
        EventBus.getDefault().post(new MessageEvent());
    }

}
