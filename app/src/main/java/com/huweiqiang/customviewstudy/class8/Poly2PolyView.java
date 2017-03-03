package com.huweiqiang.customviewstudy.class8;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.huweiqiang.customviewstudy.R;

/**
 * Description ${Desc}
 * Author huweiqiang
 * Date 2017/3/2.
 */

public class Poly2PolyView extends View {

    private Bitmap mBitmap;
    private Paint mShadowPaint;
    private Paint mSolidPaint;


    private static final int NUM_OF_POINT = 8;
    /**
     * 图片的折叠后的总宽度
     */
    private int mTranslateDis;

    /**
     * 折叠后的总宽度与原图宽度的比例
     */
    private static float mFactor = 0.8f;
    /**
     * 折叠块的个数
     */
    private int mNumOfFolds = 8;
    private int mFoldWidth;
    /**
     * 折叠时，每块的宽度
     */
    private int mTranslateDisPerFold;


    private Matrix[] mMatrices = new Matrix[mNumOfFolds];
    private float mDepth;


    public Poly2PolyView(Context context) {
        this(context, null);
    }

    public Poly2PolyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Poly2PolyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        initBitmap();
    }

    private void initShadow() {
        Matrix shadowMatrix = new Matrix();
        shadowMatrix.setScale(mTranslateDisPerFold, 1);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mShadowPaint.setStyle(Paint.Style.FILL);
        LinearGradient shadowGradient = new LinearGradient(0, 0, 0.5f, 0, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        shadowGradient.setLocalMatrix(shadowMatrix);
        mShadowPaint.setShader(shadowGradient);
        mShadowPaint.setAlpha((int) (0.9 * 255));

        mSolidPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSolidPaint.setStyle(Paint.Style.FILL);
        int alpha = (int) (255 * mFactor * 0.8f);
        mSolidPaint.setColor(Color.argb(alpha, 0, 0, 0));
    }

    private void initMatrix() {
        for (int i = 0; i < mNumOfFolds; i++) {
            Matrix matrix = new Matrix();

            int srcIndex = i * mFoldWidth;

            float[] src = {
                    srcIndex, 0,
                    mFoldWidth + srcIndex, 0,
                    mFoldWidth + srcIndex, mBitmap.getHeight(),
                    srcIndex, mBitmap.getHeight()
            };

            boolean isEven = i % 2 == 0;
            int dstIndex = i * mTranslateDisPerFold;

            float[] dst = {
                    dstIndex, isEven ? 0 : mDepth,
                    mTranslateDisPerFold + dstIndex, isEven ? mDepth : 0,
                    mTranslateDisPerFold + dstIndex, isEven ? mBitmap.getHeight() - mDepth : mBitmap.getHeight(),
                    dstIndex, isEven ? mBitmap.getHeight() : mBitmap.getHeight() - mDepth
            };

            matrix.setPolyToPoly(src, 0, dst, 0, 4);
            mMatrices[i] = matrix;
        }
    }

    private void initSize() {
        mFoldWidth = mBitmap.getWidth() / mNumOfFolds;
        mTranslateDis = (int) (mBitmap.getWidth() * mFactor);
        mTranslateDisPerFold = mTranslateDis / mNumOfFolds;

        mDepth = (float) Math.sqrt(mFoldWidth * mFoldWidth - mTranslateDisPerFold * mTranslateDisPerFold);
    }

    private void initBitmap() {
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initSize();

        initMatrix();

        initShadow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mNumOfFolds; i++) {
            canvas.save();
            canvas.concat(mMatrices[i]);
            canvas.clipRect(mFoldWidth * i, 0, mFoldWidth * i + mFoldWidth, mBitmap.getHeight());
            canvas.drawBitmap(mBitmap, 0, 0, null);

            canvas.translate(mFoldWidth * i, 0);
            if (i % 2 == 0) {
                canvas.drawRect(0, 0, mFoldWidth, mBitmap.getHeight(), mSolidPaint);
            } else {
                canvas.drawRect(0, 0, mFoldWidth, mBitmap.getHeight(), mShadowPaint);
            }

            canvas.restore();
        }
    }

    float lastX = 0;
    float lastY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float nowX = event.getX();
                float nowY = event.getY();
                if (Math.abs(nowX - lastX) > Math.abs(nowY - lastY) && Math.abs(nowX - lastX) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    if (nowX - lastX > 0) {
                        mTranslateDis = mTranslateDis + nowX - lastX > mBitmap.getWidth() ? mBitmap.getWidth() : (int) (mTranslateDis + nowX - lastX);
                    } else {
                        mTranslateDis = mTranslateDis + nowX - lastX < 0 ? 0 : (int) (mTranslateDis + nowX - lastX);
                    }
                    mFactor = (float) mTranslateDis / (float) mBitmap.getWidth();
                    lastX = nowX;
                    lastY = nowY;
                    Log.d("xxx", "mFactor:" + mFactor + ",mTranslateDis:" + mTranslateDis);
                    requestLayout();
                    invalidate();
                }
                break;
        }
        return true;
    }
}
