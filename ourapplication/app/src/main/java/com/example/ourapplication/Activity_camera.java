package com.example.ourapplication;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//<<<<<<< Updated upstream
//=======
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.example.ourapplication.ResultActivity;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

//>>>>>>> Stashed changes
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.ourapplication.Weixin;

public class Activity_camera extends AppCompatActivity {
//>>>>>>> Stashed changes

    //*************************************************
    private List<imageUriSet> mBgList= new ArrayList<>();
    private Toolbar toolbar;
    private bgAdapter mBgAdapter;
    private Button reChooseButton;
    private Button transformButton;
    private Button bgButton;
    private Button yesButton;
    private Button noButton;
    private saveWindow mSaveWindow;
    //*************************************************

    /////////////////////////////////////////////// //\\//
    private Matrix matrix = new Matrix();

    private Matrix savedMatrix = new Matrix();
    // 不同状态的表示：
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;     // 定义第一个按下的点，两只接触点的重点，以及出事的两指按下的距离：
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private float oriDis = 1f;
    /////////////////////////////////////////////// //\\//

    private ImageView picture;
    private Uri imageUri;
    private File clipFile;
    //设置素描的常量请求

    private Bitmap mSourceBitmap;
    private Bitmap mConvertedBitmap;
    Uri clipPhotoUri;

    private static final int radius = 10;
    private static final int TYPE_CONVERT = 3;
    private ProgressDialog mDialog;
    private static final int ThUMB_SIZE = 150;
    private String APP_ID ="wx93285cfd2b026fc0";
    private IWXAPI wxApi = WXAPIFactory.createWXAPI(Activity_camera.this,APP_ID); //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);




        //******************************布局设置***************************************************

        toolbar = (Toolbar)findViewById(R.id.toolbar_main2activity);
        setSupportActionBar(toolbar);

        ActionBar actBar = getSupportActionBar();
        if(actBar!=null){
            actBar.setDisplayHomeAsUpEnabled(true);
        }

        initBackground();     //背景图片初始化

        final RecyclerView bgRecView = (RecyclerView)findViewById(R.id.bgshow);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bgRecView.setLayoutManager(layoutManager);

        mBgAdapter = new bgAdapter(mBgList);
        bgRecView.setAdapter(mBgAdapter);
        bgRecView.setVisibility(View.INVISIBLE);
        mBgAdapter.setOnItemClickListener(new bgAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {          //背景图片点击事件
                //Toast.makeText(Activity_camera.this, position+" "+layoutManager.getItemCount(),Toast.LENGTH_SHORT).show();
                //View view = layoutManager.findViewByPosition(position);
                //Toast.makeText(Activity_camera.this,backgroundChoosed.getName(), Toast.LENGTH_SHORT).show();
                imageUriSet bgChoosed = mBgList.get(position);
                TextView text_bg = (TextView)findViewById(R.id.text_bg);
                text_bg.setText(bgChoosed.getImageParam());
                //picture.setImageResource(bgchoosed.getImageId());

            }
        });


        final RelativeLayout bgshowControl = (RelativeLayout)findViewById(R.id.bgshowcontrol);
        bgshowControl.setVisibility(View.INVISIBLE);

        bgButton = (Button) findViewById(R.id.bgbutton);               //背景按钮点击
        bgButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                bgRecView.setVisibility(View.VISIBLE);
                bgshowControl.setVisibility(View.VISIBLE);

            }
        });

        reChooseButton = (Button) findViewById(R.id.chbutton);           //重新选图按钮点击
        reChooseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent camera_back = new Intent(Activity_camera.this,MainActivity.class);
                startActivity(camera_back);
                finish();
            }
        });

        transformButton = (Button) findViewById(R.id.transformbutton);      //合成图片按钮点击
        transformButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new ConvertTask().execute(new Integer[] { TYPE_CONVERT, radius });

            }
        });

        yesButton = (Button)findViewById(R.id.yesbut);    //背景选取确定
        yesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                bgRecView.setVisibility(View.INVISIBLE);
                bgshowControl.setVisibility(View.INVISIBLE);
                Toast.makeText(Activity_camera.this, "背景选择成功", Toast.LENGTH_SHORT).show();

            }
        });


        noButton = (Button)findViewById(R.id.nobut);    //背景选取关闭
        noButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                bgRecView.setVisibility(View.INVISIBLE);
                bgshowControl.setVisibility(View.INVISIBLE);
            }
        });

//        Button camera_next = (Button)findViewById(R.id.camera_next);
//        camera_next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent cemera_next = new Intent(Activity_camera.this,ResultActivity.class);
//                startActivity(cemera_next);
//                finish();
//            }
//        });
        //*********************************************************************************

        clipPhotoUri = Uri.parse(getIntent().getStringExtra("photoUri"));

        clipFile = getFileStreamPath(PhotoClipperUtil.getPath(this,clipPhotoUri));
        picture = (ImageView) findViewById(R.id.picture);
        //find the ImageView
        //在创建的时候就可以把bitmap数据取出来放到ImageView上面
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(PhotoClipperUtil.getPath(this,clipPhotoUri));
        //这里用的游标找到的，说明在sqlite里面有记录，注意
        picture.setImageBitmap(bitmap);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
/*            case Take_photo:
//              if the intent is take_photo
                if (resultCode == RESULT_OK) {
//              if the resultCode is ok then
                    try {
                        Toast.makeText(MyApplication.getContext(), "保存成功", Toast.LENGTH_SHORT).show();
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().
                                openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                        //显示保存成功，而且用BitmapFactory解析imageUri，也就是意图里面的imageUri
                        //外界的程序可以通过getContentResolver()访问，这里暴露了接口，要和getContentProvider里面的接口相对应
                        //从imageUri取出数据流，用BitmapFactory转换成bitmap，然后放在视图picture里面
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;*/
        }
    }

    private class ConvertTask extends AsyncTask<Integer, Void, Bitmap> {
        @Override
        protected void onPostExecute(Bitmap result) {
            mDialog.dismiss();
            if (result != null) {
                mConvertedBitmap = result;
                picture.setImageBitmap(result);
            }

        }

        @Override
        protected void onPreExecute() {
            if (mDialog == null) {
                mDialog = new ProgressDialog(Activity_camera.this);
            }
            mDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            int type = params[0];
            int r = params[1];
            if (mSourceBitmap == null) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) picture
                        .getDrawable();
                mSourceBitmap = bitmapDrawable.getBitmap();
            } else if (mConvertedBitmap != null) {
                mConvertedBitmap.recycle();
                mConvertedBitmap = null;
            }

            Bitmap result = null;
            switch (type) {
                case TYPE_CONVERT:
                    result = testSketch.testGaussBlur(mSourceBitmap, r, r / 3);
                    break;
            }

            return result;
        }

    }
/*    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTransformedBitmap != null) ;
        mTransformedBitmap.recycle();
        mTransformedBitmap = null;
        //如果转换的 Bitmap还占用内存，清空
    }*/


//***********************************************************************************************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.save:           //保存图片
                mSaveWindow = new saveWindow(Activity_camera.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PhotoClipperUtil.saveMyBitmap(clipFile,mConvertedBitmap);
                        Toast.makeText(Activity_camera.this, "朋友圈", Toast.LENGTH_SHORT).show();
                        Weixin.WeiXinRegister(Activity_camera.this);
                        Weixin.image_share(mConvertedBitmap, 0,Activity_camera.this);
                        mSaveWindow.dismiss();            //关闭按钮

                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSaveWindow.dismiss();            //关闭按钮
                    }
                });
                backgroundAlpha(0.7f);

                mSaveWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        //popupwindow消失的时候恢复成原来的透明度
                        backgroundAlpha(1f);
                    }
                });


                break;
            default:
        }
        return true;
    }

    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

    private void initBackground() {                             //初始化背景图片

        mBgList.clear();
        imageUriSet bg1 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg1));
        bg1.setImageParam("bg1");
        mBgList.add(bg1);
        imageUriSet bg2 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg2));
        bg2.setImageParam("bg2");
        mBgList.add(bg2);
        imageUriSet bg3 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg3));
        bg3.setImageParam("bg3");
        mBgList.add(bg3);
        imageUriSet bg4 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg4));
        bg4.setImageParam("bg4");
        mBgList.add(bg4);
        imageUriSet bg5 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg5));
        bg5.setImageParam("bg5");
        mBgList.add(bg5);
        imageUriSet bg6 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg6));
        bg6.setImageParam("bg6");
        mBgList.add(bg6);
        imageUriSet bg7 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg7));
        bg7.setImageParam("bg7");
        mBgList.add(bg7);
        imageUriSet bg8 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg8));
        bg8.setImageParam("bg8");
        mBgList.add(bg8);
        imageUriSet bg9 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg9));
        bg9.setImageParam("bg9");
        mBgList.add(bg9);
    }
    
    /////////////////////////////////////////////// //\\//
    // 计算两个触摸点之间的距离
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Float.valueOf(String.valueOf(Math.sqrt(x * x + y * y)));
    }

    // 计算两个触摸点的中点
    private PointF middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }

    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //单指
            case MotionEvent.ACTION_DOWN:
                matrix.set(view.getImageMatrix());
                savedMatrix.set(matrix);
                startPoint.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            //双指
            case MotionEvent.ACTION_POINTER_DOWN:
                oriDis = distance(event);
                if (oriDis > 10f) {
                    savedMatrix.set(matrix);
                    midPoint = middle(event);
                    mode = ZOOM;
                }
                break;            // 手指放开
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;            // 单指滑动事件
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    //是一个手指拖动
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
                } else if (mode == ZOOM) {
                    //两个手指滑动
                    float newDist = distance(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oriDis;
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    }

                }
                break;
        }
        // 设置ImageView的Matrix
        view.setImageMatrix(matrix);
        return true;
    }

// <<<<<<< master
    public Uri getUriFromDrawableRes(int id) {
        Resources resources = (Resources)getBaseContext().getResources();
        String path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(id) + "/"
                + resources.getResourceTypeName(id) + "/"
                + resources.getResourceEntryName(id);
        return Uri.parse(path);
    }
}
// =======
    /////////////////////////////////////////////// //\\//
}
// >>>>>>> master
