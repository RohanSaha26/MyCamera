package com.example.mycamera;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.TextureView;


//public class CameraManager {
//    private static final String TAG = "CameraManager";
//    private Context context;
//    private CameraDevice cameraDevice;
//    private CameraCaptureSession cameraCaptureSession;
//    private CaptureRequest.Builder captureRequestBuilder;
//    private ImageReader imageReader;
//
//    public CameraManager(Context context) {
//        this.context = context;
//    }
//}
public class HDRActivity extends AppCompatActivity {

//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//    private TextureView textureView;
//    private CameraManager cameraManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hdr_activity);
    }
}
//        textureView = findViewById(R.id.imgView);
//        cameraManager = new CameraManager(this);
//
//        textureView.setSurfaceTextureListener(textureListener);
//    }

//    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            if (ContextCompat.checkSelfPermission(HDRActivity.this, android.Manifest.permission.CAMERA)
//                    == PackageManager.PERMISSION_GRANTED) {
//                cameraManager.openCamera();
//            } else {
//                ActivityCompat.requestPermissions(HDRActivity.this,
//                        new String[]{ android.Manifest.permission.CAMERA},
//                        REQUEST_CAMERA_PERMISSION);
//            }
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
//
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
//            return false;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
//
//        }
//    };

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                cameraManager.openCamera();
//            } else {
                // Permission denied
//                finish();
//            }
//        }
//    }
//    public void takeExposurePhotos() {
//        cameraManager.takeExposurePhotos();
//    }
