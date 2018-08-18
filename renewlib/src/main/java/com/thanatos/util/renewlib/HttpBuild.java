package com.thanatos.util.renewlib;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 *  功能描述: 网络请求获取服务器信息构造类
 *  @className: HttpBuilder
 *  @author: thanatos
 *  @createTime: 2018/8/11
 *  @updateTime: 2018/8/11 13:48
 */
public final class HttpBuild {

    /**
     * get 请求方式
     */
    public static final int GET = 0;

    /**
     * post请求方式
     */
    public static final int POST = 1;

    @IntDef({GET, POST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Method{}

    /**
     * post   body 为 json
     */
    public static final int JSON = 0;

    /**
     * post   body 为 form
     */
    public static final int FORM = 1;

    @IntDef({JSON, FORM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PostType{}


    private int method = GET; // 请求方式

    private String url = ""; //请求的url

    private int postType = JSON;

    private String json; //请求体

    private Map<String, String> form = new HashMap<>(); //请求体

    private Map<String, String> headers = new HashMap<>(); //请求头

    public void setMethod(@Method int method){
        this.method =method;
    }

    public int getMethod() {
        return method;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setBody(String json){
        this.postType = JSON;
        this.json = json;
    }

    public HttpBuild addBody(String key, String value){
        this.postType = FORM;
        this.form .put(key,value);
        return this;
    }

    public int getPostType() {
        return postType;
    }

    public Map<String, String> getForm() {
        return form;
    }

    public String getJson() {
        return json;
    }

    public Map<String, String> getHeader() {
        return headers;
    }

    public HttpBuild addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }
}
