package com.roselism.rosen.net;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.common.base.Optional;
import com.roselism.rosen.convert.Converter;
import com.roselism.rosen.convert.InStream2String;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by simon on 2016/4/26.
 * <note>该工具类还在定型阶段，所以不建议使用</note>
 *
 * @version 1.4
 */
public class Rosen {

    public static final String METHOD_POST = "POST";
    public static final String METHOD_GEt = "GET";

    private static final String TAG = "Rosen";
    private static final boolean DEBUG = false;
    private static final Config defaultConfig = new Config()
            .setConnectTimeout(5000)
            .setReadTimeout(5000);
    private Config config = defaultConfig;

    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * 打开一个connection
     *
     * @param urlPath
     * @return
     */
    @NonNull
    private Optional<HttpURLConnection> openConnection(@NonNull String urlPath) {
        Optional<HttpURLConnection> optional = Optional.absent();
        try {
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(config.getConnectTimeout());
            return optional.fromNullable(connection);
        } catch (Exception e) {
            e.printStackTrace();
            return optional.fromNullable(null);
        }
    }

    /**
     * 发送一个post请求
     *
     * @param url
     * @param callBack
     * @since 1.3
     */
    @WorkerThread
    public void post(@NonNull final String url, final ResultCallBack<InputStream> callBack) {

        if (DEBUG)
            Log.d(TAG, "post() called with: " + "url = [" + url + "]");

        new Thread(() -> {
            openConnection(url, DEFAULT_BUILDER); // 打开连接
            try {
                InputStream inputStream = mHttp.getInputStream();
                callBack.onResult(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                callBack.onResult(null);
            } finally {
                mHttp.disconnect();
            }
        }).start();
    }

    /**
     * 发送请求
     *
     * @param url
     * @param callBack
     * @since 1.3
     */
    @WorkerThread
    public void get(final String url, final ResultCallBack<InputStream> callBack, final OnErrorListener onErrorListener) {

        if (DEBUG) Log.d(TAG, "post() called with: " + "url = [" + url + "]");

        Optional<HttpURLConnection> optional = openConnection(url);
        HttpURLConnection connection = optional.orNull();
        if (connection == null) return;

        new Thread(() -> {
            try {
                InputStream inputStream = connection.getInputStream();
                callBack.onResult(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                onErrorListener.onError(e);
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
        }).start();
    }


    /**
     * 获取响应码
     *
     * @return
     * @throws IOException
     */
    private int responseCode() throws IOException {

        if (mHttp == null) throw new IOException();
        return mHttp.getResponseCode();
    }

    public void getString(final String url, final ResultCallBack<String> callBack, final OnErrorListener onErrorListener) {
        Getter<String> getter = new Getter<>();
        getter.getString(url, callBack, onErrorListener);
    }

    public interface OnErrorListener {
        void onError(Throwable throwable);
    }

    /**
     * 数据请求策略
     *
     * @param <T>
     */
    private interface Stragegy<T> {
        void request(final String url, final ResultCallBack<InputStream> callBack, final OnErrorListener onErrorListener);
    }

    /**
     * 结果回调接口
     *
     * @param <T>
     */
    public interface ResultCallBack<T> {
        void onResult(T t);
    }

    /**
     * @since 1.4
     */
    public static class Config {
        private int readTimeout;
        private int connectTimeout;

        /**
         * 设置读取超时时常，默认5000 ms
         *
         * @param readTimeout the read timeout. Non-negative.
         * @param timeUnit    read timeout unit
         * @return
         */
        public Config setReadTimeout(int readTimeout, @IntRange(from = 0, to = Integer.MAX_VALUE) TimeUnit timeUnit) {
            setReadTimeout((int) timeUnit.toMillis(readTimeout));
            return this;
        }

        /**
         * 设置读取超时时常，默认5000 ms
         *
         * @param readTimeout the read timeout. Non-negative.
         * @param timeUnit    read timeout unit
         * @return
         */
        public Config setConnectTimeout(int readTimeout, @IntRange(from = 0, to = Integer.MAX_VALUE) TimeUnit timeUnit) {
            setConnectTimeout((int) timeUnit.toMillis(readTimeout));
            return this;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        /**
         * 设置读取超时时常，默认5000 ms
         *
         * @param readTimeout the read timeout in milliseconds. Non-negative.
         * @return this
         */
        public Config setReadTimeout(@IntRange(from = 0, to = Integer.MAX_VALUE) int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        /**
         * 设置连接超时时常，默认5000ms
         *
         * @param connectTimeout
         * @return
         */
        public Config setConnectTimeout(@IntRange(from = 0, to = Integer.MAX_VALUE) int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }
    }

    /**
     * 数据缓存的操作
     * 缓存也分为存和取，这里面该如何划分？
     */
    private class Cacher {
        Map<String, Integer> map = new HashMap<>();

        public void method() {

        }
    }

    /**
     * 数据请求
     *
     * @param <R> 返回的数据的类型
     */
    private class Getter<R> {

        private Stragegy mStragegy;

        /**
         * 设置特定的数据获取策略
         *
         * @param mStragegy
         */
        public void setStragegy(Stragegy mStragegy) {
            this.mStragegy = mStragegy;
        }

        /**
         * 根据一个url获取到一个String
         *
         * @param url
         * @param callBack
         * @since 1.4
         */
        @WorkerThread
        public void getString(String url, ResultCallBack<String> callBack, OnErrorListener onErrorListener) {
            if (DEBUG)
                Log.d(TAG, "getString() called with: " + "url = [" + url + "], callBack = [" + callBack + "], onErrorListener = [" + onErrorListener + "]");

            if (mStragegy == null) {

            } else {
                ResultCallBack<InputStream> resultCallBack = inputStream -> {
                    InStream2String converter = new InStream2String();
                    get(url, (ResultCallBack<R>) callBack, onErrorListener, (Converter<InputStream, R>) converter);
                };
                mStragegy.request(url, resultCallBack, onErrorListener);
            }
        }

        public void get(String url, ResultCallBack<R> callBack, OnErrorListener onErrorListener, Converter<InputStream, R> converter) {
            ResultCallBack<InputStream> resultCallBack = inputStream -> {
                R r = converter.convert(inputStream);
                callBack.onResult(r);
            };
            mStragegy.request(url, resultCallBack, onErrorListener);
        }
    }

    /**
     * 网络数据的请求
     */
    private class Requester implements Stragegy<InputStream> {

        @Override
        @WorkerThread
        public void request(String url, ResultCallBack<InputStream> callBack, OnErrorListener onErrorListener) {
            new Thread(() -> {
                HttpURLConnection connection = openConnection(url).orNull();
                try {
                    InputStream inputStream = connection.getInputStream();
                    callBack.onResult(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    onErrorListener.onError(e);
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
            }).start();
        }
    }
}
