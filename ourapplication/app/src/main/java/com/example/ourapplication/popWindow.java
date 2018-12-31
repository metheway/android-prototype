package com.example.ourapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
/**
 * Created by 10065 on 2018/11/15.
 */


public class popWindow extends PopupWindow {
    //private static final String TAG = "popupWindow";
    private View mView;
    private Context mContext;
    private View.OnClickListener mCameraListener;
    private View.OnClickListener mAlbumListener;


    public popWindow(Activity context, View.OnClickListener cameraListener, View.OnClickListener albumListener) {
        super(context);
        this.mContext = context;
        this.mCameraListener = cameraListener;
        this.mAlbumListener = albumListener;
        Init();
    }

    private void Init(){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        mView = inflater.inflate(R.layout.pop_window, null,false);

        Button btn_Camera = (Button) mView.findViewById(R.id.cameraBtn);
        Button btn_Album = (Button) mView.findViewById(R.id.albumBtn);

        btn_Camera.setOnClickListener(mCameraListener);
        btn_Album.setOnClickListener(mAlbumListener);

        // 导入布局
        this.setContentView(mView);
        // 设置动画效果
        this.setAnimationStyle(R.style.popwindow_anim_style);
//        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
//        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        this.setWidth(700);
        this.setHeight(700);

        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(-00000);
        this.setBackgroundDrawable(dw);
        this.setOutsideTouchable(true);
        this.setTouchable(true);
        this.showAtLocation(mView, Gravity.CENTER, 0, 0);        //here
        //this.showAsDropDown(mView,0,30);


        // 单击弹出窗以外处 关闭弹出窗
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mView.findViewById(R.id.main_drawerlayout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }

        });

    }

}

