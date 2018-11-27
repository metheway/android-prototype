package com.example.ourapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Activity_camera extends AppCompatActivity {

    //*************************************************
    private List<bgSet> bgList= new ArrayList<>();
    private Toolbar toolbar;
    private bgAdapter myBgAdapter;
    private Button reChooseButton;
    private Button transformButton;
    private Button bgButton;
    private Button yesButton;
    private Button noButton;
    private saveWindow mSaveWindow;
    //*************************************************



    private ImageView picture;
    private Uri imageUri;
    //设置素描的常量请求

    private Bitmap mSourceBitmap;
    private Bitmap mConvertedBitmap;
    Uri clipPhotoUri;



    private static final int radius = 10;
    private static final int TYPE_CONVERT = 3;
    private ProgressDialog mDialog;


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

        myBgAdapter = new bgAdapter(bgList);
        bgRecView.setAdapter(myBgAdapter);
        bgRecView.setVisibility(View.INVISIBLE);
        myBgAdapter.setOnItemClickListener(new bgAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {          //背景图片点击事件
                //Toast.makeText(Activity_camera.this, position+" "+layoutManager.getItemCount(),Toast.LENGTH_SHORT).show();
                //View view = layoutManager.findViewByPosition(position);
                //Toast.makeText(Activity_camera.this,backgroundChoosed.getName(), Toast.LENGTH_SHORT).show();
                bgSet bgchoosed = bgList.get(position);
                TextView text_bg = (TextView)findViewById(R.id.text_bg);
                text_bg.setText(bgchoosed.getName());

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
                        Toast.makeText(Activity_camera.this, "朋友圈", Toast.LENGTH_SHORT).show();

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
        bgSet bg1 = new bgSet("bg1", R.drawable.bg1);
        bgList.add(bg1);
        bgSet bg2 = new bgSet("bg2", R.drawable.bg2);
        bgList.add(bg2);
        bgSet bg3 = new bgSet("bg3", R.drawable.bg3);
        bgList.add(bg3);
        bgSet bg4 = new bgSet("bg4", R.drawable.bg4);
        bgList.add(bg4);
        bgSet bg5 = new bgSet("bg5", R.drawable.bg5);
        bgList.add(bg5);
        bgSet bg6 = new bgSet("bg6", R.drawable.bg6);
        bgList.add(bg6);
        bgSet bg7 = new bgSet("bg7", R.drawable.bg7);
        bgList.add(bg7);
        bgSet bg8 = new bgSet("bg8", R.drawable.bg8);
        bgList.add(bg8);
        bgSet bg9 = new bgSet("bg9", R.drawable.bg9);
        bgList.add(bg9);
    }

}