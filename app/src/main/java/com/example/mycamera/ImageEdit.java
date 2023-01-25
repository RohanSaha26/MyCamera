package com.example.mycamera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class ImageEdit extends AppCompatActivity {

    Button select,camera;
    ImageView img;
    Bitmap bitmap;
    Mat mat;
    int SELECT_CODE = 100,CAMERA_CODE = 101;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);

        select = findViewById(R.id.select);
        img = findViewById(R.id.img);
        camera = findViewById(R.id.camera);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,SELECT_CODE);
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==SELECT_CODE&&data!=null){
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
                mat = new Mat();
                Utils.bitmapToMat(bitmap,mat);
//                img.setImageBitmap(bitmap);
                Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);
//                System.out.println(mat);
                Utils.matToBitmap(mat,bitmap);
//                img.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        if(requestCode==CAMERA_CODE&&data!=null){
            bitmap = (Bitmap)data.getExtras().get("data");
            img.setImageBitmap(bitmap);

            mat = new Mat();
            Utils.bitmapToMat(bitmap,mat);
//            Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2GRAY);
//                System.out.println(mat);
//            Utils.matToBitmap(mat,bitmap);
//                img.setImageBitmap(bitmap);

        }

    }
}