package com.example.ljh.analoglocaction;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;

import java.util.Date;

public class MainActivity extends PermissionManager implements View.OnClickListener{
    private TextureMapView mTextureMapView;
    private BaiduMap mBaiduMap;
    private SearchView mSearchView;
    private ImageView ivMore;               //选择开始定位和暂停定位
    private ImageView ivLocation;           //移动到当前位置
    private PopupWindow mPopupWindow;
    private View mPopupWindow_View;
    private TextView tvStartLocation;       //开始自定位定位
    private TextView tvStopLocation;        //停止自定义定位

    private boolean canLocation = true;    //是否允许定位
    private boolean premission = false;     //检查权限
    private boolean firstLocation = true;   //是否第一次定位
    private boolean isSelfLocation = false; //是否在自定义定位状态
    private boolean prepareSelfLocation = false; //准备自定义定位状态
    private double mLongitude;              //经度
    private double mLatitude;               //纬度
    private float mProportion = 0;           //地图默认比例
    private int mDirection = 0;              //当前位置方向
    private final int ppwWidth = 300;
    private final int ppwHigh = 220;
    private final int mSleepTime = 50;      //线程循环间隔
    private String mAddr;                    //获取详细地址信息
    private String mCountry;                 //获取国家
    private String mProvince;                //获取省份
    private String mCity;                    //获取城市
    private String mDistrict;                //获取区县
    private String mStreet;                  //获取街道信息
    public static final String AK = "GveluuiZ3O1Es75DM6VxwYGxZ7NM4i9d";
    public String mLocationQueryUrl = "http://api.map.baidu.com/place/v2/search?query=" + mAddr
            + "&tag=" + "&region=" + mCity + "&output=json&ak="+AK;

    //当前定位模式
    private final MyLocationConfiguration.LocationMode mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
    //定位客户端
    private LocationClient mLocationClient = null;
    //定位监听器
    private MyLocationListener mMyLocationListener;
    //方向传感器
    private MyOrientationListener mMyOrientationListener;
    private BitmapDescriptor icon;          //定位图标

    LocationManager mLocationManager;

    private final int ERROR = 0;                        //定位错误
    private final int LOCATION_QUERY_SUCCESS = 1;       //查找到输入的位置数据
    public static final int LOCATION_QUERY_MODE = 2;   //返回码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main2);
        initView();
        initBaiduLocation();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ERROR:
                    Log.i("aaa", "定位失败");
                    break;
                case LOCATION_QUERY_SUCCESS:
                    break;
            }
        }
    };

    /**
     * 开始自定义定位
     */
    private void StartSelfLocation() {
        ThreadPoolManager.getThreadPoolManager().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(mSleepTime);
                        setLocation();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 自定义定位
     */
    public void setLocation() {
        AddTestProvider();
        if (canLocation) {
            String providerString = LocationManager.GPS_PROVIDER;
            Location location = new Location(providerString);
            location.setLatitude(mLatitude);   // 纬度（度）
            location.setLongitude(mLongitude);  // 经度（度）
            location.setAltitude(30);    // 高程（米）
            location.setBearing(mDirection);   // 方向（度）
            location.setSpeed(10);    //速度（米/秒）
            location.setAccuracy(0.1f);   // 精度（米）
            location.setTime(new Date().getTime());   // 本地时间
            //Build.VERSION_CODES.JELLY_BEAN_MR1
            if (Build.VERSION.SDK_INT > 16) {
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            mLocationManager.setTestProviderLocation(providerString, location);
        } else {
            handler.sendEmptyMessage(ERROR);
        }
    }

    /**
     * 添加自定义定位提供器
     */
    public void AddTestProvider() {
        if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0
                || Build.VERSION.SDK_INT >= 23) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String providerString = LocationManager.GPS_PROVIDER;
            LocationProvider locationProvider = mLocationManager.getProvider(providerString);
            if (locationProvider != null) {
                mLocationManager.addTestProvider(locationProvider.getName()
                        , locationProvider.requiresNetwork()
                        , locationProvider.requiresSatellite()
                        , locationProvider.requiresCell()
                        , locationProvider.hasMonetaryCost()
                        , locationProvider.supportsAltitude()
                        , locationProvider.supportsSpeed()
                        , locationProvider.supportsBearing()
                        , locationProvider.getPowerRequirement()
                        , locationProvider.getAccuracy());
            } else {
                mLocationManager.addTestProvider(providerString, true, true,
                        false, false, true, true,
                        true, Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
            }
            mLocationManager.setTestProviderEnabled(providerString, true);
            mLocationManager.setTestProviderStatus(providerString, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            canLocation = true;
        }
    }

    /**
     * 移动到当前定位位置
     */
    private void MoveCurrentLocation(){
        LatLng latLng = new LatLng(mLatitude,mLongitude);
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.animateMapStatus(mapStatusUpdate);
//        Draw();
    }

    /**
     * 取消自定义定位
     */
    private void StopSelfLocation() {
        if (canLocation) {
            mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        }
    }

    /**
     * 开始百度地图定位
     */
    private void StartLocationClient(){
        if(!mLocationClient.isStarted() && !prepareSelfLocation){
            mLocationClient.start();
            mBaiduMap.setMyLocationEnabled(true);
            mMyOrientationListener.Start();
        }
    }

    /**
     * 停止百度地图定位
     */
    private void StopLocationClient(){
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
            mBaiduMap.setMyLocationEnabled(false);
            mMyOrientationListener.Stop();
        }
    }

    /**
     * 初始化百度定位客户端和监听器
     */
    private void initBaiduLocation() {
        /**
         * 方向传感器监听
         */
        mMyOrientationListener = new MyOrientationListener(getApplicationContext());
        mMyOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChange(float orientation) {
                mDirection = (int) orientation;
            }
        });

        /**
         *定位客户端监听
         */
        mMyLocationListener = new MyLocationListener();
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(mMyLocationListener);
        LocationClientOption option = new LocationClientOption();
        /**
         * 可选，设置定位模式，默认高精度
         LocationMode.Hight_Accuracy：高精度；
         LocationMode. Battery_Saving：低功耗；
         LocationMode. Device_Sensors：仅使用设备；
         */
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        /**
         * option.setCoorType("bd09ll");
         * 可选，设置返回经纬度坐标类型，默认gcj02
         gcj02：国测局坐标；
         bd09ll：百度经纬度坐标；
         bd09：百度墨卡托坐标；
         海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标
         */
        option.setCoorType("bd09ll");   //设置坐标类型

        /**
         * option.setScanSpan(1000);
         * 可选，设置发起定位请求的间隔，int类型，单位ms
         如果设置为0，则代表单次定位，即仅定位一次，默认为0
         如果设置非0，需设置1000ms以上才有效
         */
        option.setScanSpan(1000);

        /**
         * 可选，是否需要位置描述信息，默认为不需要，即参数为false
           如果开发者需要获得当前点的位置信息，此处必须为true
         */
        option.setIsNeedAddress(true);

        option.setOpenGps(true);        //打开gps
        mLocationClient.setLocOption(option);
    }

    private void initView() {
        //百度地图控件
        mTextureMapView = findViewById(R.id.textureMapView);
        mBaiduMap = mTextureMapView.getMap();
        mProportion = mBaiduMap.getMaxZoomLevel() - 5;      //获得默认比例为100米
        //搜索框
        mSearchView = findViewById(R.id.SearchView);
        mSearchView.setOnClickListener(this);
        //移动到当前位置图标
        ivLocation = findViewById(R.id.ivLocation);
        ivLocation.setOnClickListener(this);
        ivMore = findViewById(R.id.ivMore);
        ivMore.setOnClickListener(this);
    }

    /**
     * 显示PopupWindow
     */
    private void ShowPopupWindow(){
        //初始化PopupWindow
        mPopupWindow_View = LayoutInflater.from(this).inflate(R.layout.item_popupwindow,null);
        mPopupWindow = new PopupWindow(mPopupWindow_View,ppwWidth,ppwHigh,true);
        mPopupWindow.setContentView(mPopupWindow_View);
        tvStartLocation = mPopupWindow_View.findViewById(R.id.tvStartLocation);
        tvStopLocation = mPopupWindow_View.findViewById(R.id.tvStopLocation);
        tvStartLocation.setOnClickListener(this);
        tvStopLocation.setOnClickListener(this);
        mPopupWindow.showAsDropDown(ivMore);
    }

    /**
     *隐藏PopupWindow
     */
    private void DismissPopupWindow(){
        if(mPopupWindow.isShowing() || mPopupWindow != null){
            mPopupWindow.dismiss();
        }
    }

    /**
     * 监听
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.SearchView:
                Intent intent = new Intent(MainActivity.this,LocationQueryActivity.class);
                if(mCity != "" && mCity != null){
                    intent.putExtra("city",mCity);
                }else{
                    intent.putExtra("city","全国");
                }
                startActivityForResult(intent,LOCATION_QUERY_MODE);
                break;
            case R.id.ivLocation:
                MoveCurrentLocation();
                break;
            case R.id.ivMore:
                ShowPopupWindow();
                break;
            case R.id.tvStartLocation:
                Toast.makeText(this,"已开启自定义定位",Toast.LENGTH_SHORT).show();
                isSelfLocation = true;
                prepareSelfLocation = false;
                DismissPopupWindow();
                StartSelfLocation();
                StartLocationClient();
                break;
            case R.id.tvStopLocation:
                Toast.makeText(this,"已关闭自定义定位",Toast.LENGTH_SHORT).show();
                isSelfLocation = false;
                DismissPopupWindow();
                StopSelfLocation();
                StartLocationClient();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == LOCATION_QUERY_MODE){
            Log.i("aaa","-------------onActivityResult");
            ThreadPoolManager.getThreadPoolManager().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        prepareSelfLocation = true;
                        StopLocationClient();
                        Thread.sleep(1500);
                        mLatitude = Double.parseDouble(data.getStringExtra("latitude"));
                        mLongitude = Double.parseDouble(data.getStringExtra("longitude"));
                        MoveCurrentLocation();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
//            firstLocation = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("aaa","-------------onStart");
        StartLocationClient();
    }

    @Override
    protected void onStop() {
        super.onStop();
        StopLocationClient();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTextureMapView.onDestroy();
        if(icon != null){
            icon.recycle();
        }
    }


    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || mTextureMapView == null) {
                return;
            }
            // 开启定位图层
            mBaiduMap.setMyLocationEnabled(true);
            // 构造定位数据
            MyLocationData myLocationData = null;
            if(isSelfLocation == false) {       //没有自定义定位，处于百度地图默认定位状态
                Log.i("aaa","isSelfLocation = false");
                 myLocationData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        .direction(mDirection)
                        .latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude())
                        .build();
                mLongitude = bdLocation.getLongitude();
                mLatitude = bdLocation.getLatitude();
            }else {                             //当前在自定义定位
                Log.i("aaa","isSelfLocation = true");
                myLocationData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        .direction(mDirection)
                        .latitude(mLatitude)
                        .longitude(mLongitude)
                        .build();
            }
            //设置定位数据
            mBaiduMap.setMyLocationData(myLocationData);

            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation_black_24dp);
            MyLocationConfiguration configuration = new MyLocationConfiguration(mLocationMode, true, icon);
            mBaiduMap.setMyLocationConfiguration(configuration);
            mAddr = bdLocation.getAddrStr();
            mCountry = bdLocation.getCountry();
            mProvince = bdLocation.getProvince();
            mCity = bdLocation.getCity();
            mDistrict = bdLocation.getDistrict();
            mStreet = bdLocation.getStreet();
            mLocationQueryUrl = "http://api.map.baidu.com/place/v2/search?query=" + mAddr
                    + "&tag=" + "&region=" + mCity + "&output=json&ak="+AK;
            //移动地图到当前位置
            if (firstLocation) {
                firstLocation = false;
                LatLng latLng = new LatLng(mLatitude,mLongitude);
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng,mProportion);
                mBaiduMap.animateMapStatus(mapStatusUpdate);
            }
        }
    }
}
