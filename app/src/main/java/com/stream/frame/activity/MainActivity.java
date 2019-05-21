package com.stream.frame.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import com.ai.tensorflow.personTracking.PersonTrackerImpl;
import com.stream.frame.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private Camera mcamera;
    private int screenWidth, screenHeight;
    private boolean isPreview = false; // 是否在浏览中
    private byte[] mPreBuffer = null;
    private int pic_name = 1;

    //==============================================================================================
    private String sdcard =  Environment.getExternalStoragePublicDirectory("")+"";
    // param
    private String rootPath = sdcard + File.separator + "tensorflow-lite-demo/tracking";
    private String logFolder = rootPath +  File.separator + "log";   //the path to save log file
    private PersonTrackerImpl personTracker;
    //==============================================================================================

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //===============================================================
        /**创建AI识别*/
        /**Create AI distinguish*/
        personTracker = new PersonTrackerImpl(logFolder);
        personTracker.createPersonTracker(getAssets());
        //===============================================================

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
                    camera.setPreviewCallback(MainActivity.this); // 设置回调的类
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

        //=============================================================
        personTracker.closePersonTracker();
        //=============================================================
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
                Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);

//                NV21ToBitmap nv21ToBitmap = new NV21ToBitmap(context);
//                Bitmap bmp = nv21ToBitmap.nv21ToBitmap(tmp,size.width,size.height);
                //==================================================================================

                //================================================================================
                personTracker.personStreamDetect(bmp);
                //================================================================================

//                String picture_name = pic_name + ".jpg";
//                System.out.println(picture_name);
//                saveBitmap(bmp, picture_name);
//                pic_name = pic_name + 1;
//                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaassssssssssssssssssssssssssss");

                outstream.flush();
//                Thread.sleep(5000);
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
