package com.juyoufuli.filmselectseat;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author : dongfang
 * @Created Time : 2018-11-19  15:25
 * @Description:
 */
public class SelectSeatView extends View {
    //处理坐标之间的差异，手势变换后的坐标正题变化

    private Context context;
    /**
     * 电影屏幕的画笔
     */
    private Paint screenPaint;
    /**
     * 座位的画笔
     */
    private Paint paintSeat;
    /**
     * 文字的画笔
     */
    private TextPaint textPaint;
    /**
     * 座位的二维数组
     */
    private int[][] seatList;
    /**
     * 所有的矩形集合
     */
    private List<SelectRectBean> mRectList;

    /**
     * 所有的矩形集合
     */
    private List<Rect> mOverRectList;
    /**
     * 选中的矩形
     */
    private List<SelectRectBean> selectList;

    /**
     * 当前点击的点
     */
    private Point currentPoint;
    private LayoutInflater inflater;
    private ChildSelectListener childSelectListener;
    private ScaleGestureDetector scaleGestureDetector;
    /**
     * 默认的标签间距
     */
    private int margiHorizontal = 10;
    /**
     * 座位宽度
     */
    private int seatWidth = 80;
    /**
     * 默认的行间距
     */
    private int margiVertical = 20;
    /**
     * 排号距离左边距离
     */
    private int margiLeft = seatWidth / 2;
    /**
     * 画布的矩阵
     */
    private Matrix mCanvasMatrix = new Matrix();
    /**
     * 缩略图画布矩阵
     */
    private Matrix mCoverCanvasMatrix = new Matrix();
    /**
     * 缩放的比例
     */
    private float lastScale;
    /**
     * --- 限制缩放比例 ---
     */
    private static final float MAX_SCALE = 2.0f;
    private static final float MIN_SCALE = 1.0f;
    /**
     * 控件的尺寸
     */
    private int measuredWidth, measuredHeight;
    /**
     * 手势操作主要是移动和点击
     */
    private GestureDetector gestureDetector;
    /**
     * 排
     */
    private int row;
    /**
     * 列
     */
    private int column;
    /**
     * 空位置
     */
    public static final int EMPTY_SEAT = 0;
    /**
     * 默认座位
     */
    public static final int NORMAL_SEAT = 1;
    /**
     * 已卖座位
     */
    public static final int SELL_SEAT = 2;
    /**
     * 选中座位
     */
    public static final int SELECT_SEAT = 3;
    /**
     * Film屏幕高度
     */
    private static int filmScreenHeight = 80;
    /**
     * 座位距离屏幕的距离
     */
    private static int marginTopScreen = 200;
    /**
     * 是否在缩放中
     */
    private boolean isScaling;
    /**
     * 屏幕的尺寸
     */
    private int screenWidth, screenHeight;
    /**
     * 座位的区域矩阵
     */
    private Rect seatRect;

    /**
     * 整个画布的区域矩阵
     */
    private Rect mCanvasRect;
    /**
     * 是否绘制左上角概览图
     */
    private boolean isDrawOver;
    /**
     * 概览图画笔
     */
    private Paint overPaint;
    /**
     * 缩略和原图的缩放比，默认是4，可以和屏幕进行比例换算
     */
    private float ratioOver = 4;
    /**
     * 是否需要绘制bitmap
     */
    private boolean isDrawOverBitmap = true;
    private Bitmap mBitmap;

    private Paint paintBorder;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    isDrawOverBitmap = false;
                    isDrawOver = false;
//            LogUtil.i("handleMessage 绘制缩略图：" + isDrawOver);
                    invalidate();
                    break;
                case 2:

                    break;
                default:
                    break;
            }


        }
    };

    public void setChildSelectListener(ChildSelectListener childSelectListener) {
        this.childSelectListener = childSelectListener;
    }

    private int getRandom() {
        int endNum = 10;
        Random random = new Random();
        int result = random.nextInt(endNum);
        if (result == 0) {
            return 1;
        } else {
            return result;
        }
    }

    public void setSeatList(int[][] seatList) {
        if (seatList.length == 0 || seatList[0].length == 0) {
            return;
        }
        this.seatList = seatList;
        row = seatList.length;
        column = seatList[0].length;
        invalidate();
    }

    public SelectSeatView(@NonNull Context context) {
        this(context, null);
    }

    public SelectSeatView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public SelectSeatView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initData();
        init(context);
    }


    private void initData() {
        selectList = new ArrayList<>();
        mRectList = new ArrayList<>();
        mOverRectList = new ArrayList<>();
        currentPoint = new Point();
        seatList = new int[row][];
    }

    private void init(Context context) {
        //初始化手势包括缩放和平移
        initGesture(context);
        inflater = LayoutInflater.from(context);
        screenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        screenPaint.setColor(Color.WHITE);

        paintSeat = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSeat.setColor(Color.GREEN);

        overPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        overPaint.setColor(Color.parseColor("#bb555555"));

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setColor(Color.RED);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(1);

        textPaint = new TextPaint();
        textPaint.setTextSize(20);
        textPaint.setColor(Color.BLACK);

        int[] screenSize = getScreenSize();
        screenWidth = screenSize[0];
        screenHeight = screenSize[1];

    }

    private void initGesture(Context context) {
        lastScale = 1.0f;
        //图片完全显示的伸缩值
//        mCanvasMatrix.postTranslate(translateX, translateY);
//        mCanvasMatrix.postScale(scale, scale);
        //缩放手势
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                isScaling = true;
//                isDrawOverBitmap=true;
//                LogUtil.i("focusX = " + detector.getFocusX());       // 缩放中心，x坐标
//                LogUtil.i("focusY = " + detector.getFocusY());       // 缩放中心y坐标
//                LogUtil.i("scale = " + detector.getScaleFactor());   // 缩放因子
                float scaleFactor = detector.getScaleFactor();
                //当前的缩放比例
                float fx = detector.getFocusX();
                float fy = detector.getFocusY();
//                float[] points = mapPoint(fx, fy, mCanvasMatrix);
                float realScaleFactor = getRealScaleFactor(scaleFactor);
                mCanvasMatrix.postScale(realScaleFactor, realScaleFactor, 0, 0);
                invalidate();
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                super.onScaleEnd(detector);
                isScaling = false;
                lastScale = getMatrixScaleX();
                reviseTranslate();
            }
        });
        //移动手势
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                方向是相反的，所以需要加负号。
                LogUtil.i("onScroll_change = " + "获取移动的距离" + getMatrixTranslateX() + "---" + getMatrixTranslateY());
//                通过移动距离的大小来判断x移动或y轴移动

                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    //左边界
                    float x = seatRect.left / getMatrixScaleX() - getMatrixTranslateX();
                    float standardX = transformNewCoordX(seatRect.left);
                    //右边界
                    float xRight = seatRect.right * getMatrixScaleX() + getMatrixTranslateX();
                    float standardRightX = measuredWidth - seatWidth;
                    LogUtil.i("onScroll_change = " + "右边界" + xRight + "---" + standardRightX
                            + "\n 左边界：：" + x + "----" + standardX);
                    //如果加上新的移动的距离>边界值就设置为边界数值//distanceX==左正数，右是负数
//                    if (x < standardX) {
//                        mCanvasMatrix.postTranslate(standardX, 0);
//                    } else if (xRight < standardRightX) {
////                        mCanvasMatrix.postTranslate(xRight, 0);
//                    } else {
//                        mCanvasMatrix.postTranslate(-distanceX, 0);
//                    }

                    if (x <= standardX) {
                    } else if (xRight < standardRightX) {
                    } else {
                    }
                    mCanvasMatrix.postTranslate(-distanceX, 0);

                } else if (Math.abs(distanceX) < Math.abs(distanceY)) {
                    //上边界
                    float x = seatRect.top / getMatrixScaleY() - getMatrixTranslateY();
                    float standardX = transformNewCoordY(seatRect.top);
                    //下边界
                    float xRight = seatRect.bottom * getMatrixScaleY() + getMatrixTranslateY();
                    float standardRightX = measuredHeight - seatWidth;
//                    LogUtil.i("onScroll_change = " + "下边界：" + xRight + "---" + standardRightX
//                            + "\n 下边界：：" + x + "----" + standardX);
//                    if (standardX >= x) {
//                        mCanvasMatrix.postTranslate(0, -2);
//                    } else if (xRight < standardRightX) {
//                        mCanvasMatrix.postTranslate(0, 2);
//                    } else {
//
//                    }
                    mCanvasMatrix.postTranslate(0, -distanceY);
                } else {

                }
                //应该限制左边的坐标
                invalidate();
                return true;
            }


            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
//                LogUtil.i(event.getX() + "--onSingleTapConfirmed--" + event.getY());
                //这里做点击了处理，开始的x、y坐标
                isDrawOverBitmap = true;
                float currentX = event.getX();
                float currentY = event.getY();
                //需要做坐标点转换,转换为原始的点，在进行点击事件
//                LogUtil.i(currentX + "--before--" + currentY);
                currentPoint.set((int) currentX, (int) currentY);
                clickSeat(currentPoint);
                if (childSelectListener != null) {
                    childSelectListener.onChildSelect(selectList);
                }
                return true;
            }
        });
    }


    private void autoScroll() {
        float currentSeatBitmapWidth = (Math.abs(seatRect.right - seatRect.left)) * getMatrixScaleX();
        float currentSeatBitmapHeight = (Math.abs(seatRect.bottom - seatRect.top)) * getMatrixScaleY();
        float moveYLength = 0;
        float moveXLength = 0;

        //处理左右滑动的情况
        if (currentSeatBitmapWidth + margiLeft < getWidth()) {
            if (getMatrixTranslateX() < 0 || getMatrixScaleX() < margiLeft + margiHorizontal) {
                //计算要移动的距离
                if (getMatrixTranslateX() < 0) {
                    moveXLength = (-getMatrixTranslateX()) + margiLeft + margiHorizontal;
                } else {
                    moveXLength = margiLeft + margiHorizontal - getMatrixTranslateX();
                }
            }
        } else {

            if (getMatrixTranslateX() < 0 && getMatrixTranslateX() + currentSeatBitmapWidth > getWidth()) {
            } else {
                //往左侧滑动
                if (getMatrixTranslateX() + currentSeatBitmapWidth < getWidth()) {
                    //滑动的距离需要计算
                    moveXLength = getWidth() - (getMatrixTranslateX() + currentSeatBitmapWidth + (seatWidth * 2) * getMatrixScaleX());
                } else {
                    //右侧滑动
                    moveXLength = -getMatrixTranslateX() - margiHorizontal * 2;
                }
            }
        }

        float startYPosition = filmScreenHeight * getMatrixScaleY() / 4;
        //处理上下滑动
        if (currentSeatBitmapHeight + filmScreenHeight + marginTopScreen < getHeight()) {
            if (getMatrixTranslateY() < startYPosition) {
                moveYLength = startYPosition - getMatrixTranslateY();
            } else {
                moveYLength = -(getMatrixTranslateY() - (startYPosition));
            }

        } else {
            if (getMatrixTranslateY() < 0 && getMatrixTranslateY() + currentSeatBitmapHeight > getHeight()) {
            } else {
                //往上滑动
                if (getMatrixTranslateY() + currentSeatBitmapHeight < getHeight()) {
                    //向下调整
                    moveYLength = getHeight() - (getMatrixTranslateY() + currentSeatBitmapHeight) - marginTopScreen * getMatrixScaleY();
                } else {
                    //向上调整
                    moveYLength = -(getMatrixTranslateY() - (startYPosition) );
                }
            }
        }

        Point start = new Point();
        start.x = (int) getMatrixTranslateX();
        start.y = (int) getMatrixTranslateY();

        Point end = new Point();
        end.x = (int) (start.x + moveXLength);
        end.y = (int) (start.y + moveYLength);

        moveAnimate(start, end);

    }

    private void moveAnimate(Point start, Point end) {
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new MoveEvaluator(), start, end);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        MoveAnimation moveAnimation = new MoveAnimation();
        valueAnimator.addUpdateListener(moveAnimation);
        valueAnimator.setDuration(400);
        valueAnimator.start();
    }

    class MoveAnimation implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Point p = (Point) animation.getAnimatedValue();
            move(p);
        }
    }

    class MoveEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Point startPoint = (Point) startValue;
            Point endPoint = (Point) endValue;
            int x = (int) (startPoint.x + fraction * (endPoint.x - startPoint.x));
            int y = (int) (startPoint.y + fraction * (endPoint.y - startPoint.y));
            return new Point(x, y);
        }
    }

    private void move(Point p) {
        float x = p.x - getMatrixTranslateX();
        float y = p.y - getMatrixTranslateY();
        mCanvasMatrix.postTranslate(x, y);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //宽和高都是AT_MOST，则设置宽度所有子元素的宽度的和；高度设置为第一个元素的高度；
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureWidth(heightMeasureSpec));
        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();
    }

    /**
     * 宽度计算
     *
     * @param widthMeasureSpec
     * @return
     */
    private int measureWidth(int widthMeasureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = widthMeasureSpec;
        } else {
            result = 500;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private boolean isFirstDraw = true;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.concat(mCanvasMatrix);
        //绘制电影屏幕
        drawFilmScreen(canvas);
        //绘制座位
        drawSeatView(canvas);
        //绘制电影屏幕
        drawFilmScreen2(canvas);
        //绘制行数
        drawRowIndex(canvas);
        //是否绘制概览区域
        if (isDrawOver) {
            //绘制概览区域
            drawOverView(canvas);
            //绘制边框
            drawOverBorder(canvas);
        }
        if (isFirstDraw) {
            isFirstDraw = false;
            mCanvasRect = new Rect();
            mCanvasRect.left = 0;
            mCanvasRect.top = 0;
            mCanvasRect.right = (seatWidth + margiHorizontal) * seatList[0].length + (seatWidth + seatWidth / 2 + margiHorizontal);
            mCanvasRect.bottom = (seatWidth + margiVertical) * seatList.length + marginTopScreen - seatWidth / 2 - margiVertical;
        }
    }


    private void drawOverBorder(Canvas canvas) {
        //绘制移动的框，应该显示屏幕区域内的座位,屏幕点转换到画布上，在转换到缩略图上
        float left = transformOldCoordX(0) - transformCoverDistance2(getMatrixTranslateX());
        float top = transformOldCoordY(0) - transformCoverDistance2(getMatrixTranslateY());
        float right = left + transformCoverDistance2(measuredWidth);
        float bottom = top + transformCoverDistance2(measuredHeight);
//        float right = transformOldCoordX(measuredWidth) / getMatrixScaleX() / ratioOver - getMatrixTranslateX() / getMatrixScaleX() / ratioOver;
//        float bottom = transformOldCoordY(measuredHeight) / getMatrixScaleY() / ratioOver - getMatrixTranslateY() / getMatrixScaleY() / ratioOver;
//        LogUtil.i(left + "-右-" + right + "-下-");

        RectF rectBorder = new RectF(
                left,
                top,
                right,
                bottom);
        canvas.drawRect(rectBorder, paintBorder);
    }

    private void drawOverView(Canvas canvas) {
        float left = transformOldCoordX(0);
        float top = transformOldCoordY(0);
        float right = left + transformCoverDistance(mCanvasRect.right - mCanvasRect.left);
        float bottom = top + transformCoverDistance(mCanvasRect.bottom - mCanvasRect.top);
        RectF rect = new RectF(left, top, right, bottom);
//        mCoverCanvasMatrix.reset();
//        mCoverCanvasMatrix.postScale(1 / getMatrixScaleX(), 1 / getMatrixScaleY());
//        mCoverCanvasMatrix.postTranslate(-getMatrixTranslateX()/ getMatrixScaleX() / ratioOver, -getMatrixTranslateY()/ getMatrixScaleX() / ratioOver);
        //绘制中心线和屏幕
        drawFilmScreenCover(rect, canvas);
        //是否绘制图，一般只有点击时间才会重绘
        if (isDrawOverBitmap) {
        }
        mBitmap = drawSeatRectOver(rect, canvas);
//        if (mBitmap != null) {
//            canvas.drawBitmap(mBitmap, mCoverCanvasMatrix, overPaint);
//        }

    }

    private void drawFilmScreenCover(RectF rect, Canvas canvas) {
        //计算出实时的顶部位置，座位的矩阵部分其实是原始的坐标。
        screenPaint.setColor(Color.parseColor("#ffffff"));
        float centerX = ((rect.right + rect.left) / 2 - 1.5f);
//        LogUtil.i("中心的x坐标：：：" + centerX);
        float newLeft = (centerX - transformCoverDistance(100));
        float newRight = (centerX - transformCoverDistance(filmScreenHeight));
        float newTop = (centerX + transformCoverDistance(filmScreenHeight));
        float newBottom = (centerX + transformCoverDistance(100));
        Path path1 = new Path();
        path1.moveTo(newLeft, transformOldCoordY(0));
        path1.lineTo(newRight, transformOldCoordY(0) + transformCoverDistance(filmScreenHeight / 2));
        path1.lineTo(newTop, transformOldCoordY(0) + transformCoverDistance(filmScreenHeight / 2));
        path1.lineTo(newBottom, transformOldCoordY(0));
        path1.close();
        canvas.drawPath(path1, screenPaint);
//        canvas.drawText("屏幕", transformCoverDistance(centerX - filmScreenHeight / 4),transformCoverDistance(transformOldCoordY(filmScreenHeight / 4)), textPaint);
        canvas.drawLine(centerX, transformCoverDistance(transformOldCoordY(0)), centerX, rect.bottom, screenPaint);

    }

    private Bitmap drawSeatRectOver(RectF mRect, Canvas canvas) {
        isDrawOverBitmap = false;
        float overRectWidth = (mRect.right - mRect.left);
        float overRectHeight = (mRect.bottom - mRect.top);
        //生成bitmap
//        if (mBitmap == null) {
//            mBitmap = Bitmap.createBitmap((int) overRectWidth, (int) overRectHeight, Bitmap.Config.RGB_565);
//        }
//        Canvas canvas = new Canvas(mBitmap);
        canvas.drawRect(mRect, overPaint);
//        LogUtil.i(overRectWidth + "--宽高--" + overRectHeight);
        //每行的高度缩放会变化
//        float averageRow = overRectHeight / row;
        for (int i = 0; i < seatList.length; i++) {
            //每排多少座位
//            float averageColumn = overRectWidth / seatList[i].length;
            for (int x = 0; x < seatList[i].length; x++) {
                float top;
                if (i == 0) {
                    top = mRect.top + transformCoverDistance(marginTopScreen - seatWidth / 2 - margiVertical);
                } else {
                    top = (mRect.top + transformCoverDistance(i * seatWidth + marginTopScreen - seatWidth / 2 - margiVertical));
                }
                float left;
                //开始绘制矩阵图
                if (x == 0) {
                    left = mRect.left + transformCoverDistance(seatWidth + seatWidth / 2);
                } else {
                    left = (mRect.left + transformCoverDistance((x + 1) * seatWidth + seatWidth / 2));
                }
                int seatState = seatList[i][x];
                Rect rect = new Rect((int) (left + transformCoverDistance(margiHorizontal)),
                        (int) (top + transformCoverDistance(margiVertical)),
                        (int) (left + transformCoverDistance(seatWidth)),
                        (int) (top + transformCoverDistance(seatWidth)));
                //需要计算从中间开始绘制座位
                switch (seatState) {
                    case EMPTY_SEAT:
                        paintSeat.setColor(Color.TRANSPARENT);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    case NORMAL_SEAT:
                        paintSeat.setColor(Color.WHITE);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    case SELL_SEAT:
                        paintSeat.setColor(Color.RED);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    case SELECT_SEAT:
                        //刷新选中
                        paintSeat.setColor(Color.GREEN);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    default:
                        paintSeat.setColor(Color.TRANSPARENT);
                        canvas.drawRect(rect, paintSeat);
                        break;
                }
                boolean isSelect = seatType(i, x);
                if (isSelect) {
                    paintSeat.setColor(Color.GREEN);
                    canvas.drawRect(rect, paintSeat);
                }
                //记录矩形的位置信息
                mOverRectList.add(rect);
            }
        }

        return mBitmap;
    }

    private boolean seatType(int row, int column) {
        //绘制变化的
        if (selectList.size() > 0) {
            for (int i = 0; i < selectList.size(); i++) {
                SelectRectBean selectRectBean = selectList.get(i);
                int rowS = selectRectBean.getRealRow();
                int columnS = selectRectBean.getRealColumn();
                if (row == rowS - 1 && column == columnS - 1) {
                    return true;
                }
            }
        }
        return false;
    }


    private void drawSeatView(Canvas canvas) {
        //绘制多少排座位
        for (int i = 0; i < seatList.length; i++) {
            int top;
            if (i == 0) {
                top = marginTopScreen - seatWidth / 2 - margiVertical;
            } else {
                top = i * seatWidth + marginTopScreen - seatWidth / 2 - margiVertical;
            }
            int emptyCount = 0;
            //每排多少座位
            for (int x = 0; x < seatList[i].length; x++) {
                int left;
                //开始绘制矩阵图
                if (x == 0) {
                    left = seatWidth + seatWidth / 2;
                } else {
                    left = (x + 1) * seatWidth + seatWidth / 2;
                }
//                LogUtil.i(left + "--" + top + "--" + (left + seatWidth) + "--" + (top + seatWidth));
                int seatState = seatList[i][x];
                SelectRectBean selectRectBean = new SelectRectBean();
                Rect rect = new Rect(left + margiHorizontal, top + margiVertical, left + seatWidth, top + seatWidth);
                selectRectBean.setRect(rect);
                selectRectBean.setSeatState(seatState);
                //需要计算从中间开始绘制座位
                switch (seatState) {
                    case EMPTY_SEAT:
                        emptyCount++;
                        paintSeat.setColor(Color.TRANSPARENT);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    case NORMAL_SEAT:
                        paintSeat.setColor(Color.WHITE);
                        selectRectBean.setColumn(x + 1 - emptyCount);
                        selectRectBean.setRow(i + 1);
                        selectRectBean.setRealColumn(x + 1);
                        selectRectBean.setRealRow(i + 1);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    case SELL_SEAT:
                        paintSeat.setColor(Color.RED);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    case SELECT_SEAT:
                        paintSeat.setColor(Color.GREEN);
                        selectRectBean.setColumn(x + 1 - emptyCount);
                        selectRectBean.setRow(i + 1);
                        selectRectBean.setRealColumn(x + 1);
                        selectRectBean.setRealRow(i + 1);
                        canvas.drawRect(rect, paintSeat);
                        break;
                    default:
                        paintSeat.setColor(Color.TRANSPARENT);
                        canvas.drawRect(rect, paintSeat);
                        break;
                }
                boolean isSelect = seatType(i, x);
                if (isSelect) {
                    paintSeat.setColor(Color.GREEN);
                    canvas.drawRect(rect, paintSeat);
                    canvas.drawText(selectRectBean.getRow() + "排" + selectRectBean.getColumn() + "列", rect.left, rect.top + seatWidth / 2, textPaint);
                }
                //收集所有的位置信息
                mRectList.add(selectRectBean);

            }
        }
        //绘制变化的
//        if (selectList.size() > 0) {
//            for (int i = 0; i < selectList.size(); i++) {
//                SelectRectBean selectRectBean = selectList.get(i);
//                Rect rect = selectRectBean.getRect();
//                paintSeat.setColor(Color.GREEN);
//                canvas.drawRect(rect, paintSeat);
//                canvas.drawText(selectRectBean.getRow() + "排" + selectRectBean.getColumn() + "列", rect.left, rect.top + seatWidth / 2, textPaint);
//            }
//        }

    }

    private void drawRowIndex(Canvas canvas) {
        RectF rect = new RectF(transformOldCoordX(0), marginTopScreen - seatWidth / 2, transformOldCoordX(seatWidth), marginTopScreen + seatWidth * row - seatWidth / 2 - margiVertical);
        screenPaint.setColor(Color.parseColor("#44666666"));
        canvas.drawRoundRect(rect, 20, 20, screenPaint);
        for (int i = 0; i < row; i++) {
            int startY;
            if (i == 0) {
                startY = marginTopScreen;
            } else {
                startY = i * seatWidth + marginTopScreen;
            }
            //绘制一下左边的排数
            canvas.drawText((i + 1) + "", transformOldCoordX(margiLeft - 5), startY == 0 ? seatWidth : startY, textPaint);
        }
    }

    private void drawFilmScreen(Canvas canvas) {
        seatRect = new Rect();
        seatRect.left = seatWidth + seatWidth / 2;
        seatRect.top = marginTopScreen;
        //绘制多少排座位
        for (int i = 0; i < seatList.length; i++) {
            int top;
            if (i == 0) {
                top = marginTopScreen - seatWidth / 2 - margiVertical;
            } else {
                top = i * seatWidth + marginTopScreen - seatWidth / 2 - margiVertical;
            }
            //每排多少座位
            for (int x = 0; x < seatList[i].length; x++) {
                int left;
                //开始绘制矩阵图
                if (x == 0) {
                    left = seatWidth + seatWidth / 2;
                } else {
                    left = (x + 1) * seatWidth + seatWidth / 2;
                }
                seatRect.right = left + seatWidth + margiHorizontal;
                seatRect.bottom = top + seatWidth + margiVertical;
//

            }
        }
        float centerX = ((seatRect.right + seatRect.left) / 2);

//        LogUtil.i("drawFilmScreen = " + centerX + "---" + getMatrixTranslateY() + "***" + getMatrixTranslateX());
        canvas.drawLine(centerX, transformOldCoordY(0), centerX, marginTopScreen + seatWidth * row - seatWidth / 2 - margiVertical, screenPaint);


    }

    private void drawFilmScreen2(Canvas canvas) {
        //计算出实时的顶部位置，座位的矩阵部分其实是原始的坐标。
        screenPaint.setColor(Color.parseColor("#ffffff"));
        float centerX = ((seatRect.right + seatRect.left) / 2);
        float newLeft = (centerX - 100);
        float newRight = (centerX - filmScreenHeight);
        float newTop = (centerX + filmScreenHeight);
        float newBottom = (centerX + 100);
        Path path1 = new Path();
        path1.moveTo(newLeft, transformOldCoordY(0));
        path1.lineTo(newRight, transformOldCoordY(filmScreenHeight / 2));
        path1.lineTo(newTop, transformOldCoordY(filmScreenHeight / 2));
        path1.lineTo(newBottom, transformOldCoordY(0));
        path1.close();
        canvas.drawPath(path1, screenPaint);
        canvas.drawText("屏幕", (centerX - filmScreenHeight / 4), transformOldCoordY(filmScreenHeight / 4), textPaint);
    }

    private int downX, downY;
    private boolean pointer;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        LogUtil.i("--onTouchEvent--" + event.getAction());
        int y = (int) event.getY();
        int x = (int) event.getX();
        super.onTouchEvent(event);
        //事件分发给手势处理器进行缩放和平移
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);

        int pointerCount = event.getPointerCount();
        if (pointerCount > 1) {
            pointer = true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.removeMessages(1);
                isDrawOver = true;
                pointer = false;

                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_UP:
//                float currentX = event.getX();
//                float currentY = event.getY();
//                if (Math.abs(downX - currentX) < 5 && Math.abs(downY - currentY) < 5){
//                    //需要做坐标点转换,转换为原始的点，在进行点击事件
//                    isDrawOverBitmap = true;
//                    LogUtil.i(currentX + "--before--" + currentY);
//                    currentPoint.set((int) currentX, (int) currentY);
//                    clickSeat(currentPoint);
//                    if (childSelectListener != null) {
//                        childSelectListener.onChildSelect(selectList);
//                    }
//                }
//                LogUtil.i("是否 绘制缩略图：" + isDrawOver);
                mHandler.sendEmptyMessageDelayed(1, 2000);

                int downDX = Math.abs(x - downX);
                int downDY = Math.abs(y - downY);
                if ((downDX > 10 || downDY > 10) && !pointer) {
                    autoScroll();
                }
                break;
            default:

                break;
        }
        return true;
    }


    private void clickSeat(Point currentPoint) {
        for (int i = 0; i < mRectList.size(); i++) {
            Rect rect = mRectList.get(i).getRect();
            SelectRectBean selectRectBean = mRectList.get(i);
            if (selectRectBean.getSeatState() == SELL_SEAT ||
                    selectRectBean.getSeatState() == EMPTY_SEAT ||
                    selectRectBean.getSeatState() == EMPTY_SEAT) {
                continue;
            }
            float newLeft = rect.left * getMatrixScaleX() + 1 * getMatrixTranslateX();
            float newRight = rect.right * getMatrixScaleY() + 1 * getMatrixTranslateX();
            if (currentPoint.x > newLeft && currentPoint.x < newRight) {
                float newTop = rect.top * getMatrixScaleX() + 1 * getMatrixTranslateY();
                float newBottom = rect.bottom * getMatrixScaleY() + 1 * getMatrixTranslateY();
                if (currentPoint.y > newTop && currentPoint.y < newBottom) {
                    //点击到了某一个
                    if (selectList.contains(selectRectBean)) {
                        selectList.remove(selectRectBean);
                    } else {
                        selectList.add(selectRectBean);
                    }
                    //更新界面
                    invalidate();
                    break;
                }
            }
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtil.i("onSizeChanged");
        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();

    }

    /**
     * --------------工具方法------------------
     */

    //获取屏幕尺寸
    private int[] getScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int[] screenSize = new int[2];
        screenSize[0] = displayMetrics.widthPixels;
        screenSize[1] = displayMetrics.heightPixels;
        return screenSize;
    }

    /**
     * 校正移动后的偏差
     */
    private void reviseTranslate() {
        if (!isScaling) {
//            mCanvasMatrix.postTranslate(0, 0);
//            if (getMatrixTranslateX() > 0 || getMatrixTranslateX() < 0) {
//                mCanvasMatrix.postTranslate(0, getTranslationY());
//            } else if (getMatrixTranslateY() > 0 || getMatrixTranslateY() < 0) {
//                mCanvasMatrix.postTranslate(getMatrixTranslateX(), 0);
//            } else {
//
//            }
        }
    }

    /**
     * 转换为缩略图的数值
     *
     * @param source
     * @return
     */
    private float transformCoverDistance(float source) {
        return source / getMatrixScaleX() / ratioOver;
    }

    /**
     * 由于比例为4所以距离要缩放两次
     *
     * @param source
     * @return
     */
    private float transformCoverDistance2(float source) {

        return source / getMatrixScaleX() / getMatrixScaleX() / ratioOver;
    }

    float[] m = new float[9];

    private float getMatrixTranslateX() {
        mCanvasMatrix.getValues(m);
        return m[Matrix.MTRANS_X];
    }

    private float getMatrixTranslateY() {
        mCanvasMatrix.getValues(m);
        return m[Matrix.MTRANS_Y];
    }

    private float getMatrixScaleY() {
        mCanvasMatrix.getValues(m);
        return m[Matrix.MSCALE_Y];
    }

    private float getMatrixScaleX() {
        mCanvasMatrix.getValues(m);
        return m[Matrix.MSCALE_X];
    }

    private float getRealScaleFactor(float currentScaleFactor) {
        float realScale = 1.0f;
        // 用户当前的缩放比例
        float userScale = getMatrixScaleX();
        // 理论缩放数值
        float theoryScale = userScale * currentScaleFactor;
        // 如果用户在执行放大操作并且理论缩放数据大于4.0
        if (currentScaleFactor > 1.0f && theoryScale > MAX_SCALE) {
            realScale = MAX_SCALE / userScale;
        } else if (currentScaleFactor < 1.0f && theoryScale < MIN_SCALE) {
            realScale = MIN_SCALE / userScale;
        } else {
            realScale = currentScaleFactor;
        }
        return realScale;
    }

    /**
     * 转换为新的数值
     *
     * @param num
     * @return
     */
    private float transformNewCoordX(float num) {
        return num * getMatrixScaleX() + 1 * getMatrixTranslateX();

    }

    private float transformNewCoordY(float num) {
        return num * getMatrixScaleY() + 1 * getMatrixTranslateY();

    }

    /**
     * 转换为原数值
     *
     * @param num
     * @return
     */
    private float transformOldCoordX(float num) {
        return num - 1 * getMatrixTranslateX() / getMatrixScaleX();

    }

    private float transformOldCoordY(float num) {
        return num - 1 * getMatrixTranslateY() / getMatrixScaleY();

    }
}


