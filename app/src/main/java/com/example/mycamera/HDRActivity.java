package com.example.mycamera;

import static org.opencv.core.CvType.CV_32F;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.print.PrintHelper;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class HDRActivity extends AppCompatActivity {

    private TextureView textureView;

    //Check state orientation of output image
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private android.util.Size imageDimension;
    //Save to FILE
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    String pathF = Environment.getExternalStorageDirectory()+"/DCIM/MyCamera";
    private int isoVal;
    private float focusVal;
    public Bitmap bitmap,bitmap1;
    int camFlip; //1 - back , 0 - front

    PrintHelper printHelper = new PrintHelper(this);

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice=null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hdr_activity);
        Button galleryBtn = (Button)findViewById(R.id.galleryBtn);
        ImageView cameraChangeBtn = (ImageView)findViewById(R.id.cameraChange);
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

        File folder = new File(pathF);
        if (!folder.exists()) {
            folder.mkdir();
        }


//        camFlip = 1;
        cameraChangeBtn.setOnClickListener(v -> {
            Intent camChange;
            if(camFlip == 1)
                camChange  = new Intent(HDRActivity.this,HDRActivity.class).putExtra("flip",0);
            else
                camChange  = new Intent(HDRActivity.this,HDRActivity.class).putExtra("flip",1);
            startActivity(camChange);
            overridePendingTransition(R.anim.flip_in, R.anim.flip_out);
            finish();


        });
        camFlip = getIntent().getIntExtra("flip",1);

        galleryBtn.setOnClickListener(v -> {
            Intent gallery = new Intent(HDRActivity.this,GalleryActivity.class).putExtra("rootPath",pathF+"/");
            startActivity(gallery);
        });

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV loaded successfully");
        }
        textureView = (TextureView)findViewById(R.id.imgView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        FloatingActionButton btnCapture = (FloatingActionButton) findViewById(R.id.captureBtn);
        btnCapture.setOnClickListener(v -> {
            takePicture();
        });

    }
    //pass raw arguments by this function
    private void takePicture() {

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            //Capture image with custom size
            int width = 1920;
            int height = 1080;
//            if(jpegSizes != null && jpegSizes.length > 0)
//            {
//                width = jpegSizes[0].getWidth();
//                height = jpegSizes[0].getHeight();
//            }
            final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.FLASH_MODE_SINGLE);

            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));
            long timestamp = Calendar.getInstance().getTimeInMillis();

            String path = pathF + "/MYCAM-";
            file = new File(path+timestamp+".jpg");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    image = reader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    String imagePath = path+timestamp+".jpg";
                    if (camFlip==0){
                        Matrix matrix = new Matrix();
                        matrix.postRotate(180);
                        matrix.postScale(-1, 1);
                        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        bitmap = bitmap2;
                    }
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    saveBitmapImage(bitmap,imagePath);
                    Intent imgProcess = new Intent(HDRActivity.this,ImageProcess.class)
                            .putExtra("rootPath",pathF+"/")
                            .putExtra("imagePath",imagePath);
                    startActivity(imgProcess);


                }

                private void saveBitmapImage(Bitmap bitmap,String path) {
                    OutputStream fos;
                    try {

                        File file2 = new File(path);

                        fos = new FileOutputStream(file2);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }

            };

            reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(HDRActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();
                    createCameraPreview();

//              finish();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert  texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;

                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(HDRActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        //--
        //ISO,WB,Focal Length control
        //>> WB
//        captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_OFF);
//        RggbChannelVector rgbCV = cctToRGBCV(2765); // 2000(warm) to 100000000(cool)
//        captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
//        captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, rgbCV);

        //>> ISO
//        Range<Integer> isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
//        int minIso = isoRange.getLower(); // Minimum supported ISO value (100)
//        int maxIso = isoRange.getUpper(); // Maximum supported ISO value (6400)
//
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_OFF);
//
//        int isoValue = clamp(100, minIso, maxIso);
//        captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, isoValue);

        //>> FOCUS DISTANCE
        // Get the minimum and maximum focus distances supported by the camera
//        float minFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);//0.33333334
//        float maxFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);//20.0
//
////        Log.d("FOCUS DISTANCE",maxFocusDistance+" "+minFocusDistance);
//// Calculate the desired focus distance (between 0.0f and 1.0f)
//        float desiredFocusDistance =0.5f; // Your desired focus distance between 0.0f and 1.0f
////good 0.5 to 1.0
//// Calculate the actual focus distance based on the min and max values
//        float actualFocusDistance = minFocusDistance + (maxFocusDistance - minFocusDistance) * desiredFocusDistance;
//
//// Set the focus distance in the CaptureRequest.Builder
//        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AE_MODE_OFF);
//        captureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, actualFocusDistance);

        //>>AE
//        Range<Long> exposureTimeRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
//
//        long minExposureTime = exposureTimeRange.getLower(); //100000  nanoseconds
//        long maxExposureTime = exposureTimeRange.getUpper(); //32000000000 nanoseconds
//        Log.d("FOCUS DISTANCE",minExposureTime+" "+maxExposureTime);
//        long exposureTime = 500000;
//        captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,maxExposureTime); // Set the desired exposure time
        //--
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private RggbChannelVector cctToRGBCV(int cct) {
        // Calculate the R, G, and B gains based on the CCT
        float redGain, greenGain, blueGain;

// Calculate red gain
        float temperature = cct / 100.0f;
        if (temperature <= 66) {
            redGain = 255;
        } else {
            redGain = temperature - 60;
            redGain = (float) (329.698727446 * Math.pow(redGain, -0.1332047592));
            if (redGain < 0) {
                redGain = 0;
            } else if (redGain > 255) {
                redGain = 255;
            }
        }

// Calculate green gain
        if (temperature <= 66) {
            greenGain = temperature;
            greenGain = (float) (99.4708025861 * Math.log(greenGain) - 161.1195681661);
            if (greenGain < 0) {
                greenGain = 0;
            } else if (greenGain > 255) {
                greenGain = 255;
            }
        } else {
            greenGain = temperature - 60;
            greenGain = (float) (288.1221695283 * Math.pow(greenGain, -0.0755148492));
            if (greenGain < 0) {
                greenGain = 0;
            } else if (greenGain > 255) {
                greenGain = 255;
            }
        }

// Calculate blue gain
        if (temperature >= 66) {
            blueGain = 255;
        } else if (temperature <= 19) {
            blueGain = 0;
        } else {
            blueGain = temperature - 10;
            blueGain = (float) (138.5177312231 * Math.log(blueGain) - 305.0447927307);
            if (blueGain < 0) {
                blueGain = 0;
            } else if (blueGain > 255) {
                blueGain = 255;
            }
        }

// Create the RggbChannelVector with the calculated gains
        RggbChannelVector rggbChannelVector = new RggbChannelVector(
                redGain / 255.0f,
                greenGain / 255.0f,
                greenGain / 255.0f,
                blueGain / 255.0f
        );
        return  rggbChannelVector;
    }

    private void openCamera(int camF) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (camF==1){

                String cameraId = manager.getCameraIdList()[0];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },REQUEST_CAMERA_PERMISSION);
                    return;
                }
                manager.openCamera(cameraId,stateCallback,null);
            }
            else {
                String[] cameraIds = manager.getCameraIdList();
                for (String cameraId : cameraIds) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                    int cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                        // Found front camera
                        imageDimension = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                .getOutputSizes(SurfaceTexture.class)[0];
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                            return;
                        }
                        manager.openCamera(cameraId, stateCallback, null);
                        return;
                    }
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera(camFlip);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable())
            openCamera(camFlip);
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread= null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
}