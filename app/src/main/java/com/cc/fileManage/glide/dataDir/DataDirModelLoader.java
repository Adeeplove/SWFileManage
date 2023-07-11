package com.cc.fileManage.glide.dataDir;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.cc.fileManage.entity.file.MFile;

public class DataDirModelLoader implements ModelLoader<MFile, Bitmap> {

    private final Context context;

    public DataDirModelLoader(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public LoadData<Bitmap> buildLoadData(@NonNull MFile s, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(s.getPath()), new DataDirDataFetcher(context, s));
    }

    @Override
    public boolean handles(@NonNull MFile s) {
        return s.isDirectory();
    }

    public static class Factory implements ModelLoaderFactory<MFile, Bitmap> {

        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ModelLoader<MFile, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new DataDirModelLoader(context);
        }

        @Override
        public void teardown() {}
    }
}
