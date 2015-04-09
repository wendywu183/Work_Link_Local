package com.centraltrillion.worklink.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

@SuppressLint("DrawAllocation")
public class CutPicView extends View implements OnTouchListener {

    private Context mContext;
    private Paint mPaint;
    private Paint mRoundPaint;
    private Paint mFgroundPaint;
    private Paint mClearFgPaint;
    private Paint mCutPicPaint;

    private Bitmap mBitmap;
    private Bitmap mLastBitmap;
    private Bitmap mFgBitmap;
    private Bitmap output;
    private Bitmap mNewZoomBitmap;


    private Point mCenterPoint;
    private Point mBitmapPoint;
    private PointF mFirstDragPoint;
    private PointF mFirstZoomPoint;
    private PointF mSecondZoomPoint;

    private Canvas mFgCanvas;
    private Canvas canvas;
    private Rect src;
    private Paint paint;
    private RectF rectF;
    private Rect dst;
    private RectF mRect;

    private final int MODE_DRAG = 0;
    private final int MODE_ZOOM = 1;

    private int mCurrMode;
    private int mRadius;
    private int mRingWidth;
    private int mBitmapLastWidth;
    private int mBitmapLastHeight;
    private int mAvatorWidth;
    private float scale = 0;
    private float oriDis;
    private boolean isFinishFirstZoomed;
    private boolean isSecondePointerUp;
    private int mDegreesRotated = 0;

    private WindowManager wm = null;
    private DisplayMetrics metrics = null;

    public CutPicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        initView();
    }

    private void initView() {
        mRadius = getScreenWidth() * 1 / 3;
        mRingWidth = 4;
        mAvatorWidth = 2 * mRadius;

        mPaint = new Paint();
        mRoundPaint = new Paint();
        mFgroundPaint = new Paint();
        mClearFgPaint = new Paint();
        mCutPicPaint = new Paint();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setARGB(255, 255, 225, 255);
        mPaint.setStrokeWidth(mRingWidth);

        mRoundPaint.setStrokeWidth(mRadius);
        mRoundPaint.setAntiAlias(true);
        mRoundPaint.setARGB(255, 0, 0, 0);

        mFgroundPaint.setAntiAlias(true);
        mFgroundPaint.setARGB(185, 0, 0, 0);
        mFgroundPaint.setXfermode(new PorterDuffXfermode(Mode.XOR));
        mCutPicPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        mCenterPoint = new Point();
        mBitmapPoint = new Point();
        mFirstDragPoint = new PointF();
        mFirstZoomPoint = new PointF();
        mSecondZoomPoint = new PointF();

        mClearFgPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        // mClearFgPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));

        initCutPicParams();

        setOnTouchListener(this);

    }

    private void initCutPicParams() {
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        float offsetX = mCenterPoint.x - mBitmapPoint.x - mRadius;
        float offsetY = mCenterPoint.y - mBitmapPoint.y - mRadius;

        top = offsetY;
        bottom = offsetY + mAvatorWidth;
        left = offsetX;
        right = offsetX + mAvatorWidth;

        dst_left = 0;
        dst_top = 0;
        dst_right = mAvatorWidth;
        dst_bottom = mAvatorWidth;

        output = Bitmap.createBitmap(mAvatorWidth,
                mAvatorWidth, Config.ARGB_8888);
        canvas = new Canvas(output);

        paint = new Paint();
        src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
        rectF = new RectF(dst);

        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);

    }

    /**
     */
    private void setCutPicPosition() {
        src.set(mCenterPoint.x - mBitmapPoint.x - mRadius,
                mCenterPoint.y - mBitmapPoint.y - mRadius,
                mCenterPoint.x - mBitmapPoint.x - mRadius + mAvatorWidth,
                mCenterPoint.y - mBitmapPoint.y - mRadius + mAvatorWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFgBitmap == null && mBitmap != null) {
            mLastBitmap = mBitmap;
            mBitmapLastWidth = mBitmap.getWidth();
            mBitmapLastHeight = mBitmap.getHeight();
            mFgBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
            mFgCanvas = new Canvas(mFgBitmap);
            mRect = new RectF(0, 0, getWidth(), getHeight());
            mCenterPoint.set(getWidth() / 2, getHeight() / 2);
        }
        setCutPicPosition();
        if (mFgCanvas != null) {
            mFgCanvas.drawPaint(mClearFgPaint);

            if (mBitmap != null) {
                if (!isFinishFirstZoomed) {
                    scale = getHeight() / mBitmap.getHeight();
                    mBitmap = zoomImg(mLastBitmap, (int) (scale * mBitmap.getWidth()), getHeight());
                    mBitmapPoint.set((getWidth() - mBitmap.getWidth()) / 2, 0);
                    isFinishFirstZoomed = true;
                }
                canvas.drawBitmap(mBitmap, mBitmapPoint.x, mBitmapPoint.y, mPaint);
            }

            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius, mPaint);
            mFgCanvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius, mRoundPaint);
            mFgCanvas.drawRect(mRect, mFgroundPaint);
            canvas.drawBitmap(mFgBitmap, null, mRect, mPaint);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mBitmap != null) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mCurrMode = MODE_DRAG;
                    mFirstDragPoint.set(event.getX(), event.getY());
                    mFirstZoomPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mCurrMode = MODE_ZOOM;
                    mSecondZoomPoint.set(event.getX(), event.getY());
                    oriDis = distance(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mCurrMode == MODE_DRAG) {
                        if (isSecondePointerUp) {
                            mFirstDragPoint.set(event.getX(), event.getY());
                            isSecondePointerUp = false;
                        }
                        mBitmapPoint.set(mBitmapPoint.x + (int) (event.getX() - mFirstDragPoint.x),
                                mBitmapPoint.y + (int) (event.getY() - mFirstDragPoint.y));
                        mFirstDragPoint.set(event.getX(), event.getY());
                    } else if (mCurrMode == MODE_ZOOM) {
                        float newDist = distance(event);
                        if (newDist > 10f) {
                            float scale = newDist / oriDis;
                            int x = (int) (mBitmapPoint.x + (mBitmap.getWidth() - scale * mBitmapLastWidth) / 2);
                            int y = (int) (mBitmapPoint.y + (mBitmap.getHeight() - scale * mBitmapLastHeight) / 2);
                            mBitmap = zoomImg(mLastBitmap, (int) (scale * mBitmapLastWidth), (int) (scale * mBitmapLastHeight));
                            mBitmapPoint.set(x, y);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mBitmapLastWidth = mBitmap.getWidth();
                    mBitmapLastHeight = mBitmap.getHeight();
                    mCurrMode = MODE_DRAG;
                    isSecondePointerUp = true;
                    break;
                default:
                    break;
            }
            requestLayout();
            invalidate();
        }

        return true;
    }

    /**
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    /**
     * @param bitmap
     * @return
     */
    public Bitmap toRoundBitmap() {

        canvas.drawRoundRect(rectF, mRadius, mRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(mBitmap, src, dst, paint);
        return output;
    }

    public void rotateImage(int degrees) {

        if (mBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            mLastBitmap = mBitmap;
            mBitmapLastWidth = mBitmap.getWidth();
            mBitmapLastHeight = mBitmap.getHeight();
            mDegreesRotated += degrees;
            mDegreesRotated = mDegreesRotated % 360;
            requestLayout();
            invalidate();
        }
    }

    /**
     * @param bm        bitmap
     * @param newWidth
     * @param newHeight
     * @return bitmap
     */
    private Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        if (newHeight == 0 || newWidth == 0) {
            return bm;
        }
        float scale = 0;
        int width = bm.getWidth();
        int height = bm.getHeight();
        if (newHeight > 0) {
            scale = ((float) newHeight) / height;
            newWidth = (int) (scale * width);

        } else {
            scale = ((float) newWidth) / width;
            newHeight = (int) (scale * height);
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        mNewZoomBitmap = null;
        mNewZoomBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return mNewZoomBitmap;
    }

    /**
     * @param event
     * @return
     */
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * @return
     */
    private int getScreenWidth() {
        return metrics.widthPixels;
    }

    private int getScreenHeight() {
        return metrics.heightPixels;
    }

    private int resizeBitmapWidth(int width){
        if(width >= getScreenWidth())
            return getScreenWidth();
        else
            return width;
    }

    private int resizeBitmapHeight(int height){
        if(height >= getScreenHeight())
            return getScreenHeight();
        else
            return height;
    }
}
