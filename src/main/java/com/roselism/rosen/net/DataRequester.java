package com.roselism.rosen.net;

/**
 * 数据请求者
 * 与RoseHttp搭配使用有神奇效果
 *
 * @param <T> 返回的数据类型
 */
public class DataRequester<T> {
    private Stragegy<T> mStragegy;

    public void setStragegy(Stragegy<T> mStragegy) {
        this.mStragegy = mStragegy;
    }

    public void getData(ResultCallBack<T> callBack) {
        mStragegy.request(callBack);
    }

    /**
     * 数据请求策略
     *
     * @param <T>
     */
    public interface Stragegy<T> {
        void request(ResultCallBack<T> callBack);
    }

    /**
     * 结果回调接口
     *
     * @param <T>
     */
    public interface ResultCallBack<T> {
        void onResult(T t);
    }
}
