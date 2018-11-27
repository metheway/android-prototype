package com.example.ourapplication;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private List<picSet> myHisList= new ArrayList<>();           //系统推荐&历史记录


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar history_show_toolbar = (Toolbar)findViewById(R.id.toolbar_history);
        setSupportActionBar(history_show_toolbar);

        ActionBar picActBar = getSupportActionBar();
        if(picActBar!=null){
            picActBar.setDisplayHomeAsUpEnabled(true);
            picActBar.setHomeButtonEnabled(true);
            picActBar.setTitle("历史记录");
        }

        initHistoryShow();
        RecyclerView hisView = (RecyclerView)findViewById(R.id.historyshow);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        hisView.setLayoutManager(layoutManager);

        picViewAdapter hisAdapter = new picViewAdapter(myHisList);
        hisView.setAdapter(hisAdapter);


    }

    private void initHistoryShow() {

        //获取历史记录

        if(myHisList==null){              //历史记录为空，获取几张推荐图片显示在首页

        }
        picSet recommend1 = new picSet(R.drawable.u16,"系统推荐");
        myHisList.add(recommend1);

        picSet recommend2 = new picSet(R.drawable.u17,"系统推荐");
        myHisList.add(recommend2);

        picSet recommend3 = new picSet(R.drawable.u18,"系统推荐");
        myHisList.add(recommend3);

        picSet recommend4 = new picSet(R.drawable.u2,"系统推荐");
        myHisList.add(recommend4);

        picSet recommend5 = new picSet(R.drawable.u1,"系统推荐");
        myHisList.add(recommend5);

        picSet recommend6 = new picSet(R.drawable.u3,"系统推荐");
        myHisList.add(recommend6);

        picSet recommend7 = new picSet(R.drawable.u5,"系统推荐");
        myHisList.add(recommend7);

        picSet recommend8 = new picSet(R.drawable.u4,"系统推荐");
        myHisList.add(recommend8);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }
}
