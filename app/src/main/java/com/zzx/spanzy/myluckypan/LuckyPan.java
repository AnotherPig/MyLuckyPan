package com.zzx.spanzy.myluckypan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Spanzy on 2018/8/5.
 */
public class LuckyPan extends SurfaceView implements SurfaceHolder.Callback ,Runnable{

    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    public LuckyPan(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);

        //可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        //设置常亮
        setKeepScreenOn(true);
    }

    /**
     * 用于绘制的线程
     */
    private Thread t;
    /**
     * 线程的控制开关
     */
    private boolean isRunning;

    /**
     * 盘块的奖项
     */
    private String[] mStrs = new String[]{"apple","banana","grape","orange","strawberry","watermelon"};

    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,20,
            getResources().getDisplayMetrics());

    /**
     * 奖项的图片
     */
    private int[] mImgs = new int[]{R.drawable.apple,R.drawable.banana,R.drawable.grape,
            R.drawable.orange,R.drawable.strawberry,R.drawable.watermelon};

    /**
     * 与图片对应的bitmap数组
     */
    private Bitmap[] mImgsBitmap;

    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.lucky_bg);

    /**
     * 盘块的颜色
     */
    private int[] mColors =new int[]{0XFFFFC300,0XFFF17E01,0XFFFFC300,0XFFF17E01,0XFFFFC300,0XFFF17E01};
    public LuckyPan(Context context) {
        super(context,null);
    }

    private int mItemCount = 6;

    /**
     * 整个盘块的范围
     */
    private RectF mRange = new RectF();

    /**
     * 整个盘块的直径
     */
    private int mRadius;

    /**
     * 绘制盘块的画笔
     */
    private Paint mArcPaint;
    /**
     * 绘制文本的画笔
     */
    private Paint mTextPaint;

    /**
     * 滚动速度
     */
    private double mSpeed ;

    private volatile float mStartAngle = 0;

    /**
     * 判断是否点击了停止按钮
     */
    private boolean isShouldEnd;
    /**
     * 转盘中心位置
     */
    private int mCenter;
    /**
     * 这里我们的padding直接以paddingleft为准
     */
    private int mPadding;


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(),getMeasuredHeight());

        mPadding = getPaddingLeft();
        //直径
        mRadius = width - mPadding*2;
        //中心点
        mCenter = width/2;

        //强制设置成正方形
        setMeasuredDimension(width,width);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //初始化绘制盘块的画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        //初始化绘制盘块的画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);

        //初始化盘块绘制的范围
        mRange = new RectF(mPadding,mPadding,mPadding+mRadius,mPadding+mRadius);

        //初始化图片
        mImgsBitmap = new Bitmap[mItemCount];

        for (int i = 0; i < mItemCount; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(),mImgs[i]);

        }

        isRunning = true;
        t = new Thread(this);
        t.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;

    }

    @Override
    public void run() {
        while (isRunning){
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < 50){
                try {
                    Thread.sleep(50-(end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null){
                //draw something
                //绘制背景
                drawBg();
                //绘制盘块

                float tmpAngle = mStartAngle;
                float sweepAngle = 360 / mItemCount;

                for (int i = 0; i < mItemCount; i++) {
                    mArcPaint.setColor(mColors[i]);
                    //绘制盘块
                    mCanvas.drawArc(mRange,tmpAngle,sweepAngle,true,mArcPaint);

                    //绘制文本
                    drawText(tmpAngle,sweepAngle,mStrs[i]);

                    //绘制奖项图片
                    drawIcon(tmpAngle,mImgsBitmap[i]);

                    tmpAngle += sweepAngle;

                }

                mStartAngle += mSpeed;
                //如果点击了停止按钮
                if (isShouldEnd){
                    mSpeed -= 1;
                }
                if (mSpeed <= 0){
                    mSpeed = 0;
                    isShouldEnd = false;
                }



            }
        }catch (Exception e){
            
        }finally {
            if (mCanvas != null){
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    public void luckyStart(int index){
        //计算每一项的角度
        float angle = 360/mItemCount;
        //计算每一项的中奖范围（当前index）

        //绘制初始位置到指针垂直位置270
        float from = 270 - (index + 1)*angle;
        float end = from + angle;

        //设置停下来需要旋转的距离
        float targetFrom = 4*360 + from;
        float targetEnd = 4*360 + end;

        /**
         * v1 -> 0
         * 且每次-1
         *
         * (v1 + 0)*(v1 + 1)/2 = targetFrom;
         * v1*v1 + v1 - 2*targetFrom = 0;
         * v1 = (-1 + Math.sqrt(1 + 8*targetFrom))/2
         */
        float v1 = (float) ((-1 + Math.sqrt(1+8*targetFrom))/2);
        float v2 = (float) ((-1 + Math.sqrt(1+8*targetEnd))/2);

        mSpeed = v1 + Math.random()*(v2-v1);
//        mSpeed = 50;
        isShouldEnd = false;
    }

    public void luckyEnd(){
        mStartAngle = 0;
        isShouldEnd = true;
    }

    /**
     * 转盘是否在旋转
     * @return
     */
    public boolean isStart(){
        return mSpeed!=0;
    }

    public boolean isShouldEnd(){
        return isShouldEnd;
    }

    /**
     * 绘制奖项图片
     * @param tmpAngle
     * @param bitmap
     */
    private void drawIcon(float tmpAngle, Bitmap bitmap) {
        //设置图片的宽度为直径的1/8
        int imgWith = mRadius/8;

        float angle = (float) ((tmpAngle + 360/mItemCount/2)*Math.PI/180);

        int x = (int) (mCenter + mRadius/2/2*Math.cos(angle));
        int y = (int) (mCenter + mRadius/2/2*Math.sin(angle));

        //确定图片位置
        Rect rect = new Rect(x-imgWith/2,y-imgWith/2,x + imgWith/2,y+imgWith/2);
        mCanvas.drawBitmap(bitmap,null,rect,null);

    }

    /**
     * 绘制每个盘块的文本
     * @param tmpAngle
     * @param sweepAngle
     * @param mStr
     */
    private void drawText(float tmpAngle, float sweepAngle, String mStr) {
        Path path = new Path();
        path.addArc(mRange,tmpAngle,sweepAngle);

        //利用水平偏移量让文字居中
        float textWith = mTextPaint.measureText(mStr);
        int hOffset = (int) (mRadius*Math.PI/mItemCount/2-textWith/2);
        int vOffset = mRadius/2/6;//垂直偏移量

        mCanvas.drawTextOnPath(mStr,path,hOffset,vOffset,mTextPaint);

    }

    /**
     * 绘制背景
     */
    private void drawBg() {
        mCanvas.drawColor(0xffffff);
        mCanvas.drawBitmap(mBgBitmap,null,new Rect(mPadding/2,mPadding/2,
                getMeasuredWidth()-mPadding/2,getMeasuredHeight()-mPadding/2),
                null);
    }
}
