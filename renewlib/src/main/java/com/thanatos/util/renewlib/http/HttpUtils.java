package com.thanatos.util.renewlib.http;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *  功能描述: 网络访问
 *  @className: HttpUtils
 *  @author: thanatos
 *  @createTime: 2018/8/11
 *  @updateTime: 2018/8/11 10:29
 */
public class HttpUtils {

    private static Context mContext;
    private String mHttpUrl; // 网络访问url
    private Map<String, String> headers = new HashMap<>();
    private int method = 0; // 0 get   1: post   2:form
    private String json = ""; // json字符串
    private Map<String, String> form = new HashMap<>();//表单数据
    private HttpCallBack mHttpCallBack; // 网络回调

    private static final Handler mHandler = new Handler(Looper.getMainLooper());

    private HttpUtils (){}

    private static class Builder{
        private static final HttpUtils HTTP_UTILS = new HttpUtils();
    }

    /**
     * 获取当前类实类对象
     * @return
     */
    public static HttpUtils getInstance(){
        return Builder.HTTP_UTILS;
    }

    /**
     * 初始化上下文对象
     * @return
     */
    public static void init(Context context){
        mContext = context;
    }

    /**
     * 设置请求url
     * @param url 请求的url
     * @return
     */
    public HttpUtils setHttpUrl(String url){
        this.mHttpUrl = url;
        return this;
    }

    /**
     * 添加请求的头部
     * @param key 键
     * @param value 值
     * @return
     */
    public HttpUtils addHeader(String key, String value){
        this.headers.put(key, value);
        return this;
    }

    public HttpUtils addHeader(Map<String, String> headers){
        if (headers != null){
            this.headers.putAll(headers);
        }
        return this;
    }

    /**
     * GET请求
     * @return
     */
    public HttpUtils get(HttpCallBack callBack){
        this.mHttpCallBack = callBack;
        build();
        return this;
    }

    /**
     * post请求
     * @return
     */
    public HttpUtils post(HttpCallBack callBack){
        return post("",callBack);
    }

    /**
     * post请求带body json 字符串
     * @param json
     * @return
     */
    public HttpUtils post(String json, HttpCallBack callBack){
        if (TextUtils.isEmpty(json)){
            json = "";
        }
        this.mHttpCallBack = callBack;
        this.json = json;
        build();
        return this;
    }

    /**
     * post请求 form表单
     * @param form
     * @return
     */
    public HttpUtils form(Map<String, String> form, HttpCallBack callBack){
        if (form == null){
            form = new HashMap<>();
        }
        this.form = form;
        this.mHttpCallBack = callBack;
        build();
        return this;
    }

    /**
     * 启动子线程进行网络访问
     */
    private void build(){
        ErrorException errorException = new ErrorException();
        errorException.successful = false;
        errorException.code = -1;
        if (!isConnected(mContext)){
            if (mHttpCallBack != null){
                errorException.msg = "当前网络不可用";
                mHttpCallBack.next("",errorException);
            }
            return;
        }
        if (TextUtils.isEmpty(mHttpUrl)){
            if (mHttpCallBack != null){
                errorException.msg = "url 不能为空";
                mHttpCallBack.next("",errorException);
            }
            return;
        }
        if (!mHttpUrl.startsWith("http")){
            if (mHttpCallBack != null){
                errorException.msg = "url 格式不正确";
                mHttpCallBack.next("",errorException);
            }
            return;
        }

        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    execute();
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (mHttpCallBack != null){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ErrorException errorException = new ErrorException();
                                errorException.successful = false;
                                errorException.code = -1;

                                if (e instanceof NetworkErrorException){
                                    errorException.msg = "当前网络不可用";
                                }else if (e instanceof MalformedURLException ||
                                        e instanceof ProtocolException){
                                    errorException.msg = "地址解析错误";
                                }

                                mHttpCallBack.next("",errorException);
                            }
                        });

                    }
                }
            }
        }.start();
    }

    /**
     * 进行网络请求
     */
    private void execute() throws Exception {
        HttpURLConnection httpURLConnection;
        if (mHttpUrl.startsWith("https")){
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(mHttpUrl).openConnection();
            httpsURLConnection.setSSLSocketFactory(getDefault());
            httpURLConnection = httpsURLConnection;
        }else {
            httpURLConnection = (HttpURLConnection) new URL(mHttpUrl).openConnection();
        }

        String m = "GET";
        if (method == 0){
            m = "GET";
        }else {
            m = "POST";
        }
        httpURLConnection.setRequestMethod(m);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoOutput(method != 0);
        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        Set<Map.Entry<String, String>> h = headers.entrySet();
        for (Map.Entry<String, String> stringStringEntry : h) {
            httpURLConnection.setRequestProperty(stringStringEntry.getKey(),
                    stringStringEntry.getValue());
        }
        if (method == 1){
            httpURLConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
        }else {
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        }
        httpURLConnection.setRequestProperty("accept","application/json");
        if (method != 0){
            getBody(httpURLConnection);
        }
        final ErrorException errorException = new ErrorException();
        if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
            errorException.code = 200;
            errorException.msg = "获取数据成功";
            errorException.successful = true;
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream()));
            final StringBuilder s = new StringBuilder();
            String temp;
            while((temp = bufferedReader.readLine()) != null){
                s.append(temp);
            }
            bufferedReader.close();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mHttpCallBack != null){
                        mHttpCallBack.next(s.toString(),errorException);
                    }
                }
            });
        }else {
            errorException.code = httpURLConnection.getResponseCode();
            errorException.msg = "获取数据失败";
            errorException.successful = false;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mHttpCallBack != null){
                        mHttpCallBack.next("",errorException);
                    }
                }
            });
        }
    }

    private void getBody(HttpURLConnection connection) throws IOException {

        DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
        StringBuilder formStr = new StringBuilder();
        Set<Map.Entry<String, String>> set = form.entrySet();
        for (Map.Entry<String, String> stringStringEntry : set) {
            formStr.append(stringStringEntry.getKey())
                    .append("=")
                    .append(stringStringEntry.getValue())
                    .append("&");
        }
        formStr = new StringBuilder(formStr.substring(0, formStr.lastIndexOf("&")));
        String body = method == 1? json : formStr.toString();
        dos.write(body.getBytes());
        dos.flush();
        dos.close();
    }


    /**
     * 网络回调接口
     */
    public interface HttpCallBack{
        void next(String string, ErrorException e);
    }

    /**
     * 判断网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                return info.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }

    private SSLSocketFactory getDefault() throws Exception {
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null,new TrustManager[]{trustManager},new SecureRandom());
        return sslContext.getSocketFactory();
    }
}
