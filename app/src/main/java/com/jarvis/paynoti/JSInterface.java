package com.jarvis.paynoti;

import android.webkit.JavascriptInterface;

public class JSInterface {
    public interface JSCallBackListener {
        void onReceivemsg(String token,String nickName);
    }


    private JSCallBackListener listener;

    public void SetOnReceiveInfo(JSCallBackListener _listener) {
        listener = _listener;
    }

    @JavascriptInterface

    public void sendToken(String token,String nickName){


        System.out.println("token"+token+"nick:"+nickName);

        if (listener != null) {
            listener.onReceivemsg(token,nickName);
        }

    }
}
