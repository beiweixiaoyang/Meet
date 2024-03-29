package com.example.meet.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.meet.R;
import com.example.meet.utils.SpUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    //添加好友
    public static final String CHANNEL_ADD_FRIEND = "add_friend";
    //同意好友
    public static final String CHANNEL_AGREED_FRIEND = "agreed_friend";
    //消息
    public static final String CHANNEL_MESSAGE = "message";

    private static NotificationManager mInstance = null;

    private Context mContext;
    private android.app.NotificationManager notificationManager;

    private List<String> mIdList = new ArrayList<>();

    private NotificationManager() {

    }

    public static NotificationManager getInstance() {
        if (mInstance == null) {
            synchronized (NotificationManager.class) {
                if (mInstance == null) {
                    mInstance = new NotificationManager();
                }
            }
        }
        return mInstance;
    }

    public void createChannel(Context mContext) {
        this.mContext = mContext;
        notificationManager = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channelId = CHANNEL_ADD_FRIEND;
            String channelName ="添加好友";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = CHANNEL_AGREED_FRIEND;
            channelName = "同意好友申请";
            importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

            channelId = CHANNEL_MESSAGE;
            channelName = "好友消息";
            importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);
        }
    }

    private void createNotificationChannel(String channelId, String channelName, int importance) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 发送通知
     *
     * @param objectid  发送人ID
     * @param channelId 渠道ID
     * @param title     标题
     * @param text      内容
     * @param mBitmap   头像
     * @param intent    跳转目标
     */
    private void pushNotification(String objectid, String channelId, String title, String text, Bitmap mBitmap, PendingIntent intent) {
        //对开关进行限制
        boolean isTips = SpUtils.getInstance().getBoolean("isTips", true);
        if (!isTips) {
            return;
        }

        Notification notification = new NotificationCompat.Builder(mContext, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_message)
                .setLargeIcon(mBitmap)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build();

        if (!mIdList.contains(objectid)) {
            mIdList.add(objectid);
        }
        notificationManager.notify(mIdList.indexOf(objectid), notification);
    }

    /**
     * 发送添加好友的通知
     */
    public void pushAddFriendNotification(String objectid, String title, String text, Bitmap mBitmap, PendingIntent intent) {
        pushNotification(objectid, CHANNEL_ADD_FRIEND, title, text, mBitmap, intent);
    }

    /**
     * 发送同意好友的通知
     */
    public void pushArgeedFriendNotification(String objectid, String title, String text, Bitmap mBitmap, PendingIntent intent) {
        pushNotification(objectid, CHANNEL_AGREED_FRIEND, title, text, mBitmap, intent);
    }

    /**
     * 发送消息的通知
     */
    public void pushMessageNotification(String objectid, String title, String text, Bitmap mBitmap, PendingIntent intent) {
        pushNotification(objectid, CHANNEL_MESSAGE, title, text, mBitmap, intent);
    }
}
