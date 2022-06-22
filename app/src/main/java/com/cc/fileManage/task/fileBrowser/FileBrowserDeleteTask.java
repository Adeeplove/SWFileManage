package com.cc.fileManage.task.fileBrowser;

import android.app.AlertDialog;

import java.lang.ref.WeakReference;
import java.util.List;
import android.content.Context;
import androidx.documentfile.provider.DocumentFile;

import com.blankj.utilcode.util.FileUtils;
import com.cc.fileManage.entity.file.DFileMethod;
import com.cc.fileManage.entity.file.JFile;
import com.cc.fileManage.entity.file.ManageFile;
import com.cc.fileManage.task.AsynchronousTask;

public class FileBrowserDeleteTask extends AsynchronousTask<String, String>
{
    private final WeakReference<Context> weakReference;

    private AlertDialog dialog;

    private final List<ManageFile> data;

    private OnDeleteListener onDeleteListener;

    public FileBrowserDeleteTask(Context context, List<ManageFile> data){
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
        dialog.setMessage("正在删除...");
        dialog.show();
    }

    //执行后台任务（耗时操作）,不可在此方法内修改UI
    @Override
    protected String doInBackground() {
        try{
            for(ManageFile file : data){
                Context context = weakReference.get();
                if(context == null) break;
                ///===
                publishProgress("up", "正在删除 " + file.getFileName());
                if(file instanceof JFile){
                    //删除文件
                    FileUtils.delete(((JFile) file).getFile());
                }else{
                    DocumentFile du = DFileMethod.getDocumentFile(context, file.getFilePath());
                    if(du == null){
                        continue;
                    }
                    if(file.isFile()){
                        du.delete();
                    }else{
                        deleteDir(context, du);
                    }
                }
            }
        }catch(Exception e){
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
        try{
            //刷新
            if(onDeleteListener != null)
                onDeleteListener.onDeleteFiles();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(dialog != null){
                dialog.dismiss();
            }
        }
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

    /**
     * delete dir   删除文件夹
     * @param dir   文件夹
     * @return      删除结果
     */
    private boolean deleteDir(Context context, DocumentFile dir) {
        if (dir.isDirectory()) {
            List<DocumentFile> docu = DFileMethod.documentFileLists(context, dir);
            for(DocumentFile df : docu){
                boolean success = deleteDir(context, df);
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public interface OnDeleteListener{
        void onDeleteFiles();
    }
}