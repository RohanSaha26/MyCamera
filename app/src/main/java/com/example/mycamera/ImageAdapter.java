// package com.android.myapplication;
package com.example.mycamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    ArrayList<File> arrayList;
    Context context;
    OnItemClickListener onItemClickListener;

    public ImageAdapter(Context context, ArrayList<File> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        String title = arrayList.get(position).getName();
        String path = arrayList.get(position).getPath();

        if(arrayList.get(position).isDirectory()){
            holder.title.setText(title);
            holder.size.setText("Folder");
            holder.imageView.setImageResource(R.drawable.folder);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,GalleryActivity.class);
                   String rootPath = arrayList.get(position).getPath();
                   intent.putExtra("rootPath",rootPath);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   context.startActivity(intent);
                }
            });
        }else {
            holder.title.setText(title);
            holder.size.setText(getSize(arrayList.get(position).length()));
            Glide.with(context).load(path).placeholder(R.drawable.ic_baseline_broken_image_24).into(holder.imageView);
            holder.itemView.setOnClickListener(v -> onItemClickListener.onClick(v, arrayList.get(position).getPath()));
        }

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent("com.google.android.apps.photos");
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse(arrayList.get(position).getPath()), "image/jpg");
//                context.startActivity(intent);
////                context.startActivity(Intent.createChooser(intent, "Open image using"));
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static String getSize(long size) {
        if (size <= 0) {
            return "0";
        }

        double d = (double) size;
        int log10 = (int) (Math.log10(d) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double power = Math.pow(1024.0d, log10);
        stringBuilder.append(decimalFormat.format(d/power));
        stringBuilder.append(" ");
        stringBuilder.append(new String[] {"B", "KB", "MB", "GB", "TB"}[log10]);
        return stringBuilder.toString();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title, size;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item_image);
            title = itemView.findViewById(R.id.list_item_title);
            size = itemView.findViewById(R.id.list_item_size);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(View view, String path);
    }
}