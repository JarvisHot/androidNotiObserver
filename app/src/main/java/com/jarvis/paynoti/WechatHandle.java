package com.jarvis.paynoti;

import android.app.Notification;

import java.util.HashMap;
import java.util.Map;

public class WechatHandle extends NofiticationHandle {
    public WechatHandle(String pkgtype, Notification notification, PostInterface postpush){
        super(pkgtype,notification,postpush);
    }
    public void handleNotification(){
        if((title.contains("微信支付")|title.contains("微信收款"))&&content.contains("收款")){
            Map<String,String> postmap=new HashMap<String,String>();
            postmap.put("type","wechat");
            postmap.put("time",notitime);
            postmap.put("title",title);
            postmap.put("money",extractMoney(content));
            postmap.put("content",content);
            postpush.doPost(postmap);

        }

    }
}
