package com.cc.fileManage.callback;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.module.TEXCreator;
import com.cc.fileManage.utils.TexFileUtil;

import java.io.File;
import java.io.FileOutputStream;

public class ConvertTexCallback extends AsyncTask<String, String, Boolean>{

    private Context context;

    private File file;

    //格式
    private TEXFile.PixelFormat PixelFormat;
    //类型
    private TEXFile.TextureType TextureType;

    private boolean GenerateMipmaps;

    private boolean preMultiplyAlpha;

    //弹框
    private ProgressDialog dialog;

    public ConvertTexCallback(Context context, File file){
        this.context = context;
        this.file = file;
    }

    public TEXFile.PixelFormat getPixelFormat() {
        return PixelFormat;
    }

    public void setPixelFormat(TEXFile.PixelFormat pixelFormat) {
        PixelFormat = pixelFormat;
    }

    public TEXFile.TextureType getTextureType() {
        return TextureType;
    }

    public void setTextureType(TEXFile.TextureType textureType) {
        TextureType = textureType;
    }

    public boolean isGenerateMipmaps() {
        return GenerateMipmaps;
    }

    public void setGenerateMipmaps(boolean generateMipmaps) {
        GenerateMipmaps = generateMipmaps;
    }

    public boolean isPreMultiplyAlpha() {
        return preMultiplyAlpha;
    }

    public void setPreMultiplyAlpha(boolean preMultiplyAlpha) {
        this.preMultiplyAlpha = preMultiplyAlpha;
    }

    @Override
    protected void onPreExecute()
    {
        dialog = new ProgressDialog(context);
        dialog.setMessage("正在转换...");
        dialog.setCancelable(false);
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
        FileOutputStream fo = null;
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
            tc.ConvertPNGToTex(bitmap, fo);
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
