package com.cc.fileManage.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("AppCompatCustomView")
public class EllipsizeTextView extends TextView {
    private static final String ELLIPSIS = "...";

    //默认结尾
    private TextUtils.TruncateAt ellipsizeNoun = TextUtils.TruncateAt.END;

    public void setEllipsizeNoun(TextUtils.TruncateAt ellipsizeNoun) {
        this.ellipsizeNoun = ellipsizeNoun;
    }

    public TextUtils.TruncateAt getEllipsizeNoun() {
        return ellipsizeNoun;
    }

    public interface EllipsizeListener {
        void ellipsizeStateChanged(boolean ellipsized);
    }

    private final List<EllipsizeListener> ellipsizeListeners = new ArrayList<>();
    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private String fullText;
    //private int maxLines = -1;
    private float lineSpacingMultiplier = 1.0f;
    private float lineAdditionalVerticalPadding = 0.0f;

    public EllipsizeTextView(Context context) {
        super(context);
    }

    public EllipsizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EllipsizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addEllipsizeListener(EllipsizeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        ellipsizeListeners.add(listener);
    }

    public void removeEllipsizeListener(EllipsizeListener listener) {
        ellipsizeListeners.remove(listener);
    }

    public boolean isEllipsized() {
        return isEllipsized;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        this.lineAdditionalVerticalPadding = add;
        this.lineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        if (!programmaticChange) {
            fullText = text.toString();
            isStale = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStale) {
            super.setEllipsize(null);
            resetText();
        }
        super.onDraw(canvas);
    }

    /**
     * 重新设置文本
     */
    private void resetText() {
        //获取控件最大行数
        int maxLines = getMaxLines();
        //当前的文本
        String workingText = fullText;
        //
        boolean ellipsized = false;

        //最大行大于-1
        if (maxLines != -1) {
            //画笔
            Paint paint = new Paint();
            paint.setTextSize(getTextSize());

            //计算文本长度
            float textLenght = paint.measureText(workingText);

            //如果文本长度大于 控件长度 * 总行数
            if(textLenght > (getWidth() * maxLines)){
                //循环 如果文本 大于总行数长度
                while (paint.measureText(workingText + ELLIPSIS) > (getWidth() * maxLines)) {
                    //文本大于省略符
                    if(workingText.length() > ELLIPSIS.length()){
                        //
                        switch(getEllipsizeNoun()){
                            case START:
                                workingText = workingText.substring(ELLIPSIS.length());
                                break;
                            case END:
                            default:
                                workingText = workingText.substring(0, workingText.length() - ELLIPSIS.length());
                        }
                    }else{
                        break;
                    }
                }
                //
                switch(getEllipsizeNoun()){
                    case START:
                        workingText = ELLIPSIS + workingText;
                        break;
                    case END:
                    default:
                        workingText = workingText + ELLIPSIS;
                }
                ellipsized = true;
            }
        }

        //如果有改变 重新设置text
        if (!workingText.contentEquals(getText())) {
            programmaticChange = true;
            try {
                setText(workingText);
            } finally {
                programmaticChange = false;
            }
        }

        isStale = false;
        if (ellipsized != isEllipsized) {
            isEllipsized = ellipsized;
            for (EllipsizeListener listener : ellipsizeListeners) {
                listener.ellipsizeStateChanged(ellipsized);
            }
        }
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt where) {
        // Ellipsize settings are not respected
    }
}