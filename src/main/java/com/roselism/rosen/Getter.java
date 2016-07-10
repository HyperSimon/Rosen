package com.roselism.rosen;

/**
     * 数据请求
     *
     * @param <R> 返回的数据的类型
     */
    private static class Getter<R> {

        private GetStragegy mGetStragegy;

        /**
         * 设置特定的数据获取策略
         * 如果没有指定，那么将会从三级缓存中获取
         *
         * @param getStragegy
         */
        public void setGetStragegy(GetStragegy getStragegy) {
            this.mGetStragegy = getStragegy;
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
                Request request = new Request();
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

        public void get(String url, ResultCallBack<R> callBack) {
            get(url, callBack, simpleErrorListener, noNeedConverter);
        }

        public void get(@NonNull String url, ResultCallBack<R> callBack, OnErrorListener onErrorListener, Converter<InputStream, R> converter) {
            Preconditions.checkNotNull(url);

            ResultCallBack<InputStream> resultCallBack = inputStream -> {
                if (DEBUG) Log.d(TAG, "get() called with: " + "url = [" + url + "]");
                R r = converter.convert(inputStream);
                callBack.onResult(r);
            };

            if (mGetStragegy == null) {

                Request request = new Request();
//            mStragegy = requester;
                request.request(url, resultCallBack, onErrorListener);
            }
        }
    }
