package com.example.chenshuang.csvideo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class CropImageViewActivity extends Activity {
    CropImageView cropImageView;
    private TextView yes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image_view);
        Uri uri = Uri.parse(getIntent().getStringExtra("URI"));
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        cropImageView =(CropImageView)findViewById(R.id.cropImageView);
        yes =(TextView)findViewById(R.id.yes);
        cropImageView.setImageBitmap(bitmap);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap cropbitmap = cropImageView.getCroppedImage();
                Uri cropuri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), cropbitmap, null,null));
                String croppach = cropuri.toString();
                Intent intent = new Intent();
                intent.putExtra("croppach", croppach); //将计算的值回传回去
                //通过intent对象返回结果，必须要调用一个setResult方法，
                //setResult(resultCode, data);第一个参数表示结果返回码，一般只要大于1就可以，但是
                setResult(3, intent);
                finish();
            }
        });

    }


}
