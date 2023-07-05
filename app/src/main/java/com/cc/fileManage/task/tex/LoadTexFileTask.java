package com.cc.fileManage.task.tex;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;

import com.blankj.utilcode.util.ToastUtils;
import com.cc.fileManage.entity.TEXFile;
import com.cc.fileManage.task.AsynchronousTask;
import com.cc.fileManage.utils.CommonUtil;
import com.cc.fileManage.utils.TexFileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class LoadTexFileTask extends AsynchronousTask<String, String, Bitmap>
{
    private final WeakReference<Context> weakReference;

    private final InputStream inputFile;

    //TEXTool
    private TEXFile currentFile;
    //弹框
    private ProgressDialog dialog;
    //监听
    private LoadImageListener loadImageListener;

    public LoadTexFileTask(Context context, File texFile) throws FileNotFoundException {
        this(context, new FileInputStream(texFile));
    }

    public LoadTexFileTask(Context context, InputStream inputFile){
        this.weakReference = new WeakReference<>(context);
        this.inputFile = inputFile;
    }

    public void setLoadImageListener(LoadImageListener loadImageListener)
    {
        this.loadImageListener = loadImageListener;
    }

    @Override
    protected void onPreExecute()
    {
        Context context = weakReference.get();
        if(context == null) return;
        dialog = new ProgressDialog(context);
        dialog.setMessage("正在加载...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected Bitmap doInBackground(String... strings)
    {
        try{
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
        ToastUtils.showShort(values[0]);
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