package com.example.ourapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.example.upload.NetworkTask;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.example.ourapplication.ResultActivity;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

//>>>>>>> Stashed changes
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.ourapplication.Weixin;

import static com.example.ourapplication.MainActivity.CLIP_ICON_DIC;

public class Activity_camera extends AppCompatActivity implements View.OnTouchListener{
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

    ///////////////////////////////////////////////
    private Matrix matrix = new Matrix();

    private Matrix savedMatrix = new Matrix();
    // ???????????
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;     // ?????????????????????????????????????????????
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private float oriDis = 1f;
    /////////////////////////////////////////////// //\\//

    private ImageView picture;
    private Uri imageUri;
    private File clipFile;
    //????????????????

    private Bitmap mSourceBitmap;
    private Bitmap mConvertedBitmap;
    Uri clipPhotoUri;

    private static final int radius = 30;
    private static final int TYPE_CONVERT = 3;
    private ProgressDialog mDialog;
    private static final int THUMB_SIZE = 150;
    private String APP_ID ="wx93285cfd2b026fc0";
    private IWXAPI wxApi = WXAPIFactory.createWXAPI(Activity_camera.this,APP_ID); //
    private Context cContext;
    private final Bitmap[] originalBitmap = {null};
    private final Bitmap[] backgroundBitmap = {null};

    public static String imagePath ;

    public int historyNum = 0;
    public int currentNum = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cContext = this;
        //******************************????????***************************************************

        toolbar = (Toolbar)findViewById(R.id.toolbar_main2activity);
        setSupportActionBar(toolbar);

        ActionBar actBar = getSupportActionBar();
        if(actBar!=null){
            actBar.setDisplayHomeAsUpEnabled(true);
        }
        Log.i("camera","ddddddddddd");
        initBackground();     //???????????

        final RecyclerView bgRecView = (RecyclerView)findViewById(R.id.bgshow);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bgRecView.setLayoutManager(layoutManager);

        mBgAdapter = new bgAdapter(mBgList);
        bgRecView.setAdapter(mBgAdapter);
        bgRecView.setVisibility(View.INVISIBLE);

        mBgAdapter.setOnItemClickListener(new bgAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {          //????????????
                //Toast.makeText(Activity_camera.this, position+" "+layoutManager.getItemCount(),Toast.LENGTH_SHORT).show();
                //View view = layoutManager.findViewByPosition(position);
                //Toast.makeText(Activity_camera.this,backgroundChoosed.getName(), Toast.LENGTH_SHORT).show();
                imageUriSet bgChoosed = mBgList.get(position);
                TextView text_bg = (TextView)findViewById(R.id.text_bg);
                text_bg.setText(bgChoosed.getImageParam());
                //picture.setImageResource(bgchoosed.getImageId());
                //选取了背景色
/*                Bitmap tmpBitmap = BitmapFactory.decodeFile(PhotoClipperUtil.getPath(Activity_camera.this
                        ,bgChoosed.getImageUri()));*/

                try {
                    originalBitmap[0] = MediaStore.Images.Media.getBitmap(cContext.getContentResolver(),
                            bgChoosed.getImageUri());
                    backgroundBitmap[0] = originalBitmap[0].copy(Bitmap.Config.ARGB_4444,true);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });


        final RelativeLayout bgshowControl = (RelativeLayout)findViewById(R.id.bgshowcontrol);
        bgshowControl.setVisibility(View.INVISIBLE);

        bgButton = (Button) findViewById(R.id.bgbutton);               //??????????
        bgButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                bgRecView.setVisibility(View.VISIBLE);
                bgshowControl.setVisibility(View.VISIBLE);

            }
        });

        reChooseButton = (Button) findViewById(R.id.chbutton);           //????????????
        reChooseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent camera_back = new Intent(Activity_camera.this,MainActivity.class);
                startActivity(camera_back);
                finish();
            }
        });

        transformButton = (Button) findViewById(R.id.transformbutton);      //???????????
        transformButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new ConvertTask().execute(new Integer[] { TYPE_CONVERT, radius });

            }
        });

        yesButton = (Button)findViewById(R.id.yesbut);    //?????????
        yesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                bgRecView.setVisibility(View.INVISIBLE);
                bgshowControl.setVisibility(View.INVISIBLE);
                Toast.makeText(Activity_camera.this, "成功放入背景", Toast.LENGTH_SHORT).show();

                float dx = 0;
//                float dy = (backgroundBitmap[0].getHeight() - drawBitmap.getHeight()) / 2;
                float dy = 0;
                Matrix matrix = new Matrix();
                float finalWidth = 500f;
                float finalHeight = 500f;

                Bitmap bitmap = Bitmap.createBitmap(500,500, Bitmap.Config.ARGB_4444);
                Canvas canvas = new Canvas(bitmap);
                Matrix matrix1 = new Matrix();
                matrix1.setScale(finalWidth / backgroundBitmap[0].getWidth(),
                        finalHeight / backgroundBitmap[0].getHeight());
                Paint paint1 = new Paint();
                canvas.drawBitmap(backgroundBitmap[0],matrix1,paint1);

                BitmapDrawable drawable = (BitmapDrawable) picture.getDrawable();
                Bitmap drawBitmap = drawable.getBitmap();
                matrix.setTranslate(dx,dy);
                matrix.setScale(finalWidth / drawBitmap.getWidth(),finalHeight / drawBitmap.getHeight());
                Paint paint = new Paint();
                paint.setAlpha(150);
                canvas.drawBitmap(drawBitmap,matrix,paint);

                picture.setImageBitmap(bitmap);

            }
        });


        noButton = (Button)findViewById(R.id.nobut);    //?????????
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
        imagePath = PhotoClipperUtil.getPath(cContext,clipPhotoUri);
        clipFile = new File(imagePath);
        if(!clipFile.exists()){
            try {
                clipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        picture = (ImageView) findViewById(R.id.picture);
        //find the ImageView找到视图显示
        //???????????????bitmap????????????ImageView????
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(PhotoClipperUtil.getPath(this,clipPhotoUri));
        //??????????????????????sqlite??????????????
        picture.setImageBitmap(bitmap);

    }



    private class ConvertTask extends AsyncTask<Integer, Void, Bitmap> {
        @Override
        protected void onPostExecute(Bitmap result) {
            mDialog.dismiss();
            if (result != null) {
                mConvertedBitmap = result.copy(Bitmap.Config.ARGB_8888,true);
                picture.setImageBitmap(mConvertedBitmap);
                picture.setOnTouchListener(Activity_camera.this);
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


//***********************************************************************************************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }
    Bitmap finalBitmap ;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.save:           //??????
                BitmapDrawable drawable = (BitmapDrawable) picture.getDrawable();

                finalBitmap = drawable.getBitmap();
                PhotoClipperUtil.saveMyBitmap(clipFile,finalBitmap);
                Toast.makeText(Activity_camera.this, "保存成功了，感谢使用！", Toast.LENGTH_SHORT).show();

                mSaveWindow = new saveWindow(Activity_camera.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Weixin.WeiXinRegister(Activity_camera.this);
                        Weixin.image_share(finalBitmap, 0,Activity_camera.this);
                        mSaveWindow.dismiss();            //?????

                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSaveWindow.dismiss();            //?????
                    }
                });
                backgroundAlpha(0.7f);

                mSaveWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {


                        //popupwindow??????????????????????
                        backgroundAlpha(1f);
                        uploadImage(PhotoClipperUtil.getPath(cContext,clipPhotoUri));//自动上传图片
                        Toast.makeText(Activity_camera.this,"上传成功",Toast.LENGTH_SHORT).show();
                        /*SharedPreferences sharedPreferences = getSharedPreferences("history",
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor mEditor = sharedPreferences.edit();
                        historyNum = sharedPreferences.getInt("historyNum",0);
                        currentNum = sharedPreferences.getInt("currentNum",0);
                        if(historyNum == 0){
                            mEditor.putInt("historyNum",1);
                            currentNum = 1;//如果是第一次上传，那么把历史图片和当前图片都改成1的数量就好了
                        }else{
                            if(historyNum >=6){
                                currentNum = (currentNum + 1) % 7;
                                if(currentNum == 0){
                                    currentNum = 1;
                                }
                            }else{
                                historyNum++;
                                currentNum++;
                            }
                        }
                        mEditor.putInt("historyNum",historyNum);
                        mEditor.putInt("currentNum",currentNum);
                        mEditor.commit();*/
/*                        SQLite db = new SQLite(Activity_camera.this,"nums",null,1);
                        SQLiteDatabase database = db.getWritableDatabase();
                        if(!db.exists("history")){
                            //如果创建了表格，那么读取，如果没有创建，那么创建表，插入初始值0
                            database.execSQL("CREATE TABLE history ( '_history' integer primary key , '_current' integer)");
                            ContentValues values =new ContentValues();
                            values.put("_history",historyNum);
                            values.put("_current",currentNum);
                            database.insert("history",null,values);
                        }else{//如果db里面是存在表格的，读取放到historyNum和currentNum里面就好了
                            Cursor cursor = database.query("history",null,null,
                                    null,null,null,null);
                            if(cursor.moveToFirst()){
                                do{
                                    historyNum = cursor.getInt(cursor.getColumnIndex("_history"));
                                    currentNum = cursor.getInt(cursor.getColumnIndex("_current"));
                                    Log.i("camera",historyNum + "abccccccccc");
                                    Log.i("camera",currentNum + "abccccccccc");
                                }while(cursor.moveToNext());
                            }
                            cursor.close();
                        }

                        //取出文件，看已经保存了多少个文件
                        if(historyNum == 5 ){
                            if(currentNum == 5){
                                currentNum = 0;
                            }else{
                                currentNum ++;
                            }
                        }else{
                            historyNum = (historyNum + 1) % 6;
                            currentNum ++;
                        }
                        //保存num数到数据库里
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("_history",historyNum);
                        contentValues.put("_current",currentNum);
                        database.update("history",contentValues,null,null);

                        File finalFile = new File(CLIP_ICON_DIC , "history" + currentNum + ".png");

                        if(!finalFile.exists()){
                            try {
                                finalFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }//创建历史图片的文件，用来上传*/
                        /*for(int i = 0 ;i < historyNum ;i++){
                            File historyFile = new File(CLIP_ICON_DIC,"history" +
                            String.valueOf(i + 1) + ".png");
                            if(!historyFile.exists()){
                                try {
                                    historyFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            //保存图片
                        }
                        File currentFile = new File(CLIP_ICON_DIC,"history" +
                            currentNum + ".png");
                        PhotoClipperUtil.saveMyBitmap(currentFile,finalBitmap);
*/
                    }
                });
                break;
            default:
        }
        return true;
    }

    private void uploadImage(String path) {
        new NetworkTask().execute(path);
    }

    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

    private void initBackground() {                             //???????????

        mBgList.clear();
        imageUriSet bg1 = new imageUriSet(getUriFromDrawableRes(R.drawable.bg1));
        bg1.setImageParam("bg1");
        Log.i("camera",bg1.getImageUri().toString());
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
    
    ///////////////////////////////////////////////
    // ?????????????????????
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Float.valueOf(String.valueOf(Math.sqrt(x * x + y * y)));
    }

    // ???????????????????
    private PointF middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }

    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //???
            case MotionEvent.ACTION_DOWN:
                matrix.set(view.getImageMatrix());
                savedMatrix.set(matrix);
                startPoint.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            //??
            case MotionEvent.ACTION_POINTER_DOWN:
                oriDis = distance(event);
                if (oriDis > 10f) {
                    savedMatrix.set(matrix);
                    midPoint = middle(event);
                    mode = ZOOM;
                }
                break;            // ??????
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;            // ??????????
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    //???????????
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
                } else if (mode == ZOOM) {
                    //???????????
                    float newDist = distance(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oriDis;
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    }

                }
                break;
        }
        // ????ImageView??Matrix
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
//}
// >>>>>>> master
