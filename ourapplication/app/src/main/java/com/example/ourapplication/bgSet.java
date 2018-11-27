package com.example.ourapplication;

/**
 * Created by 10065 on 2018/11/15.
 */

public class bgSet {
    private String name;
    private int imageId;

    public bgSet(String name, int imageId){
        this.imageId = imageId;
        this.name = name;
    }

    public String getName(){
        return name;
    }
    public int getId(){
        return imageId;
    }
}
