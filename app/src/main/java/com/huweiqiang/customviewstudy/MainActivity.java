package com.huweiqiang.customviewstudy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FoldView mFoldView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* mFoldView = (FoldView) findViewById(R.id.page_turn_view);

        set();*/

        /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
        ImageView viewById = (ImageView) findViewById(R.id.image);
        viewById.setBitmap(bitmap);*/
    }

    private void set() {
        List<Bitmap> bitmaps = new ArrayList<>();
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.beautiful));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.girl));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.img));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.bricks));
        bitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.water));
        mFoldView.setBitmaps(bitmaps);
    }
}
