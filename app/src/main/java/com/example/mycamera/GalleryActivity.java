// package com.android.myapplication;
package com.example.mycamera;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class GalleryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<File> arrayList = new ArrayList<File>();
    ImageAdapter adapter = new ImageAdapter(GalleryActivity.this,arrayList);
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

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            switch (direction){
                 case ItemTouchHelper.LEFT:
                     MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(GalleryActivity.this);
                     alertDialogBuilder.setMessage("Are you sure you want to delete this image ?");
                     alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             String[] projection = new String[]{MediaStore.Images.Media._ID};
                             String selection = MediaStore.Images.Media.DATA + " = ?";
                             String finalPath = arrayList.get(pos).getPath();
                             String[] selectionArgs = new String[]{new File(finalPath).getAbsolutePath()};
                             Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                             ContentResolver contentResolver = getContentResolver();
                             Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
                             if (cursor.moveToFirst()) {
                                 long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                                 Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                                 try {
                                     contentResolver.delete(deleteUri, null, null);
                                     boolean delete1 = new File(finalPath).delete();
                                     Log.e("TAG", delete1 + "");
                                     adapter.notifyItemRemoved(pos);
                                     Toast.makeText(GalleryActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                 } catch (Exception e) {
                                     e.printStackTrace();
                                     Toast.makeText(GalleryActivity.this, "Deleting Error", Toast.LENGTH_SHORT).show();
                                 }
                             } else {
                                 Toast.makeText(GalleryActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
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


                     break;
                 case ItemTouchHelper.RIGHT:
                     break;
             }
        }
    };

    private void deletePhoto(String path) throws IOException {

    }

    @Override
    protected void onResume() {
        super.onResume();
        getImages();
    }

    private void getImages(){
        arrayList.clear();
//        String filePath = Environment.getExternalStorageDirectory()+"/DCIM/Camera/";
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("rootPath");
        File file = new File(filePath);
        int count = 0;
        File[] files = file.listFiles();
        if (files != null) {
            for (File file1 : files) {
                if (file1.getPath().endsWith(".jpg")||file1.isDirectory()) {
                    arrayList.add(file1);
                    count++;
                }
            }
        }
        if (count==0){
            (findViewById(R.id.nothingToShow)).setVisibility(View.VISIBLE);
        }
        Collections.reverse(arrayList);

        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((view, path) -> startActivity(new Intent(GalleryActivity.this, ImageViewerActivity.class).putExtra("image",path)));
    }
}