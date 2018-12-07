package com.example.ourapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.ourapplication.PhotoClipperUtil.getImageContentUri;

public class MainActivity extends AppCompatActivity {

    //******************************************************************************
    private Context mContext;
    private popWindow myPopWindow;
    private DrawerLayout mainDrawerLayout;
    private List<imageUriSet> mRecmdList= new ArrayList<>();           //系统推荐&历史记录
    private TextView hintText;
    //******************************************************************************



    public static final String HEAD_ICON_DIC = Environment
            .getExternalStorageDirectory()
            + File.separator + "headIcon";//存在sd卡上的headIcon里面
    private File headIconFile = null;// 相册或者拍照保存的文件
    private File headClipFile = null;// 裁剪后的头像
    private String headFileNameStr = "tmp.jpg";//初始化这两个图片,实际不是这两个文件名称
    private String clipFileNameStr = "clipIcon.jpg";
    protected final String TAG = getClass().getSimpleName();
    private Uri pictureUri;//这个是照片的Uri，实际上就是headIconFile的Uri

    //权限相关

    private static final int REQUEST_IMAGE_GET = 0 ;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private final int CLIP_PHOTO_BIG_REQUEST_CODE = 2;
    private final int CLIP_PHOTO_SMALL_REQUEST_CODE = 3;//备用
    private final String IMAGE_TYPE = "image/*";

    public static final int REQUEST_STORAGE = 103;
    
    private byte[] result;
    Uri imageUri;
    ImageView headImg;
    Bitmap bitmap = null;
    private Uri outPutUri;
    private TextView username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent=getIntent();
        String name=intent.getStringExtra("用户名");

        //***************************布局设置****************************************
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_mainwindow);
        setSupportActionBar(toolbar);

        ActionBar actBar = getSupportActionBar();
        if(actBar!=null){
            actBar.setDisplayHomeAsUpEnabled(true);
        }

        mainDrawerLayout = (DrawerLayout)findViewById(R.id.main_drawerlayout);
        NavigationView navView = (NavigationView)findViewById(R.id.nav_view);

        Resources resource=(Resources)getBaseContext().getResources();
        ColorStateList csl=(ColorStateList)resource.getColorStateList(R.color.text_color_navigation);
        navView.setItemTextColor(csl);
        navView.getMenu().findItem(R.id.email_icon).setTitle(intent.getStringExtra("用户名"));
        //navView.setCheckedItem(R.id.email_icon);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){

                    case R.id.history_icon:            //选择查看历史背景
                        mainDrawerLayout.closeDrawers();
                        Intent history_show = new Intent(MainActivity.this,HistoryActivity.class);
                        startActivity(history_show);
                        break;
                    case R.id.bg_icon:                  //选择查看个人背景
                        mainDrawerLayout.closeDrawers();
                        break;
                    default:
                }
                return true;
            }
        });

        initRecommand();
        RecyclerView recommandView = (RecyclerView)findViewById(R.id.recommand);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recommandView.setLayoutManager(layoutManager);

        recmdAdapter recmdAdapter = new recmdAdapter(mContext,recommandView,mRecmdList);                      //recmdAdapter.java
        recommandView.setAdapter(recmdAdapter);


        createFile();       //创建文件夹存放图片

        hintText = (TextView)findViewById(R.id.hint);

        Button picChoose = (Button) findViewById(R.id.addbutton);              //相册选图按钮
        picChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPopWindow = new popWindow(MainActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.this,"choose camera ", Toast.LENGTH_SHORT).show();
                        //选择的是拍照的话，判断有没有权限
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                                        .CAMERA) != PackageManager.PERMISSION_GRANTED
                                ) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission
                                    .CAMERA
                            }, REQUEST_IMAGE_CAPTURE);

                        } else {
                            //权限已经申请了，那么直接拍照
                            imageCapture();
                        }
                        myPopWindow.dismiss();

                    }

                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //权限申请，相册
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                                PackageManager.PERMISSION_GRANTED) {
                            //权限还没有授予，需要在这里申请
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMAGE_GET);
                        } else {
//                          Intent intent = new Intent(Intent.ACTION_PICK);
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            //判断系统中有没有处理这个intent的活动
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(intent, REQUEST_IMAGE_GET);
                            } else {
                                Toast.makeText(MainActivity.this, "没有找到图片查看器",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        myPopWindow.dismiss();
                    }
                });
                backgroundAlpha(0.7f);

                myPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        //popupwindow消失的时候恢复成原来的透明度
                        backgroundAlpha(1f);
                    }
                });
                hintText.setVisibility(View.INVISIBLE);
            }
        });

//        Button photo = (Button)findViewById(R.id.Photo_main);
//        Button picture = (Button)findViewById(R.id.Picture_main);

    }

    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

    public void createFile(){
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasReadStoragePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED
                || hasReadStoragePermission != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission
                    .READ_EXTERNAL_STORAGE
            }, REQUEST_STORAGE);//103
        } else {
            initHeadIconFile();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_STORAGE :
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initHeadIconFile();
                }
                else{
                    Toast.makeText(this,"你拒绝了请求", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_IMAGE_GET :
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    //判断系统中是否有处理该intent的活动
                    if(intent.resolveActivity(getPackageManager())!= null){
                        startActivityForResult(intent,REQUEST_IMAGE_GET);
                    }else{
                        Toast.makeText(MainActivity.this,"未找到图片查看器", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(this,"你拒绝了请求",Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    imageCapture();
                }else{
                    Toast.makeText(this,"你拒绝了请求", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case CLIP_PHOTO_BIG_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this,"请求成功，转向转换界面", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,Activity_camera.class);
//                    intent.putExtra("bitmap",result);
                    intent.putExtra("photoUri",outPutUri.toString());
                    startActivity(intent);
                    //这个时候bitmap里面应该存着图片了，直接传入就可以接收。
                } else {
                    Toast.makeText(this,"请求拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:            //拍照
                if (resultCode == RESULT_OK) {
                    //拍照后返回,调用系统裁剪
                    //Uri.fromFile()得到file://，而getUriFromFile()得到content开头的
                    clipperBigPic(this,pictureUri);
                    //安卓无法识别file://开头的uri
                }
                break;
            case REQUEST_IMAGE_GET:                //相册选图
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        String filePath = "";
                        Uri originalUri = data.getData(); // 获得图片的uri
                        if (originalUri != null) {
                            filePath = PhotoClipperUtil.getPath(this,originalUri);
                           //这里可以用，因为是取相册里的图片，应该封装进了sqlite里面,这里搜索出来的是context开头的
                        }

                        if (filePath != null && filePath.length() > 0) {
//                            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
//                            saveMyBitmap(headIconFile,bitmap);
                            //保存过程，会不会添加到sqlite
//                            clipperBigPic(this,getImageContentUri(this,headIconFile));
                            clipperBigPic(this,originalUri);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

/*
    private void transBitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();//初始化一个流对象
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);//把bitmap100%高质量压缩 到 output对象里
        result = output.toByteArray();//转换成功了  result就是一个bit的资源数组
    }
*/

    private void initHeadIconFile() {
        headIconFile = new File(HEAD_ICON_DIC);
        headClipFile = getTempFile();             //裁剪后的图像
        clipFileNameStr = headClipFile.getName().toString();    //获取文件名

        if(!headIconFile.exists()){
            boolean mkdirs = headIconFile.mkdirs();//如果是第一次，那么创建文件夹，那么创建
        }
        headIconFile = new File(HEAD_ICON_DIC,headFileNameStr);
        headClipFile = new File(HEAD_ICON_DIC,clipFileNameStr);//创建裁剪文件
    }



    private void imageCapture() {
        //拍照
        Intent intent ;
//        File pictureFile = headIconFile;
        //判断当前系统,获取文件headIconFile的Uri，就是原文件的uri
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            pictureUri = FileProvider.getUriForFile(this,
                    "com.example.ourapplication.fileprovider",headIconFile);
        }else{
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(headIconFile);
        }
        //去拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT,pictureUri);//把拍照的照片存在headIconFile里面
        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);

    }


    /**
     * 系统裁剪大的图片
     *
     */

    private void clipperBigPic(Context context, Uri uri) {
        if (null == uri) {
            Log.i(TAG, "Uri不存在");
            return;
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

//            String url = headIconFile.getAbsolutePath();
            //            String url = PhotoClipperUtil.getPath(context, uri);
            //这里不能从uri得到url，原因是getPath这个方法用cursor找的路径，而没有把路径添加到数据库里
            intent.setDataAndType(uri, IMAGE_TYPE);
            //裁剪后图片输出
            outPutUri = getImageContentUri(this,headClipFile);
            //添加上输出的uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
            intent.putExtra("noFaceDetection", false);//去除默认的人脸识别，否则和剪裁框重叠
            //临时授权该Uri所代表的文件的读权限,不加入该flag将导致无法加载图片
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //临时授权该Uri所代表的文件的写权限,不加入该flag将导致无法加载图片
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        }else{
/*            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                String url = PhotoClipperUtil.getPath(this, uri);//这个方法是处理4.4以上图片返回
                // 的Uri对象不同的处理方法
                String url = headIconFile.getAbsolutePath();
                intent.setDataAndType(Uri.fromFile(new File(url)), IMAGE_TYPE);
            } else {*/
            intent.setDataAndType(uri, IMAGE_TYPE);
//            }
            //裁剪后图片输出
            outPutUri = getImageContentUri(this,headClipFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
        }
        //发送裁剪命令
        intent.putExtra("crop", true);
        //X方向上的比例
        intent.putExtra("aspectX", 1);
        //Y方向上的比例
        intent.putExtra("aspectY", 1);
        //裁剪区的宽
        intent.putExtra("outputX", 500);
        //裁剪区的高
        intent.putExtra("outputY", 500);
        //是否保留比例
        intent.putExtra("scale", true);
        //返回数据,可以不用，直接用uri就可以了
        intent.putExtra("return-data", false);
        //输出图片格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        //裁剪图片保存位置
        //启动
        startActivityForResult(intent, CLIP_PHOTO_BIG_REQUEST_CODE);
    }

    private Uri getTempUri() {

        return Uri.fromFile(getTempFile());//这个
    }

    private File getTempFile() {
        String fileName = new Date().getTime()+".jpg";       //生成文件名
        File file = new File(HEAD_ICON_DIC, fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    //***************************************************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                  //设定toolbar的menu
        getMenuInflater().inflate(R.menu.personal_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                     //toolbar点击事件处理
        switch (item.getItemId()) {              //判断点击的哪一个菜单项
            case R.id.home:
                finish();
                break;
            case R.id.personal_info:
                mainDrawerLayout.openDrawer(GravityCompat.END);
                break;
            default:
                break;
        }
        return true;
    }

    private void initRecommand() {

        mRecmdList.clear();
        imageUriSet recommend1 = new imageUriSet(getUriFromDrawableRes(R.drawable.u16));
        recommend1.setImageParam("系统推荐");
        mRecmdList.add(recommend1);
//        Log.i(TAG,"推荐图片uri "+recommend1.getImageUri().toString());

        imageUriSet recommend2 = new imageUriSet(getUriFromDrawableRes(R.drawable.u17));
        recommend2.setImageParam("系统推荐");
        mRecmdList.add(recommend2);

        imageUriSet recommend3 = new imageUriSet(getUriFromDrawableRes(R.drawable.u18));
        recommend3.setImageParam("系统推荐");
        mRecmdList.add(recommend3);

        imageUriSet recommend4 = new imageUriSet(getUriFromDrawableRes(R.drawable.u2));
        recommend4.setImageParam("系统推荐");
        mRecmdList.add(recommend4);

        imageUriSet recommend5 = new imageUriSet(getUriFromDrawableRes(R.drawable.u1));
        recommend5.setImageParam("系统推荐");
        mRecmdList.add(recommend5);

        imageUriSet recommend6 = new imageUriSet(getUriFromDrawableRes(R.drawable.u3));
        recommend6.setImageParam("系统推荐");
        mRecmdList.add(recommend6);

        imageUriSet recommend7 = new imageUriSet(getUriFromDrawableRes(R.drawable.u5));
        recommend7.setImageParam("系统推荐");
        mRecmdList.add(recommend7);

        imageUriSet recommend8 = new imageUriSet(getUriFromDrawableRes(R.drawable.u4));
        recommend8.setImageParam("系统推荐");
        mRecmdList.add(recommend8);

        //可以在首页展示历史记录
    }

    public Uri getUriFromDrawableRes(int id) {
        Resources resources=(Resources)getBaseContext().getResources();
        String path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(id) + "/"
                + resources.getResourceTypeName(id) + "/"
                + resources.getResourceEntryName(id);
        return Uri.parse(path);
    }
    //**************************************************************************************
}