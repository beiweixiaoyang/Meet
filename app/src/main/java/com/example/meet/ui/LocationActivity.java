package com.example.meet.ui;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.meet.R;
import com.example.meet.adapter.CommonAdapter;
import com.example.meet.adapter.CommonViewHolder;
import com.example.meet.base.BaseBackActivity;
import com.example.meet.manager.DialogManager;
import com.example.meet.manager.MapManager;
import com.example.meet.utils.LogUtils;
import com.example.meet.view.DialogView;
import com.example.meet.view.LoadingView;

import java.util.ArrayList;
import java.util.List;

/**
 * 地图界面
 */
public class LocationActivity extends BaseBackActivity implements View.OnClickListener, PoiSearch.OnPoiSearchListener {

    private MapView mMapView;
    private EditText et_search;
    private ImageView iv_poi;
    private AMap map;//地图的控制器对象
    private boolean isShow;

    private DialogView mPoiView;
    private LoadingView mLoadView;
    private RecyclerView mConstellationnView;
    private TextView tv_cancel;
    private CommonAdapter<PoiItem>mCommonAdapter;
    private List<PoiItem>mLists=new ArrayList<>();

    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private double mLongitude;
    private double mLatitude;
    private String mAddress;
    private int ITEM = -1;


    public static void startActivity(Activity activity,boolean isShow,double la,double lo,
                                     String address,int requestCode){
        Intent intent=new Intent(activity,LocationActivity.class);
        intent.putExtra("isShow",isShow);
        intent.putExtra("la",la);
        intent.putExtra("lo",lo);
        intent.putExtra("address",address);
        activity.startActivityForResult(intent,requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initPoiView();
        initView(savedInstanceState);
    }

    private void initPoiView() {
        mLoadView=new LoadingView(this);
        mPoiView= DialogManager.getInstance().initDialogView(this,
                R.layout.dialog_select_constellation, Gravity.BOTTOM);
        tv_cancel=mPoiView.findViewById(R.id.tv_cancel);
        mConstellationnView=mPoiView.findViewById(R.id.mConstellationnView);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogManager.getInstance().hideDialog(mPoiView);
            }
        });
        mCommonAdapter=new CommonAdapter<PoiItem>(mLists, new CommonAdapter.OnBindDataListener<PoiItem>() {
            @Override
            public void onBindViewHolder(PoiItem model, CommonViewHolder holder, int type, int position) {
                holder.setText(R.id.tv_age_text,model.toString());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogManager.getInstance().hideDialog(mPoiView);
                        MapManager.getInstance().address2poi(model.toString(), new MapManager.OnAddress2PoiGeocodeListener() {
                            @Override
                            public void address2poi(double la, double lo, String address) {
                                ITEM = position;
                                mLatitude=la;
                                mLongitude=lo;
                                mAddress=address;
                                updatePoi(la, lo, address);
                            }
                        });
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_me_age_item;
            }
        });
        mConstellationnView.setLayoutManager(new LinearLayoutManager(this));
        mConstellationnView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mConstellationnView.setAdapter(mCommonAdapter);
    }

    private void initView(Bundle savedInstanceState) {
        mMapView=findViewById(R.id.mMapView);
        et_search=findViewById(R.id.et_search);
        iv_poi=findViewById(R.id.iv_poi);
        iv_poi.setOnClickListener(this);
        mMapView.onCreate(savedInstanceState);
        if(map == null){
            map = mMapView.getMap();
        }
        MyLocationStyle myLocationStyle=new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(2000);
        map.setMyLocationStyle(myLocationStyle);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setMyLocationEnabled(true);
        //缩放
        map.moveCamera(CameraUpdateFactory.zoomTo(18));
        Intent intent=getIntent();
        isShow=intent.getBooleanExtra("isShow",false);
        if (!isShow){
            //如果不显示 则作为展示类地图 接收外界传递的地址显示
            double la = intent.getDoubleExtra("la", 0);
            double lo = intent.getDoubleExtra("lo", 0);
            String address = intent.getStringExtra("address");
            updatePoi(la, lo, address);
        }
        map.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
//                LogUtils.e("location"+location);
//                LogUtils.e("location.."+location.getExtras().toString());
            }
        });
    }

    /**
     * 更新地址信息
     * @param la 经度
     * @param lo 纬度
     * @param address 地址
     */
    private void updatePoi(double la, double lo, String address) {
        map.setMyLocationEnabled(true);
        supportInvalidateOptionsMenu();
        //显示位置
        LatLng latLng = new LatLng(la, lo);
        map.clear();
        map.addMarker(new MarkerOptions().position(latLng).title("位置").snippet(address));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isShow){
            getMenuInflater().inflate(R.menu.location_menu,menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_send){
            LogUtils.e("click send");
            Intent intent=new Intent();
            if(ITEM>0){
                intent.putExtra("la",mLatitude);
                intent.putExtra("lo",mLongitude);
                intent.putExtra("address",mAddress);
            }else{
                intent.putExtra("la",map.getMyLocation().getLatitude());
                intent.putExtra("lo",map.getMyLocation().getLongitude());
                intent.putExtra("address",map.getMyLocation().getExtras().getString("desc"));
            }
            setResult(RESULT_OK,intent);
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.iv_poi){
            String keyWord=et_search.getText().toString().trim();
            if(!TextUtils.isEmpty(keyWord)){
                try {
                    poiSearch(keyWord);
                } catch (AMapException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void poiSearch(String keyWord) throws AMapException {
        mLoadView.show("正在搜索");
        query = new PoiSearch.Query(keyWord, "", "");
        query.setPageSize(6);// 设置每页最多返回多少条poiitem
        query.setPageNum(1);//设置查询页码
        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
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
    public void onPoiSearched(PoiResult poiResult, int i) {
        LogUtils.e(String.valueOf(poiResult.getPois()));
        //获取到结果
        mLoadView.hide();
        if (mLists.size() > 0) {
            mLists.clear();
        }
        mLists.addAll(poiResult.getPois());
        mCommonAdapter.notifyDataSetChanged();
        DialogManager.getInstance().showDialog(mPoiView);
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}