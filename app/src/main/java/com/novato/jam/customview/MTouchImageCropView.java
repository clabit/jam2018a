package com.novato.jam.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class MTouchImageCropView extends ImageView
{
	Matrix matrix;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 5f;
    float[] m;

    int viewWidth, viewHeight;
    static final int CLICK = 3;
    float saveScale = 1f;
    protected float origWidth, origHeight;
    int oldMeasuredWidth, oldMeasuredHeight;

    ScaleGestureDetector mScaleDetector;

    Context context;

    public MTouchImageCropView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public MTouchImageCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }
        
    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix = new Matrix();
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
 
        setOnTouchListener(new OnTouchListener() {
 
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());
 
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                       last.set(curr);
                        start.set(last);
                        mode = DRAG;
                        break;
                        
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            float deltaX = curr.x - last.x;
                            float deltaY = curr.y - last.y;
                            float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
                            float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);
                            matrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            last.set(curr.x, curr.y);
                        }
                        break;
 
                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        int xDiff = (int) Math.abs(curr.x - start.x);
                        int yDiff = (int) Math.abs(curr.y - start.y);
                        if (xDiff < CLICK && yDiff < CLICK)
                            performClick();
                        break;
 
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                }
                
                setImageMatrix(matrix);
                invalidate();
                return true; // indicate event was handled
            }
 
        });
    }
 
    public void setMaxZoom(float x) {
        maxScale = x;
    }
 
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }
 
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            }
//            else if (saveScale < minScale) {
//                saveScale = minScale;
//                mScaleFactor = minScale / origScale;
//            }
            else {

                if (origWidth <= origHeight) {
                    if ((viewWidth - x0 * 2 >= origWidth * saveScale)) {//
                        saveScale = (viewWidth - x0 * 2) / origWidth;
                        mScaleFactor = saveScale / origScale;
                    }
                    else if ((viewHeight - y0 * 2 >= origHeight * saveScale)) {
                        saveScale = (viewHeight - y0 * 2) / origHeight;
                        mScaleFactor = saveScale / origScale;
                    }

                } else {
                    if ((viewHeight - y0 * 2 >= origHeight * saveScale)) {//
                        saveScale = (viewHeight - y0 * 2) / origHeight;
                        mScaleFactor = saveScale / origScale;
                    }
                    else if ((viewWidth - x0 * 2 >= origWidth * saveScale)) {//
                        saveScale = (viewWidth - x0 * 2) / origWidth;
                        mScaleFactor = saveScale / origScale;
                    }

                }

//                Log.e("munx","c="+x0 + " : " + y0  + "//" +origWidth +","+origHeight + ","+saveScale+ ","+mScaleFactor);
            }
//            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)
//                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);
//            else
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());

            fixTrans();
            return true;
        }
    }
 
    public boolean canScrollHor(int direction) {
    	
    	if (saveScale > 1f)
    		return true;
    	else
    		return false;
//        final int offset = computeHorizontalScrollOffset();
//        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
//        if (range == 0) return false;
//        if (direction < 0) {
//            return offset > 0;
//        } else {
//            return offset < range - 1;
//        }
    }
    
    void fixTrans() {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        
        float fixTransX = getFixTrans(0,transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(1,transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);
    }
 
    float getFixTrans(int type, float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        //x0, y0, x1,y2,
        float padding = y0;
        if(type == 0){
            padding = x0;
        }



//        if (contentSize <= viewSize) {
//            minTrans = 0;
//            maxTrans = viewSize - contentSize;
//        } else {
//            minTrans = viewSize - contentSize;
//            maxTrans = 0;
//        }

//        if (contentSize <= viewSize - padding * 2) {
//            Log.e("munx","t="+type + " : "+viewSize +","+contentSize + ","+padding);
//
//            minTrans = -padding;//0;
//            maxTrans = viewSize - contentSize + padding;
//        }
//        else {
//            Log.e("munx","c="+type + " : " + trans  + "//" +viewSize +","+contentSize + ","+padding);
//
//            minTrans = viewSize - contentSize - padding;
//            maxTrans = padding;
//        }


        minTrans = viewSize - contentSize - padding;
        maxTrans = padding;



        if (trans < minTrans)
            return -trans + minTrans;

        if (trans > maxTrans)
            return -trans + maxTrans;


        return 0;
    }
    
    float getFixDragTrans(float delta, float viewSize, float contentSize) {
//        if (contentSize <= viewSize) {
//            return 0;
//        }
        return delta;
    }
 
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        
        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return;
        oldMeasuredHeight = viewHeight;
        oldMeasuredWidth = viewWidth;
 
        if (saveScale == 1) {
            //Fit to screen.
            float scale;
 
            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();
            
            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);
 
            float scaleX = (float) viewWidth / (float) bmWidth;
            float scaleY = (float) viewHeight / (float) bmHeight;
            scale = Math.max(scaleX, scaleY);
            matrix.setScale(scale, scale);
 
            // Center the image
            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;
 
            matrix.postTranslate(redundantXSpace, redundantYSpace);
 
            origWidth = viewWidth - 2 * redundantXSpace;
            origHeight = viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);

        }
        fixTrans();
    }



    private float position_w;
    private float position_h;
    private float x0;
    private float y0;
    private Paint mPaintTransparent = new Paint();
    private Paint mPaintFrame = new Paint();
    private int mFrameColor = Color.parseColor("#83ffffff");
    private int mOverlayColor = Color.parseColor("#AA1C1C1C");
    private float mFrameStrokeWeight = 3.0f;

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int x = this.getWidth();
        int y = this.getHeight();
//        Paint paint = new Paint();
//        paint.setColor(0xaa000000);
        position_w=(float) (Math.min(x, y)*0.9);
        position_h=(float) (Math.min(x, y)*0.9);


        if(mCurMode == CropMode.SQUARE_4_3){
            float ori =(float) (Math.min(x, y)*0.9);
            position_w=(float) (ori*0.8);
            position_h=(float) (ori*0.6);
        }
        else if(mCurMode == CropMode.SQUARE_3_4){
            float ori =(float) (Math.min(x, y)*0.9);
            position_w=(float) (ori*0.6);
            position_h=(float) (ori*0.8);
        }
        else if(mCurMode == CropMode.SQUARE_16_9){
            float ori =(float) (Math.min(x, y)*0.9);
            position_w=(float) (ori*0.96);
            position_h=(float) (ori*0.54);
        }
        else if(mCurMode == CropMode.SQUARE_9_16){
            float ori =(float) (Math.min(x, y)*0.9);
            position_w=(float) (ori*0.54);
            position_h=(float) (ori*0.96);
        }


        x0=(x-position_w)/2;
        y0=(y-position_h)/2;
        float x1=x0+position_w,y1=y0;
        float x2=x1,y2=y1+position_h;
        float x3=x0,y3=y0+position_h;


        if(mCurMode == CropMode.CIRCLE) {

            mPaintTransparent.setFilterBitmap(true);
            mPaintTransparent.setColor(mOverlayColor);
            mPaintTransparent.setStyle(Paint.Style.FILL);

            Path path = new Path();
            path.addRect(0, 0, x, y,
                    Path.Direction.CW);
            path.addCircle((x0 + x1) / 2,
                    (y0 + y2) / 2,
                    (x1 - x0) / 2, Path.Direction.CCW);
            canvas.drawPath(path, mPaintTransparent);

        }


        mPaintFrame.setAntiAlias(true);
        mPaintFrame.setFilterBitmap(true);
        mPaintFrame.setStyle(Paint.Style.STROKE);
        mPaintFrame.setColor(mFrameColor);
        mPaintFrame.setStrokeWidth(mFrameStrokeWeight);

        canvas.drawRect(x0, y0, x1,y2,
                mPaintFrame);






    }


    public enum CropMode{CIRCLE, SQUARE_1_1
        ,SQUARE_4_3, SQUARE_3_4, SQUARE_16_9, SQUARE_9_16
    }

    private CropMode mCurMode = CropMode.SQUARE_1_1;
    public void setCropMode(CropMode mode){
        mCurMode = mode;

        invalidate();
    }



    private Bitmap originBitmap;
    @Override
    public void setImageBitmap(Bitmap bm) {
        if(originBitmap!=null){
            originBitmap.recycle();
        }
        super.setImageBitmap(bm);
        originBitmap = bm;
    }

    public Bitmap getCroppedImage() {

        float[] m = new float[9];
        matrix.getValues(m);

        float xx = originBitmap.getWidth() / origWidth;
        float yy = originBitmap.getHeight() / origHeight;

        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];

        float l = (x0 / saveScale) * xx;
        float t = (y0 / saveScale) * yy;
        float r = ( (viewWidth - x0*2) / saveScale) * xx;
        float b = ( (viewHeight - y0*2) / saveScale) * yy;



        int x = Math.round(l - (transX / saveScale * xx));
        int y = Math.round(t - (transY / saveScale * yy));
        int w = Math.round(( (viewWidth - x0*2) / saveScale) * xx);//Math.round(r - l);
        int h = Math.round(( (viewHeight - y0*2) / saveScale) * yy);//Math.round(b - t);


//        Log.e("munx", "o: "+originBitmap.getWidth() +"//"+originBitmap.getHeight());
//        Log.e("munx", "f: "+l +"//"+t+"//"+r+"//"+b);
//        Log.e("munx", "x: "+x +"//"+y+"//"+w+"//"+h);


        if(x > originBitmap.getWidth() || y > originBitmap.getHeight() || w-x > originBitmap.getWidth() || w-h > originBitmap.getHeight() ){
            Log.e("munx", "crop origin err");
            return getCroppedImage_ori();
        }



        Bitmap cropped = Bitmap.createBitmap(originBitmap, x, y, w, h, null, false);

        if(mCurMode == CropMode.CIRCLE){
            return getCircularBitmap(cropped);
        }

        return cropped;
    }

    public Bitmap getCroppedImage_ori() {
        setDrawingCacheEnabled(true);
        Bitmap bitMap=getDrawingCache();
        final Bitmap croppedBitmap = Bitmap.createBitmap(bitMap,
                (int) x0,
                (int) y0,
                (int) position_w,
                (int) position_h);
        bitMap.recycle();
        setDrawingCacheEnabled(false);

        if(mCurMode == CropMode.CIRCLE){
            return getCircularBitmap(croppedBitmap);
        }

        return croppedBitmap;

    }
    private Bitmap getCircularBitmap(Bitmap square) {
        if (square == null) return null;
        Bitmap output = Bitmap.createBitmap(square.getWidth(), square.getHeight(),
                Bitmap.Config.ARGB_8888);

        final Rect rect = new Rect(0, 0, square.getWidth(), square.getHeight());
        Canvas canvas = new Canvas(output);

        int halfWidth = square.getWidth() / 2;
        int halfHeight = square.getHeight() / 2;

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        canvas.drawCircle(halfWidth, halfHeight, Math.min(halfWidth, halfHeight), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(square, rect, rect, paint);

        square.recycle();

        return output;
    }

}
