package com.stream.frame.callback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.stream.frame.utils.NV21ToBitmap;

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
    private Context context;

    public StreamCallBack(byte[] preBuffer,Context mContext){
        this.mPreBuffer = preBuffer;
        this.context = mContext;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        try {
            // 调用image.compressToJpeg（）将YUV格式图像数据data转为jpg格式
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            if (image != null && data != null) {

                //设置缓冲区
                int previewSize = size.width * size.height * 3 / 2;
                if (mPreBuffer == null) {
                    mPreBuffer = new byte[previewSize];
                }
                camera.addCallbackBuffer(mPreBuffer);

                ByteArrayOutputStream outstream = new ByteArrayOutputStream(data.length);
                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, outstream);
                byte[] tmp = outstream.toByteArray();

                //==================================================================================
//                Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);

                NV21ToBitmap nv21ToBitmap = new NV21ToBitmap(context);
//                Bitmap bmp = nv21ToBitmap.nv21ToBitmap(tmp,size.width,size.height);
                //==================================================================================

                String picture_name = pic_name + ".jpg";
                System.out.println(picture_name);

                saveBitmap(nv21ToBitmap.nv21ToBitmap(tmp,size.width,size.height), picture_name);

                pic_name = pic_name + 1;
                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaassssssssssssssssssssssssssss");
                outstream.flush();
                Thread.sleep(5000);
            }
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
    }

    //新添加的保存到手机的方法
    @SuppressLint("SdCardPath")
    private void saveBitmap(Bitmap bitmap, String bitName) {
        File appDir = new File(Environment.getExternalStorageDirectory()+"/"+"smartPhoneCamera", "Images");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, bitName);     // 创建文件
        try {                                       // 写入图片
            FileOutputStream fos = new FileOutputStream(file);
            Bitmap endBit = Bitmap.createScaledBitmap(bitmap, 720, 1280, true); //创建新的图像大小
            endBit.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
