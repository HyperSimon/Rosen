package com.roselism.rosen;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.Map;

/**
 * 缓存大统领 Cacher
 * Created by simon on 16-7-10.
 */
public class Cacher {
    private static Map<String, Bitmap> mipCache;
    private static Map<String, String> stringCache;
//    Context mContext;

    public Cacher(Context context) {
//        mContext = context;
    }

    public void cache2Memoery(String name, String value) {
        stringCache.put(name, value);
    }

    public void cache2Memory(String name, Bitmap bitmap) {
        mipCache.put(name, bitmap);
    }

    /**
     * 缓存到磁盘
     *
     * @param filename
     * @param path
     */
    public void cache2Local(String filename, String path, String value) {

    }

    public void cache2Local(String filename, String path, Bitmap value) {

    }
}
