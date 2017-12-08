package com.example.chenshuang.csvideo;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

/**
 * 预览录制完成后的视频
 */
public class PreviewActivity extends AppCompatActivity {

    public VideoView videoView;
    public String filePath;
    public View cancle;
    public View sure;
    public boolean isLooping = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_layout);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        filePath = getIntent().getStringExtra("videoPath");
        if (!TextUtils.isEmpty(filePath)) {
            videoView = (VideoView) findViewById(R.id.videoView);
            cancle = findViewById(R.id.cancle);
            sure = findViewById(R.id.sure);
            sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_OK);
                    finish();
                }
            });
            cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (filePath != null) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    isLooping = false;
                    finish();
                }
            });
            playVideo();
        } else {
            Toast.makeText(this, "无法预览视频!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void playVideo() {
        if (Build.VERSION.SDK_INT >= 24) {
            Uri uri = FileProvider.getUriForFile(this, "com.example.chenshuang.csvideo", new File(filePath));
            videoView.setVideoURI(uri);
        } else {
            videoView.setVideoPath("file://"+filePath);
            Log.d("Jam","file://"+filePath);
        }
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("Jam", "onCompletion");
                if(isLooping){
                    playVideo();
                }
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(PreviewActivity.this, "播放视频错误!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        videoView.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
        isLooping = false;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.suspend();    //释放所占用的内存资源
    }

    /**
     * 避免重回界面的时候视频播放器不播放
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        playVideo();
    }
}
