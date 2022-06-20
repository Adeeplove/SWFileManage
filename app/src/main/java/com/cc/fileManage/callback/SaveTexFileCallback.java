package com.cc.fileManage.callback;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.graphics.Bitmap;

import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.module.TEXCreator;

public class SaveTexFileCallback extends AsyncTask<String, String, String>
{
    private Context context;
    //保存的图片
    private Bitmap texImage;
    //格式
    private TEXFile.PixelFormat PixelFormat;
    //类型
    private TEXFile.TextureType TextureType;

    private boolean GenerateMipmaps;

    private boolean preMultiplyAlpha;

    //弹窗
    private ProgressDialog dialog;

    public void setTexImage(Bitmap texImage) {
        this.texImage = texImage;
    }

    public void setGenerateMipmaps(boolean generateMipmaps)
    {
        GenerateMipmaps = generateMipmaps;
    }

    public void setPreMultiplyAlpha(boolean preMultiplyAlpha)
    {
        this.preMultiplyAlpha = preMultiplyAlpha;
    }

    public SaveTexFileCallback(Context context, TEXFile.PixelFormat format, TEXFile.TextureType type){
        this.context = context;
        this.PixelFormat = format;
        this.TextureType = type;
    }

    @Override
    protected void onPreExecute()
    {
        dialog = new ProgressDialog(context);
        dialog.setMessage("正在保存...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(String[] values) {
        Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(String[] format)
    {
        FileOutputStream fo = null;
        try
        {
            //文件
            File file = new File(format[0]);

            if(!file.exists()){
                publishProgress("文件不存在!");
                return null;
            }
            String name = file.getName();
            if(name.lastIndexOf(".") != -1){
                name = name.substring(0, name.lastIndexOf("."));
            }

            String outFile = file.getParent() + "/" + name + "_" + format[1] +".tex";
            fo = new FileOutputStream(outFile);

            TEXCreator tc = new TEXCreator(PixelFormat, TextureType);
            tc.setGenerateMipmaps(GenerateMipmaps);
            tc.setPreMultiplyAlpha(preMultiplyAlpha);

            //error
            tc.ConvertPNGToTex(texImage, fo);

            return "ok";
        }
        catch (Exception e)
        {
            publishProgress(e.getLocalizedMessage());
        }
        finally{
            try
            {
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
        if(result == null){
            Toast.makeText(context, "保存失败!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if(dialog != null)
            dialog.dismiss();
    }
}