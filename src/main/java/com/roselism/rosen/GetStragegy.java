package com.roselism.rosen;

/**
     * 数据请求策略
     *
     * @param <T>
     */
    private interface GetStragegy<T> {
        void request(final String url, final ResultCallBack<InputStream> callBack, final OnErrorListener onErrorListener);
    }
