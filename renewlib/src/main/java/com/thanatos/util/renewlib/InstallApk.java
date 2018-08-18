package com.thanatos.util.renewlib;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.io.File;

/**
 *  功能描述: 安装apk工具类
 *  @className: InstallApk
 *  @author: thanatos
 *  @createTime: 2018/8/11
 *  @updateTime: 2018/8/11 16:01
 */
public final class InstallApk {

    private Context mContext;
    private File mFile;

    InstallApk(Context context, File file){
        this.mContext = context;
        this.mFile = file;
    }

    /**
     * 开始安装apk
     */
    public void next(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){

            Uri apkUri = DownloadFileProvider.getUriForFile(mContext, "com.thanatos.util.renewlib", mFile);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setDataAndType(apkUri,
                    "application/vnd.android.package-archive");
        }else {
            i.setDataAndType(Uri.fromFile(mFile),
                    "application/vnd.android.package-archive");
        }
        mContext.startActivity(i);
    }

}
