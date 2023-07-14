package com.cc.fileManage.glide.apk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.cc.fileManage.entity.file.JFile;
import com.cc.fileManage.entity.file.MFile;
import com.cc.fileManage.module.arsc.data.ArscFile;
import com.cc.fileManage.module.arsc.data.BaseTypeChunk;
import com.cc.fileManage.module.arsc.data.ResTableTypeInfoChunk;
import com.cc.fileManage.utils.AXmlUtil;
import com.cc.fileManage.utils.AntZipUtil;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipInput;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ApkDataFetcher implements DataFetcher<Bitmap> {

    private final MFile file;

    private final Context context;

    public ApkDataFetcher(Context context, MFile file) {
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
            if(file instanceof JFile) {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(file.getPath(), PackageManager.GET_ACTIVITIES);
                ///
                if(packageInfo != null) {
                    ApplicationInfo info = packageInfo.applicationInfo;
                    // 必须设置此路径 不然获取失败
                    info.sourceDir = file.getPath();
                    info.publicSourceDir = file.getPath();
                    //
                    Drawable drawable = info.loadIcon(packageManager);
                    return drawable2Bitmap(drawable);
                }
            } else {
//                return loadIcon();
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

    // 读取apk信息
    private Bitmap loadIcon() {
        //
//        try (ZipInput zipInput = new ZipInput(file)){
//            ZipEntry entry = zipInput.getEntry("AndroidManifest.xml");
//            if(entry != null) {
//                InputStream inputStream = zipInput.getInputStream(entry);
//                if(inputStream != null) {
//                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                    ///
//                    String id = "";
//                    if(id != null) {
//                        entry = zipInput.getEntry("resources.arsc");
//                        String resPath = loadIcon(zipInput.getInputStream(entry), Integer.parseInt(id, 16));
//                        if(!TextUtils.isEmpty(resPath)) {
//                            Log.e("e", "resPath： "+resPath);
//                            entry = zipInput.getEntry(resPath);
//                            if(entry != null) {
//                                Drawable drawable = Drawable
//                                        .createFromStream(zipInput.getInputStream(entry), entry.getName());
//                                return drawable2Bitmap(drawable);
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return null;
    }

    // id
//    private String loadIcon(InputStream inputStream, int resId) throws Exception{
//        if(inputStream == null) return null;
//        ////
//        ArscFile arscFile = new ArscFile();
//        arscFile.parse(AntZipUtil.streamToByteArray(inputStream));
//        ////
//        List<ResTableTypeInfoChunk> chunkList = new ArrayList<>();
//        long pkgId = (resId & 0xff000000L) >> 24;
//        //short packageId = (short) (resId >> 24 & 0xff);
//        if (arscFile.resTablePackageChunk.pkgId == pkgId) {
//            int typeId = (resId & 0x00ff0000) >> 16;
//            List<BaseTypeChunk> typeList = arscFile.resTablePackageChunk.typeInfoIndexer.get(typeId);
//            if(typeList != null && typeList.size() > 0) {
//                for (int i = 1; i < typeList.size(); ++i) {
//                    if (typeList.get(i) instanceof ResTableTypeInfoChunk) {
//                        ResTableTypeInfoChunk infoChunk = (ResTableTypeInfoChunk) typeList.get(i);
//                        chunkList.add(infoChunk);
//                    }
//                }
//            }
//        }
//        ///////////////////////////////
//        int entryId = resId & 0x0000ffff, density = 0;
//        String resPath = null;
//        for (ResTableTypeInfoChunk chunk : chunkList) {
//            if(chunk.resConfig.density == 0 || chunk.resConfig.density == 0xFFFF) continue;
//            //
//            if(chunk.resConfig.density >= density) {
//                String path = chunk.getResPath(entryId);
//                if(!TextUtils.isEmpty(path) && !path.toLowerCase().endsWith(".xml")) {
//                    resPath = path;
//                    density = chunk.resConfig.density;
//                }
//            }
//        }
//        arscFile.close();
//        return resPath;
//    }

    // Drawable转换成Bitmap
    private Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
