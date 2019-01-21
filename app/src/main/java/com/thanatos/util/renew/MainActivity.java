package com.thanatos.util.renew;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.thanatos.util.renewlib.Download;
import com.thanatos.util.renewlib.DownloadBuild;
import com.thanatos.util.renewlib.HttpBuild;
import com.thanatos.util.renewlib.InstallApk;
import com.thanatos.util.renewlib.LogUtils;
import com.thanatos.util.renewlib.ReNew;
import com.thanatos.util.renewlib.VersionInterceptor;

public class MainActivity extends AppCompatActivity {

    private String url = "http://182.61.14.72/download/app/android/devoteApp.apk";

    private InstallApk installApk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReNew.init(getApplicationContext());
        ReNew.getInstance().execute(true,new VersionInterceptor() {
            @Override
            public HttpBuild start(HttpBuild builder) {
                builder.setUrl("https://www.baidu.com");
                builder.setMethod(HttpBuild.GET);
                return builder;
            }

            @Override
            public DownloadBuild dataResult(String string) {
                LogUtils.d(string);
                DownloadBuild downloadBuild = new DownloadBuild();
                downloadBuild.url = url;
                downloadBuild.download = true;
                return downloadBuild;
            }

            @Override
            public void unEnableStartDownloadManager(Download download) {
                download.startSystemDownloadManager();
            }

            @Override
            public void progress(int progress) {
                LogUtils.d("进度："+progress);
            }

            @Override
            public void install(InstallApk installApk) {
                MainActivity.this.installApk = installApk;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (getPackageManager().canRequestPackageInstalls()){
                        //installApk.next();
                    }else {
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},100);
                    }
                }else {
                    //installApk.next();
                }

            }

            @Override
            public void error(String msg, int code) {
                LogUtils.d("msg: "+msg+"    code:"+code);

            }
        },"邻里社区");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 100){
            //installApk.next();
        }else {
            Intent intent =new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if ( requestCode == 100 && resultCode == RESULT_OK){
                //installApk.next();
            }
        }
    }
}
