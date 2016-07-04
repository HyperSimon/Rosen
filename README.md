# Rosen
Android 轻量级网络框架

## 简介
主要介绍Rosen的简单使用，主要包含：

>一般的get请求 </br>
一般的post请求 </br>
文件下载 </br>
加载图片 </br>
支持请求回调，直接返回对象、对象集合 </br>




## Simple


### request a bitmap &amp show in imageview

```java
Rosen.get("http://188.188.5.20:8080/zhbj/10007/1452327318UU91.jpg",
        new Rosen.ResultCallBack<Bitmap>() {
            @Override
            public void onResult(Bitmap bm) {
                imageview.setImageBitmap(bm);
            }
        },
        new Rosen.OnErrorListener() {
            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }
        }, Rosen.InStream2Bitmap);

```


with lambda &amp method reference
```java
Rosen.get("url", imageview::setImageBitmap, Throwable::printStackTrace, Rosen.InStream2Bitmap);
```

