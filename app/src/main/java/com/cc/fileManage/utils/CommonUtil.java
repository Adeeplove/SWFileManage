package com.cc.fileManage.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CommonUtil {

    @SuppressWarnings("all")
    public static void saveBitmap2file(Bitmap bmp, Context context, String savePath, boolean update) {
        File filePic = new File(savePath);
        try {
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            //Toast.makeText(context, "保存成功,位置:" + filePic.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "保存失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        // 最后通知图库更新
        if(update)
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + savePath)));
    }


    /**
     * 把原图按1/10的比例压缩
     *
     * @param path 原图的路径
     * @return 压缩后的图片
     */
    public static Bitmap getCompressPhoto(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 10; // 图片的大小设置为原来的十分之一
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle  被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotateImageView(int angle, Bitmap bitmap) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError ignored) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }

    /* 
     * 上下反转图片
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap scaleImageView(Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);//上下反转
        try{
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            ///
            return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        } catch(OutOfMemoryError e){
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap sizeCompress(Bitmap bitmap, int width, int height, int rqsW, int rqsH) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            bitmap.recycle();
            // 用option设置返回的bitmap对象的一些属性参数
            final BitmapFactory.Options options = new BitmapFactory.Options();
            int inSampleSize;
            if (rqsW == 0 || rqsH == 0) {
                options.inSampleSize = 1;
            } else if (height > rqsH || width > rqsW) {
                final int heightRatio = Math.round((float) height / (float) rqsH);
                final int widthRatio = Math.round((float) width / (float) rqsW);
                inSampleSize = Math.min(heightRatio, widthRatio);
                options.inSampleSize = inSampleSize;
            }
            // 主要通过option里的inSampleSize对原图片进行按比例压缩
            byte[] bytes = outputStream.toByteArray();
            return BitmapFactory.decodeByteArray(bytes,0, bytes.length, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    ///
    public static Bitmap sizeCompress(String path, int rqsW, int rqsH) {
        // 用option设置返回的bitmap对象的一些属性参数
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;// 设置仅读取Bitmap的宽高而不读取内容
        BitmapFactory.decodeFile(path, options);// 获取到图片的宽高，放在option里边
        final int height = options.outHeight;//图片的高度放在option里的outHeight属性中
        final int width = options.outWidth;
        int inSampleSize;
        if (rqsW == 0 || rqsH == 0) {
            options.inSampleSize = 1;
        } else if (height > rqsH || width > rqsW) {
            final int heightRatio = Math.round((float) height / (float) rqsH);
            final int widthRatio = Math.round((float) width / (float) rqsW);
            inSampleSize = Math.min(heightRatio, widthRatio);
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
        }
        return BitmapFactory.decodeFile(path, options);// 主要通过option里的inSampleSize对原图片进行按比例压缩
    }
}

