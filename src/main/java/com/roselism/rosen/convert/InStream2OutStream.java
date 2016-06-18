package com.roselism.rosen.convert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 输入流转输出流
 * Created by simon on 2016/4/27.
 */
public class InStream2OutStream implements Converter<InputStream, OutputStream> {

    File out; // 输出文件

    /**
     * @param out 设置输出的文件
     */
    public InStream2OutStream(File out) {
        this.out = out;
    }

    @Override
    public OutputStream convert(InputStream in) {

        OutputStream output = null;
        try {
            output = new FileOutputStream(out);
            int len;
            byte[] buffer = new byte[1024 * 2];
            while ((len = in.read(buffer)) != -1)
                output.write(buffer, 0, len);
        } catch (IOException e) {// IO操作异常
            e.printStackTrace();
        }
        return output;
    }
}
