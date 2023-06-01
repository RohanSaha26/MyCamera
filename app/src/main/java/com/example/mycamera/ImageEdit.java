package com.example.mycamera;

import static android.widget.Toast.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import com.marvinlabs.widget.floatinglabel.color.ColorCMYK;


public class ImageEdit extends AppCompatActivity {

    Spinner spinner;
    SeekBar seekBar;
//    ImageView resultImg;
    ImageView sourceImg;
    TextView seekVal,nameImg;
    //                      0/.        1/       2/.              3/.             4/.        5/.       6/.        7/.          8/.          9/.       10/.     11/.          12/.            13/.       14/.        15/.
    String[] algoList = {"Original","BW","Negative BW", "Negative Color","Sharpening","Water Art","Vignette","CLAHE","Gaussian Blur","Median Blur","Hue","Saturation","Brightness","HDR Effect","Warm Tone","Cool Tone","Cyan","Magenta","Yellow","Black"};
    Bitmap resultBitmap;
    int choice,seekBarValue,changeStatus=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit_new);

//        findViewById(R.id.lottieLoad).setVisibility(View.INVISIBLE);
        //Get Image from another intent.
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");
        String editPath = Environment.getExternalStorageDirectory()+"/DCIM/MyCamera/MyCameraEdit";
        String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        Log.d("paath",editPath);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        spinner = findViewById(R.id.dropdown);
        sourceImg = (ImageView) findViewById(R.id.img1);
        nameImg = (TextView)findViewById(R.id.imgStatusName);
//        resultImg =  (ImageView) findViewById(R.id.img2);

        sourceImg.setImageBitmap(bitmap);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, algoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choice = position;
                if (position==13||position==14||position==15||position==9||position==8||position==7||position==4||position==1||position==2||position==6){
                    seekBar.setProgress(0);
                    seekBarValue = 0;
                }

                else if (position==10||position==11||position==12||position==5){
                    seekBar.setProgress(50);
                    seekBarValue = 50;
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                resultBitmap = bitmap;
            }
        });

        seekBar= (SeekBar)findViewById(R.id.bar);
        seekVal = (TextView) findViewById(R.id.seekValue);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue = progress;
                seekVal.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        findViewById(R.id.applyAlgo).setOnClickListener(v -> {
            findViewById(R.id.lottieLoad).setVisibility(View.VISIBLE);
            resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            switch (choice){
                case 0: //ORIGINAL
                    resultBitmap = bitmap;
                    break;
                case 1: //BW
                    Mat grayMat = new Mat();
                    Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY);
                    Utils.matToBitmap(grayMat, resultBitmap);
                    break;
                case 2: //NEGATIVE BW
                    float inte1 = seekScalling(seekBarValue,0,100,1,2);
                    resultBitmap = func(bitmap,width,height,inte1,2);
                    break;
                case 3: //NEGATIVE COLOR
                    float inte2 = seekScalling(seekBarValue,0,100,1,2);
                    resultBitmap = func(bitmap,width,height,inte2,3);
                    break;
                case 4: //SHARPENING
                    float valSharp = seekScalling(seekBarValue,0,100,0,10);
                    Utils.matToBitmap(sharpening(mat,valSharp), resultBitmap);
                    break;
                case 5: //WATER ART
                    resultBitmap = applyWaterArt(bitmap,mat,width,height,seekBarValue);
                    break;
                case 6: //VIGNETTE
                    float valVignette = seekScalling(seekBarValue,0,100,1200,500);
                    resultBitmap = applyVignette(bitmap,valVignette);//1- small(full black) //1400-original(no change)

                    break;
                case 7: //CLAHE
                    float valClip = seekScalling(seekBarValue,0,100,1,20);
                    Mat claheMat = applyCLAHE(mat,valClip);
                    Utils.matToBitmap(claheMat, resultBitmap);
                    break;
                case 8: //GAUSSIAN BLUR
                    Mat gauss = new Mat();
                    int s = 2*seekBarValue + 1;//odd(2n+1)
                    Imgproc.GaussianBlur(mat, gauss, new org.opencv.core.Size(s,s), 0);
                    Utils.matToBitmap(gauss, resultBitmap);
                    break;
                case 9: //MEDIAN BLUR
                    Mat blur  = new Mat();
                    int k = 2*seekBarValue + 1;//odd(2n+1)
                    Imgproc.medianBlur(mat,blur,k);
                    Utils.matToBitmap(blur, resultBitmap);
                    break;
                case 10: //HUE
                    float valH = seekScalling(seekBarValue,0,100,0,2);
                    resultBitmap = func(bitmap,width,height,valH,10);
                    break;
                case 11: //SATURATION
                    float valS = seekScalling(seekBarValue,0,100,0,2);
                    resultBitmap = func(bitmap,width,height,valS,11);
                    break;
                case 12: //BRIGHTNESS
                    float valV = seekScalling(seekBarValue,0,100,0.2F,2);
                    resultBitmap = func(bitmap,width,height,valV,12);
                    break;
                case 13: //HDR Effect
                    resultBitmap = applyHDREdit(bitmap,mat,width,height,seekBarValue);
                    break;
                case 14: //WARM TONE
                    float toneWarm = seekScalling(seekBarValue,0,100,0,1);
                    resultBitmap = applyTone(bitmap,toneWarm,14);
                    break;
                case 15: //COOL TONE
                    float toneCool = seekScalling(seekBarValue,0,100,0,1);
                    resultBitmap = applyTone(bitmap,toneCool,15);
                    break;
                case 16:
                    resultBitmap = applyCYMK(bitmap,width,height,seekBarValue,1);
                    break;
                case 17:
                    resultBitmap = applyCYMK(bitmap,width,height,seekBarValue,2);
                    break;
                case 18:
                    resultBitmap = applyCYMK(bitmap,width,height,seekBarValue,3);
                    break;
                case 19:
                    resultBitmap = applyCYMK(bitmap,width,height,seekBarValue,4);
                    break;
            }
//            resultImg.setImageBitmap(resultBitmap);
            sourceImg.setImageBitmap(resultBitmap);
            changeStatus = 1;
            nameImg.setText(""+algoList[choice]+"-"+seekBarValue);
            findViewById(R.id.lottieLoad).setVisibility(View.INVISIBLE);
        });
        findViewById(R.id.save).setOnClickListener(v -> {

            File folder = new File(editPath);
            if (!folder.exists()) {
                folder.mkdir();
            }
            saveBitmapImage(resultBitmap,editPath+"/"+fileNameWithoutExtension+"-edited.jpg");
            Toast.makeText(this,"Image Saved Successfully.", LENGTH_SHORT).show();
        });
        findViewById(R.id.change).setOnClickListener(v -> {
            if (resultBitmap!=null){

                if (changeStatus==0){
                    sourceImg.setImageBitmap(resultBitmap);
                    changeStatus=1;
                    nameImg.setText(""+algoList[choice]+"-"+seekBarValue);
                }else {
                    sourceImg.setImageBitmap(bitmap);
                    changeStatus=0;
                    nameImg.setText("Original");
                }
                findViewById(R.id.change).setRotation(findViewById(R.id.change).getRotation() - 90);
            }
            else{
                sourceImg.setImageBitmap(bitmap);
                Toast.makeText(this,"Apply first to see changes.", LENGTH_SHORT).show();
            }
        });
    }


    public static Bitmap applyVignette(Bitmap bitmap, float radius) {
        // Create a new bitmap with the same dimensions as the original
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // Create a canvas to draw on the new bitmap
        Canvas canvas = new Canvas(output);

        // Create a paint object
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Set up a radial gradient shader
        RadialGradient gradient = new RadialGradient(
                bitmap.getWidth() / 2f, // Center X
                bitmap.getHeight() / 2f, // Center Y
                radius, // Radius
                new int[] {0x00000000, 0xFF000000}, // Colors (transparent to black)
                new float[] {0.8f, 1.0f}, // Color positions
                Shader.TileMode.CLAMP // Shader tiling mode
        );

        // Set the shader on the paint object
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        // Apply the vignette effect by drawing the bitmap with the radial gradient shader
        canvas.drawBitmap(bitmap, 0, 0, paint);
        paint.setShader(gradient);
        canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);

        return output;
    }
    private Bitmap applyCYMK(Bitmap bitmap,int width, int height, int seekBarValue,int CYMKchoice){
        Bitmap res = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        float val = seekScalling(seekBarValue,0,100,0.5F,2);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            int cyan = 255 - Color.red(pixel);
            int magenta = 255 - Color.green(pixel);
            int yellow = 255 - Color.blue(pixel);
            int black = Math.min(cyan, Math.min(magenta, yellow));

            // Edit the CMYK values here
            if (CYMKchoice==1)
                cyan *= val;
            else if (CYMKchoice==2)
                magenta *= val;
            else if(CYMKchoice==3)
                yellow*=val;
            else if(CYMKchoice==4)
                black*=val;
            else
                cyan*=1;

            int newCyan = Color.argb(255, cyan, cyan, cyan);
            int newMagenta = Color.argb(255, magenta, magenta, magenta);
            int newYellow = Color.argb(255, yellow, yellow, yellow);
            int newBlack = Color.argb(255, black, black, black);

            int newPixel = Color.argb(255, 255-newCyan, 255-newMagenta, 255-newYellow);
            res.setPixel(i % width, i / width, newPixel);
        }
        return res;
    }
    private Bitmap applyWaterArt(Bitmap bitmap, Mat mat, int w, int h, int seekBarValue) {
        Bitmap res= Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int s = (int)seekScalling(seekBarValue,0,100,3,11);
        int k = 2 * s + 1;//odd(2n+1)
        Mat clearedMat = new Mat();
        Mat filteredMat = new Mat();
        Mat gaussMat = new Mat();
        Mat sharped = new Mat();
        Imgproc.medianBlur(mat,clearedMat,k);
        Imgproc.medianBlur(clearedMat,clearedMat,k);
        Imgproc.medianBlur(clearedMat,clearedMat,k);

        Imgproc.GaussianBlur(clearedMat,gaussMat,new org.opencv.core.Size(k,k),2);
        Core.addWeighted(clearedMat,1.5,gaussMat,-0.5,0,sharped);
        Core.addWeighted(sharped,1.4,gaussMat,-0.2,10,sharped);
        sharped = sharpening(sharped,0.5F);
        Utils.matToBitmap(sharped, res);
        return res;
    }

    private Bitmap applyHDREdit(Bitmap bitmap, Mat mat, int w, int h, float val) {
        Bitmap res= Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //sharpen(0.2-0.4) ; clahe(1-4) ; saturation(1-1.5)
        float sh = seekScalling(val,0,100,0.2F,0.4F);
        float cl = seekScalling(val,0,100,1,4);
        float st = seekScalling(val,0,100,1,1.5F);
//        Mat e = applyCLAHE(sharpening(mat, 0.5F),3);
        Mat e = sharpening(applyCLAHE(mat,cl), sh);

        Utils.matToBitmap(e, res);
        res = func(res,w,h, st,11);
        return res;
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

    private float seekScalling(float seekBarValue, float inpMin, float inpMax, float outMin, float outMax) {
        return ((seekBarValue - inpMin) / (inpMax - inpMin)) * (outMax - outMin) + outMin;
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

    private Bitmap applyTone(Bitmap bitmap, float toneLevel,int t) {
        Bitmap resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        float redScale,greenScale,blueScale;
        if(t==14){ //Warm
            redScale = 1.0f + toneLevel;
            greenScale = 0.95f - (0.05f * toneLevel);
            blueScale = 0.85f - (0.15f * toneLevel);
        }
        else if (t==15){ //Cool
             redScale = 0.85f - (0.15f * toneLevel);
             greenScale = 0.95f - (0.05f * toneLevel);
             blueScale = 1.0f + toneLevel;

        }
        else {
            redScale=0;greenScale=0;blueScale=0;
        }
        colorMatrix.set(new float[]{
                redScale, 0, 0, 0, 0,  // Red channel
                0, greenScale, 0, 0, 0,  // Green channel
                0, 0, blueScale, 0, 0,  // Blue channel
                0, 0, 0, 1, 0      // Alpha channel
        });

        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return resultBitmap;
    }

//    public static Mat singleScaleRetinex(Mat img, double variance) {
//        Log.d("RETI","singleScaleRetinex");
//        Mat retinex = new Mat();
//        Mat blurredImg = new Mat();
//        Imgproc.GaussianBlur(img, blurredImg, new Size(0, 0), variance);
//        Log.d("RETI","singleScaleRetinex--2");
//        Mat logImg = new Mat();
//        Mat logBlurredImg = new Mat();
//        Core.log(img, logImg);
//        Log.d("RETI","singleScaleRetinex--3");
//        Core.log(blurredImg, logBlurredImg);
//        Log.d("RETI","singleScaleRetinex--4");
//        Mat diff = new Mat();
//        Core.absdiff(logImg, logBlurredImg, diff);
//        Log.d("RETI","singleScaleRetinex--5");
//
//        Core.subtract(diff, new Scalar(0), retinex);
//        Log.d("RETI","singleScaleRetinex--end");
//
//        return retinex;
//    }
//    public static Mat SSR(Mat img, double variance) {
//        Log.d("RETI","SSR");
//        img.convertTo(img, CvType.CV_64F, 1.0);
//        Mat img_retinex = singleScaleRetinex(img, variance);
////        Mat img_retinex = img;
//        List<Mat> channels = new ArrayList<>();
//        Core.split(img_retinex, channels);
//
//        for (Mat channel : channels) {
//            MatOfInt hist = new MatOfInt();
//            MatOfInt histCount = new MatOfInt();
//            Imgproc.calcHist(Collections.singletonList(channel), new MatOfInt(0), new Mat(), hist, new MatOfInt(256), new MatOfFloat(0, 256));
//
//            int zeroCount = 0;
//            float[] histData = new float[(int) (hist.total() * hist.channels())];
//            hist.get(0, 0, histData);
//
//            for (int i = 0; i < histData.length; i++) {
//                if (histData[i] == 0) {
//                    zeroCount++;
//                }
//            }
//
//            float lowVal = 0;
//            float highVal = 1;
//
//            for (int i = 0; i < histData.length; i++) {
//                if (histData[i] < zeroCount * 0.1) {
//                    lowVal = i / 100.0f;
//                }
//
//                if (histData[i] < zeroCount * 0.1) {
//                    highVal = i / 100.0f;
//                    break;
//                }
//            }
//
//            Core.subtract(channel, new Scalar(lowVal), channel);
//            Core.divide(channel, new Scalar(highVal - lowVal), channel);
//            Core.multiply(channel, new Scalar(255), channel);
//        }
//
//        Core.merge(channels, img_retinex);
//        img_retinex.convertTo(img_retinex, CvType.CV_8U);
//        Log.d("RETI","SSR--end");
//        return img_retinex;
//    }
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