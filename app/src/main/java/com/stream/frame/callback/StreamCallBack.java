package com.stream.frame.callback;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.stream.frame.utils.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 我新添加的关键类   保存图片名称为 pic_name +".jpg";
 * */
public class StreamCallBack implements Camera.PreviewCallback {
    private int pic_name = 1;
    private byte[] mPreBuffer;

    public StreamCallBack(byte[] preBuffer){
        this.mPreBuffer = preBuffer;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        try {
            // 调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            if (data != null) {

                //设置缓冲区
                int previewSize = size.width * size.height * 3 / 2;
                if (mPreBuffer == null) {
                    mPreBuffer = new byte[previewSize];
                }
                camera.addCallbackBuffer(mPreBuffer);

                ByteArrayOutputStream outstream = new ByteArrayOutputStream(data.length);
                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, outstream);
                byte[] tmp = outstream.toByteArray();
                Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);

                String picture_name = pic_name + ".jpg";
                System.out.println(picture_name);

                FileUtil.saveBitmap(bmp, picture_name);

                pic_name = pic_name + 1;
                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaassssssssssssssssssssssssssss");
                outstream.flush();
                Thread.sleep(5000);
            }
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
    }
}
