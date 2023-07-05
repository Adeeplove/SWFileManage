package com.cc.fileManage.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/*
 *可移动按钮 父视图必须为RelativeLayout
 *2018 10 26
 */
public class MoveButton extends androidx.appcompat.widget.AppCompatButton
{
    private final Context context;

    private boolean clickOrMove;

    private int lastX, lastY;
    private int downX ,downY;

    private final int screenWidth;
    private final int screenHeight;

    public MoveButton(Context context){
        super(context);

        this.context = context;
        this.screenWidth = getScreenWidth(context);
        this.screenHeight = getScreenHeight(context);
    }

    public MoveButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        this.screenWidth = getScreenWidth(context);
        this.screenHeight = getScreenHeight(context);
    }

    public MoveButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.context = context;
        this.screenWidth = getScreenWidth(context);
        this.screenHeight = getScreenHeight(context);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获得屏幕高度
     *
     */
    public static int getScreenWidth(Context context)
    {
        WindowManager wm = (WindowManager) context
            .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     */
    public static int getScreenHeight(Context context)
    {
        WindowManager wm = (WindowManager) context
            .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    //重写onTouchEvent方法
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int ea = event.getAction();
        switch (ea) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                downX = lastX;
                downY = lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                int l = getLeft() + dx;
                int b = getBottom() + dy;
                int r = getRight() + dx;
                int t = getTop() + dy;
                if (l < 0) {
                    l = 0;
                    r = l + getWidth();
                }
                if (t < 0) {
                    t = 0;
                    b = t + getHeight();
                }
                if (r > screenWidth) {
                    r = screenWidth;
                    l = r - getWidth();
                }
                if (b > screenHeight) {
                    b = screenHeight;
                    t = b - getHeight();
                }
                layout(l, t, r, b);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                postInvalidate();

                RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(dp2px(context, 60), dp2px(context, 60));
                rl.leftMargin = getLeft();
                rl.topMargin = getTop();

                rl.setMargins(getLeft(), getTop(), 0, 0);
                setLayoutParams(rl);
                break;
            case MotionEvent.ACTION_UP:
                //判断是单击事件或是拖动事件，位移量大于5则断定为拖动事件
                clickOrMove = Math.abs((int) (event.getRawX() - downX)) > 5
                        || Math.abs((int) (event.getRawY() - downY)) > 5;
                break;
        }
        return clickOrMove || super.onTouchEvent(event);
    }
}



