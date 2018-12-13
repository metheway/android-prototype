package com.example.download;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class NetServiceTask extends AsyncTask<String, Void, Bitmap> implements Runnable {
    private String address;
    private URLPostHandler urlPostHandler = null;

    public NetServiceTask(String address, URLPostHandler urlPostHandler) {
        this.address = address;
        this.urlPostHandler = urlPostHandler;
    }

    /**
     * 表示任务执行之前的操作
     */
    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
    }

    /**
     * 主要是完成耗时的操作
     */
    @Override
    protected Bitmap doInBackground(String... arg0) {
        InputStream inputStream = NetService.getInputStreamByUrl(arg0[0]);
        if (inputStream != null) {
            return BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
        }
        return null;
    }

    /**
     * 主要是更新UI的操作
     */
    @Override
    protected void onPostExecute(Bitmap result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        if (this.urlPostHandler != null && result != null) {
            this.urlPostHandler.PostHandler(result);
        }
    }

    @Override
    public void run() {
        execute(this.address);
    }
}