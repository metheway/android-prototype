package com.example.ourapplication;

/**
 * Created by 10065 on 2018/11/25.
 */

public class picSet {
    private int imageUri;
    private String date;

    public picSet(int imageUri, String date){
        this.imageUri = imageUri;
        this.date = date;
    }

    public int getImageUri(){
        return imageUri;
    }

    public String getDate(){
        return date;
    }
}
