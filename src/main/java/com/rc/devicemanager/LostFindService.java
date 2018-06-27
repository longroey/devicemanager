package com.rc.devicemanager;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class LostFindService extends Service {
    private SmsReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @author Administrator
     *         短信的广播接收者
     */
    private class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            //实现短信拦截功能
            Bundle extras = intent.getExtras();
            Object datas[] = (Object[]) extras.get("pdus");
            for (Object data : datas) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) data);
                //得到电话号码
                String originatingAddress = sms.getOriginatingAddress();
                //得到短信内容
                String messageBody = sms.getMessageBody();
                System.out.println("号码:" + originatingAddress + ";内容:" + messageBody);
                if ("#*location*#".equals(messageBody)) {
                    System.out.println("手机定位");
                    // 启动位置监听的服务
                    context.startService(new Intent(context, LocationService.class));
                    abortBroadcast();// 中断短信传递
                } else if ("#*lockscreen*#".equals(messageBody)) {
                    // 设备策略管理器
                    DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    // 初始化管理员组件
                    ComponentName mDeviceComponentName = new ComponentName("com.rc.devicemanager", "com.rc.devicemanager.DeviceReceiver");
                    // 要执行超级管理员功能, 必须激活管理员权限:设置->安全->设备管理器
                    if (mDPM.isAdminActive(mDeviceComponentName)) {// 判断当前是否已经激活
                        mDPM.lockNow();// 立即锁屏
                        mDPM.resetPassword("123456", 0);// 重新设置密码, 传""取消密码
                    } else {
                        Toast.makeText(context, "您还没有激活超级管理员权限!", Toast.LENGTH_SHORT).show();
                    }

                    abortBroadcast();//终止广播
                } else if ("#*wipedata*#".equals(messageBody)) {//远程清除数据
                    // 设备策略管理器
                    DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    // 初始化管理员组件
                    ComponentName mDeviceComponentName = new ComponentName("com.rc.devicemanager", "com.rc.devicemanager.DeviceReceiver");
                    // 判断当前是否已经激活
                    if (mDPM.isAdminActive(mDeviceComponentName)) {
                        // WIPE_EXTERNAL_STORAGE表示清除手机内存和sdcard,
                        // 0表示只清除手机内存(恢复出厂设置)
                        mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
                        Toast.makeText(context, "数据清除完成!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "您还没有激活超级管理员权限!", Toast.LENGTH_SHORT).show();
                    }
                    abortBroadcast();//终止广播
                } if ("#*alarm*#".equals(messageBody)) {
                    // 播放报警音乐
                    System.out.println("播放报警音乐");
                    // asset , raw(可以通过id引入)
                    // 播放媒体音乐的音量和手机铃声音量无关
                    MediaPlayer player = MediaPlayer.create(context, R.raw.alarm);
                    // 音量最大, 基于系统音量的比值
                    player.setVolume(1f, 1f);
                    player.setLooping(true);// 单曲循环
                    player.start();// 开始播放
                    // 4.4+版本上,无法拦截短信, 调此方法没有, 比如当前应用时默认短信应用才可以
                    // 操作短信数据库, 删除数据库相关短信内容, 间接达到删除短信目的
                    abortBroadcast();// 中断短信传递

                }
            }

        }
    }

    @Override
    public void onCreate() {
        //短信广播接收者
        receiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(Integer.MAX_VALUE);//级别一样，清单文件，谁先注册谁先执行，如果级别一样，代码比清单要高
        //注册短信监听
        registerReceiver(receiver, filter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        //取消注册短信的监听广播
        unregisterReceiver(receiver);
        super.onDestroy();
    }



    public LostFindService() {
    }

}
