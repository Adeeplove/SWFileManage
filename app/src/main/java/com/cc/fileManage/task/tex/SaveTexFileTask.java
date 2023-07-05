package com.cc.fileManage.task.tex;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.module.file.TEXCreator;
import com.cc.fileManage.task.AsynchronousTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class SaveTexFileTask extends AsynchronousTask<String, String, String>
{
    private final WeakReference<Context> weakReference;
    //保存的图片
    private Bitmap texImage;
    //格式
    private final TEXFile.PixelFormat pixelFormat;
    //类型
    private final TEXFile.TextureType textureType;

    private boolean generateMipmaps;

    private boolean preMultiplyAlpha;

    //弹窗
    private ProgressDialog dialog;

    public void setTexImage(Bitmap texImage) {
        this.texImage = texImage;
    }

    public void setGenerateMipmaps(boolean generateMipmaps)
    {
        this.generateMipmaps = generateMipmaps;
    }

    public void setPreMultiplyAlpha(boolean preMultiplyAlpha)
    {
        this.preMultiplyAlpha = preMultiplyAlpha;
    }

    public SaveTexFileTask(Context context, TEXFile.PixelFormat format, TEXFile.TextureType type){
        this.weakReference = new WeakReference<>(context);
        this.pixelFormat = format;
        this.textureType = type;
    }

    @Override
    protected void onPreExecute()
    {
        Context context = weakReference.get();
        if(context == null) return;
        ////
        dialog = new ProgressDialog(context);
        dialog.setMessage("正在保存...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(String[] values) {
        ToastUtils.showShort(values[0]);
    }

    @Override
    protected String doInBackground(String... strings)
    {
        FileOutputStream fo = null;
        try {
            //文件
            File file = new File(strings[0]);
            if(!file.exists()){
                publishProgress("文件不存在!");
                return null;
            }
            String name = file.getName();
            if(name.lastIndexOf(".") != -1){
                name = name.substring(0, name.lastIndexOf("."));
            }

            String outFile = file.getParent() + "/" + name + "_" + pixelFormat.getName() +".tex";
            fo = new FileOutputStream(outFile);

            TEXCreator tc = new TEXCreator(pixelFormat, textureType);
            tc.setGenerateMipmaps(generateMipmaps);
            tc.setPreMultiplyAlpha(preMultiplyAlpha);
            //error
            tc.convertPNGToTex(texImage, fo);
            return "ok";
        } catch (Exception e) {
            publishProgress(e.getLocalizedMessage());
        } finally{
            try {
                if(fo != null)
                    fo.close();
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result)
    {
        if(dialog != null)
            dialog.dismiss();
        ToastUtils.showShort(result == null ? "保存失败!" : "已保存");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if(dialog != null)
            dialog.dismiss();
    }
}