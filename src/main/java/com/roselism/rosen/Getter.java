package com.roselism.rosen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.roselism.rosen.convert.Converter;
import com.roselism.rosen.convert.InStream2String;
import com.roselism.rosen.util.Preconditions;

import java.io.File;
import java.io.InputStream;

/**
 * 数据请求
 *
 * @param <R> 返回的数据的类型
 * @since 1.5
 */
public class Getter<R> {

    public static final StoreStragegy PRIVATE_LOCAL_STORAGEGY = context -> context.getCacheDir();
    public static final StoreStragegy EXTERNAL_CACHE_STR = context -> context.getExternalCacheDir();

    public final static OnErrorListener simpleErrorListener = throwable -> throwable.printStackTrace();
    public final static Converter noNeedConverter = parameter -> parameter;
    public static final Converter<InputStream, Bitmap> InStream2BitmapStragegy = input -> BitmapFactory.decodeStream(input); // 流转bitmap策略
    public static final Converter<InputStream, String> InStream2StringStragegy = input -> new InStream2String().convert(input); // 流转String策略
    public static final Transformer<String> simpleTransformer = string -> string;

    private static final boolean DEBUG = false;
    private static final String TAG = "Getter";
    private ResultCallBack<R> mResultCallBack;
    private OnErrorListener mOnErrorListener;
    private Converter<InputStream, R> mConverter;
    private GetStragegy mGetStragegy;
    private StoreStragegy mStoreStragegy;
    private Transformer<String> mTransformer;

    public Getter() {
    }

    public Getter<R> setResultCallBack(ResultCallBack<R> resultCallBack) {
        this.mResultCallBack = resultCallBack;
        return this;
    }

    public Getter<R> setOnErrorListener(OnErrorListener onErrorListener) {
        mOnErrorListener = onErrorListener;
        return this;
    }

    public Getter<R> setStoreStragegy(StoreStragegy storeStragegy) {
        mStoreStragegy = storeStragegy;
        return this;
    }

    public Getter<R> setConverter(Converter converter) {
        mConverter = converter;
        return this;
    }

    /**
     * 设置特定的数据获取策略
     * 如果没有指定，那么将会从三级缓存中获取
     */
    public Getter setGetStragegy(GetStragegy getStragegy) {
        mGetStragegy = getStragegy;
        return this;
    }

    public Getter setTransformer(Transformer<String> transformer) {
        this.mTransformer = transformer;
        return this;
    }

    /**
     * 通过一个url发送请求
     *
     * @param url
     * @since 1.5
     */
    public void get(String url) {
//        if (mGetStragegy == null) mGetStragegy = new Rosen.Request();
        if (mOnErrorListener == null) mOnErrorListener = simpleErrorListener;
        if (mConverter == null) mConverter = noNeedConverter;
        if (mTransformer == null) mTransformer = simpleTransformer;

        final ResultCallBack<InputStream> resultCallBack = inputStream -> { // 统一接口
            R r = mConverter.convert(inputStream);
            mResultCallBack.onResult(r);
        };

        mGetStragegy = new DiskGetStrogegy();
        mGetStragegy.request(mTransformer.transfrom(url), new ResultCallBack<InputStream>() {
            @Override
            public void onResult(InputStream inputStream) {

                if (inputStream == null) {
                    mGetStragegy = new Rosen.Request();
                    mGetStragegy.request(url, new ResultCallBack<InputStream>() {
                        @Override
                        public void onResult(InputStream in) {
                            resultCallBack.onResult(in);
                        }
                    }, mOnErrorListener);
                } else {
                    resultCallBack.onResult(inputStream);
                }

            }
        }, throwable -> { // 磁盘读取发生错误
            mGetStragegy = new Rosen.Request();
            mGetStragegy.request(url, new ResultCallBack<InputStream>() {
                @Override
                public void onResult(InputStream inputStream1) {
                    resultCallBack.onResult(inputStream1);
                }
            }, mOnErrorListener);
        });
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

        if (mGetStragegy == null) {
            // TODO: 16-6-18 自行检测，检查内存是否有缓存，没有缓存检查硬盘，没有的话读取网络

            ResultCallBack<InputStream> resultCallBack = inputStream -> {
                InStream2String converter = new InStream2String();
                get(url, (ResultCallBack<R>) callBack, onErrorListener, (Converter<InputStream, R>) converter);
            };
            Rosen.Request request = new Rosen.Request();
            mGetStragegy = request;
            request.request(url, resultCallBack, onErrorListener);

        } else {
            ResultCallBack<InputStream> resultCallBack = inputStream -> {
                InStream2String converter = new InStream2String();
                get(url, (ResultCallBack<R>) callBack, onErrorListener, (Converter<InputStream, R>) converter);
            };
            mGetStragegy.request(url, resultCallBack, onErrorListener);
        }
    }

    /**
     * @param url
     * @param callBack
     * @since 1.4
     */
    public void get(String url, ResultCallBack<R> callBack) {
        get(url, callBack, Rosen.simpleErrorListener, Rosen.noNeedConverter);
    }

    /**
     * @param url
     * @param callBack
     * @param onErrorListener
     * @param converter
     * @deprecated 不再推荐使用, 使用<code>   public void get(String url) </code>代替
     */
    public void get(@NonNull String url, ResultCallBack<R> callBack, OnErrorListener onErrorListener, Converter<InputStream, R> converter) {
        Preconditions.checkNotNull(url);

        ResultCallBack<InputStream> resultCallBack = inputStream -> {
            if (DEBUG) Log.d(TAG, "get() called with: " + "url = [" + url + "]");
            R r = converter.convert(inputStream);
            callBack.onResult(r);
        };

        if (mGetStragegy == null) {
            Rosen.Request request = new Rosen.Request();
            request.request(url, resultCallBack, onErrorListener);
        }
    }

    public interface StoreStragegy {
        File from(Context context);
    }

    public interface Transformer<T> {
        T transfrom(T t);
    }
}
