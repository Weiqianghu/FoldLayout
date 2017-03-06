package com.huweiqiang.customviewstudy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.huweiqiang.customviewstudy.class8.LeftDrawerLayout;

public class MainActivity extends AppCompatActivity {
    private LeftDrawerLayout dl;
    private ListView lv;
    private TextView tv_noimg;
    private ImageView iv_icon, iv_bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDragLayout();
        initView();

    }

    private void initDragLayout() {
        dl = (LeftDrawerLayout) findViewById(R.id.dl);
        dl.setDragListener(new LeftDrawerLayout.DragListener() {
            //界面打开的时候
            @Override
            public void onOpen() {
            }

            //界面关闭的时候
            @Override
            public void onClose() {
            }

            //界面滑动的时候
            @Override
            public void onDrag(float percent) {
//                ViewHelper.setAlpha(iv_icon, 1 - percent);
            }
        });
    }

    private void initView() {
       /* iv_icon = (ImageView) findViewById(R.id.iv_icon);
        iv_bottom = (ImageView) findViewById(R.id.iv_bottom);
        tv_noimg = (TextView) findViewById(R.id.iv_noimg);

        lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                R.layout.item_text, new String[]{"item 01", "item 01",
                "item01", "item 01", "item 01", "item 01",
                "item01", "item 01", "item 01", "item 01",
                "item01", "item 01", "item 01", "item 01",
                "item01", "item 01", "item 01",
                "item01", "item 01", "item 01", "item 01"}));
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    intposition, long arg3) {
                Toast.makeText(MainActivity.this, "ClickItem " + position, Toast.LENGTH_SHORT).show();
            }
        });
        iv_icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dl.open();
            }
        });*/
    }
}
