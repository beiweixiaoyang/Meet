package com.example.meet.manager;

import android.content.Context;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.example.meet.utils.LogUtils;

/**
 * 初始化高德sdk
 * 实现经度纬度和地址的正反编码
 */
public class MapManager {
    private static volatile MapManager instance;

    private GeocodeSearch geocodeSearch;

    private OnAddress2PoiGeocodeListener address2poi;
    private OnPoi2AddressGeocodeListener poi2address;

    private MapManager() {
    }

    public static MapManager getInstance() {
        if (instance == null) {
            synchronized (MapManager.class) {
                if (instance == null) {
                    instance = new MapManager();
                }
            }
        }
        return instance;
    }

    private GeocodeSearch.OnGeocodeSearchListener listener=new GeocodeSearch.OnGeocodeSearchListener() {
        @Override
        public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
            //坐标轴转地址
            if (i == AMapException.CODE_AMAP_SUCCESS) {
                if (regeocodeResult != null) {
                    if (poi2address != null) {
                        poi2address.poi2address(regeocodeResult.getRegeocodeAddress()
                                .getFormatAddress());
                    }
                }
            }
        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
            //地址转坐标轴
            if (i == AMapException.CODE_AMAP_SUCCESS) {
                if (geocodeResult != null) {
                    if (address2poi != null) {
                        if (geocodeResult.getGeocodeAddressList() != null &&
                                geocodeResult.getGeocodeAddressList().size() > 0) {
                            GeocodeAddress address = geocodeResult.getGeocodeAddressList().get(0);
                            address2poi.address2poi(
                                    address.getLatLonPoint().getLatitude(),
                                    address.getLatLonPoint().getLongitude(),
                                    address.getFormatAddress()
                            );
                        }
                    }
                }
            }
        }
    };
    public void initMap(Context context) {
        LogUtils.e("initMap");
        //进行高德SDK隐私合规检查
        MapsInitializer.updatePrivacyShow(context, true, true);
        MapsInitializer.updatePrivacyAgree(context, true);
        ServiceSettings.updatePrivacyShow(context, true, true);
        ServiceSettings.updatePrivacyAgree(context, true);
        try {
            geocodeSearch=new GeocodeSearch(context);
        } catch (AMapException e) {
            e.printStackTrace();
        }
        geocodeSearch.setOnGeocodeSearchListener(listener);
    }

    /**
     * 地址转经纬度
     *
     * @param address
     */
    public MapManager address2poi(String address, OnAddress2PoiGeocodeListener listener) {
        this.address2poi = listener;
        GeocodeQuery query = new GeocodeQuery(address, "");
        geocodeSearch.getFromLocationNameAsyn(query);
        return instance;
    }

    /**
     * 经纬度转地址
     *
     * @param la
     * @param lo
     */
    public MapManager poi2address(double la, double lo,OnPoi2AddressGeocodeListener listener) {
        this.poi2address = listener;
        RegeocodeQuery query = new RegeocodeQuery(
                new LatLonPoint(la, lo), 3000, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
        return instance;
    }

    public interface OnPoi2AddressGeocodeListener {
        void poi2address(String address);
    }

    public interface OnAddress2PoiGeocodeListener {
        void address2poi(double la, double lo, String address);
    }
    /**
     * 获取静态地图Url
     *
     * @param la
     * @param lo
     * @return
     */
    public String getMapUrl(double la, double lo) {
        String url = "https://restapi.amap.com/v3/staticmap?location=" + lo + "," + la +
                "&zoom=17&scale=2&size=150*150&markers=mid,,A:" + lo + ","
                + la + "&key=" + "389bc08b815e3146bfd1e45fd7f47fc5";
        LogUtils.i("url:" + url);
        return url;
    }
}
