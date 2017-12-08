package com.example.chenshuang.csvideo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final int START_CSVIDEO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startCsVideo(View view) {
        Intent intent = new Intent();
        intent.setClass(this, CsVideo.class);
        startActivityForResult(intent, START_CSVIDEO);
        overridePendingTransition(R.anim.translation_in, R.anim.translation_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case START_CSVIDEO:
                if (resultCode == RESULT_OK) {
                    String videoPaths = data.getStringExtra("videoPath");
                    String imagePath = data.getStringExtra("imagePath");
                    /**
                     * 在下面进行你的逻辑判断
                     * csVideo只会返回一个路径,不是视频路径,就是视频路径
                     * 当然后面还会继续优化,比如视频第一帧图,或者图片原图路径和裁剪路径一起返回
                     */

                    if (!TextUtils.isEmpty(videoPaths)) {

                    } else if (!TextUtils.isEmpty(imagePath)) {

                    }
                }
                break;
        }
    }
}
