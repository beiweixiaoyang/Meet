package com.example.meet.base;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.meet.utils.SystemUI;

public class BaseUIActivity extends BaseActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUI.fixSystemUI(this);
    }
}
