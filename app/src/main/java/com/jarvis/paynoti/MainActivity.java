package com.jarvis.paynoti;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences sp;

    public SocketClient socketClient;

    private WebView web;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    //拍照图片路径
    private String cameraFielPath;
    private final static int FILE_CAMERA_RESULT_CODE = 129;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("url", Context.MODE_PRIVATE);
        // 通知栏监控器开关
        if (!isEnabled()) {

            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "监控器开关已打开", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //授权成功

            } else {
                //授权失败
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && web.canGoBack()) {
            web.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出H5界面

    }

    class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.loadUrl(request.getUrl().toString());
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            System.out.print("webview finished");
            super.onPageFinished(view, url);

            boolean firstload =  sp.getBoolean("firstLoad",true);

            if (firstload){
                view.loadUrl(url);
                System.out.print("first load");
                SharedPreferences.Editor edit = sp.edit();
                edit.putBoolean("firstLoad",false);
                edit.apply();
            }

        }
    }
    class myWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);

        }
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            uploadMessageAboveL = filePathCallback;
            take();
            return true;
        }
    }
    private void take() {
        //拍照图片保存位置
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");
        if ( !imageStorageDir.exists() ) {
            imageStorageDir.mkdirs();
        }
        cameraFielPath = imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File file = new File(cameraFielPath);
        //需要显示应用的意图列表，这个list的顺序和选择菜单上的图标顺序是相关的，请注意。
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        //获取手机里所有注册相机接收意图的应用程序，放到意图列表里(无他相机，美颜相机等第三方相机)
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent i = new Intent(captureIntent);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            i.setPackage(packageName);
            i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            cameraIntents.add(i);
        }
        //相册选择器
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        //intent选择器
        Intent chooserIntent = Intent.createChooser(i, "选择模式");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        this.startActivityForResult(chooserIntent, FILE_CAMERA_RESULT_CODE);
    }

    // 判断是否打开了通知监听权限
    private boolean isEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == uploadMessageAboveL) {
            return;
        }
        //没有返回值时的处理
        if (resultCode != RESULT_OK) {
            //需要回调onReceiveValue方法防止下次无法响应js方法
            if (uploadMessageAboveL != null) {
                uploadMessageAboveL.onReceiveValue(null);
                uploadMessageAboveL = null;
            }

            return;
        }

        Uri result = null;
        if (requestCode == FILE_CAMERA_RESULT_CODE) {
            if (null != data && null != data.getData()) {
                result = data.getData();
            }
            if (result == null && hasFile(cameraFielPath)) {
                result = Uri.fromFile(new File(cameraFielPath));
            }
            //5.0以上设备的数据处理
            if (uploadMessageAboveL != null) {
                uploadMessageAboveL.onReceiveValue(new Uri[]{result});
                uploadMessageAboveL = null;

            }
        }
    }

    private boolean hasFile(String strFile) {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }

        }
        catch (Exception e)
        {
            return false;
        }

        return true;

    }


}
