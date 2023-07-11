package com.cc.fileManage.task.fileBrowser;

import android.app.AlertDialog;
import android.content.Context;

import com.cc.fileManage.entity.file.MFile;
import com.cc.fileManage.task.AsynchronousTask;

import java.lang.ref.WeakReference;
import java.util.List;

public class FileBrowserDeleteTask extends AsynchronousTask<String, String, String>
{
    private final WeakReference<Context> weakReference;

    private AlertDialog dialog;

    private final List<MFile> data;

    private OnDeleteListener onDeleteListener;

    public FileBrowserDeleteTask(Context context, List<MFile> data){
        this.weakReference = new WeakReference<>(context);
        this.data = data;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    //执行后台任务前做一些UI操作
    @Override
    protected void onPreExecute() {
        Context context = weakReference.get();
        if(context == null) return;
        ///=====================
        dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(false);
        dialog.getWindow().setDimAmount(0f);
        dialog.setMessage("正在删除...");
        dialog.show();
    }

    //执行后台任务（耗时操作）,不可在此方法内修改UI
    @Override
    protected String doInBackground(String... strings) {
        try{
            for(MFile file : data){
                Context context = weakReference.get();
                if(context == null) break;
                ///===
                publishProgress("up", "正在删除 " + file.getName());
                file.delete();
            }
        } catch(Exception e){
            publishProgress("error", "删除失败!", e.getMessage());
        }
        return null;
    }

    //更新进度信息
    @Override
    protected void onProgressUpdate(String... progresses)
    {
        if(progresses[0].equals("up"))
            dialog.setMessage(progresses[1]);
        else
            showDialog(progresses[1], progresses[2]);
    }

    //执行完后台任务后更新UI
    @Override
    protected void onPostExecute(String result) {
        if(dialog != null){
            dialog.dismiss();
        }
        //刷新
        if(onDeleteListener != null)
            onDeleteListener.onDeleteFiles();
    }

    /**
     *  提示信息
     * @param title        标题
     * @param message       信息
     */
    private void showDialog(String title, String message){
        Context context = weakReference.get();
        if(context == null) return;
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setTitle(title);
        ab.setMessage(message);
        ab.show();
    }

    public interface OnDeleteListener{
        void onDeleteFiles();
    }
}