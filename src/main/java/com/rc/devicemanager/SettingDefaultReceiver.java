package com.rc.devicemanager;

import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

public class SettingDefaultReceiver extends BroadcastReceiver {

    private static final String TAG = "SettingDefaultReceiver";
    private DevicePolicyManager mDPM;
    private DeviceAdminInfo mDeviceAdmin;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            mDPM = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName cn = new ComponentName("com.rc.devicemanager", "com.rc.devicemanager.DeviceReceiver");
            ActivityInfo ai;
            try {
                ai = mContext.getPackageManager().getReceiverInfo(cn, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Unable to retrieve device policy " + cn, e);
                return;
            }
            // When activating, make sure the given component name is actually a valid device admin.
            // No need to check this when deactivating, because it is safe to deactivate an active
            // invalid device admin.
            if (!mDPM.isAdminActive(cn)) {
                List avail = mContext.getPackageManager().queryBroadcastReceivers(new Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED),
                        PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS);
                int count = avail == null ? 0 : avail.size();
                boolean found = false;
                for (int i = 0; i < count; i++) {
                    ResolveInfo ri = (ResolveInfo) avail.get(i);
                    if (ai.packageName.equals(ri.activityInfo.packageName) && ai.name.equals(ri.activityInfo.name)) {
                        try {
                            // We didn't retrieve the meta data for all possible matches, so
                            // need to use the activity info of this specific one that was retrieved.
                            ri.activityInfo = ai;
                            DeviceAdminInfo dpi = new DeviceAdminInfo(mContext, ri);
                            found = true;
                        } catch (XmlPullParserException e) {
                            Log.w(TAG, "Bad " + ri.activityInfo, e);
                        } catch (IOException e) {
                            Log.w(TAG, "Bad " + ri.activityInfo, e);
                        }
                        break;
                    }
                }
                if (!found) {
                    Log.w(TAG, "Request to add invalid device admin: " + cn);
                    return;
                }
            } else {
                Log.w(TAG, "already active!");
                return;
            }
            ResolveInfo ri = new ResolveInfo();
            ri.activityInfo = ai;
            try {
                mDeviceAdmin = new DeviceAdminInfo(mContext, ri);
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Unable to retrieve device policy " + cn, e);
                return;
            } catch (IOException e) {
                Log.w(TAG, "Unable to retrieve device policy " + cn, e);
                return;
            }
            try {
                mDPM.setActiveAdmin(mDeviceAdmin.getComponent(), false);
            } catch (RuntimeException e) {
                Log.w(TAG, "Exception trying to activate admin " + mDeviceAdmin.getComponent(), e);
                return;
            }
        } else {
            Log.w(TAG, "has actived!");
            return;
        }
    }
}
