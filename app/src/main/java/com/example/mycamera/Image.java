// package com.android.myapplication;
package com.example.mycamera;


public class Image {
    String title;
    String path;
    long size;

    public Image(String title, String path, long size) {
        this.title = title;
        this.path = path;
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public long getSize() {
        return size;
    }
}