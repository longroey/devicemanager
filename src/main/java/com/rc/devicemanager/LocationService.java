package com.rc.devicemanager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;

public class LocationService extends Service {

    private static final String SETTING_DEFAULT_INIT_PREFS = "custom_settings_default_prefs";
    private static final String SAFE_PHONE_NUMBER = "safe_phone_number";
    private static final String DEFAULT_SAFE_PHONE_NUMBER = "18707140640";
    private LocationManager mLM;
    private MyListener mListener;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLM = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 初始化标准
        Criteria criteria = new Criteria();
        // 设置精度标准
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 允许花费流量
        criteria.setCostAllowed(true);
        // 获取当前最合适的位置提供者,（那个信号号就用哪个，3G，GPS等）
        // 参1:标准,参2:是否可用
        String bestProvider = mLM.getBestProvider(criteria, true);

        System.out.println("best provider:" + bestProvider);
        mListener = new MyListener();
        // 请求位置更新, 参1:位置提供者,参2:最短更新时间(经过多少时间请求定位一次),
        //参3:最短更新距离(离开上一次范围多大请求定位一次);参4:位置监听;
        // 参2参3改为0,表示只要位置变化,就马上更新
        mLM.requestLocationUpdates(bestProvider, 0, 0, mListener);
    }

    class MyListener implements LocationListener {

        // 位置发生变化
        @Override
        public void onLocationChanged(Location location) {
            //得到最新更新的位置
            String j = "j:" + location.getLongitude();
            String w = "w:" + location.getLatitude();
            String accuracy = "accuracy:" + location.getAccuracy();

            String result = j + "\n" + w + "\n" + accuracy;

            // 发送经纬度给安全号码
            SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(SETTING_DEFAULT_INIT_PREFS, Context.MODE_PRIVATE);
            String phone = sharedPrefs.getString(SAFE_PHONE_NUMBER, DEFAULT_SAFE_PHONE_NUMBER);

            SmsManager sm = SmsManager.getDefault();
            sm.sendTextMessage(phone, null, result, null, null);

            //发送一次短信服务就停止
            stopSelf();// 服务自杀的方法
        }

        // 状态发生变化（信号的变化）
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("onStatusChanged");
        }

        // 用户打开GPS
        @Override
        public void onProviderEnabled(String provider) {
            System.out.println("onProviderEnabled");
        }

        // 用户关闭GPS
        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("onProviderDisabled");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止位置监听
        mLM.removeUpdates(mListener);
        mListener = null;
    }
}
