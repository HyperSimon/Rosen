package com.roselism.rosen;

import java.io.InputStream;

/**
 * 数据请求策略
 *
 * @param <T>
 */
public interface GetStragegy<T> {
    void request(final String url, final ResultCallBack<InputStream> callBack, final OnErrorListener onErrorListener);
}
