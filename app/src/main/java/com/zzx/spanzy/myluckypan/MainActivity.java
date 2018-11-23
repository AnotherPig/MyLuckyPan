package com.zzx.spanzy.myluckypan;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private LuckyPan mLuckyPan;
    private ImageView mStartBtn;
    private int LuckyPrize = 0;//选中的奖项


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLuckyPan = findViewById(R.id.id_LuckyPan);
        mStartBtn = findViewById(R.id.id_start_btn);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLuckyPan.isStart()){
                    mLuckyPan.luckyStart(LuckyPrize);
//                    mLuckyPan.luckyStart(0);
                    mStartBtn.setImageResource(R.drawable.stop);
                }else{
                    if (!mLuckyPan.isShouldEnd()){
                        mLuckyPan.luckyEnd();
                        mStartBtn.setImageResource(R.drawable.start);
                    }
                }
            }
        });
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lucky_item,menu);
        title = menu.findItem(R.id.menu_prize);
        return true;
    }
    MenuItem title;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_apple:
                LuckyPrize = 0;
                title.setTitle("apple");
                 return true;
             case R.id.menu_item_banana:
                 LuckyPrize = 1;
                 title.setTitle("banana");
                 return true;
             case R.id.menu_item_grape:
                 LuckyPrize = 2;
                 title.setTitle("grape");
                 return true;
             case R.id.menu_item_orange:
                 LuckyPrize = 3;
                 title.setTitle("orange");
                 return true;
             case R.id.menu_item_strawberry:
                 LuckyPrize = 4;
                 title.setTitle("strawberry");
                 return true;
             case R.id.menu_item_watermelon:
                 LuckyPrize = 5;
                 title.setTitle("watermelon");
                 return true;
             default: return super.onOptionsItemSelected(item);

        }


    }
}
