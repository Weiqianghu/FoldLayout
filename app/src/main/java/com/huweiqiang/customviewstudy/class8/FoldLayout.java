package com.huweiqiang.customviewstudy.class8;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Description ${Desc}
 * Author huweiqiang
 * Date 2017/3/3.
 */

public class FoldLayout extends ViewGroup {
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mShadowPaint;
    private Paint mSolidPaint;

    /**
     * 折叠后的总宽度与原图宽度的比例
     */
    private static float mFactor = 1f;
    /**
     * 折叠块的个数
     */
    private int mNumOfFolds = 8;
    private int mFoldWidth;
    /**
     * 折叠时，每块的宽度
     */
    private int mTranslateDisPerFold;

    private int mViwWidth;
    private int mViewHeight;


    private Matrix[] mMatrices = new Matrix[mNumOfFolds];
    private float mDepth;

    private GestureDetector mGestureDetector;
    private float mTranslation;

    public FoldLayout(Context context) {
        this(context, null);
    }

    public FoldLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoldLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        this.setWillNotDraw(false);
    }

    private void init() {
        mCanvas = new Canvas();
        mGestureDetector = new GestureDetector(getContext(), new ScrollGestureDetector());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 1) {
            throw new IllegalStateException("FoldLayout must only have one child view!");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View child = getChildAt(0);
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View child = getChildAt(0);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

        mCanvas.setBitmap(mBitmap);

        refresh();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViwWidth = w;
        mViewHeight = h;
        initBitmap();

        mTranslation = mViwWidth;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mFactor == 0) {
            return;
        }
        if (mFactor == 1) {
            super.dispatchDraw(canvas);
            return;
        }
        super.dispatchDraw(mCanvas);
        for (int i = 0; i < mNumOfFolds; i++) {
            canvas.save();

            canvas.concat(mMatrices[i]);
            canvas.clipRect(mFoldWidth * i, 0, mFoldWidth * i + mFoldWidth, mViewHeight);
            canvas.drawBitmap(mBitmap, 0, 0, null);
            canvas.translate(mFoldWidth * i, 0);
            if (i % 2 == 0) {
                canvas.drawRect(0, 0, mFoldWidth, mViewHeight, mSolidPaint);
            } else {
                canvas.drawRect(0, 0, mFoldWidth, mViewHeight, mShadowPaint);
            }

            canvas.restore();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private void refresh() {
        initSize();

        initMatrix();

        initShadow();
    }

    private void initBitmap() {
        mBitmap = Bitmap.createBitmap(mViwWidth, mViewHeight, Bitmap.Config.ARGB_8888);
    }

    private void initSize() {
        mFoldWidth = mViwWidth / mNumOfFolds;
        /*
      图片的折叠后的总宽度
     */
        int translateDis = (int) (mViwWidth * mFactor);
        mTranslateDisPerFold = translateDis / mNumOfFolds;

        mDepth = (float) Math.sqrt(mFoldWidth * mFoldWidth - mTranslateDisPerFold * mTranslateDisPerFold);
    }

    private void initMatrix() {
        for (int i = 0; i < mNumOfFolds; i++) {
            Matrix matrix = new Matrix();

            int srcIndex = i * mFoldWidth;

            float[] src = {
                    srcIndex, 0,
                    mFoldWidth + srcIndex, 0,
                    mFoldWidth + srcIndex, mViewHeight,
                    srcIndex, mViewHeight
            };

            boolean isEven = i % 2 == 0;
            int dstIndex = i * mTranslateDisPerFold;

            float[] dst = {
                    dstIndex, isEven ? 0 : mDepth,
                    mTranslateDisPerFold + dstIndex, isEven ? mDepth : 0,
                    mTranslateDisPerFold + dstIndex, isEven ? mViewHeight - mDepth : mViewHeight,
                    dstIndex, isEven ? mViewHeight : mViewHeight - mDepth
            };

            matrix.setPolyToPoly(src, 0, dst, 0, 4);
            mMatrices[i] = matrix;
        }
    }

    private void initShadow() {
        Matrix shadowMatrix = new Matrix();
        shadowMatrix.setScale(mTranslateDisPerFold, 1);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mShadowPaint.setStyle(Paint.Style.FILL);
        LinearGradient shadowGradient = new LinearGradient(0, 0, 0.5f, 0, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        shadowGradient.setLocalMatrix(shadowMatrix);
        mShadowPaint.setShader(shadowGradient);
        mShadowPaint.setAlpha((int) (0.8 * 255 * (1 - mFactor)));

        mSolidPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSolidPaint.setStyle(Paint.Style.FILL);
        int alpha = (int) (0.8 * 255 * (1 - mFactor));
        mSolidPaint.setColor(Color.argb(alpha, 0, 0, 0));
    }

    class ScrollGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mTranslation -= distanceX;

            if (mTranslation < 0) {
                mTranslation = 0;
            }
            if (mTranslation > mViwWidth) {
                mTranslation = mViwWidth;
            }

            mFactor = Math.abs((mTranslation) / ((float) mViwWidth));

            refresh();
            invalidate();
            return true;
        }
    }
}
