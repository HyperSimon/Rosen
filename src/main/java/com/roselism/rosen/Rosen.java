package com.roselism.rosen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.internal.util.Predicate;
import com.roselism.rosen.convert.Converter;
import com.roselism.rosen.convert.InStream2String;
import com.roselism.rosen.util.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 三级缓存还未实现,现在只是简单的实现了网络缓存,内存和磁盘还正在构建中
 * <p>
 * <p>
 * base on from RoseHttp
 *
 * @version 1.4
 */
public class Rosen {
    public static final Converter<InputStream, Bitmap> InStream2BitmapStragegy = input -> BitmapFactory.decodeStream(input); // 流转bitmap策略
    public static final Converter<InputStream, String> InStream2StringStragegy = input -> new InStream2String().convert(input); // 流转String策略
    public final static OnErrorListener simpleErrorListener = throwable -> throwable.printStackTrace();
    public final static Converter noNeedConverter = parameter -> parameter;

    private static final String TAG = "Rosen";
    private static final boolean DEBUG = true;
    private static final Config defaultConfig = new Config()
            .setConnectTimeout(5000)
            .setReadTimeout(5000)
            .setMethod(Config.METHOD_GET);
    private static Config config = defaultConfig;

    /**
     * 请求一个 String
     *
     * @param url
     * @param callBack
     * @param onErrorListener
     * @deprecated 不再推荐使用
     */
    public static void requestString(final String url, final ResultCallBack<String> callBack, final OnErrorListener onErrorListener) {
        Getter<String> getter = new Getter<>();
        getter.getString(url, callBack, onErrorListener);
    }

    public static <R> void requestString(final String url, Getter<R> getter) {

    }

    /**
     * @param url
     * @param callback
     * @since 1.5
     */
    public static void requestString(final String url, ResultCallBack<String> callback) {
        Getter<String> getter = new Getter<>();
        getter.setConverter(Rosen.InStream2StringStragegy);
        getter.setResultCallBack(callback);
        getter.get(url);
    }

    /**
     * 加载url所在的图片
     *
     * @param url
     * @param callBack
     * @since 1.4
     */
    public static void loadImage(final String url, final ResultCallBack<Bitmap> callBack) {
        Getter<Bitmap> getter = new Getter<>();
        getter.setConverter(Rosen.InStream2BitmapStragegy);
        getter.setResultCallBack(callBack);
        getter.get(url);
    }

    /**
     * @param url
     * @param imageView
     * @since 1.5
     */
    public static void loadImage(String url, ImageView imageView) {
        Getter<Bitmap> getter = new Getter<>();
        getter.setResultCallBack(bitmap -> imageView.setImageBitmap(bitmap))
                .setConverter(Rosen.InStream2BitmapStragegy);
        getter.get(url);
    }

    /**
     * 打开一个connection
     *
     * @param urlPath
     * @return
     */
    @NonNull
    private static HttpURLConnection openConnection(@NonNull String urlPath) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        configConnection(connection);
        return connection;
    }

    private static void configConnection(HttpURLConnection connection) {
        try {
            connection.setConnectTimeout(config.getConnectTimeout());
            connection.setReadTimeout(config.getReadTimeout());
            connection.setRequestMethod(config.getMethod());
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送get请求
     * <note>if you want to get a string data, you can use requestString() instand</note>
     *
     * @param url
     * @param callBack
     * @since 1.3
     */
    public static <R> void get(final String url, final ResultCallBack<R> callBack, Converter<InputStream, R> converter) {
        get(url, callBack, simpleErrorListener, converter);
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


    public static void setConfig(Config cofig) {
        config = cofig;
    }

    /**
     * 发送一个post请求
     *
     * @param url
     * @param callBack
     * @since 1.3
     */
    public void post(@NonNull final String url, final ResultCallBack<InputStream> callBack) {
        post(url, callBack, simpleErrorListener, noNeedConverter);
    }

    public void post(@NonNull final String url, final ResultCallBack<InputStream> callBack, OnErrorListener onErrorListener, Converter converter) {
        Getter<InputStream> getter = new Getter<>();
        Preconditions.checkNotNull(url, "url地址不能为null");
        getter.get(url, callBack, onErrorListener, converter);
    }


    private interface Cacher {
        void add(String key, String value);
    }

    /**
     * 配置类，用于配置Rosen框架
     *
     * @since 1.4
     */
    public static class Config {
        /**
         * The HTTP method (GET,POST,PUT,etc.).
         */
        private static final String METHOD_GET = "GET";
        private static int readTimeout;
        private static int connectTimeout;
        private static boolean useCache;
        private static String method;

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

        public String getMethod() {
            return method;
        }

        public Config setMethod(String method) {
            Config.method = method;
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

    private static class Local implements GetStragegy<InputStream> {

        Predicate<String> mPredicate;

        public void setPredicate(Predicate predicate) {
            mPredicate = predicate;
        }

        @Override
        public void request(String url, ResultCallBack<InputStream> callBack, OnErrorListener onErrorListener) {
            if (mPredicate.apply(url)) {
                // TODO: 16-7-8 加载本地缓存

            }
        }
    }

    /**
     * 网络数据的请求
     */
    public static class Request implements GetStragegy<InputStream> {

        @Override
        public void request(String url, ResultCallBack<InputStream> callBack, OnErrorListener onErrorListener) {
            new Thread(() -> {
                HttpURLConnection connection = null;
                try {
                    connection = openConnection(url);
                    InputStream inputStream = connection.getInputStream();
                    callBack.onResult(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    onErrorListener.onError(e);
                } catch (NullPointerException e) {
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
