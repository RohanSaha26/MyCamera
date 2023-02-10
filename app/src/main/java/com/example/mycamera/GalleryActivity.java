// package com.android.myapplication;
package com.example.mycamera;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GalleryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<Image> arrayList = new ArrayList<>();
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    getImages();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(GalleryActivity.this));
        recyclerView.setHasFixedSize(true);

        if (ActivityCompat.checkSelfPermission(GalleryActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else if (ActivityCompat.checkSelfPermission(GalleryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            getImages();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getImages();
    }

    private void getImages(){
        arrayList.clear();
        String filePath = Environment.getExternalStorageDirectory()+"/DCIM/Camera/";
        File file = new File(filePath);
        int count = 0;
        File[] files = file.listFiles();
        if (files != null) {
            for (File file1 : files) {
                if (file1.getPath().contains("MYCAM")&&file1.getPath().endsWith(".jpg")) {
                    arrayList.add(new Image(file1.getName(), file1.getPath(), file1.length()));
                    count++;
                }
            }
        }
        if (count==0){
            (findViewById(R.id.nothingToShow)).setVisibility(View.VISIBLE);
        }
        Collections.reverse(arrayList);
        ImageAdapter adapter = new ImageAdapter(GalleryActivity.this,arrayList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, path) -> startActivity(new Intent(GalleryActivity.this, ImageViewerActivity.class).putExtra("image", path)));
    }
}