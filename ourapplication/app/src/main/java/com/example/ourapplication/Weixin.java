package com.example.ourapplication;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.File;


public class Weixin extends AppCompatActivity {

    private Bitmap mSourceBitmap;
    private Bitmap mConvertedBitmap;
    private static final String Description = "test";
    Uri clipPhotoUri;
    private static final String APP_ID ="wx93285cfd2b026fc0";
//    private String path = (String)getIntent().getSerializableExtra("path");
    private static IWXAPI wxApi;
    private static final int ThUMB_SIZE = 150;
//    private OnResponseListener listener;
//    private ResponseReceiver receiver;

    public static void WeiXinRegister(Context context){
        wxApi = WXAPIFactory.createWXAPI(context,APP_ID);
        wxApi.registerApp(APP_ID);
        return;
    }

//    private static final int ThUMB_SIZE = 150;
    public static void image_share(Bitmap bitmap, int sendtype, Context context){




//        File file = new File(imgurl);
//        if(!file.exists()){
//            Toast.makeText(context, "图片不存在",Toast.LENGTH_LONG).show();
//        }
//        WXImageObject imgObj = new WXImageObject();
//        imgObj.setImagePath(imgurl);
        //微信图片对象；


        WXImageObject imgObj = new WXImageObject(bitmap);


        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap,ThUMB_SIZE,ThUMB_SIZE,true);
        thumbBmp.recycle();
//        msg.thumbData = Util.bmpToByteArray(thumbBmp,true);



        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = sendtype==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;
        wxApi.sendReq(req);
        return;
    }
}
