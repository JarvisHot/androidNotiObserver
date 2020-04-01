package com.jarvis.paynoti;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NotificationMonitorService extends NotificationListenerService implements PostInterface {

    private String posturl=null;
    private Context context=null;
    private String getPostUrl(){
        SharedPreferences sp=getSharedPreferences("url", 0);
//                this.posturl =sp.getString("posturl", "");

        if (posturl==null)

            return null;
        else
            return posturl;
    }
    // 在收到消息时触发
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        getPostUrl();
        Bundle extras = sbn.getNotification().extras;
        // 获取接收消息APP的包名
        String notificationPkg = sbn.getPackageName();
        // 获取接收消息的抬头
        String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        // 获取接收消息的内容
        String notificationText = extras.getString(Notification.EXTRA_TEXT);
        Log.i("XSL_Test", "Notification posted " + notificationTitle + " & " + notificationText+"包名" +notificationPkg);

        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }
        if(extras==null)
            return;
        NofiticationHandle notihandle = getNotificationHandle(notificationPkg,notification,this);
        if (notihandle != null) {
            notihandle.handleNotification();
        }

    }

    // 在删除消息时触发
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        Bundle extras = sbn.getNotification().extras;
        // 获取接收消息APP的包名
        String notificationPkg = sbn.getPackageName();
        // 获取接收消息的抬头
        String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
        // 获取接收消息的内容
        String notificationText = extras.getString(Notification.EXTRA_TEXT);
        Log.i("XSL_Test", "Notification removed " + notificationTitle + " & " + notificationText + "包名" +notificationPkg);

    }
    private NofiticationHandle getNotificationHandle(String pkg, Notification notification, PostInterface postpush){
        //mipush
//        if("com.xiaomi.xmsf".equals(pkg)){
//
//        }
        //支付宝
        if("com.eg.android.AlipayGphone".equals(pkg)){
            return new AlipayHandle("com.eg.android.AlipayGphone",notification,postpush);
        }

        //应用管理GCM代收
//        if("android".equals(pkg)){
//
//        }
        //微信
        if("com.tencent.mm".equals(pkg)){
            return new WechatHandle("com.tencent.mm",notification,postpush);
        }
        //收钱吧
        if("com.wosai.cashbar".equals(pkg)){

        }
        //云闪付
        if("com.unionpay".equals(pkg)){

        }
        //工银商户之家
        if("com.icbc.biz.elife".equals(pkg)){

        }




        return null;

    }


    @Override
    public void doPost(Map<String, String> params) {




    }



}
