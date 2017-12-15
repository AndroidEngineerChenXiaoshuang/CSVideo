package com.example.chenshuang.csvideo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 视频录制
 */

public class CsVideo extends AppCompatActivity implements SurfaceHolder.Callback, View.OnTouchListener {

    public static final int REQUEST_CROP = 2;


    public File imageFile;


    public TextView info;


    public Camera camera;


    public StartRecordingView startRecordingView;


    public SurfaceView surfaceView;


    public SurfaceHolder surfaceHolder;


    public MediaRecorder mediaRecorder;


    public Handler handler;


    public RelativeLayout parent;


    public int displayWidth;


    public int displayHeight;


    public int videoRectSize;


    public boolean isFocusAreas = false;


    public CamcorderProfile profile;


    //标识是否是多点触控
    public boolean isPointerMode = false;


    //记录两点距离的值
    public int oldValue;


    //获取视频信息
    public MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();


    /**
     * 前后置状态
     */
    public int cameraId;


    public File childFile;


    /**
     * 初始化操作
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carmer_layout);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(CsVideo.this, permissions.toArray(new String[permissions.size()]), 1);
        }else{
            initView();
        }


    }



    /**
     * 权限回调,获取回调权限信息,判断用户是否允许权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 ) {
                for(int result : grantResults){
                    if(result != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(CsVideo.this,"授权失败!",Toast.LENGTH_SHORT).show();
                        finish();
                        return ;
                    }
                }
                initView();
            }
        }
    }



    /**
     * 初始化组件
     */
    public void initView() {
        parent = (RelativeLayout) findViewById(R.id.parentLayout);
        info = (TextView) findViewById(R.id.info);
        if(Build.VERSION.SDK_INT < 24){
            findViewById(R.id.camera_state).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraChangeState();
                }
            });
        }else{
            findViewById(R.id.camera_state).setVisibility(View.GONE);
        }
        //获取到录制按钮实例
        startRecordingView = (StartRecordingView) findViewById(R.id.startRecordingView);

        //监听录制事件回调
        startRecordingView.setRecording(new StartRecordingView.Recording() {
            @Override
            public void start() {
                info.animate().alpha(0).setDuration(200).start();
                play();
            }

            @Override
            public void stop() {
                startRecordingView.setEnable(false);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        info.setAlpha(1);
                        CsVideo.this.stop(false);
                        if(!isCarsh){
                            mediaMetadataRetriever.setDataSource(childFile.getPath());
                            int videoLength = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                            if(videoLength >= 2000){
                                Intent intent = new Intent(CsVideo.this,PreviewActivity.class);
                                intent.putExtra("videoPath",childFile.getPath());
                                startActivityForResult(intent,1);
                            }else{
                                if(childFile.exists()){
                                    childFile.delete();
                                }
                                startRecordingView.setEnable(true);
                            }
                        }else{
                            if(childFile.exists()){
                                childFile.delete();
                            }
                            startRecordingView.setEnable(true);
                        }


                    }
                },1000);
            }

            @Override
            public void cancle() {
                CsVideo.this.stop(true);
                if(childFile.exists()){
                    childFile.delete();
                }
            }

            @Override
            public void takePhoto() {
                CsVideo.this.takePhoto();
            }
        });

        surfaceView = new SurfaceView(this);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        parent.addView(surfaceView,0,layoutParams);

        surfaceView.setOnTouchListener(this);

        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback(this);

        handler = new Handler();

        profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

        initCamera();

        videoRectSize = ((int) dip2px(100) / 2);
    }

    public void initCamera(){
        try {
            camera = Camera.open(cameraId);
        }catch (RuntimeException e){
            if ("Fail to connect to camera service".equals(e.getMessage())) {
                Toast.makeText(CsVideo.this,"无法打开相机，请检查是否已经开启权限",Toast.LENGTH_SHORT).show();
            } else if ("Camera initialization failed".equals(e.getMessage())) {
                Toast.makeText(CsVideo.this,"相机初始化失败，无法打开",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CsVideo.this,"相机发生未知错误，无法打开",Toast.LENGTH_SHORT).show();
            }
            finish();
            return;
        }

        setCameraParameters();

    }


    /**
     * 设置相机参数
     */
    private void setCameraParameters() {
        if (null != camera) {
            camera.setDisplayOrientation(90);
            Camera.Parameters params = camera.getParameters();
            Camera.Size preViewSize = getOptimalSize(params.getSupportedPreviewSizes(), 1920, 1080);
            if (null != preViewSize) {
                params.setPreviewSize(preViewSize.width, preViewSize.height);
            }

            Camera.Size pictureSize = getOptimalSize(params.getSupportedPictureSizes(), 1920, 1080);
            if (null != pictureSize) {
                params.setPictureSize(pictureSize.width, pictureSize.height);
            }
            //设置图片格式
            params.setPictureFormat(ImageFormat.JPEG);
            params.setJpegQuality(100);
            params.setJpegThumbnailQuality(100);

            List<String> modes = params.getSupportedFocusModes();
            if (modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                //支持自动聚焦模式
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            camera.setParameters(params);
        }

    }


    /**
     * 获取最优尺寸
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;
        if (w > h)
            targetRatio = (double) w / h;
        if (sizes == null)
            return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (size.height >= size.width)
                ratio = (float) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }


    /**
     * 程序被销毁后执行
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null ) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    /**
     * 开始录制视频
     */
    public void play() {
        if (mediaRecorder == null && camera != null) {
            camera.unlock();    //解锁camera便于MediaRecorder使用相机
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);  //设置音频源
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);  //设置视频源
            // 设置输出格式和编码格式（针对低于API Level 8版本）否则就用setProfile方法
            if (Build.VERSION.SDK_INT >= 8) {
                mediaRecorder.setProfile(profile);
            } else {
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);   //设置输出格式
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);   //设置音频的编码格式
                mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);//设置视频的编码格式
            }
            mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);   //设置视频的分辨率
            mediaRecorder.setVideoFrameRate(profile.videoFrameRate);    //设置视频播放时候的帧动画
            mediaRecorder.setVideoEncodingBitRate(10 * 1024 * 1024); //设置视频编码流的大小
            if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                mediaRecorder.setOrientationHint(90);
            }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
                mediaRecorder.setOrientationHint(270);
            }
            mediaRecorder.setMaxDuration(1000 * 10);  //设置最大的录制时间
            File file = getFile();
            if (file != null) {
                String path = file.getPath();
                mediaRecorder.setOutputFile(path);  //设置输出文件的路径
                mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());    // 用上面连接预览中设置的对象来指定应用程序的SurfaceView 预览layout元素。
                try {
                    mediaRecorder.prepare();    //准备好
                    mediaRecorder.start();  //开始录制视频
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }




    public boolean isCarsh;

    /**
     * 停止录制
     */
    public void stop(boolean isStopPreView) {
        try{
            if (mediaRecorder != null ) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            if (camera != null && isStopPreView) {
                camera.stopPreview();
            }
        }catch(Exception e){
            mediaRecorder = null;
            isCarsh = true;
        }
    }


    /**
     * 创建录制后保存的MP4文件
     * @return  返回的保存后的MP4文件
     */
    private File getFile() {
        //针对有SD卡的设备
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(Environment.getExternalStorageDirectory().getPath() + "/TestCameraDir");
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            childFile = new File(dirFile, getDate() + "_video.mp4");
            if (!childFile.exists()) {
                try {
                    childFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return childFile;
        } else {
            Toast.makeText(CsVideo.this, "系统检测不到SD卡!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    /**
     * 创建多媒体保存的文件
     * @return  返回的保存后的MP4文件
     */
    private File getFileForImg() {
        //针对有SD卡的设备
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(Environment.getExternalStorageDirectory().getPath() + "/TestCameraDir");
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            childFile = new File(dirFile, getDate() + "_img.jpg");
            if (!childFile.exists()) {
                try {
                    childFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return childFile;
        } else {
            Toast.makeText(CsVideo.this, "系统检测不到SD卡!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    /**
     * 根据时间来创建文件名
     * @return
     */
    private String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceHolder = holder;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if(camera != null){
                        camera.setPreviewDisplay(surfaceHolder);
                        camera.startPreview();
                        startRecordingView.setEnable(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }




    private void takePhoto() {
        if (camera == null || !startRecordingView.isEnable()) {
            return;
        }
        startRecordingView.setEnable(false);
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    savePhoto(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void savePhoto(byte[] data) throws IOException {
        imageFile = getFileForImg();
        if(imageFile != null){
            int rotation;
            if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                rotation = 90;
            }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
                rotation = -90;
            }else{
                Toast.makeText(CsVideo.this,"发生未知错误!",Toast.LENGTH_SHORT).show();
                startRecordingView.setEnable(true);
                return;
            }
            Bitmap bm0 = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix m = new Matrix();
            m.setRotate(rotation,(float) bm0.getWidth() / 2, (float) bm0.getHeight() / 2);
            final Bitmap bm = Bitmap.createBitmap(bm0, 0, 0, bm0.getWidth(), bm0.getHeight(), m, true);
            bm.compress(Bitmap.CompressFormat.JPEG,100,new FileOutputStream(imageFile));
            startUcrop("file://"+imageFile.getPath());
        }else{
            Toast.makeText(CsVideo.this,"发生未知错误!",Toast.LENGTH_SHORT).show();
            startRecordingView.setEnable(true);
        }

    }


    //启动裁剪图片
    private void startUcrop(String path) {
        Intent intent = new Intent(this, CropImageViewActivity.class);
        intent.putExtra("URI", path);
        startActivityForResult(intent, REQUEST_CROP);
    }



    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop(true);
    }



    /**
     * 手动聚焦
     * @param point 坐标系统
     */
    protected boolean onFocus(Point point, Camera.AutoFocusCallback callback) {
        if (camera == null) {
            return false;
        }

        Camera.Parameters parameters = null;
        try {
            parameters = camera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回

        if (Build.VERSION.SDK_INT >= 14) {

            if (parameters.getMaxNumFocusAreas() <= 0) {
                return focus(callback);
            }

            //定点对焦
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            int left = point.x - 200;
            int top = point.y - 200;
            int right = point.x + 200;
            int bottom = point.y + 200;
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 600));
            parameters.setFocusAreas(areas);
            try {
                //本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
                //目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
                camera.setParameters(parameters);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return false;
            }
        }
        return focus(callback);
    }



    /**
     * 自动对焦
     */
    private boolean focus(Camera.AutoFocusCallback callback) {
        try {
            camera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    /**
     * 切换摄像头
     */
    public void cameraChangeState() {
        if (camera != null) {
            int cameraCount = Camera.getNumberOfCameras();
            if (cameraCount > 0) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < cameraCount; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                            if (camera != null) {
                                camera.stopPreview();
                                camera.release();
                                camera = null;
                            }
                            initCamera();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if(camera != null){
                                            camera.setPreviewDisplay(surfaceHolder);
                                            camera.startPreview();
                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 1000);
                            break;
                        }
                    } else if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                            if (camera != null) {
                                camera.stopPreview();
                                camera.release();
                                camera = null;
                            }
                            initCamera();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if(camera != null){
                                            camera.setPreviewDisplay(surfaceHolder);
                                            camera.startPreview();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 1000);
                            break;
                        }
                    }
                }
            } else {
                Toast.makeText(this, "您的设备不支持拍摄功能", Toast.LENGTH_SHORT).show();
            }

        }
    }



    /**
     * 触摸屏幕事件,多点交互视屏缩放功能
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getX();
                int y = (int) event.getY();
                Point point = new Point();
                point.x = x;
                point.y = y;
                onFocus(point, null);
                if (!isFocusAreas) {
                    isFocusAreas = true;
                    if (displayHeight == 0) {
                        displayWidth = surfaceView.getMeasuredWidth();
                        displayHeight = surfaceView.getMeasuredHeight();
                    }
                    int computerX = x - videoRectSize > 0 ? x + videoRectSize >= displayWidth ? displayWidth - videoRectSize * 2 : x - videoRectSize : 0;
                    int computerY = y - videoRectSize > 0 ? y + videoRectSize >= displayHeight ? displayHeight - videoRectSize * 2 : y - videoRectSize : 0;
                    final VideoRect videoRect = new VideoRect(this);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(((int) dip2px(100)), ((int) dip2px(100)));
                    layoutParams.leftMargin = computerX;
                    layoutParams.topMargin = computerY;
                    parent.addView(videoRect, layoutParams);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isFocusAreas = false;
                            parent.removeView(videoRect);
                        }
                    }, 2000);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isPointerMode = true;
                oldValue = (int) Math.abs(event.getX(0) - event.getX(1));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                isPointerMode = false;
                oldValue = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                //如果是多点交互就进行缩放功能
                if (isPointerMode && camera != null && camera.getParameters() != null) {
                    Camera.Parameters parameters = camera.getParameters();
                    if (parameters.isZoomSupported()) {
                        int x1 = (int) event.getX(0);
                        int x2 = (int) event.getX(1);
                        int newValue = Math.abs(x1 - x2);
                        if (newValue >= oldValue + 10) {
                            int maxZoomValue = parameters.getMaxZoom();
                            int zoomValue = parameters.getZoom();
                            if (zoomValue < maxZoomValue) {
                                zoomValue++;
                            }
                            parameters.setZoom(zoomValue);
                            oldValue = newValue;
                        } else if (newValue <= oldValue - 10) {
                            int zoomValue = parameters.getZoom();
                            if (zoomValue > 0) {
                                zoomValue--;
                            }
                            parameters.setZoom(zoomValue);
                            oldValue = newValue;
                        }
                        camera.setParameters(parameters);
                    }
                }
                break;
        }
        return true;
    }



    /**
     * 回调后判断是否成功,成功后视频地址给外部     key值为videoPath
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                Intent intent = new Intent();
                intent.putExtra("videoPath",childFile.getPath());
                setResult(RESULT_OK,intent);
                finish();
            }
        }else if(requestCode == REQUEST_CROP ){
            if(data != null){
                String croppach = data.getStringExtra("croppach");
                if(!TextUtils.isEmpty(croppach)){
                    Uri imgeUri = Uri.parse(croppach);
                    String imagePath = Utils.getRealPathFromUri(CsVideo.this,imgeUri);
                    File file = new File(imagePath);
                    if(file.exists()){
                        Intent intent = new Intent();
                        intent.putExtra("imagePath",imagePath);
                        setResult(RESULT_OK,intent);
                        finish();
                    }else{
                        Toast.makeText(CsVideo.this,"文件可能被移动或者被删除",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            if(imageFile != null && imageFile.exists()){
                imageFile.delete();
            }
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.translation_in2,R.anim.translation_out2);
        finish();
    }


    /**
     * dp转换成px
     */
    public float dip2px(float dp) {
        float i = this.getResources().getDisplayMetrics().density;
        return dp * i + 0.5f;
    }


    public void finish(View view) {
        finish();
        overridePendingTransition(R.anim.translation_in2,R.anim.translation_out2);
    }
}
