package com.thanatos.util.renewlib;

import com.thanatos.util.renewlib.http.ErrorException;
import com.thanatos.util.renewlib.http.HttpUtils;

import java.lang.ref.WeakReference;

/**
 *  功能描述:
 *  @className: DefaultHttpCallBack
 *  @author: thanatos
 *  @createTime: 2018/8/11
 *  @updateTime: 2018/8/11 14:52
 */
class DefaultHttpCallBack implements HttpUtils.HttpCallBack{

    private WeakReference<ReNew> reNewWrf;
    private WeakReference<VersionInterceptor> versionInterceptorWrf;
    DefaultHttpCallBack(ReNew reNew, VersionInterceptor interceptor){
        this.reNewWrf = new WeakReference<>(reNew);
        this.versionInterceptorWrf = new WeakReference<>(interceptor);
    }

    @Override
    public void next(String string, ErrorException e) {
        if (reNewWrf == null || reNewWrf.get() == null
                || versionInterceptorWrf == null || versionInterceptorWrf.get() == null){
            return;
        }
        if (e.successful){
            DownloadBuild downloadBuild = versionInterceptorWrf.get().dataResult(string);
            if (downloadBuild != null && downloadBuild.download){
                reNewWrf.get().doNext(downloadBuild.download,downloadBuild.url);
            }

        }else {
            versionInterceptorWrf.get().error(e.msg,e.code);
        }
    }
}
