package com.example.upload;

import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.ourapplication.Activity_camera;
import com.example.ourapplication.LoginActivity;

import java.io.File;

import static android.support.constraint.Constraints.TAG;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class NetworkTask extends AsyncTask<String,Integer,String>{
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        return doPost(params[0]);
    }

    private String doPost(String param) {
        OkHttpClient mOkHttpClient = new OkHttpClient();

        String result = "error";
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID,把用户名传进去
        String userName = LoginActivity.username;//这里实际为登陆的用户名值，根据用户名在服务器上创立个人文件夹
        String imagePath = Activity_camera.imagePath;
        builder.addFormDataPart("userId", userName);
        builder.addFormDataPart("image", imagePath,
                RequestBody.create(MediaType.parse("image/jpeg"), new File(imagePath)));

        RequestBody requestBody = builder.build();
        Request.Builder reqBuilder = new Request.Builder();
        Request request = reqBuilder
                .url(Constant.BASE_URL + "/UploadServlet")
                .post(requestBody)
                .build();

        Log.d(TAG, "请求地址 " + Constant.BASE_URL + "/UploadServlet");
        try{
            Response response = mOkHttpClient.newCall(request).execute();
            Log.d(TAG, "响应码 " + response.code());
            if (response.isSuccessful()) {
                String resultValue = response.body().string();
                Log.d(TAG, "响应体 " + resultValue);
                return resultValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    protected void onPostExecute(String result) {
        if(!"error".equals(result)) {
            Log.i(TAG, "图片地址 " + Constant.BASE_URL + result);
//            Glide.with(mContext)
//            Glide.with(new MainActivity())//从哪一个上下文传
//                    .load(Constant.BASE_URL + result)
//                    .into(mPictureIv);
        }
    }
}
