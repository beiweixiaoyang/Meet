package com.example.meet.utils;

import android.app.Activity;

import java.util.HashSet;

/**
 * Activity工具类
 */
public class ActivityUtils {

    private static ActivityUtils instance = new ActivityUtils();

    private static HashSet<Activity> hashSet = new HashSet<>();

    private ActivityUtils() {

    }

    public static ActivityUtils getInstance() {
        return instance;
    }

    /**
     * 填充
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        try {
            hashSet.add(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出
     */
    public void exit() {
        try {
            for (Activity activity : hashSet) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
