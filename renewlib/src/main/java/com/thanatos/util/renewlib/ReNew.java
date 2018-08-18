package com.thanatos.util.renewlib;

import android.content.Context;
import android.text.TextUtils;

import com.thanatos.util.renewlib.http.HttpUtils;


/**
 *  功能描述: 版本更新工具类
 *  @className: ReNew
 *  @author: thanatos
 *  @createTime: 2018/8/11
 *  @updateTime: 2018/8/11 10:24
 */
public class ReNew {

    private static Context sContext;

    private String mApkName = "";

    private VersionInterceptor mVersionInterceptor;

    private ReNew(){}

    private static final class Builder{
        private static final ReNew RE_NEW = new ReNew();
    }

    /**
     * 初始化
     * @param context 上下文对象
     */
    public static void init(Context context){
        sContext = context;
        HttpUtils.init(context);
    }

    /**
     * 获取当前类唯一对象
     * @return
     */
    public static ReNew getInstance(){
        return Builder.RE_NEW;
    }

    /**
     * 开始执行版本升级
     * @param interceptor 拦截器
     * @param apkName apk 名称
     */
    public void execute(VersionInterceptor interceptor, String apkName){
        execute(false,interceptor,apkName);
    }

    /**
     * 开始执行版本更新
     * @param skipDownloadInfo 是否跳过获取版本信息
     * @param interceptor 拦截器
     * @param apkName apk名称
     */
    public void execute(boolean skipDownloadInfo, VersionInterceptor interceptor, String apkName){
        if (sContext == null){
            LogUtils.d("请先初始化 ReNew");
            return;
        }
        if (interceptor == null) {
            LogUtils.d("版本升级拦截器为空");
            return;
        }
        this.mVersionInterceptor = interceptor;
        if (null == apkName){
            apkName = "base";
        }
        this.mApkName = apkName;
        if (skipDownloadInfo){
            DownloadBuild downloadBuild = this.mVersionInterceptor.dataResult("");
            if (TextUtils.isEmpty(downloadBuild.url) ||
                    !downloadBuild.url.startsWith("http")){
                LogUtils.d("apk远程下载路径错误");
                this.mVersionInterceptor.error("DownloadBuild 请求的 url 错误",-1);
                return;
            }
            doNext(downloadBuild.download,downloadBuild.url);
            return;
        }
        build();
    }

    /**
     * 构造信息
     */
    private void build() {
        HttpBuild httpBuilder = this.mVersionInterceptor.start(new HttpBuild());
        if (TextUtils.isEmpty(httpBuilder.getUrl())){
            LogUtils.d("HttpBuilder 请求的 url 不能为空");
            this.mVersionInterceptor.error("HttpBuilder 请求的 url 不能为空",-1);
            return;
        }
        if (!httpBuilder.getUrl().startsWith("http")){
            LogUtils.d("HttpBuilder url 格式不正确");
            this.mVersionInterceptor.error("HttpBuilder url 格式不正确",-1);
            return;
        }
        DefaultHttpCallBack customCallBack = new DefaultHttpCallBack(this,this.mVersionInterceptor);
        HttpUtils httpUtils = HttpUtils.getInstance().addHeader(httpBuilder.getHeader())
                .setHttpUrl(httpBuilder.getUrl());
        if (httpBuilder.getMethod() == HttpBuild.GET){
            httpUtils.get(customCallBack);
        }else {
            if (httpBuilder.getPostType() == HttpBuild.JSON){
                httpUtils.post(httpBuilder.getJson(), customCallBack);
            }else {
                httpUtils.form(httpBuilder.getForm(), customCallBack);
            }
        }

    }

    /**
     * 判断是否可以开始下载apk
     * @param next
     * @param url
     */
    void doNext(boolean next, String url){
        if (next){
            startDownloadApk(url);
        }
    }

    /**
     * 开始下载apk
     * @param url
     */
    private void startDownloadApk(String url) {
        new Download(sContext,mVersionInterceptor)
                .execute(url,mApkName);
    }



}
