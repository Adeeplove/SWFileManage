package com.boycy815.pinchimageview;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.Color;

public class AlphaDrawable extends Drawable {
    //密度
    private final int density;

    private final Paint paint = new Paint();

    private final Paint whiteBlock = new Paint();
    private final Paint grayBlock = new Paint();
    private int rectWidth;
    private int rectHeight;
    private Bitmap bitmap;
    //
    private final boolean initAlpha;

    public AlphaDrawable(int density, boolean initAlpha) {
        this.density = density;
        this.whiteBlock.setColor(Color.WHITE);
        this.grayBlock.setColor(Color.LTGRAY);
        this.initAlpha = initAlpha;
    }

    private void drawBitmap() {
        if (getBounds().width() > 0 && getBounds().height() > 0) {
            this.bitmap = Bitmap.createBitmap(getBounds().width(), getBounds().height(), Bitmap.Config.ARGB_8888);

            if(initAlpha) {
                Canvas canvas = new Canvas(this.bitmap);
                Rect rect = new Rect();

                boolean drawWhite = true;
                for (int y = 0; y <= this.rectHeight; y++) {
                    boolean drawGray = drawWhite;
                    for (int x = 0; x <= this.rectWidth; x++) {
                        rect.top = this.density * y;
                        rect.left = this.density * x;
                        rect.bottom = rect.top + this.density;
                        rect.right = rect.left + this.density;
                        canvas.drawRect(rect, drawGray ? this.whiteBlock : this.grayBlock);
                        drawGray = !drawGray;
                    }
                    drawWhite = !drawWhite;
                }
            }
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.bitmap, null, getBounds(), this.paint);
    }

    @SuppressLint("WrongConstant")
    public int getOpacity() {
        return 0;
    }

    @Override
    protected void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        int height = rect.height();
        this.rectWidth = (int) Math.ceil((double) (rect.width() / this.density));
        this.rectHeight = (int) Math.ceil((double) (height / this.density));
        drawBitmap();
    }

    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("Alpha is not supported by this drawable.");
    }

    public void setColorFilter(ColorFilter colorFilter) {
        throw new UnsupportedOperationException("ColorFilter is not supported by this drawable.");
    }
}
