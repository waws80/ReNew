package com.thanatos.util.renewlib;

import android.app.Application;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 *  功能描述:
 *  @className: Download
 *  @author: thanatos
 *  @createTime: 2018/8/11
 *  @updateTime: 2018/8/11 14:58
 */
final class Download {

    private VersionInterceptor mVersionInterceptor;

    private Context mContext;

    private DownloadManager mDownloadManager;

    private boolean isFinish = false;

    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private File mFile;

    private long downloadId = -1;

    Download(Context context, VersionInterceptor interceptor){
        this.mContext = context;
        this.mVersionInterceptor = interceptor;
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    /**
     * 执行安装包下载
     * @param url
     * @param apkName
     */
    void execute(String url, String apkName){
        int state = mContext.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED){
            //系统下载器被禁用了
            mVersionInterceptor.unEnableStartDownloadManager();
            return;
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("正在下载新版本");
        clearApk(apkName+".apk");
        mFile = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),apkName+".apk");
        request.setDestinationUri(Uri.fromFile(mFile));
        request.setMimeType("application/vnd.android.package-archive");
        downloadId = mDownloadManager.enqueue(request);
        DownloadFinishReceiver receiver = new DownloadFinishReceiver();
        this.mContext.registerReceiver(receiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        new Thread(){
            @Override
            public void run() {
                super.run();
                while (!isFinish){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mVersionInterceptor.progress(getProgress());
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * 获取下载进度
     * @return
     */
    private int getProgress(){
        //查询进度
        DownloadManager.Query query = new DownloadManager.Query()
                .setFilterById(downloadId);
        Cursor cursor = mDownloadManager.query(query);
        int progress = 0;
        //获得游标
        if (cursor != null && cursor.moveToFirst()) {
            //当前的下载量
            int downloadSoFar = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            //文件总大小
            int totalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            progress = (int) (downloadSoFar * 1.0f / totalBytes * 100);
            cursor.close();
            return progress;
        }
        return progress;

    }

    /**
     * 安装包下载完成广播接收器
     */
    private class DownloadFinishReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus(context);
        }

        //检查下载状态
        private void checkStatus(Context context) {
            DownloadManager.Query query = new DownloadManager.Query();
            //通过下载的id查找
            query.setFilterById(downloadId);
            Cursor cursor = mDownloadManager.query(query);
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch (status) {
                    //下载暂停
                    case DownloadManager.STATUS_PAUSED:
                        break;
                    //下载延迟
                    case DownloadManager.STATUS_PENDING:
                        break;
                    //正在下载
                    case DownloadManager.STATUS_RUNNING:
                        break;
                    //下载完成
                    case DownloadManager.STATUS_SUCCESSFUL:
                        //下载完成安装APK
                        isFinish = true;
                        mVersionInterceptor.progress(100);
                        LogUtils.d("下载完成了");
                        //提升文件的读写权限，防止安装失败
                        setPermission(mFile.getPath());
                        //安装apk
                        InstallApk installApk = new InstallApk(context,mFile);
                        mVersionInterceptor.install(installApk);
                        cursor.close();
                        context.unregisterReceiver(this);
                        break;
                    //下载失败
                    case DownloadManager.STATUS_FAILED:
                        Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
                        cursor.close();
                        context.unregisterReceiver(this);
                        break;
                }
            }
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

    /**
     * 删除之前的apk
     * @param apkName apk名字
     * @return
     */
    private void clearApk(String apkName) {
        File apkFile = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                apkName);
        if (apkFile.exists()) {
            apkFile.delete();
        }
    }














}
