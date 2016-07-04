# Rosen
Android 轻量级网络框架



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

