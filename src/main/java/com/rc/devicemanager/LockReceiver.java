package com.rc.devicemanager;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class LockReceiver extends BroadcastReceiver {

    private static final String TAG = "LockReceiver";
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdminSample;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "LockReceiver onReceive, action: " + action);

        mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //mDeviceAdminSample = new ComponentName(context, DeviceReceiver.class);
        mDeviceAdminSample = new ComponentName("com.rc.devicemanager", "com.rc.devicemanager.DeviceReceiver");
        Log.d(TAG,"mDeviceAdminSample: " + mDeviceAdminSample);

        if (mDPM.isAdminActive(mDeviceAdminSample)) {
            if (action.equals("com.rc.devicemanager.action.CLEAR_LOCK_PASSWORD")) {
                mDPM.resetPassword("", 0);
            }

            if (action.equals("com.rc.devicemanager.action.CHANGE_LOCK_PASSWORD")) {
                String password = intent.getExtras().getString("password", "");
                if (/* TextUtils.isEmpty(password) || */ password.length() >= 4 ) {
                    mDPM.resetPassword(password, 0);
                } else {
                    Toast.makeText(context, "Password length must be at least 4", Toast.LENGTH_SHORT).show();
                }
            }

            if (action.equals("com.rc.devicemanager.action.LOCK_SCREEN")) {
                mDPM.lockNow();
            }

            if (action.equals("com.rc.devicemanager.action.WIPE_DATA")) {
                int external = intent.getIntExtra("external_storage", 0);
                if (external != 0) {
                    mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);// mDPM.wipeData(1);
                } else {
                    mDPM.wipeData(0);
                }
            }
            /*if (!mDPM.isAdminActive(mDeviceAdminSample)) {
                Intent it = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                it.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
                it.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "重置密码");
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(it);
            }*/


            /*if (mDPM.isAdminActive(mDeviceAdminSample)) {
                mDPM.resetPassword("666666", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);//重置密码 空即无密码
                //mDPM.resetPassword("666666", DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT);//重置密码 空即无密码
                //mDPM.resetPassword("666666", 0);//重置密码 空即无密码
                mDPM.lockNow();//锁屏
                //mDPM.wipeData(0);//恢复出厂设置，0改为1可清除手机和sd卡的数据
                //清除数据---sd卡的数据
                mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);


                //mDPM.removeActiveAdmin(mDeviceAdminSample);//设置为非激活状态
            } else {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "一键锁屏");
                startActivity(intent);

            }*/



        }

    }

}
