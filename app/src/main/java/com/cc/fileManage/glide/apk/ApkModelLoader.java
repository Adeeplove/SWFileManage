package com.cc.fileManage.glide.apk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.cc.fileManage.entity.file.ManageFile;

public class ApkModelLoader implements ModelLoader<ManageFile, Bitmap> {

    private final Context context;

    public ApkModelLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<Bitmap> buildLoadData(@NonNull ManageFile s, int width, int height, @NonNull Options options) {
        ObjectKey key = new ObjectKey(s.getPath());
        return new LoadData<>(key, new ApkDataFetcher(context, s));
    }

    @Override
    public boolean handles(@NonNull ManageFile s) {
        return s.isFile();
    }

    public static class Factory implements ModelLoaderFactory<ManageFile, Bitmap> {

        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ModelLoader<ManageFile, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new ApkModelLoader(context);
        }

        @Override
        public void teardown() {
        }
    }
}
