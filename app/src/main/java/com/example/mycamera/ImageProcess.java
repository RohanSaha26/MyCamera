package com.example.mycamera;

import static org.opencv.core.CvType.CV_32F;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class ImageProcess extends AppCompatActivity {

    private RecyclerView checkListRecyclerView;
    private CheckListAdapter checkListAdapter;
    String[] checkListArray = {"Red","Green","Blue","Grayscale","Black White Negative","Color Negative",
    "Median Blur","Sharp Image","CLAHE","Laplacian","HDR Effect"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process_new);
        LottieAnimationView load = (LottieAnimationView) findViewById(R.id.lottieLoad1);

        Intent intent = getIntent();
        String pathF = intent.getStringExtra("rootPath");
        String imagePath = intent.getStringExtra("imagePath");
        String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        String path = pathF + "/"+fileNameWithoutExtension;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        String[] parts = pathF.split("/");
        String p = "Phone > ";
        for (int i = 4; i < parts.length; i++) {
            p = p + parts[i]+" >";
        }
        p = p + fileNameWithoutExtension;

        //----
        checkListRecyclerView = findViewById(R.id.checkList);
        checkListAdapter = new CheckListAdapter(checkListArray);

        checkListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkListRecyclerView.setAdapter(checkListAdapter);

        //----
        TextView pathroot = (TextView)findViewById(R.id.pathRoot);
        pathroot.setText(p);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        ImageView imgprocessView = (ImageView) findViewById(R.id.imgProcessView);
        imgprocessView.setImageBitmap(bitmap);
        imgprocessView.setOnClickListener(v -> {
           Intent imgViewer = new Intent(ImageProcess.this, ImageViewerActivity.class);
           imgViewer.putExtra("image",imagePath);
           startActivity(imgViewer);
        });

        findViewById(R.id.saveImg).setOnClickListener(v -> {
            int[] arr = checkListAdapter.getSelectedItemsArray();
            Log.d("CheckListArray", Arrays.toString(arr));

            if (isAllZero(arr)) {
                Toast.makeText(this,"Select any checkbox to save.",Toast.LENGTH_SHORT).show();
//                findViewById(R.id.lottieLoad1).setVisibility(View.GONE);

            }
            else {
                Toast.makeText(this,"Please wait...",Toast.LENGTH_LONG).show();
                findViewById(R.id.lottieLoad1).setVisibility(View.VISIBLE);
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.mkdir();
                };
                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap, mat);
                org.opencv.core.Size kernelSize7 = new org.opencv.core.Size(7,7);
                int k = 9;
                if (isOne(arr,0)||isOne(arr,1)||isOne(arr,2)
                        ||isOne(arr,3)||isOne(arr,4)||isOne(arr,5)){
                    Bitmap redB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Bitmap greenB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Bitmap blueB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Bitmap grayScaleB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Bitmap negGrayScaleB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Bitmap negColorB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int pixel = bitmap.getPixel(x, y);
                            int red = Color.red(pixel);
                            int green = Color.green(pixel);
                            int blue = Color.blue(pixel);
                            int gray = (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);
                            redB.setPixel(x, y, Color.rgb(red, 0, 0));
                            greenB.setPixel(x, y, Color.rgb(0, green, 0));
                            blueB.setPixel(x, y, Color.rgb(0, 0, blue));
                            grayScaleB.setPixel(x, y, Color.rgb(gray, gray, gray));
                            negGrayScaleB.setPixel(x, y, Color.rgb(255 - gray, 255 - gray, 255 - gray));
                            negColorB.setPixel(x, y, Color.rgb(255-red, 255-green, 255-blue));
                            float[] hsv = new float[3];
                            Color.RGBToHSV(red, green, blue, hsv);
                            float saturation = hsv[1];
                            saturation*=1.3F;
                            hsv[1] = saturation;
                            int color = Color.HSVToColor(hsv);
                        }
                    }
                    if (isOne(arr,0)) //RED
                        saveBitmapImage(redB,path+"/"+fileNameWithoutExtension+"-red.jpg");
                    if (isOne(arr,1)) //GREEN
                        saveBitmapImage(greenB,path+"/"+fileNameWithoutExtension+"-greenB.jpg");
                    if (isOne(arr,2)) //BLUE
                        saveBitmapImage(blueB,path+"/"+fileNameWithoutExtension+"-blue.jpg");
                    if (isOne(arr,3)) //GRAYSCALE
                        saveBitmapImage(grayScaleB,path+"/"+fileNameWithoutExtension+"-bw.jpg");
                    if (isOne(arr,4)) //BLACKWHITE NEGATIVE
                        saveBitmapImage(negGrayScaleB,path+"/"+fileNameWithoutExtension+"-bw-negative.jpg");
                    if (isOne(arr,5))//COLOR NEGATIVE
                        saveBitmapImage(negColorB,path+"/"+fileNameWithoutExtension+"-col-negative.jpg");
                }
                if (isOne(arr,6)){
                    Mat blur  = new Mat();
                    Imgproc.medianBlur(mat,blur,k);
                    Bitmap blB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(blur, blB);
                    saveBitmapImage(blB,path+"/"+fileNameWithoutExtension+"-blur.jpg");
                }//MEDIAN BLUR
                if (isOne(arr,7)){
                    Bitmap sharpB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Mat sharp = sharpening(mat,5);
                    Utils.matToBitmap(sharp, sharpB);
                    saveBitmapImage(sharpB,path+"/"+fileNameWithoutExtension+"-sharpened.jpg");
                }//SHARPENING
                if (isOne(arr,8)){
                    Mat clahe = applyCLAHE(mat,10);
                    Bitmap claheB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(clahe, claheB);
                    saveBitmapImage(claheB,path+"/"+fileNameWithoutExtension+"-CLAHE.jpg");
                }//CLAHE
                if (isOne(arr,9)){
                    Mat laplas = new Mat();
                    Mat grayImage = new Mat();
                    Mat laplacianImage = new Mat();
                    Imgproc.cvtColor(mat, grayImage, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.Laplacian(grayImage, laplacianImage, CvType.CV_16S, 3);
                    laplacianImage.convertTo(laplacianImage, CvType.CV_8U);
                    mat.copyTo(laplas, laplacianImage);
                    Bitmap lapB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(laplas, lapB);
                    saveBitmapImage(lapB,path+"/"+fileNameWithoutExtension+"-laplas.jpg");
                }//LAPLACIAN
                if(isOne(arr,10)){
                    Bitmap hdrB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    hdrB = applyHDREdit(bitmap,mat,width,height,0);
                    saveBitmapImage(hdrB,path+"/"+fileNameWithoutExtension+"-HDR.jpg");
                }//HDR EFFECT
                findViewById(R.id.lottieLoad1).setVisibility(View.GONE);
                Toast.makeText(this,"Images Saved.",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        findViewById(R.id.closeIntent).setOnClickListener(v -> {
            finish();
        });
        findViewById(R.id.deleteImg).setOnClickListener(v -> {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(ImageProcess.this);
            alertDialogBuilder.setMessage("Are you sure you want to delete this image ?");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String[] projection = new String[]{MediaStore.Images.Media._ID};
                    String selection = MediaStore.Images.Media.DATA + " = ?";
                    String[] selectionArgs = new String[]{new File(imagePath).getAbsolutePath()};
                    Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
                    if (cursor.moveToFirst()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        try {
                            contentResolver.delete(deleteUri, null, null);
                            boolean delete1 = new File(imagePath).delete();
                            Log.e("TAG", delete1 + "");
                            Toast.makeText(ImageProcess.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ImageProcess.this, "Deleting Error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ImageProcess.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.show();

        });
        findViewById(R.id.editMore).setOnClickListener(v -> {
            Intent imgEdit = new Intent(ImageProcess.this,ImageEdit.class)
                    .putExtra("imagePath",imagePath);
            startActivity(imgEdit);
        });
    }

    private boolean isOne(int[] arr, int pos) {
        if (arr[pos]==1)
            return true;
        else
            return false;
    }
    public boolean isAllZero(int[] array) {
        for (int item : array) {
            if (item != 0) {
                return false;
            }
        }
        return true;
    }
    private Mat sharpening(Mat mat,float val) {

        float alpha = (2*val + 1)/2;
        float beta = 1-alpha;
        Mat gaussMat = new Mat();
        Mat sharped = new Mat();
        Imgproc.GaussianBlur(mat, gaussMat, new org.opencv.core.Size(7,7), 0);
        Core.addWeighted(mat,alpha,gaussMat,beta,0,sharped);
        return sharped;
    }
    public Mat applyCLAHE(Mat inputImage,float val) {
        Mat labImage = new Mat();
        Imgproc.cvtColor(inputImage, labImage, Imgproc.COLOR_BGR2Lab);

        ArrayList<Mat> labPlanes = new ArrayList<>();
        Core.split(labImage, labPlanes);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(val); //Contrast Limited
        Mat lChannel = labPlanes.get(0);
        clahe.apply(lChannel, lChannel); // Only apply on L channel.
        Core.merge(labPlanes, labImage);
        Mat outputImage = new Mat();
        Imgproc.cvtColor(labImage, outputImage, Imgproc.COLOR_Lab2BGR);
        return outputImage;
    }
    private Bitmap applyHDREdit(Bitmap bitmap, Mat mat, int w, int h, float val) {
        Bitmap res= Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //sharpen(0.2-0.4) ; clahe(1-4) ; saturation(1-1.5)
        float sh = 0.3F;
        float cl = 3;
        float st = 1.3F;
//        Mat e = applyCLAHE(sharpening(mat, 0.5F),3);
        Mat e = sharpening(applyCLAHE(mat,cl), sh);

        Utils.matToBitmap(e, res);
        res = func(res,w,h, st,11);
        return res;
    }
    public Bitmap func(Bitmap bitmap,int width,int height,float val,int ch){
        Bitmap res = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                int gray = (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);

                if(ch==2){ //Negative BW
                    res.setPixel(x, y, Color.rgb((int)(255 - gray*val), (int)(255 - gray*val), (int)(255 - gray*val)));
                }
                else if(ch==3){ //Negative Color
                    res.setPixel(x, y, Color.rgb((int)(255-red*val), (int)(255-green*val), (int)(255-blue*val)));

                }
                //hsv[0] => hue ; hsv[1] => saturation ; hue[2] => value
                else if(ch==10){ //Hue
                    float[] hsv = new float[3];
                    Color.RGBToHSV(red, green, blue, hsv);
                    float hue = hsv[0];
                    hue*=val;
                    hsv[0] = hue;
                    int color = Color.HSVToColor(hsv);
                    res.setPixel(x, y, Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
                }
                else if(ch==11){ //Saturation
                    float[] hsv = new float[3];
                    Color.RGBToHSV(red, green, blue, hsv);
                    float saturation = hsv[1];
                    saturation*=val;
                    hsv[1] = saturation;
                    int color = Color.HSVToColor(hsv);
                    res.setPixel(x, y, Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
                }

                else if(ch==12){ //Brightness
                    float[] hsv = new float[3];
                    Color.RGBToHSV(red, green, blue, hsv);
                    float value = hsv[2];
                    value*=val;
                    hsv[2] = value;
                    int color = Color.HSVToColor(hsv);
                    res.setPixel(x, y, Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
                }
            }
        }
        return res;
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
}