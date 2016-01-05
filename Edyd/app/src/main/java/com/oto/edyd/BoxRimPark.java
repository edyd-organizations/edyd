package com.oto.edyd;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.oto.edyd.utils.Common;
import com.oto.edyd.utils.CusProgressDialog;

import java.util.List;


/**
 * 周边停车
 * author liubaozhong
 */
public class BoxRimPark extends Activity  implements PoiSearch.OnPoiSearchListener {
    private MapView mMapView;// 地图控件
    private AMap aMap;
    private Context mActivity;
    //定位
    private LocationManagerProxy mLocationManger;
    private boolean mIsGetGPS = false;// 记录GPS定位是否成功
    private AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location != null && location.getAMapException().getErrorCode() == 0) {
                mIsGetGPS = true;
                startSearchQuery(location);

            } else {
                Common.showToastlong(mActivity, "定位出现异常");
                dissmissProgressDialog();
            }
        }
    };
    private CusProgressDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_refuel);
        initField(savedInstanceState);
        searchRefuel();
    }

    private void searchRefuel() {
        if (!mIsGetGPS) {
            mLocationManger = LocationManagerProxy.getInstance(this);
            //进行一次定位
            mLocationManger.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, mLocationListener);
            showProgressDialog();// 显示进度框
            return;
        }
        mIsGetGPS = false;

    }

    private void showProgressDialog() {
        loadingDialog = new CusProgressDialog(mActivity, "正在获取数据...");
        loadingDialog.getLoadingDialog().show();
    }

    private int currentPage;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private PoiResult poiResult;

    private void startSearchQuery(AMapLocation location) {
        currentPage = 0;
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query("停车场", "", location.getCity());

        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        poiSearch = new PoiSearch(this, query);
//        poiSearch.setBound(new SearchBound(new LatLonPoint(locationMarker.getPosition().latitude,
//                locationMarker.getPosition().longitude), 1000));
        poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(location.getLatitude(), location.getLongitude())
                , 3000));//设置周边搜索的中心点以及区域
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    private void initField(Bundle savedInstanceState) {
        mActivity = this;
        //初始化地图
        mMapView = (MapView) findViewById(R.id.refuel_mapview);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();


    }

    public void back(View view) {
        finish();
    }



    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        if (rCode == 0) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        aMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        Common.showToast(mActivity, "没搜索到加油站");
                    } else {
                        Common.showToast(mActivity, "没搜索到加油站！");
                    }
                }
            } else {
                Common.showToast(mActivity, "没搜索到加油站！");
            }
        } else if (rCode == 27) {
            Common.showToast(mActivity, "搜索失败,请检查网络连接！");

        } else if (rCode == 32) {
            Common.showToast(mActivity, "key验证无效！");

        } else {
            Common.showToast(mActivity, "未知错误，请稍后重试!错误码为" + rCode);
        }
    }

    private void dissmissProgressDialog() {
        loadingDialog.getLoadingDialog().dismiss();
    }

    // -------------生命周期必须重写方法----------------
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        // 以上两句必须重写

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        // 以上两句必须重写
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationManger.destroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }

}
