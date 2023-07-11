package com.cc.fileManage.glide;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.cc.fileManage.entity.file.MFile;
import com.cc.fileManage.glide.apk.ApkModelLoader;
import com.cc.fileManage.glide.dataDir.DataDirModelLoader;

@GlideModule
public class IGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.prepend(MFile.class, Bitmap.class, new ApkModelLoader.Factory(context));
        registry.prepend(MFile.class, Bitmap.class, new DataDirModelLoader.Factory(context));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
