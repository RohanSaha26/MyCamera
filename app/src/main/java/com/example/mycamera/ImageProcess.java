package com.example.mycamera;

import static org.opencv.core.CvType.CV_32F;

import androidx.appcompat.app.AppCompatActivity;

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
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

public class ImageProcess extends AppCompatActivity {

    CheckBox cR,cG,cB,cGr,cGrn,cCn,cSharp,cReti,cCLAHE,cLap,cBlur,cS2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process);

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
        cR = findViewById(R.id.saveRed);
        cG = findViewById(R.id.saveGreen);
        cB = findViewById(R.id.saveBlue);
        cGr = findViewById(R.id.saveGrayscale);
        cGrn = findViewById(R.id.saveNegativeBW);
        cCn = findViewById(R.id.saveNegativeColor);
        cSharp = findViewById(R.id.saveSharpimage);
        cReti = findViewById(R.id.saveRetinex);
        cCLAHE = findViewById(R.id.saveClahe);
        cLap = findViewById(R.id.saveLaplacian);
        cBlur = findViewById(R.id.saveMedianBlur);
        cS2 = findViewById(R.id.saveSaturated);
        TextView pathroot = (TextView)findViewById(R.id.pathRoot);
        pathroot.setText(p);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        ImageView imgprocessView = (ImageView) findViewById(R.id.imgProcessView);
        imgprocessView.setImageBitmap(bitmap);
        findViewById(R.id.saveImg).setOnClickListener(v -> {
            File folder = new File(path);
            if (!folder.exists()) {
                folder.mkdir();
            };
            //@------
            if(cR.isChecked()||cG.isChecked()||cB.isChecked()||cGr.isChecked()||cGrn.isChecked()||cCn.isChecked()||cS2.isChecked()){
                Bitmap redB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Bitmap greenB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Bitmap blueB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Bitmap grayScaleB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Bitmap negGrayScaleB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Bitmap negColorB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Bitmap saturatedB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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

                        //Saturated
                        float[] hsv = new float[3];
                        Color.RGBToHSV(red, green, blue, hsv);
                        //hsv[0] => hue ; hsv[1] => saturation ; hue[2] => value
                        float hue = hsv[0],saturation = hsv[1], value = hsv[2];
//                                hue*=2;saturation *= 2;value*=2;
                        saturation*=2;
                        hsv[0] = hue ; hsv[1] = saturation ; hsv[2] = value;
                        int color = Color.HSVToColor(hsv);
                        saturatedB.setPixel(x, y, Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));

                    }
                }
                if (cR.isChecked())
                    saveBitmapImage(redB,path+"/"+fileNameWithoutExtension+"-red.jpg");
                if (cG.isChecked())
                    saveBitmapImage(greenB,path+"/"+fileNameWithoutExtension+"-greenB.jpg");
                if (cB.isChecked())
                    saveBitmapImage(blueB,path+"/"+fileNameWithoutExtension+"-blue.jpg");
                if (cGr.isChecked())
                    saveBitmapImage(grayScaleB,path+"/"+fileNameWithoutExtension+"-bw.jpg");
                if (cGrn.isChecked())
                    saveBitmapImage(negGrayScaleB,path+"/"+fileNameWithoutExtension+"-bw-negative.jpg");
                if (cCn.isChecked())
                    saveBitmapImage(negColorB,path+"/"+fileNameWithoutExtension+"-col-negative.jpg");
                if (cS2.isChecked())
                    saveBitmapImage(saturatedB,path+"/"+fileNameWithoutExtension+"-saturatedX2.jpg");

            }
            //@------
            //&------
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            org.opencv.core.Size kernelSize7 = new org.opencv.core.Size(7,7);
            if (cBlur.isChecked()) {//MEDIAN BLUR
                int k = 9;
                Mat clearedMat = new Mat();
                Imgproc.medianBlur(mat,clearedMat,k);
                Bitmap blB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(clearedMat, blB);
                saveBitmapImage(blB,path+"/"+fileNameWithoutExtension+"-blur.jpg");
            }
            if (cSharp.isChecked()){//SHARPENED
                Mat gaussMat = new Mat();
                Mat sharped = new Mat();
                Imgproc.GaussianBlur(mat, gaussMat, kernelSize7, 0);
                Core.addWeighted(mat,5.5,gaussMat,-4.5,0,sharped);
                Bitmap sharpB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(sharped, sharpB);
                saveBitmapImage(sharpB,path+"/"+fileNameWithoutExtension+"-sharpened.jpg");

            }
            if(cLap.isChecked()){//LAPLACIAN
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
            }
            if (cCLAHE.isChecked()){//CLAHE
                Mat claheMat = applyCLAHE(mat);
                Bitmap claheB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(claheMat, claheB);
                saveBitmapImage(claheB,path+"/"+fileNameWithoutExtension+"-CLAHE.jpg");
            }
            //&------

            Toast.makeText(this,"Images Saved.",Toast.LENGTH_SHORT).show();
            finish();
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

    public Mat applyCLAHE(Mat inputImage) {
        Mat labImage = new Mat();
        Imgproc.cvtColor(inputImage, labImage, Imgproc.COLOR_BGR2Lab);

        ArrayList<Mat> labPlanes = new ArrayList<>();
        Core.split(labImage, labPlanes);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(2.0);
        Mat lChannel = labPlanes.get(0);
        clahe.apply(lChannel, lChannel);
        Core.merge(labPlanes, labImage);
        Mat outputImage = new Mat();
        Imgproc.cvtColor(labImage, outputImage, Imgproc.COLOR_Lab2BGR);
        return outputImage;
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