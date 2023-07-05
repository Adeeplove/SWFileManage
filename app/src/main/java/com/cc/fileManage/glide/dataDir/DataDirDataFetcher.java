package com.cc.fileManage.glide.dataDir;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.cc.fileManage.entity.ImageID;
import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.utils.CommonUtil;

public class DataDirDataFetcher implements DataFetcher<Bitmap> {

    private final ManageFile file;

    private final Context context;

    public DataDirDataFetcher(Context context, ManageFile file) {
        this.context = context;
        this.file = file;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
        callback.onDataReady(getApkIcon());
    }

    /**
     * 获取安装包图标
     * @return  安装包图标
     */
    private Bitmap getApkIcon() {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(file.getName(), 0);
            ///
            if(packageInfo != null) {
                ApplicationInfo info = packageInfo.applicationInfo;
                Bitmap finalIcon;
                try {
                    Drawable drawable = info.loadIcon(packageManager);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    // 图标
                    finalIcon = bitmapDrawable.getBitmap();
                } catch (Exception e) {
                    finalIcon = BitmapFactory.decodeResource(context.getResources(), android.R.mipmap.sym_def_app_icon);
                }
                Bitmap finalBitmap = BitmapFactory.decodeResource(context.getResources(), ImageID.image_folder);
                if(finalIcon != null && finalBitmap != null) {
                    int width = finalBitmap.getWidth() / 2 - 5, height = finalBitmap.getHeight() / 2 - 5;
                    Bitmap icon = CommonUtil.sizeCompress(finalIcon, finalIcon.getWidth(), finalIcon.getHeight(), width, height);
                    if(icon != null) {
                        Bitmap bitmap = finalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        ///
                        finalBitmap.recycle();
                        if(bitmap != null) {
                            //画布
                            Canvas can = new Canvas(bitmap);
                            //画笔
                            Paint paint = new Paint();
                            //抗锯齿
                            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
                            //防抖动
                            paint.setDither(true);
                            ///
                            //位置 要绘制的bitmap区域
                            Rect srcRect = new Rect(0, 0, icon.getWidth(), icon.getHeight());
                            //要绘制的位置
                            Rect destRect = new Rect(width, height,
                                    srcRect.width() + width, srcRect.height() + height);
                            //画
                            can.drawBitmap(icon, srcRect, destRect, paint);
                            //
                            icon.recycle();
                            //
                            return bitmap;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void cleanup() {}

    @Override
    public void cancel() {}

    @NonNull
    @Override
    public Class<Bitmap> getDataClass() {
        return Bitmap.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
