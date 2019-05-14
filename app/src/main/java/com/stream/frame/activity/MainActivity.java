package com.stream.frame.activity;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.stream.frame.R;
import com.stream.frame.callback.StreamCallBack;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private Camera mcamera;
    private int screenWidth, screenHeight;
    private boolean isPreview = false; // 是否在浏览中

    private int pic_name = 1;
    private byte[] mPreBuffer = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = findViewById(R.id.surface_view);
        screenHeight = 640;
        screenWidth = 480;
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        takePhone();
    }

    public void takePhone() {
        // check Android 6 permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (mcamera == null) {
                mcamera = getMcamera();
                if (mHolder != null) {
                    setStartPreview(mcamera, mHolder);
                }
            }
        }  else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    //在这里面添加了一些内容
    private Camera getMcamera() {
        Camera camera;
        try {
            camera = Camera.open();
            if (camera != null && !isPreview) {
                try {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPreviewSize(screenWidth, screenHeight); // 设置预览照片的大小
                    parameters.setPreviewFpsRange(20, 30); // 每秒显示20~30帧
                    parameters.setPictureFormat(ImageFormat.NV21); // 设置图片格式
                    parameters.setPictureSize(screenWidth, screenHeight); // 设置照片的大小
                    parameters.setPreviewFrameRate(3);// 每秒3帧 每秒从摄像头里面获得3个画面,
                    camera.setPreviewCallback(new StreamCallBack(mPreBuffer)); // 设置回调的类
                    camera.addCallbackBuffer(mPreBuffer);
                    System.out.println("asasasasasasasasssssssssssssssssssssssssssss");
                    camera.startPreview(); // 开始预览
                    camera.autoFocus(null); // 自动对焦
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isPreview = true;
            }
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
            Toast.makeText(this, "无法获取前置摄像头", Toast.LENGTH_LONG);
        }
        return camera;
    }

    /*
      开始预览相机内容
       */
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    释放相机资源
     */
    private void releaseCamera() {
        if (mcamera != null) {
            mcamera.setPreviewCallback(null);
            mcamera.stopPreview();
            mcamera.release();
            mcamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mcamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setStartPreview(mcamera, mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
