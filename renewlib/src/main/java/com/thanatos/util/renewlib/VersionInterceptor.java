package com.thanatos.util.renewlib;

/**
 *  功能描述: 版本更新拦截器
 *  @className: VersionInterceptor
 *  @author: thanatos
 *  @createTime: 2018/8/11
 *  @updateTime: 2018/8/11 13:38
 */
public interface VersionInterceptor {

    /**
     * 传入版本更新信息获取接口参数
     * @return
     */
    HttpBuild start(HttpBuild builder);

    /**
     *
     * @param string
     * @return
     */
    DownloadBuild dataResult(String string);


    void unEnableStartDownloadManager();

    void progress(int progress);


    void install(InstallApk installApk);
    /**
     * 网络获取数据失败、拦截器拦截
     * @param msg
     * @param code
     */
    void error(String msg, int code);
}
