package com.thanatos.util.renewlib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.IOException;

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

    public File getFile() {
        return mFile;
    }

    /**
     * 开始安装apk
     */
    public void next(Uri apkUri){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){

            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setDataAndType(apkUri,
                    "application/vnd.android.package-archive");
        }else {
            i.setDataAndType(Uri.fromFile(mFile),
                    "application/vnd.android.package-archive");
        }
        mContext.startActivity(i);
    }


    /**
     * 提升文件的读写权限
     * @param path
     */
    private void setPermission(String path){
        String command = "chmod 777 "+path;
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
