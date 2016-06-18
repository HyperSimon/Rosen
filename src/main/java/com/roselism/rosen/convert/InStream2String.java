package com.roselism.rosen.convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 将InputStream转换成为String
 * Created by simon on 2016/4/26.
 */
public class InStream2String implements Converter<InputStream, String> {
    @Override
    public String convert(InputStream inputStream) {

        ByteArrayOutputStream outputStream = null; // 输出流
        try {
            int len;
            byte[] buffer = new byte[2048];
            outputStream = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                outputStream.close(); // 关流
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return outputStream.toString();

    }
}
