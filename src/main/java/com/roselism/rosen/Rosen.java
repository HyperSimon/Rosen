package com.roselism.rosen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;

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
 * <p>
 * from RoseHttp
 *
 * @version 1.4
 */
public class Rosen {
    public static final Converter<InputStream, Bitmap> InStream2Bitmap = input -> BitmapFactory.decodeStream(input);

    private static final String TAG = "Rosen";
    private static final boolean DEBUG = true;
    private final static OnErrorListener simpleErrorListener = throwable -> throwable.printStackTrace();
    private final static Converter noNeedConverter = parameter -> parameter;
    private static final Config defaultConfig = new Config()
            .setConnectTimeout(5000)
            .setReadTimeout(5000);
    private static Config config = defaultConfig;

    public static void requestString(final String url, final ResultCallBack<String> callBack, final OnErrorListener onErrorListener) {
        Getter<String> getter = new Getter<>();
        getter.getString(url, callBack, onErrorListener);
    }

    /**
     * 打开一个connection
     *
     * @param urlPath
     * @return
     */
    @NonNull
    private static Optional<HttpURLConnection> openConnection(@NonNull String urlPath) {
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
     * 发送请求
     *
     * @param url
     * @param callBack
     * @since 1.3
     */
    public static <R> void get(final String url, final ResultCallBack<R> callBack, final OnErrorListener onErrorListener, Converter<InputStream, R> converter) {
        if (DEBUG) Log.d(TAG, "post() called with: " + "url = [" + url + "]");

        Getter<R> getter = new Getter<>();
        getter.get(url, callBack, onErrorListener, converter);
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * 发送一个post请求
     *
     * @param url
     * @param callBack
     * @since 1.3
     */
    public void post(@NonNull final String url, final ResultCallBack<InputStream> callBack) {
        if (DEBUG) Log.d(TAG, "post() called with: " + "url = [" + url + "]");
        post(url, callBack, simpleErrorListener, noNeedConverter);
    }

    public void post(@NonNull final String url, final ResultCallBack<InputStream> callBack, OnErrorListener onErrorListener, Converter converter) {
        Getter<InputStream> getter = new Getter<>();
        getter.get(url, callBack, onErrorListener, converter);
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

    private interface Cacher {
        void add(String key, String value);
    }

    /**
     * @since 1.4
     */
    public static class Config {
        private static int readTimeout;
        private static int connectTimeout;
        private static boolean useCache;

        public static int getConnectTimeout() {
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

        public boolean isUseCache() {
            return useCache;
        }

        /**
         * 设置是否使用缓存
         *
         * @param useCache
         */
        public Config setUseCache(boolean useCache) {
            Config.useCache = useCache;
            return this;
        }

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
    }

    /**
     * 数据请求
     *
     * @param <R> 返回的数据的类型
     */
    private static class Getter<R> {

        private Stragegy mStragegy;

        /**
         * 设置特定的数据获取策略
         *
         * @param stragegy
         */
        public void setStragegy(Stragegy stragegy) {
            this.mStragegy = stragegy;
        }

        /**
         * 根据一个url获取到一个String
         *
         * @param url
         * @param callBack
         * @since 1.4
         */
        public void getString(String url, ResultCallBack<String> callBack, OnErrorListener onErrorListener) {
            if (DEBUG)
                Log.d(TAG, "requestString() called with: " + "url = [" + url + "], callBack = [" + callBack + "], onErrorListener = [" + onErrorListener + "]");

            if (mStragegy == null) {
                // TODO: 16-6-18 自行检测，检查内存是否有缓存，没有缓存检查硬盘，没有的话读取网络

                ResultCallBack<InputStream> resultCallBack = inputStream -> {
                    InStream2String converter = new InStream2String();
                    get(url, (ResultCallBack<R>) callBack, onErrorListener, (Converter<InputStream, R>) converter);
                };
                Requester requester = new Requester();
                mStragegy = requester;
                requester.request(url, resultCallBack, onErrorListener);

            } else {
                ResultCallBack<InputStream> resultCallBack = inputStream -> {
                    InStream2String converter = new InStream2String();
                    get(url, (ResultCallBack<R>) callBack, onErrorListener, (Converter<InputStream, R>) converter);
                };
                mStragegy.request(url, resultCallBack, onErrorListener);
            }
        }

        public void get(String url, ResultCallBack<R> callBack) {
            get(url, callBack, simpleErrorListener, noNeedConverter);
        }

        public void get(String url, ResultCallBack<R> callBack, OnErrorListener onErrorListener, Converter<InputStream, R> converter) {
            ResultCallBack<InputStream> resultCallBack = inputStream -> {
                R r = converter.convert(inputStream);
                callBack.onResult(r);
            };
            Requester requester = new Requester();
//            mStragegy = requester;
            requester.request(url, resultCallBack, onErrorListener);
        }
    }

    /**
     * 网络数据的请求
     */
    private static class Requester implements Stragegy<InputStream> {

        @Override
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

    /**
     * 数据缓存的操作
     * 缓存也分为存和取，这里面该如何划分？
     */
    private class StringCacher {
        Map<String, String> map = new HashMap<>();

        public void addInMemory(String key, String value) {
            map.put(key, value);
        }
    }

    private class BitmapCacher {
        LruCache<String, Bitmap> cache = new LruCache<>(10);

        public void putInMemory(String key, Bitmap value) {
            cache.put(key, value);
        }

        public Bitmap getFromMemory(String key) {
            return cache.get(key);
        }

        public void save2Local(String key, Bitmap value) {
            // TODO: 16-6-18 将bitmap 缓存到本地
        }

        public Bitmap getFromLocal(String key) {
            // TODO: 16-6-18 获取与key值对应的本地bitmap缓存
            return null;
        }

        public Bitmap get(String key) {
            return getFromMemory(key) != null ? getFromMemory(key) : getFromLocal(key);
        }

        public void put(String key, Bitmap value) {
            if (getFromLocal(key) == null) {
                save2Local(key, value);
            }
            if (getFromMemory(key) == null) {
                putInMemory(key, value);
            }
        }
    }
}
