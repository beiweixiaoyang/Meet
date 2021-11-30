package com.example.meet.ui;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.example.meet.R;
import com.example.meet.base.BaseBackActivity;

import java.util.List;

/**
 * 地图界面
 */
public class LocationActivity extends BaseBackActivity implements View.OnClickListener {

    private MapView mMapView;
    private EditText et_search;
    private ImageView iv_poi;
    private AMap map;//地图的控制器对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState) {
        mMapView=findViewById(R.id.mMapView);
        et_search=findViewById(R.id.et_search);
        iv_poi=findViewById(R.id.iv_poi);
        mMapView.onCreate(savedInstanceState);
        if(map == null){
            map = mMapView.getMap();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {

    }
}