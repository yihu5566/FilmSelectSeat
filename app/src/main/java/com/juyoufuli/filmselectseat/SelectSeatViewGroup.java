package com.juyoufuli.filmselectseat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author : dongfang
 * @Created Time : 2018-11-19  15:25
 * @Description:
 */
public class SelectSeatViewGroup extends ViewGroup  {


    private Context context;
    private Paint mainPaint;
    private TextPaint textPaint;
    /**
     * 需要说二维数组来定位行数列数坐标
     */
    private List<String> textList;
    private List<String> selectList;
    private List<String> tempSelectList;

    private String[] textDefault = {""};
    private LayoutInflater inflater;
    private List<TextView> textViewList;
    private ChildSelectListener childSelectListener;
    private ChildClickListener childClickListener;
    private int maxWidth;
    private ScaleGestureDetector scaleGestureDetector;
    /**
     * 默认的标签间距
     */
    private int margiHorizontal = 10;
    /**
     * 默认的行间距
     */
    private int margiVertical = 10;

    Paint mPaint;

    private Model model;
    private Matrix matrix;
    private float preScale;//之前的伸缩值
    private float curScale;//当前的伸缩值

    private int[] screenSize;//屏幕尺寸信息
    private float translateX;//平移到屏幕中心的X轴距离
    private float translateY;//平移到屏幕中心的Y轴距离
    private boolean isChangeScaleType;//是否转换为Matrix模式

    public void setModel(Model model) {
        this.model = model;
    }

    public void setTextList(List<String> textList) {
        //移除以前的view
        removeAllViews();
        initData();
        this.textList= textList;
        for (int i = 0; i < textList.size(); i++) {
            selectList.add("null");
        }
        //绘制每一个小的textView
        drawTextView();
        requestLayout();
        invalidate();
    }

    public void setChildClickListener(ChildClickListener childClickListener) {
        this.childClickListener = childClickListener;
    }

    public void setChildSelectListener(ChildSelectListener childSelectListener) {
        this.childSelectListener = childSelectListener;
    }

    public SelectSeatViewGroup(@NonNull Context context) {
        this(context, null);
    }

    public SelectSeatViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
        initData();
        init(context);
    }

    private void initData() {
        textList = new ArrayList<>();
        selectList = new ArrayList<>();

        textViewList = new ArrayList<>();
        tempSelectList = new ArrayList<>();
    }

    private void init(Context context) {
        curScale=1.0f;
        screenSize=getScreenSize();
        translateX=screenSize[0]/2-getWidth()/2;//使图片显示在中心
        translateY=getHeight()/2-screenSize[1]/2;

        matrix=new Matrix();
        preScale=screenSize[0]*1.0f/getWidth();//图片完全显示的伸缩值
        isChangeScaleType=true;

        scaleGestureDetector=new ScaleGestureDetector(context,new FilmScaleGestureDetector());

        inflater = LayoutInflater.from(context);
        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainPaint.setColor(Color.RED);

        textPaint = new TextPaint();
        textPaint.setTextSize(20);
        textPaint.setColor(Color.BLACK);

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);

    }

    public SelectSeatViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //由于布局非常规，所以要自己测量
        measureMyChild(widthMeasureSpec, heightMeasureSpec);

    }

    private void measureMyChild(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //宽度
        maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int count = getChildCount();
        //总高度
        int contentHeight = 0;
        //记录最宽的行宽
        int maxLineWidth = 0;
        // 每行宽度
        int startLayoutWidth = 0;
        //一行中子控件最高的高度，用于决定下一行高度应该在目前基础上累加多少
        int maxChildHeight = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LogUtil.i("onLayout--getPaddingRight:" +
                    child.getPaddingRight() +
                    "getPaddingLeft：" + child.getPaddingLeft());

            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            //测量的宽高
            int childMeasureWidth = child.getMeasuredWidth();
            int childMeasureHeight = child.getMeasuredHeight();
            LogUtil.i("onLayout--width:" + maxWidth + "startLayoutWidth：");
            if (startLayoutWidth + childMeasureWidth < maxWidth) {
                //如果一行没有排满，继续往右排列
                startLayoutWidth += childMeasureWidth + margiHorizontal;
            } else {
                // 初始化为0
                maxChildHeight = 0;
                startLayoutWidth = 0;

            }
            if (childMeasureHeight > maxChildHeight) {
                maxChildHeight = childMeasureHeight;
            }
            //获取总的高度
            contentHeight += maxChildHeight + margiVertical;
            //获取最长的行总的宽度
            maxLineWidth = Math.max(maxLineWidth, startLayoutWidth);
        }

        //如果没有子元素，就设置宽高都为0（简化处理）
        if (getChildCount() == 0) {
            setMeasuredDimension(0, 0);
        } else
            //宽和高都是AT_MOST，则设置宽度所有子元素的宽度的和；高度设置为第一个元素的高度；
            if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(maxLineWidth, contentHeight);
            }
            //如果宽度是wrap_content，则宽度为所有子元素的宽度的和
            else if (widthMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(maxLineWidth, heightSize);
            }
            //如果高度是wrap_content，则高度为第一个子元素的高度
            else if (heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(widthSize, contentHeight);
            }

    }


    private void drawTextView() {
        for (String text : textList) {
            LogUtil.d("view"+text);
            TextView label = new TextView(context);
            label.setPadding(30, 30, 0, 0);
            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40);
            label.setBackgroundResource(R.drawable.selector_text_bg);
            label.setText(text);
            label.setTextColor(createColorStateList("#ffffffff", "#ff44e6ff"));
            addView(label);
        }

    }

    private static ColorStateList createColorStateList(String selected, String normal) {
        int[] colors = new int[]{Color.parseColor(selected), Color.parseColor(normal)};
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};
        ColorStateList colorList = new ColorStateList(states, colors);
        return colorList;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtil.i("onSizeChanged");

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        LogUtil.i("onLayout--width:" + maxWidth);
        final int count = getChildCount();
        int childMeasureWidth = 0;
        int childMeasureHeight = 0;
        // 开始的X位置
        int startLayoutWidth = getPaddingLeft();
        // 开始的Y位置
        int startLayoutHeight = getPaddingTop();
        //一行中子控件最高的高度，用于决定下一行高度应该在目前基础上累加多少
        int maxChildHeight = 0;
        for (int i = 0; i < count; i++) {
            int position = i;
            TextView child = (TextView) getChildAt(i);
            //注意此处不能使用getWidth和getHeight，这两个方法必须在onLayout执行完，才能正确获取宽高
            childMeasureWidth = child.getMeasuredWidth() + child.getPaddingLeft() + child.getPaddingRight();
            childMeasureHeight = child.getMeasuredHeight() + child.getPaddingTop() + child.getPaddingBottom();
            LogUtil.i("onLayout--width:" + maxWidth + "startLayoutWidth：" + startLayoutWidth);
            if (startLayoutWidth + childMeasureWidth < maxWidth - getPaddingRight()) {
                //如果一行没有排满，继续往右排列
                left = startLayoutWidth;
                right = left + childMeasureWidth;
                top = startLayoutHeight;
                bottom = top + childMeasureHeight;
            } else {
                //排满后换行
                startLayoutWidth = getPaddingLeft();
                startLayoutHeight += maxChildHeight + margiVertical;
                maxChildHeight = 0;

                left = startLayoutWidth;
                right = left + childMeasureWidth;
                top = startLayoutHeight;
                bottom = top + childMeasureHeight;
            }
            //宽度累加
            startLayoutWidth += childMeasureWidth + margiHorizontal;
            if (childMeasureHeight > maxChildHeight) {
                maxChildHeight = childMeasureHeight;
            }
            //确定子控件的位置，四个参数分别代表（左上右下）点的坐标值
            child.layout(left, top, right, bottom);
            initListener(child, position);
        }

    }

    private void initListener(TextView child, final int position) {
        textViewList.add(child);
        child.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Model.SELECT == model) {

                    if ("null".equals(selectList.get(position))) {
                        selectList.add(position, textList.get(position));
                    }else {
                        selectList.add(position, "null");
                    }

                    boolean equals = "null".equals(selectList.get(position));
                    v.setSelected(!equals);
                    //转换一下，不能操作原始集合。
                    tempSelectList.addAll(selectList);
                    tempSelectList.removeAll(Collections.singleton("null"));
                    childSelectListener.onChildSelect(tempSelectList, position);

                } else if (Model.SINGLE_SELECT == model) {
                    for (int i = 0; i < textList.size(); i++) {
                        selectList.remove(i);
                        if (position == i) {
                            selectList.add(i, textList.get(i));
                        } else {
                            selectList.add(i, "null");
                        }
                        boolean equals = "null".equals(selectList.get(i));
                        LogUtil.i("SINGLE_SELECT" + !equals);
                        textViewList.get(i).setSelected(!equals);
                    }
                    childSelectListener.onChildSelect(textList.get(position), position);
                } else if (Model.CLICK == model) {
                    if (childClickListener != null) {
                        childClickListener.onChildClick(v, textList.get(position), position);
                    }
                }
            }
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return scaleGestureDetector.onTouchEvent(event);
    }
    public class FilmScaleGestureDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        public FilmScaleGestureDetector() {
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if(isChangeScaleType){
                isChangeScaleType=false;
            }
            curScale=detector.getScaleFactor()*preScale;//当前的伸缩值*之前的伸缩值 保持连续性

            if(curScale>5||curScale<0.1){//当放大倍数大于5或者缩小倍数小于0.1倍 就不伸缩图片 返回true取消处理伸缩手势事件
                preScale=curScale;
                return true;
            }

            matrix.setScale(curScale,curScale,getWidth()/2,getHeight()/2);//在屏幕中心伸缩
            matrix.preTranslate(translateX,translateY);//使图片平移到屏幕中心显示

            preScale=curScale;//保存上一次的伸缩值
            return false;
        }
    }

    //获取屏幕尺寸
    private int[] getScreenSize(){
        DisplayMetrics displayMetrics=new DisplayMetrics();
        WindowManager windowManager= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int[] screenSize=new int[2];
        screenSize[0]=displayMetrics.widthPixels;
        screenSize[1]=displayMetrics.heightPixels;
        return screenSize;
    }

}
