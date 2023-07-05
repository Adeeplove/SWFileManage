package com.cc.fileManage.task.tex;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.module.file.TEXCreator;
import com.cc.fileManage.task.AsynchronousTask;
import com.cc.fileManage.utils.TexFileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class ConvertTexTask extends AsynchronousTask<String, String, Boolean> {

    private final WeakReference<Context> weakReference;

    private final File file;        //tex文件
    //格式
    private TEXFile.PixelFormat PixelFormat;
    //类型
    private TEXFile.TextureType TextureType;

    private boolean GenerateMipmaps;

    private boolean preMultiplyAlpha;

    //弹框
    private ProgressDialog dialog;

    public ConvertTexTask(Context context, File file){
        this.weakReference = new WeakReference<>(context);
        this.file = file;
    }

    public void setPixelFormat(TEXFile.PixelFormat pixelFormat) {
        PixelFormat = pixelFormat;
    }

    public void setTextureType(TEXFile.TextureType textureType) {
        TextureType = textureType;
    }

    public void setGenerateMipmaps(boolean generateMipmaps) {
        GenerateMipmaps = generateMipmaps;
    }

    public void setPreMultiplyAlpha(boolean preMultiplyAlpha) {
        this.preMultiplyAlpha = preMultiplyAlpha;
    }

    @Override
    protected void onPreExecute()
    {
        Context context = weakReference.get();
        if(context == null) return;
        ///============
        dialog = new ProgressDialog(context);
        dialog.setMessage("正在转换...");
        dialog.setCancelable(false);
        dialog.getWindow().setDimAmount(0f);
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        Bitmap bitmap = null;
        if(file.getName().endsWith(".tex")){
            TEXFile tex = TexFileUtil.openTexFile(file);
            if(tex != null){
                bitmap = TexFileUtil.loadTexBitmap(tex);
            }
        }else {
            bitmap = BitmapFactory.decodeFile(file.getPath());
        }
        //===================
        if(bitmap == null){
            return false;
        }
        //====
        FileOutputStream fo;
        try{
            String name = file.getName();
            if(name.lastIndexOf(".") != -1){
                name = name.substring(0, name.lastIndexOf("."));
            }
            String outFile = file.getParent() + "/" + name + "_" + PixelFormat.getName() +".tex";
            fo = new FileOutputStream(outFile);

            TEXCreator tc = new TEXCreator(PixelFormat, TextureType);
            tc.setGenerateMipmaps(GenerateMipmaps);
            tc.setPreMultiplyAlpha(preMultiplyAlpha);
            //error
            tc.convertPNGToTex(bitmap, fo);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean succeed)
    {
        if(dialog != null)
            dialog.dismiss();
        if(convertListener != null)
            convertListener.onConvert(succeed);
    }

    private OnConvertListener convertListener;

    public void setConvertListener(OnConvertListener convertListener) {
        this.convertListener = convertListener;
    }

    public interface OnConvertListener{
        void onConvert(boolean succeed);
    }
}
