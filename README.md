# Rosen
Android 轻量级网络框架

## 简介
主要介绍Rosen的简单使用，主要包含：

>一般的get请求 </br>
一般的post请求 </br>
文件下载 </br>
加载图片 </br>
支持请求回调，直接返回对象、对象集合 </br>





## How to use ?

### request a bitmap &amp; show in imageview

```java
String picUrl = "http://img3.duitang.com/uploads/item/201605/25/20160525093455_Qa2yR.thumb.700_0.jpeg";
Rosen.get(picUrl,
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


with lambda &amp; method reference
```java
String picUrl = "http://img3.duitang.com/uploads/item/201605/25/20160525093455_Qa2yR.thumb.700_0.jpeg";

// method 1
Rosen.get(picUrl, imageview::setImageBitmap, Throwable::printStackTrace, Rosen.InStream2Bitmap);

// method 2
Rosen.get(picUrl, imageview::setImageBitmap, Rosen.InStream2BitmapStragegy);

// method 3 just use getImage:
Rosen.getImage(picUrl, imageview::setImageBitmap);

```


## Simple
