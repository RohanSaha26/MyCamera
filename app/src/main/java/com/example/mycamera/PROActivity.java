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
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class PROActivity extends AppCompatActivity {

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
    CameraCharacteristics characteristics;
    //Save to FILE
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    String pathF = Environment.getExternalStorageDirectory()+"/DCIM/MyCamera";
    String imagePath;
    public Bitmap bitmap;

    boolean flashStatus=false;
    int isoValue,wbValue;
    float fdValue;
    int camFlip; //1 - back , 0 - front
    int currentWBValue = 5000;//2000-10000
    int currentISOValue = 3250;//100-6400
    float currentFDValue = 1.0f;//0.0 - 1.0
    SeekBar isoSeek,wbSeek,fdSeek;
    TextView isoVal,wbVal,fdVal;

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
        setContentView(R.layout.activity_pro_activity);
        Button galleryBtn = (Button)findViewById(R.id.galleryBtn);
        ImageView cameraChangeBtn = (ImageView)findViewById(R.id.cameraChange);
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

        File folder = new File(pathF);
        if (!folder.exists()) {
            folder.mkdir();
        }
        ImageView flash = (ImageView) findViewById(R.id.flash);
        flash.setOnClickListener(v -> {
            if (flashStatus==false){
                toggleFlash();
                flashStatus=true;
                flash.setImageResource(R.drawable.flash_off);
            }
            else if(flashStatus==true){
                toggleFlash();
                flashStatus=false;
                flash.setImageResource(R.drawable.flash_on);
            }
        });

//ISO
        isoSeek= (SeekBar)findViewById(R.id.isoSeek);
        isoVal = (TextView) findViewById(R.id.isoVal);
        isoSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                isoValue = progress;
                isoVal.setText(progress+"");
                if (isoValue != currentISOValue) {
                    Range<Integer> isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_OFF);
                    captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, isoValue);
                    float minFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);//0.33333334
                    float maxFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);//20.0
                    float actualFocusDistance = minFocusDistance + (maxFocusDistance - minFocusDistance) * (.8F); //0<fdValue<100 converted to 0.0f to 1.0f
                    captureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, actualFocusDistance);

                    try {
                        cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    currentISOValue = isoValue;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
//WB
        wbSeek= (SeekBar)findViewById(R.id.wbSeek);
        wbVal = (TextView) findViewById(R.id.wbVal);
        wbVal.setText(currentWBValue+"");
        wbSeek.setProgress(currentWBValue);
        wbSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                wbValue = progress;
                wbVal.setText(progress+"");
                if (wbValue != currentWBValue) {
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
                    RggbChannelVector rgbCV = cctToRGBCV(wbValue); // 2000(warm) to 10000(cool)
                    captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
                    captureRequestBuilder.set(CaptureRequest.COLOR_CORRECTION_GAINS, rgbCV);
                    try {
                        cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    currentWBValue = wbValue;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
////AE
//        SeekBar aeSeek= (SeekBar)findViewById(R.id.aeSeek);
//        TextView aeVal = (TextView) findViewById(R.id.aeVal);
//        aeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                aeValue = progress;
//                aeVal.setText(progress+"");
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//FD
        fdSeek = (SeekBar)findViewById(R.id.fdSeek);
        fdVal = (TextView) findViewById(R.id.fdVal);
        fdSeek.setProgress(100);
        fdVal.setText(currentFDValue+"");
        fdSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fdValue = progress/100.0F;
                fdVal.setText(String.valueOf(fdValue));
                if (fdValue != currentFDValue) {
                    float minFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);//0.33333334
                    float maxFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);//20.0
                    float actualFocusDistance = minFocusDistance + (maxFocusDistance - minFocusDistance) * (fdValue); //0<fdValue<100 converted to 0.0f to 1.0f
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_OFF);
                    captureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, actualFocusDistance);
                    try {
                        cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    currentFDValue = fdValue;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.rawOff).setOnClickListener(v -> {
            Intent rowModeFF;
            rowModeFF  = new Intent(PROActivity.this,MainActivity.class);
            startActivity(rowModeFF);
            overridePendingTransition(R.anim.flip_in, R.anim.flip_out);
            finish();
        });

//        camFlip = 1;
        cameraChangeBtn.setOnClickListener(v -> {
            Intent camChange;
            if(camFlip == 1)
                camChange  = new Intent(PROActivity.this,PROActivity.class).putExtra("flip",0);
            else
                camChange  = new Intent(PROActivity.this,PROActivity.class).putExtra("flip",1);
            startActivity(camChange);
            overridePendingTransition(R.anim.flip_in, R.anim.flip_out);
            finish();


        });
        camFlip = getIntent().getIntExtra("flip",1);

        galleryBtn.setOnClickListener(v -> {
            Intent gallery = new Intent(PROActivity.this,GalleryActivity.class).putExtra("rootPath",pathF+"/");
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

    private void toggleFlash() {
        flashStatus = !flashStatus;

        try {
            if (flashStatus) {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            } else {
                captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            }
            CaptureRequest previewRequest = captureRequestBuilder.build();
            cameraCaptureSessions.setRepeatingRequest(previewRequest, null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
            String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
            String path = pathF + "/MYCAM-";
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = imageReader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imagePath = path+timestamp+"-RAW.jpg";
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    Bitmap bitmapTexture = textureView.getBitmap();
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmapTexture, 2160, 3840, true);
                    saveBitmapImage(resizedBitmap,imagePath);

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
                    fdSeek.setProgress(100);
                    currentWBValue = 5000;
                    currentISOValue = 3250;
                    currentFDValue = 1.0f;
                    Intent imgProcess = new Intent(PROActivity.this,ImageProcess.class)
                            .putExtra("rootPath",pathF+"/")
                            .putExtra("imagePath",imagePath);
                    startActivity(imgProcess);
                    Toast.makeText(PROActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();
                    createCameraPreview();
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
            // Add the white balance control
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
                    Toast.makeText(PROActivity.this, "Changed", Toast.LENGTH_SHORT).show();
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
        //--
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private RggbChannelVector cctToRGBCV(int cct) {
        double temp = cct / 100.0;

        // Red
        double red;
        if (temp <= 66) {
            red = 255;
        } else {
            red = 329.698727446 * Math.pow(temp - 60, -0.1332047592);
            red = Math.max(0, Math.min(255, red));
        }

        // Green
        double green;
        if (temp <= 66) {
            green = 99.4708025861 * Math.log(temp) - 161.1195681661;
            green = Math.max(0, Math.min(255, green));
        } else {
            green = 288.1221695283 * Math.pow(temp - 60, -0.0755148492);
            green = Math.max(0, Math.min(255, green));
        }

        // Blue
        double blue;
        if (temp >= 66) {
            blue = 255;
        } else if (temp <= 19) {
            blue = 0;
        } else {
            blue = 138.5177312231 * Math.log(temp - 10) - 305.0447927307;
            blue = Math.max(0, Math.min(255, blue));
        }

        // Calculate scale factor
        double sum = red + green + blue;
        double scaleFactor = sum > 0 ? 255 / sum : 0;

        // Apply scale factor
        red *= scaleFactor;
        green *= scaleFactor;
        blue *= scaleFactor;

        // Convert RGB to RggbChannelVector
        float redFloat = (float) (red / 255.0);
        float greenFloat = (float) (green / 255.0);
        float blueFloat = (float) (blue / 255.0);

        float redEdge = redFloat / (redFloat + greenFloat + blueFloat);
        float greenEdge = greenFloat / (redFloat + greenFloat + blueFloat);

        return new RggbChannelVector(redEdge, greenEdge, greenEdge, blueFloat);
    }

    private void openCamera(int camF) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (camF==1){

                String cameraId = manager.getCameraIdList()[0];
                characteristics = manager.getCameraCharacteristics(cameraId);
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