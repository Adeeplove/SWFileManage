package com.cc.fileManage.glide.dataDir;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.cc.fileManage.entity.file.ManageFile;

public class DataDirModelLoader implements ModelLoader<ManageFile, Bitmap> {

    private final Context context;

    public DataDirModelLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<Bitmap> buildLoadData(@NonNull ManageFile s, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(s.getPath()), new DataDirDataFetcher(context, s));
    }

    @Override
    public boolean handles(@NonNull ManageFile s) {
        return s.isDirectory();
    }

    public static class Factory implements ModelLoaderFactory<ManageFile, Bitmap> {

        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ModelLoader<ManageFile, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new DataDirModelLoader(context);
        }

        @Override
        public void teardown() {}
    }
}
