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
import org.opencv.photo.MergeMertens;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageEdit extends AppCompatActivity {

    Spinner spinner;
    SeekBar seekBar;
    ImageView sourceImg;
    TextView seekVal,nameImg;
    String[] algoList = {"Original","Grayscale","Negative_Grayscale",
            "Negative_Color","Sharpening","Water_Art","Vignette",
            "CLAHE","Gaussian_Blur","Median_Blur","Hue","Saturation",
            "Brightness","HDR_Effect","Warm_Tone","Cool_Tone","Cyan",
            "Magenta","Yellow","Black","Night_Enhancement"};
    Bitmap resultBitmap;
    String imgLastName="";
    int choice,seekBarValue,changeStatus=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit_new);
        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath");
        String editPath = Environment.getExternalStorageDirectory()+"/DCIM/MyCamera/MyCameraEdit";
        String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        spinner = findViewById(R.id.dropdown);
        sourceImg = (ImageView) findViewById(R.id.img1);
        nameImg = (TextView)findViewById(R.id.imgStatusName);
        sourceImg.setImageBitmap(bitmap);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, algoList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choice = position;
                if (position==13||position==14||position==15||position==9||
                        position==8||position==7||position==4||position==1||
                        position==2||position==6||position==20){
                    seekBar.setProgress(0);
                    seekBarValue = 0;
                }

                else if (position==10||position==11||position==12||
                        position==5||position==16||position==17||
                        position==18||position==19){
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
                    imgLastName = algoList[0];
                    break;
                case 1: //BW
                    Mat grayMat = new Mat();
                    Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGR2GRAY);
                    Utils.matToBitmap(grayMat, resultBitmap);
                    imgLastName = algoList[1];
                    break;
                case 2: //NEGATIVE BW
                    float inte1 = seekScalling(seekBarValue,0,100,1,2);
                    resultBitmap = func(bitmap,width,height,inte1,2);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 3: //NEGATIVE COLOR
                    float inte2 = seekScalling(seekBarValue,0,100,1,2);
                    resultBitmap = func(bitmap,width,height,inte2,3);
                    imgLastName = algoList[choice]+seekBarValue;

                    break;
                case 4: //SHARPENING
                    float valSharp = seekScalling(seekBarValue,0,100,0,10);
                    Utils.matToBitmap(sharpening(mat,valSharp), resultBitmap);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 5: //WATER ART
                    resultBitmap = applyWaterArt(bitmap,mat,width,height,seekBarValue);
                    imgLastName = algoList[choice]+seekBarValue;

                    break;
                case 6: //VIGNETTE
                    float valVignette = seekScalling(seekBarValue,0,100,1200,500);
                    resultBitmap = applyVignette(bitmap,valVignette);//1- small(full black) //1400-original(no change)
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 7: //CLAHE
                    float valClip = seekScalling(seekBarValue,0,100,1,20);
                    Mat claheMat = applyCLAHE(mat,valClip);
                    Utils.matToBitmap(claheMat, resultBitmap);
                    imgLastName = algoList[choice]+seekBarValue;

                    break;
                case 8: //GAUSSIAN BLUR
                    Mat gauss = new Mat();
                    int s = 2*seekBarValue + 1;//odd(2n+1)
                    Imgproc.GaussianBlur(mat, gauss, new org.opencv.core.Size(s,s), 0);
                    Utils.matToBitmap(gauss, resultBitmap);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 9: //MEDIAN BLUR
                    Mat blur  = new Mat();
                    int k = 2*seekBarValue + 1;//odd(2n+1)
                    Imgproc.medianBlur(mat,blur,k);
                    Utils.matToBitmap(blur, resultBitmap);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 10: //HUE
                    float valH = seekScalling(seekBarValue,0,100,0,2);
                    resultBitmap = func(bitmap,width,height,valH,10);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 11: //SATURATION
                    float valS = seekScalling(seekBarValue,0,100,0,2);
                    resultBitmap = func(bitmap,width,height,valS,11);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 12: //BRIGHTNESS
                    float valV = seekScalling(seekBarValue,0,100,0.2F,2);
                    resultBitmap = func(bitmap,width,height,valV,12);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 13: //HDR Effect
                    resultBitmap = applyHDREdit(bitmap,mat,width,height,seekBarValue);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 14: //WARM TONE
                    float toneWarm = seekScalling(seekBarValue,0,100,0,1);
                    resultBitmap = applyTone(bitmap,toneWarm,14);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 15: //COOL TONE
                    float toneCool = seekScalling(seekBarValue,0,100,0,1);
                    resultBitmap = applyTone(bitmap,toneCool,15);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 16://CYAN
                    float cVal = seekScalling(seekBarValue,0,100,2,0);
                    resultBitmap = applyCYMK(bitmap,width,height,cVal,1);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 17://MAGENTA
                    float mVal = seekScalling(seekBarValue,0,100,2,0);
                    resultBitmap = applyCYMK(bitmap,width,height,mVal,2);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 18://YELLOW
                    float kVal = seekScalling(seekBarValue,0,100,2,0);
                    resultBitmap = applyCYMK(bitmap,width,height,kVal,3);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 19://BLCK
                    float bVal = seekScalling(seekBarValue,0,100,2,0.2F);
                    resultBitmap = applyCYMK(bitmap,width,height,bVal,4);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;
                case 20://NIGHT ENHANCEMENT
                    resultBitmap = enhanceNightImage(bitmap,seekBarValue);
                    imgLastName = algoList[choice]+seekBarValue;
                    break;

            }
//            resultImg.setImageBitmap(resultBitmap);
            sourceImg.setImageBitmap(resultBitmap);
            changeStatus = 1;
            if (choice!=0)
                nameImg.setText(""+algoList[choice]+"-"+seekBarValue);
            findViewById(R.id.lottieLoad).setVisibility(View.INVISIBLE);
        });
        findViewById(R.id.save).setOnClickListener(v -> {

            File folder = new File(editPath);
            if (!folder.exists()) {
                folder.mkdir();
            }
            if (imgLastName.equals("")||imgLastName.equals("Original")){
//                saveBitmapImage(bitmap,editPath+"/"+fileNameWithoutExtension+"-"+imgLastName+".jpg");
                Toast.makeText(this,"Original image already exist.", LENGTH_SHORT).show();
            }
            else
            {
                saveBitmapImage(resultBitmap,editPath+"/"+fileNameWithoutExtension+"-"+imgLastName+".jpg");
                Toast.makeText(this,"Image Saved.", LENGTH_SHORT).show();
            }



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
    //---------------------------ALGORITHMS------------------------------
    public static Bitmap applyVignette(Bitmap bitmap, float radius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        RadialGradient gradient = new RadialGradient(
                bitmap.getWidth() / 2f, // Center X
                bitmap.getHeight() / 2f, // Center Y
                radius, // Radius
                new int[] {0x00000000, 0xFF000000}, // Colors (transparent to black)
                new float[] {0.8f, 1.0f}, // Color positions
                Shader.TileMode.CLAMP // Shader tiling mode
        );
        paint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        paint.setShader(gradient);
        canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
        return output;
    }
    private Bitmap applyCYMK(Bitmap bitmap,int width, int height, float val,int CYMKchoice){
        Bitmap res = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);
            int newPixel;
            if (CYMKchoice==1){

                newPixel = Color.rgb( boundTo((int)(red*val)), green,blue);
            }
            else if (CYMKchoice==2){
                newPixel = Color.rgb(red, boundTo((int)(green*val)),blue);

            }
            else if(CYMKchoice==3){
                newPixel = Color.rgb(red,green , boundTo((int)(blue*val)));

            }
            else if(CYMKchoice==4){
                newPixel = Color.rgb(boundTo((int)(red*val)),boundTo((int)(green*val)) , boundTo((int)(blue*val)));

            }
            else{
                newPixel = Color.rgb(0, 0, 0);
            }

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
//1. CLAHE with range(1-4); 2. Sharpening with range(0.2-0.4);3. Saturation with range(1-1.5)
        Utils.matToBitmap(e, res);
        res = func(res,w,h, st,11);//saturation
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
    public Bitmap enhanceNightImage(Bitmap inputBitmap,int seekBarValue) {
        float val = seekScalling(seekBarValue,0,100,1,5);
        float valSharp = seekScalling(seekBarValue,0,100,1,10);
        Mat inputMat = new Mat();
        Utils.bitmapToMat(inputBitmap, inputMat);
        Mat outputMat = new Mat();
        List<Mat> channels = new ArrayList<>();
        Core.split(inputMat, channels);
        for (Mat channel : channels) {
            Core.multiply(channel, new Scalar(val), channel);//1-5
        }
        Core.merge(channels, outputMat);
        Bitmap outputBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888);

        Mat resultImage = new Mat();
        Core.addWeighted(inputMat, 0.5, outputMat, 0.5, 0, resultImage);
        Mat gaussMat = new Mat();
        Mat sharped = new Mat();
        float alpha = (2*valSharp + 1)/2;
        float beta = 1-alpha;
        Imgproc.GaussianBlur(resultImage, gaussMat, new org.opencv.core.Size(7,7), 0);
        Core.addWeighted(resultImage,alpha,gaussMat,beta,0,sharped);
        Utils.matToBitmap(sharped, outputBitmap);
        return outputBitmap;
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
                //hsv[0] => hue ; hsv[1] => saturation ; hsv[2] => value
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
    private int boundTo(int value) {
        int newVal=value;
        if (newVal>255)
            newVal = 255;
        else if (newVal<0)
            newVal = 0;
        return newVal;
    }
    private float seekScalling(float seekBarValue, float inpMin, float inpMax, float outMin, float outMax) {
        return ((seekBarValue - inpMin) / (inpMax - inpMin)) * (outMax - outMin) + outMin;
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