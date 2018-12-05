package com.example.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetService {
    public static InputStream getInputStreamByUrl(String address){
        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(address);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(2 * 1000);
            urlConnection.setRequestMethod("GET");
            return urlConnection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
