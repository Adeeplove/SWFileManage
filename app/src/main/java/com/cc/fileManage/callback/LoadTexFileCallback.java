package com.cc.fileManage.callback;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.utils.CommonUtil;
import com.cc.fileManage.utils.TexFileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class LoadTexFileCallback extends AsyncTask<String, String, Bitmap>
{
    private Context context;

    private File texFile;

    private InputStream inputFile;

    //TEXTool
    private TEXFile currentFile;

    //弹框
    private ProgressDialog dialog;

    //监听
    private LoadImageListener loadImageListener;

    public LoadTexFileCallback(Context context, File texFile){
        this.context = context;
        this.texFile = texFile;
    }

    public LoadTexFileCallback(Context context, InputStream inputFile){
        this.context = context;
        this.inputFile = inputFile;
    }

    public void setLoadImageListener(LoadImageListener loadImageListener)
    {
        this.loadImageListener = loadImageListener;
    }

    @Override
    protected void onPreExecute()
    {
        dialog = new ProgressDialog(context);
        dialog.setMessage("正在加载...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected Bitmap doInBackground(String[] p1)
    {
        try{
            if(texFile != null)
                return openTexFile(texFile);
            else
                return openTexFile(inputFile);
        }catch(Exception e){
            e.printStackTrace();
            publishProgress("打开失败");
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(String[] values)
    {
        Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCancelled()
    {
        if(dialog != null)
            dialog.dismiss();
    }

    @Override
    protected void onPostExecute(Bitmap result)
    {
        if(dialog != null)
            dialog.dismiss();

        if(result != null)
            loadImageListener.loadImage(result, currentFile);
    }

    //打开tex文件
    private Bitmap openTexFile(File file) throws Exception{
        FileInputStream fi = new FileInputStream(file);
        return openTexFile(fi);
    }

    //打开texFile
    private Bitmap openTexFile(InputStream file) throws Exception{
        //实例化
        currentFile = TexFileUtil.openTexFile(file);
        if(currentFile == null){
            throw new Exception("文件打开失败!");
        }
        //
        Bitmap bitmap = TexFileUtil.loadTexBitmap(currentFile);
        if(bitmap != null)
            return CommonUtil.scaleImageView(bitmap);//反转y轴
        else
            throw new Exception("图片加载失败!");
    }

    public interface LoadImageListener{
        void loadImage(Bitmap bitmap, TEXFile texFile);
    }
}